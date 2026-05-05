package com.trackng.hours.dao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateWorkerRequest(
		@NotBlank @Size(max = 200) String name,
		@NotBlank @Pattern(regexp = "^[+]?[0-9\\s]{8,20}$", message = "Телефонът трябва да е 8–20 цифри (по желание + в началото).") String phoneNumber,
		@NotBlank @Size(min = 6, max = 100) String password) {
}
