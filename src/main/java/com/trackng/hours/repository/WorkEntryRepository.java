package com.trackng.hours.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trackng.hours.model.WorkEntry;

public interface WorkEntryRepository extends JpaRepository<WorkEntry, Long> {

	@Query("SELECT e FROM WorkEntry e JOIN FETCH e.worker w ORDER BY w.name ASC, e.workStart DESC")
	List<WorkEntry> findAllOrderedByWorkerName();

	@Query("SELECT e FROM WorkEntry e JOIN FETCH e.worker w WHERE e.workStart >= :from AND e.workStart < :to ORDER BY w.name ASC, e.workStart DESC")
	List<WorkEntry> findByWorkStartRangeOrdered(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("SELECT e FROM WorkEntry e JOIN FETCH e.worker w WHERE w.id = :workerId ORDER BY e.workStart DESC")
	List<WorkEntry> findByWorkerIdOrdered(@Param("workerId") Long workerId);

	@Query("SELECT e FROM WorkEntry e JOIN FETCH e.worker w WHERE w.id = :workerId AND e.workStart >= :from AND e.workStart < :to ORDER BY e.workStart DESC")
	List<WorkEntry> findByWorkerIdAndWorkStartRange(
			@Param("workerId") Long workerId,
			@Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to);
}
