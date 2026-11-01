package com.smartqueue.repository;

import com.smartqueue.model.Counter;
import com.smartqueue.model.CounterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CounterRepository extends JpaRepository<Counter, Long> {
    Optional<Counter> findByCounterNumber(int counterNumber);
    List<Counter> findByDepartmentId(Long departmentId);
    List<Counter> findByStatus(CounterStatus status);
    List<Counter> findByDepartmentIdAndStatus(Long departmentId, CounterStatus status);
}
