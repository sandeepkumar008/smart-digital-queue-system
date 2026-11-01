package com.smartqueue.repository;

import com.smartqueue.model.PriorityLevel;
import com.smartqueue.model.QueueTicket;
import com.smartqueue.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QueueTicketRepository extends JpaRepository<QueueTicket, Long> {

    Optional<QueueTicket> findByTokenNumber(String tokenNumber);

    List<QueueTicket> findByStatus(TicketStatus status);

    List<QueueTicket> findByStatusOrderByIssuedAtAsc(TicketStatus status);

    List<QueueTicket> findByDepartmentIdAndStatus(Long departmentId, TicketStatus status);

    // Fetch waiting tickets ordered by priority (VIP first, then Senior, then Regular), then FIFO by issuedAt
    @Query("SELECT t FROM QueueTicket t WHERE t.department.id = :departmentId AND t.status = 'WAITING' ORDER BY t.priority DESC, t.issuedAt ASC")
    List<QueueTicket> findWaitingTicketsForDepartment(@Param("departmentId") Long departmentId);

    // Fetch overall waiting tickets ordered by priority then FIFO
    @Query("SELECT t FROM QueueTicket t WHERE t.status = 'WAITING' ORDER BY t.priority DESC, t.issuedAt ASC")
    List<QueueTicket> findAllWaitingTicketsOrdered();

    // Find currently serving tickets
    @Query("SELECT t FROM QueueTicket t WHERE t.status = 'SERVING' ORDER BY t.calledAt DESC")
    List<QueueTicket> findCurrentlyServingTickets();

    // Find ticket currently being served at a specific counter
    Optional<QueueTicket> findByCounterIdAndStatus(Long counterId, TicketStatus status);

    long countByStatus(TicketStatus status);

    long countByDepartmentIdAndStatus(Long departmentId, TicketStatus status);

    // Count tickets ahead of a given ticket in the same department
    @Query("SELECT COUNT(t) FROM QueueTicket t WHERE t.department.id = :deptId AND t.status = 'WAITING' " +
           "AND (t.priority > :priority OR (t.priority = :priority AND t.issuedAt < :issuedAt))")
    long countTicketsAhead(@Param("deptId") Long deptId,
                           @Param("priority") PriorityLevel priority,
                           @Param("issuedAt") LocalDateTime issuedAt);

    // Get max sequence number for a department today
    @Query("SELECT COALESCE(MAX(t.sequenceNumber), 0) FROM QueueTicket t WHERE t.department.id = :deptId AND t.issuedAt >= :startOfDay")
    int findMaxSequenceNumberToday(@Param("deptId") Long deptId, @Param("startOfDay") LocalDateTime startOfDay);

    // Find all tickets issued today
    @Query("SELECT t FROM QueueTicket t WHERE t.issuedAt >= :startOfDay ORDER BY t.issuedAt DESC")
    List<QueueTicket> findTicketsIssuedSince(@Param("startOfDay") LocalDateTime startOfDay);

    // Average wait time for completed tickets today
    @Query("SELECT COALESCE(AVG(t.actualWaitMinutes), 0.0) FROM QueueTicket t WHERE t.status = 'COMPLETED' AND t.completedAt >= :startOfDay")
    double findAverageWaitTimeToday(@Param("startOfDay") LocalDateTime startOfDay);
}
