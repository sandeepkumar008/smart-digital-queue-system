package com.smartqueue.service;

import com.smartqueue.dto.*;
import com.smartqueue.model.*;
import com.smartqueue.repository.CounterRepository;
import com.smartqueue.repository.DepartmentRepository;
import com.smartqueue.repository.QueueTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QueueService {

    private final QueueTicketRepository ticketRepository;
    private final DepartmentRepository departmentRepository;
    private final CounterRepository counterRepository;

    // Cache the most recently called ticket for voice/audio chimes
    private volatile TicketResponseDto lastCalledTicket = null;

    public QueueService(QueueTicketRepository ticketRepository,
                        DepartmentRepository departmentRepository,
                        CounterRepository counterRepository) {
        this.ticketRepository = ticketRepository;
        this.departmentRepository = departmentRepository;
        this.counterRepository = counterRepository;
    }

    /**
     * Issue a new digital queue token/ticket
     */
    @Transactional
    public TicketResponseDto issueTicket(TicketRequestDto request) {
        Department dept = departmentRepository.findByCode(request.getDepartmentCode().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Department code not found: " + request.getDepartmentCode()));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        int nextSeq = ticketRepository.findMaxSequenceNumberToday(dept.getId(), startOfDay) + 1;
        String tokenNumber = String.format("%s-%03d", dept.getCode(), nextSeq);

        // Calculate dynamic estimated wait time
        long waitingAhead = ticketRepository.countTicketsAhead(dept.getId(), request.getPriority(), LocalDateTime.now());
        List<Counter> activeCounters = counterRepository.findByDepartmentIdAndStatus(dept.getId(), CounterStatus.AVAILABLE);
        activeCounters.addAll(counterRepository.findByDepartmentIdAndStatus(dept.getId(), CounterStatus.BUSY));
        int counterCount = Math.max(1, activeCounters.size());

        int baseMinutes = (int) Math.ceil(((double) waitingAhead * dept.getAvgServiceMinutes()) / counterCount);
        // Priority adjustment factor
        int estimatedWait = switch (request.getPriority()) {
            case VIP_URGENT -> Math.max(3, (int) (baseMinutes * 0.4));
            case SENIOR_CITIZEN -> Math.max(5, (int) (baseMinutes * 0.7));
            case REGULAR -> Math.max(dept.getAvgServiceMinutes(), baseMinutes);
        };

        QueueTicket ticket = new QueueTicket(tokenNumber, nextSeq, dept, request.getCustomerName(),
                request.getCustomerPhone(), request.getPriority(), estimatedWait);

        QueueTicket saved = ticketRepository.save(ticket);
        return mapToResponse(saved);
    }

    /**
     * Counter operator calls the next customer in their department queue
     */
    @Transactional
    public TicketResponseDto callNext(Long counterId) {
        Counter counter = counterRepository.findById(counterId)
                .orElseThrow(() -> new IllegalArgumentException("Counter not found: " + counterId));

        // If counter was previously serving a customer, auto-complete it
        Optional<QueueTicket> currentlyServing = ticketRepository.findByCounterIdAndStatus(counterId, TicketStatus.SERVING);
        currentlyServing.ifPresent(ticket -> completeTicketInternal(ticket, counter));

        // Find next waiting ticket for this counter's department (VIP/Senior priority first, then FIFO)
        List<QueueTicket> waitingQueue = ticketRepository.findWaitingTicketsForDepartment(counter.getDepartment().getId());

        if (waitingQueue.isEmpty()) {
            // Check if there are any urgent VIP tickets in other departments that can be assisted
            List<QueueTicket> allWaiting = ticketRepository.findAllWaitingTicketsOrdered();
            if (!allWaiting.isEmpty() && allWaiting.get(0).getPriority() == PriorityLevel.VIP_URGENT) {
                waitingQueue = List.of(allWaiting.get(0));
            } else {
                counter.setStatus(CounterStatus.AVAILABLE);
                counterRepository.save(counter);
                return null; // Queue is empty
            }
        }

        QueueTicket nextTicket = waitingQueue.get(0);
        LocalDateTime now = LocalDateTime.now();
        nextTicket.setStatus(TicketStatus.SERVING);
        nextTicket.setCounter(counter);
        nextTicket.setCalledAt(now);

        long waitSeconds = Duration.between(nextTicket.getIssuedAt(), now).getSeconds();
        nextTicket.setActualWaitMinutes((int) Math.ceil(waitSeconds / 60.0));

        QueueTicket savedTicket = ticketRepository.save(nextTicket);

        counter.setStatus(CounterStatus.BUSY);
        counterRepository.save(counter);

        TicketResponseDto responseDto = mapToResponse(savedTicket);
        this.lastCalledTicket = responseDto;
        return responseDto;
    }

    /**
     * Recall customer to replay audio chime announcement
     */
    public TicketResponseDto recall(Long counterId) {
        Optional<QueueTicket> current = ticketRepository.findByCounterIdAndStatus(counterId, TicketStatus.SERVING);
        if (current.isPresent()) {
            TicketResponseDto dto = mapToResponse(current.get());
            this.lastCalledTicket = dto;
            return dto;
        }
        return null;
    }

    /**
     * Complete service for the current ticket at the counter
     */
    @Transactional
    public TicketResponseDto completeCurrentTicket(Long counterId) {
        Counter counter = counterRepository.findById(counterId)
                .orElseThrow(() -> new IllegalArgumentException("Counter not found: " + counterId));

        Optional<QueueTicket> current = ticketRepository.findByCounterIdAndStatus(counterId, TicketStatus.SERVING);
        if (current.isEmpty()) {
            counter.setStatus(CounterStatus.AVAILABLE);
            counterRepository.save(counter);
            return null;
        }

        QueueTicket completed = completeTicketInternal(current.get(), counter);
        return mapToResponse(completed);
    }

    private QueueTicket completeTicketInternal(QueueTicket ticket, Counter counter) {
        LocalDateTime now = LocalDateTime.now();
        ticket.setStatus(TicketStatus.COMPLETED);
        ticket.setCompletedAt(now);

        if (ticket.getCalledAt() != null) {
            long serviceSeconds = Duration.between(ticket.getCalledAt(), now).getSeconds();
            ticket.setActualServiceMinutes(Math.max(1, (int) Math.ceil(serviceSeconds / 60.0)));
        } else {
            ticket.setActualServiceMinutes(counter.getDepartment().getAvgServiceMinutes());
        }

        counter.incrementServed();
        counter.setStatus(CounterStatus.AVAILABLE);
        counterRepository.save(counter);

        return ticketRepository.save(ticket);
    }

    /**
     * Mark customer as No-Show / Skipped
     */
    @Transactional
    public TicketResponseDto markNoShow(Long counterId) {
        Counter counter = counterRepository.findById(counterId)
                .orElseThrow(() -> new IllegalArgumentException("Counter not found: " + counterId));

        Optional<QueueTicket> current = ticketRepository.findByCounterIdAndStatus(counterId, TicketStatus.SERVING);
        if (current.isEmpty()) {
            return null;
        }

        QueueTicket ticket = current.get();
        ticket.setStatus(TicketStatus.NO_SHOW);
        ticket.setCompletedAt(LocalDateTime.now());
        QueueTicket saved = ticketRepository.save(ticket);

        counter.setStatus(CounterStatus.AVAILABLE);
        counterRepository.save(counter);

        return mapToResponse(saved);
    }

    /**
     * Toggle counter status (e.g. OFFLINE, AVAILABLE, ON_BREAK)
     */
    @Transactional
    public Counter updateCounterStatus(Long counterId, CounterStatus newStatus) {
        Counter counter = counterRepository.findById(counterId)
                .orElseThrow(() -> new IllegalArgumentException("Counter not found: " + counterId));
        counter.setStatus(newStatus);
        return counterRepository.save(counter);
    }

    /**
     * Live Queue Board payload for TV Displays and Public Screens
     */
    public LiveQueueBoardDto getLiveBoard() {
        LiveQueueBoardDto board = new LiveQueueBoardDto();
        board.setLastCalledTicket(this.lastCalledTicket);

        List<Counter> counters = counterRepository.findAll();
        List<LiveQueueBoardDto.CounterDisplayDto> counterDisplays = new ArrayList<>();

        for (Counter c : counters) {
            LiveQueueBoardDto.CounterDisplayDto cd = new LiveQueueBoardDto.CounterDisplayDto();
            cd.setCounterNumber(c.getCounterNumber());
            cd.setCounterName(c.getCounterName());
            cd.setDepartmentName(c.getDepartment().getName());
            cd.setDepartmentColor(c.getDepartment().getColorHex());
            cd.setOperatorName(c.getOperatorName());
            cd.setStatus(c.getStatus().name());

            Optional<QueueTicket> servingTicket = ticketRepository.findByCounterIdAndStatus(c.getId(), TicketStatus.SERVING);
            if (servingTicket.isPresent()) {
                cd.setCurrentToken(servingTicket.get().getTokenNumber());
                cd.setCustomerName(servingTicket.get().getCustomerName());
            } else {
                cd.setCurrentToken("--");
                cd.setCustomerName("Available");
            }
            counterDisplays.add(cd);
        }
        board.setActiveCounters(counterDisplays);

        List<QueueTicket> servingList = ticketRepository.findCurrentlyServingTickets();
        board.setCurrentlyServing(servingList.stream().map(this::mapToResponse).collect(Collectors.toList()));

        List<QueueTicket> waitingList = ticketRepository.findAllWaitingTicketsOrdered();
        board.setNextUpTickets(waitingList.stream().limit(10).map(this::mapToResponse).collect(Collectors.toList()));

        Map<String, Long> waitingByDept = new HashMap<>();
        List<Department> depts = departmentRepository.findByActiveTrue();
        for (Department d : depts) {
            long count = ticketRepository.countByDepartmentIdAndStatus(d.getId(), TicketStatus.WAITING);
            waitingByDept.put(d.getName(), count);
        }
        board.setWaitingCountByDepartment(waitingByDept);

        board.setTotalWaiting(ticketRepository.countByStatus(TicketStatus.WAITING));
        board.setTotalServing(ticketRepository.countByStatus(TicketStatus.SERVING));
        board.setTotalServedToday(ticketRepository.countByStatus(TicketStatus.COMPLETED));

        return board;
    }

    /**
     * Customer tracks their specific token number
     */
    public TicketResponseDto trackToken(String tokenNumber) {
        QueueTicket ticket = ticketRepository.findByTokenNumber(tokenNumber.trim().toUpperCase())
                .orElseThrow(() -> new NoSuchElementException("Token " + tokenNumber + " not found"));
        return mapToResponse(ticket);
    }

    /**
     * Get system-wide statistics for the admin dashboard
     */
    public SystemStatsDto getSystemStats() {
        SystemStatsDto stats = new SystemStatsDto();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        stats.setTotalTicketsToday(ticketRepository.findTicketsIssuedSince(startOfDay).size());
        stats.setWaitingCount(ticketRepository.countByStatus(TicketStatus.WAITING));
        stats.setServingCount(ticketRepository.countByStatus(TicketStatus.SERVING));
        stats.setCompletedCount(ticketRepository.countByStatus(TicketStatus.COMPLETED));
        stats.setNoShowCount(ticketRepository.countByStatus(TicketStatus.NO_SHOW));
        stats.setAvgWaitTimeMinutes(Math.round(ticketRepository.findAverageWaitTimeToday(startOfDay) * 10.0) / 10.0);

        List<Department> departments = departmentRepository.findAll();
        stats.setTotalDepartments(departments.size());

        List<Counter> counters = counterRepository.findAll();
        stats.setTotalCounters(counters.size());
        stats.setOnlineCounters((int) counters.stream().filter(c -> c.getStatus() != CounterStatus.OFFLINE).count());

        Map<String, Long> deptTickets = new HashMap<>();
        for (Department d : departments) {
            long count = ticketRepository.countByDepartmentIdAndStatus(d.getId(), TicketStatus.WAITING);
            deptTickets.put(d.getName(), count);
        }
        stats.setTicketsPerDepartment(deptTickets);

        return stats;
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findByActiveTrue();
    }

    public List<Counter> getAllCounters() {
        return counterRepository.findAll();
    }

    public List<QueueTicket> getWaitingTickets(Long departmentId) {
        if (departmentId != null) {
            return ticketRepository.findWaitingTicketsForDepartment(departmentId);
        }
        return ticketRepository.findAllWaitingTicketsOrdered();
    }

    private TicketResponseDto mapToResponse(QueueTicket ticket) {
        TicketResponseDto dto = new TicketResponseDto();
        dto.setId(ticket.getId());
        dto.setTokenNumber(ticket.getTokenNumber());
        dto.setSequenceNumber(ticket.getSequenceNumber());
        dto.setDepartmentId(ticket.getDepartment().getId());
        dto.setDepartmentCode(ticket.getDepartment().getCode());
        dto.setDepartmentName(ticket.getDepartment().getName());
        dto.setDepartmentColor(ticket.getDepartment().getColorHex());
        dto.setCustomerName(ticket.getCustomerName());
        dto.setCustomerPhone(ticket.getCustomerPhone());
        dto.setPriority(ticket.getPriority());
        dto.setPriorityLabel(ticket.getPriority().getDisplayName());
        dto.setStatus(ticket.getStatus());
        dto.setStatusDescription(ticket.getStatus().getDescription());

        if (ticket.getCounter() != null) {
            dto.setCounterNumber(ticket.getCounter().getCounterNumber());
            dto.setCounterName(ticket.getCounter().getCounterName());
        }

        dto.setIssuedAt(ticket.getIssuedAt());
        dto.setCalledAt(ticket.getCalledAt());
        dto.setCompletedAt(ticket.getCompletedAt());
        dto.setEstimatedWaitMinutes(ticket.getEstimatedWaitMinutes());
        dto.setActualWaitMinutes(ticket.getActualWaitMinutes());
        dto.setActualServiceMinutes(ticket.getActualServiceMinutes());

        if (ticket.getStatus() == TicketStatus.WAITING) {
            long ahead = ticketRepository.countTicketsAhead(ticket.getDepartment().getId(), ticket.getPriority(), ticket.getIssuedAt());
            dto.setTicketsAhead(ahead);
        } else {
            dto.setTicketsAhead(0);
        }

        return dto;
    }
}
