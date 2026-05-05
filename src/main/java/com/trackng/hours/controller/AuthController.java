package com.trackng.hours.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.trackng.hours.dao.LoginRequest;
import com.trackng.hours.dao.LoginResponse;
import com.trackng.hours.dao.MeResponse;
import com.trackng.hours.model.Worker;
import com.trackng.hours.repository.WorkerRepository;
import com.trackng.hours.security.JwtService;
import com.trackng.hours.security.WorkerPrincipal;
import com.trackng.hours.service.WorkerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final WorkerRepository workerRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthController(WorkerRepository workerRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.workerRepository = workerRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		String phone = WorkerService.normalizePhone(request.phoneNumber());
		Worker worker = workerRepository.findByPhoneNumber(phone)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверен телефон или парола."));
		if (!passwordEncoder.matches(request.password(), worker.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверен телефон или парола.");
		}
		String token = jwtService.generateToken(worker);
		return new LoginResponse(token, worker.getRole().name(), worker.getName(), worker.getPhoneNumber());
	}

	@GetMapping("/me")
	public MeResponse me(@AuthenticationPrincipal WorkerPrincipal principal) {
		if (principal == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		return new MeResponse(principal.role(), principal.name(), principal.phoneNumber());
	}
}
