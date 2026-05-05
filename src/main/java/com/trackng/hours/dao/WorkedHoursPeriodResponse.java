package com.trackng.hours.dao;

import java.time.LocalDate;
import java.util.List;

/**
 * Обобщение за период — един работник или всички (само за админ).
 */
public record WorkedHoursPeriodResponse(
		boolean allWorkers,
		LocalDate periodFrom,
		LocalDate periodTo,
		List<WorkedHoursSummary> rows,
		long grandTotalMinutes,
		int grandCompleteEntries,
		double grandTotalHours) {

	public static WorkedHoursPeriodResponse ofSingle(WorkedHoursSummary row) {
		return new WorkedHoursPeriodResponse(
				false,
				row.periodFrom(),
				row.periodTo(),
				List.of(row),
				row.totalMinutes(),
				row.completeEntries(),
				row.totalHours());
	}

	public static WorkedHoursPeriodResponse ofAll(LocalDate from, LocalDate to, List<WorkedHoursSummary> rows) {
		long tm = rows.stream().mapToLong(WorkedHoursSummary::totalMinutes).sum();
		int ce = rows.stream().mapToInt(WorkedHoursSummary::completeEntries).sum();
		double th = Math.round(tm / 60.0 * 100.0) / 100.0;
		return new WorkedHoursPeriodResponse(true, from, to, rows, tm, ce, th);
	}
}
