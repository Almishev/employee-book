package com.trackng.hours.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.trackng.hours.dao.CreateWorkerRequest;
import com.trackng.hours.dao.WorkerResponse;
import com.trackng.hours.enums.WorkerRole;
import com.trackng.hours.model.Worker;
import com.trackng.hours.repository.WorkerRepository;
import com.trackng.hours.util.BulgarianLatinToCyrillic;

@Service
public class WorkerService {

	private final WorkerRepository workerRepository;
	private final PasswordEncoder passwordEncoder;

	public WorkerService(WorkerRepository workerRepository, PasswordEncoder passwordEncoder) {
		this.workerRepository = workerRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public static String normalizePhone(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Телефонът е задължителен.");
		}
		String s = raw.trim().replaceAll("\\s+", "");
		if (s.startsWith("+")) {
			s = s.substring(1);
		}
		if (!s.matches("[0-9]{8,15}")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Невалиден телефонен номер.");
		}
		return s;
	}

	public WorkerResponse createWorker(CreateWorkerRequest request) {
		String phone = normalizePhone(request.phoneNumber());
		if (workerRepository.existsByPhoneNumber(phone)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Работник с този телефон вече съществува.");
		}
		Worker w = new Worker();
		w.setName(BulgarianLatinToCyrillic.convertName(request.name().trim()));
		w.setPhoneNumber(phone);
		w.setPasswordHash(passwordEncoder.encode(request.password()));
		w.setRole(WorkerRole.WORKER);
		workerRepository.save(w);
		return toResponse(w);
	}

	public List<WorkerResponse> listWorkers() {
		return workerRepository.findAll().stream().map(this::toResponse).toList();
	}

	private WorkerResponse toResponse(Worker w) {
		return new WorkerResponse(w.getId(), w.getName(), w.getPhoneNumber(), w.getRole());
	}
}
