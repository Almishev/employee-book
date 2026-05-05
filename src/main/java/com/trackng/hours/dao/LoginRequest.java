package com.trackng.hours.dao;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank String phoneNumber,
		@NotBlank String password) {
}
