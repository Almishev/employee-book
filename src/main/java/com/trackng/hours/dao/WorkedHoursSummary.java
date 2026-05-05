package com.trackng.hours.dao;

import java.time.LocalDate;
import java.util.List;

/**
 * Обобщение на изработени часове за период (само записи с попълнен край), с разбивка по дни.
 */
public record WorkedHoursSummary(
		long workerId,
		String workerName,
		LocalDate periodFrom,
		LocalDate periodTo,
		long totalMinutes,
		int completeEntries,
		double totalHours,
		List<WorkedHoursDayBreak> byDay) {

	public static WorkedHoursSummary of(
			long workerId,
			String workerName,
			LocalDate periodFrom,
			LocalDate periodTo,
			long totalMinutes,
			int completeEntries,
			List<WorkedHoursDayBreak> byDay) {
		double hours = Math.round(totalMinutes / 60.0 * 100.0) / 100.0;
		return new WorkedHoursSummary(
				workerId,
				workerName,
				periodFrom,
				periodTo,
				totalMinutes,
				completeEntries,
				hours,
				byDay == null ? List.of() : List.copyOf(byDay));
	}
}
