package com.smartqueue.repository;

import com.smartqueue.model.CrowdHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrowdHistoryRepository extends JpaRepository<CrowdHistory, Long> {

    // Find history for a specific day of week and hour
    List<CrowdHistory> findByDayOfWeekAndHourOfDay(int dayOfWeek, int hourOfDay);

    // Find all history for a specific day of week (e.g. all Mondays)
    List<CrowdHistory> findByDayOfWeekOrderByHourOfDayAsc(int dayOfWeek);

    // Find all history for a department and day of week
    List<CrowdHistory> findByDepartmentIdAndDayOfWeekOrderByHourOfDayAsc(Long departmentId, int dayOfWeek);

    // Aggregate average arrivals by hour of day across all past weeks
    @Query("SELECT ch.hourOfDay, AVG(ch.totalArrivals), AVG(ch.avgWaitMinutes), AVG(ch.avgServiceMinutes) " +
           "FROM CrowdHistory ch GROUP BY ch.hourOfDay ORDER BY ch.hourOfDay ASC")
    List<Object[]> findHourlyAveragesOverall();

    // Aggregate average arrivals for a specific day of week by hour
    @Query("SELECT ch.hourOfDay, AVG(ch.totalArrivals), AVG(ch.avgWaitMinutes), AVG(ch.avgServiceMinutes) " +
           "FROM CrowdHistory ch WHERE ch.dayOfWeek = :dayOfWeek GROUP BY ch.hourOfDay ORDER BY ch.hourOfDay ASC")
    List<Object[]> findHourlyAveragesForDay(@Param("dayOfWeek") int dayOfWeek);

    // Aggregate average arrivals for a specific department and day of week by hour
    @Query("SELECT ch.hourOfDay, AVG(ch.totalArrivals), AVG(ch.avgWaitMinutes), AVG(ch.avgServiceMinutes) " +
           "FROM CrowdHistory ch WHERE ch.department.id = :deptId AND ch.dayOfWeek = :dayOfWeek " +
           "GROUP BY ch.hourOfDay ORDER BY ch.hourOfDay ASC")
    List<Object[]> findHourlyAveragesForDeptAndDay(@Param("deptId") Long deptId, @Param("dayOfWeek") int dayOfWeek);

    // Daily volume trend (averages per day of week: 1=Mon .. 7=Sun)
    @Query("SELECT ch.dayOfWeek, AVG(ch.totalArrivals), AVG(ch.avgWaitMinutes) " +
           "FROM CrowdHistory ch GROUP BY ch.dayOfWeek ORDER BY ch.dayOfWeek ASC")
    List<Object[]> findDayOfWeekAverages();
}
