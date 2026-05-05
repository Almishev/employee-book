package com.trackng.hours.dao;

import java.time.LocalDate;

/**
 * Изработени часове за един календарен ден (само записи с попълнен край), по дата на начало на смяната.
 */
public record WorkedHoursDayBreak(
		LocalDate date,
		long totalMinutes,
		int completeEntries,
		double totalHours) {

	public static WorkedHoursDayBreak of(LocalDate date, long totalMinutes, int completeEntries) {
		double hours = Math.round(totalMinutes / 60.0 * 100.0) / 100.0;
		return new WorkedHoursDayBreak(date, totalMinutes, completeEntries, hours);
	}
}
