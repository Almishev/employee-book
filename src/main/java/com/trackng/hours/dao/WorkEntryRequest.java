package com.trackng.hours.dao;

import java.time.LocalDateTime;

import com.trackng.hours.enums.TransportType;

import jakarta.validation.constraints.NotNull;

public record WorkEntryRequest(
		@NotNull LocalDateTime workStart,
		LocalDateTime workEnd,
		@NotNull TransportType transportType) {
}
