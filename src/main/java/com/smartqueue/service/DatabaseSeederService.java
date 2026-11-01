package com.smartqueue.service;

import com.smartqueue.model.*;
import com.smartqueue.repository.CounterRepository;
import com.smartqueue.repository.CrowdHistoryRepository;
import com.smartqueue.repository.DepartmentRepository;
import com.smartqueue.repository.QueueTicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DatabaseSeederService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeederService.class);

    private final DepartmentRepository departmentRepository;
    private final CounterRepository counterRepository;
    private final CrowdHistoryRepository crowdHistoryRepository;
    private final QueueTicketRepository queueTicketRepository;

    public DatabaseSeederService(DepartmentRepository departmentRepository,
                                 CounterRepository counterRepository,
                                 CrowdHistoryRepository crowdHistoryRepository,
                                 QueueTicketRepository queueTicketRepository) {
        this.departmentRepository = departmentRepository;
        this.counterRepository = counterRepository;
        this.crowdHistoryRepository = crowdHistoryRepository;
        this.queueTicketRepository = queueTicketRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (departmentRepository.count() == 0) {
            log.info("No existing records found. Seeding initial database with departments, counters, crowd history, and live queue tickets...");
            seedAllData();
            log.info("Database seeding completed successfully!");
        } else {
            log.info("Database already contains data ({} departments, {} counters). Ready.", 
                    departmentRepository.count(), counterRepository.count());
        }
    }

    @Transactional
    public void seedAllData() {
        // 1. Seed Departments
        Department acc = new Department("ACC", "Cash & Account Services", "Cash deposit, withdrawal, wire transfer, draft issuance", "bi-cash-stack", "#2563eb", 7);
        Department lon = new Department("LON", "Loans & Credit Mortgages", "Personal loan, home finance, SME loan review & disbursal", "bi-bank", "#7c3aed", 15);
        Department cit = new Department("CIT", "Citizen Identity & KYC", "Identity verification, biometric updates, document attestation", "bi-person-vcard", "#059669", 10);
        Department sup = new Department("SUP", "Customer Support & Grievances", "Card replacement, complaint resolution, digital banking support", "bi-headset", "#d97706", 8);

        departmentRepository.saveAll(List.of(acc, lon, cit, sup));

        // 2. Seed Counters
        Counter c1 = new Counter(1, "Counter 01", acc, "Sarah Jenkins");
        Counter c2 = new Counter(2, "Counter 02", acc, "Michael Chang");
        Counter c3 = new Counter(3, "Counter 03", lon, "David Ross");
        Counter c4 = new Counter(4, "Counter 04", lon, "Priya Sharma");
        Counter c5 = new Counter(5, "Counter 05", cit, "Elena Rostova");
        Counter c6 = new Counter(6, "Counter 06", sup, "James Wilson");

        counterRepository.saveAll(List.of(c1, c2, c3, c4, c5, c6));

        // 3. Seed 30 Days of Hourly Crowd History for ML & Prediction
        seedCrowdHistory(List.of(acc, lon, cit, sup));

        // 4. Seed Live Queue Tickets for demonstration
        seedLiveTickets(List.of(acc, lon, cit, sup), List.of(c1, c2, c3, c4, c5, c6));
    }

    private void seedCrowdHistory(List<Department> departments) {
        List<CrowdHistory> historyRecords = new ArrayList<>();
        LocalDate today = LocalDate.now();
        Random rand = new Random(42); // deterministic seed for realistic data

        // Generate past 28 days of data
        for (int dayOffset = 28; dayOffset >= 1; dayOffset--) {
            LocalDate date = today.minusDays(dayOffset);
            int dayOfWeek = date.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday

            // Operating hours: 9 AM to 6 PM (9 to 18)
            for (int hour = 9; hour <= 18; hour++) {
                // Base hourly multiplier (surge in 10-12 and 14-16)
                double hourFactor;
                switch (hour) {
                    case 9 -> hourFactor = 0.55;
                    case 10 -> hourFactor = 1.25;
                    case 11 -> hourFactor = 1.45; // Morning peak
                    case 12 -> hourFactor = 1.30;
                    case 13 -> hourFactor = 0.80; // Lunch hour dip
                    case 14 -> hourFactor = 1.15;
                    case 15 -> hourFactor = 1.35; // Afternoon peak
                    case 16 -> hourFactor = 1.05;
                    case 17 -> hourFactor = 0.70;
                    case 18 -> hourFactor = 0.35;
                    default -> hourFactor = 0.50;
                }

                // Day of week factor (Mondays and Fridays are busier)
                double dayFactor = switch (dayOfWeek) {
                    case 1 -> 1.25; // Monday rush
                    case 5 -> 1.20; // Friday rush
                    case 6 -> 0.70; // Saturday half day
                    case 7 -> 0.20; // Sunday minimal/closed
                    default -> 1.0;
                };

                for (Department dept : departments) {
                    int baseVolume = switch (dept.getCode()) {
                        case "ACC" -> 16;
                        case "CIT" -> 12;
                        case "LON" -> 8;
                        case "SUP" -> 10;
                        default -> 10;
                    };

                    int noise = rand.nextInt(5) - 2;
                    int arrivals = Math.max(2, (int) Math.round(baseVolume * hourFactor * dayFactor) + noise);
                    int served = Math.min(arrivals + rand.nextInt(3) - 1, arrivals);

                    double avgWait = Math.max(3.0, (arrivals * (dept.getAvgServiceMinutes() / 3.0)) + rand.nextDouble() * 4);
                    double avgService = dept.getAvgServiceMinutes() + (rand.nextDouble() * 2.0 - 1.0);
                    int peakQueue = Math.max(2, (int) Math.round(arrivals * 0.65));

                    String congestion;
                    if (arrivals < 12 && avgWait < 12) congestion = "LOW";
                    else if (arrivals < 24 && avgWait < 22) congestion = "MODERATE";
                    else if (arrivals < 36 && avgWait < 32) congestion = "HIGH";
                    else congestion = "CRITICAL";

                    CrowdHistory record = new CrowdHistory(date, dayOfWeek, hour, dept,
                            arrivals, served, Math.round(avgWait * 10.0) / 10.0,
                            Math.round(avgService * 10.0) / 10.0, peakQueue, congestion);

                    historyRecords.add(record);
                }
            }
        }

        crowdHistoryRepository.saveAll(historyRecords);
        log.info("Seeded {} historical crowd time-series records.", historyRecords.size());
    }

    private void seedLiveTickets(List<Department> departments, List<Counter> counters) {
        LocalDateTime now = LocalDateTime.now();

        Department acc = departments.get(0);
        Department lon = departments.get(1);
        Department cit = departments.get(2);
        Department sup = departments.get(3);

        Counter c1 = counters.get(0);
        Counter c3 = counters.get(2);
        Counter c5 = counters.get(4);

        // 1. Completed tickets earlier today
        QueueTicket comp1 = new QueueTicket("ACC-001", 1, acc, "Robert Taylor", "+1-555-0101", PriorityLevel.REGULAR, 7);
        comp1.setStatus(TicketStatus.COMPLETED);
        comp1.setCounter(c1);
        comp1.setIssuedAt(now.minusMinutes(45));
        comp1.setCalledAt(now.minusMinutes(35));
        comp1.setCompletedAt(now.minusMinutes(28));
        comp1.setActualWaitMinutes(10);
        comp1.setActualServiceMinutes(7);

        QueueTicket comp2 = new QueueTicket("LON-001", 1, lon, "Amanda Davis", "+1-555-0102", PriorityLevel.VIP_URGENT, 15);
        comp2.setStatus(TicketStatus.COMPLETED);
        comp2.setCounter(c3);
        comp2.setIssuedAt(now.minusMinutes(40));
        comp2.setCalledAt(now.minusMinutes(32));
        comp2.setCompletedAt(now.minusMinutes(18));
        comp2.setActualWaitMinutes(8);
        comp2.setActualServiceMinutes(14);

        QueueTicket comp3 = new QueueTicket("CIT-001", 1, cit, "Rajesh Patel", "+1-555-0103", PriorityLevel.SENIOR_CITIZEN, 10);
        comp3.setStatus(TicketStatus.COMPLETED);
        comp3.setCounter(c5);
        comp3.setIssuedAt(now.minusMinutes(30));
        comp3.setCalledAt(now.minusMinutes(22));
        comp3.setCompletedAt(now.minusMinutes(12));
        comp3.setActualWaitMinutes(8);
        comp3.setActualServiceMinutes(10);

        // 2. Currently Serving Tickets at Counters
        QueueTicket srv1 = new QueueTicket("ACC-002", 2, acc, "Carlos Santana", "+1-555-0104", PriorityLevel.REGULAR, 6);
        srv1.setStatus(TicketStatus.SERVING);
        srv1.setCounter(c1);
        srv1.setIssuedAt(now.minusMinutes(18));
        srv1.setCalledAt(now.minusMinutes(5));
        srv1.setActualWaitMinutes(13);
        c1.setStatus(CounterStatus.BUSY);
        c1.setTotalServedToday(1);

        QueueTicket srv2 = new QueueTicket("LON-002", 2, lon, "Grace Hopper", "+1-555-0105", PriorityLevel.REGULAR, 14);
        srv2.setStatus(TicketStatus.SERVING);
        srv2.setCounter(c3);
        srv2.setIssuedAt(now.minusMinutes(22));
        srv2.setCalledAt(now.minusMinutes(8));
        srv2.setActualWaitMinutes(14);
        c3.setStatus(CounterStatus.BUSY);
        c3.setTotalServedToday(1);

        QueueTicket srv3 = new QueueTicket("CIT-002", 2, cit, "Dr. Sunita Rao", "+1-555-0106", PriorityLevel.SENIOR_CITIZEN, 9);
        srv3.setStatus(TicketStatus.SERVING);
        srv3.setCounter(c5);
        srv3.setIssuedAt(now.minusMinutes(12));
        srv3.setCalledAt(now.minusMinutes(3));
        srv3.setActualWaitMinutes(9);
        c5.setStatus(CounterStatus.BUSY);
        c5.setTotalServedToday(1);

        // 3. Waiting Queue with mixed priorities
        QueueTicket w1 = new QueueTicket("ACC-003", 3, acc, "Arthur Dent", "+1-555-0107", PriorityLevel.VIP_URGENT, 5);
        w1.setIssuedAt(now.minusMinutes(8));

        QueueTicket w2 = new QueueTicket("ACC-004", 4, acc, "Martha Wayne", "+1-555-0108", PriorityLevel.SENIOR_CITIZEN, 9);
        w2.setIssuedAt(now.minusMinutes(7));

        QueueTicket w3 = new QueueTicket("ACC-005", 5, acc, "John Doe", "+1-555-0109", PriorityLevel.REGULAR, 14);
        w3.setIssuedAt(now.minusMinutes(5));

        QueueTicket w4 = new QueueTicket("LON-003", 3, lon, "Bruce Banner", "+1-555-0110", PriorityLevel.REGULAR, 15);
        w4.setIssuedAt(now.minusMinutes(6));

        QueueTicket w5 = new QueueTicket("LON-004", 4, lon, "Diana Prince", "+1-555-0111", PriorityLevel.VIP_URGENT, 8);
        w5.setIssuedAt(now.minusMinutes(4));

        QueueTicket w6 = new QueueTicket("CIT-003", 3, cit, "Kavita Nair", "+1-555-0112", PriorityLevel.REGULAR, 12);
        w6.setIssuedAt(now.minusMinutes(5));

        QueueTicket w7 = new QueueTicket("SUP-001", 1, sup, "Alan Turing", "+1-555-0113", PriorityLevel.REGULAR, 8);
        w7.setIssuedAt(now.minusMinutes(3));

        queueTicketRepository.saveAll(List.of(comp1, comp2, comp3, srv1, srv2, srv3, w1, w2, w3, w4, w5, w6, w7));
        counterRepository.saveAll(List.of(c1, c3, c5));

        log.info("Seeded live demo tickets across waiting, serving, and completed states.");
    }

    @Transactional
    public void resetAndReseed() {
        queueTicketRepository.deleteAll();
        crowdHistoryRepository.deleteAll();
        counterRepository.deleteAll();
        departmentRepository.deleteAll();
        seedAllData();
    }
}
