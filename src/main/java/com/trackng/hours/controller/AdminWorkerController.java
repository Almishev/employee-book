package com.trackng.hours.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.trackng.hours.dao.CreateWorkerRequest;
import com.trackng.hours.dao.WorkerResponse;
import com.trackng.hours.service.WorkerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/workers")
public class AdminWorkerController {

	private final WorkerService workerService;

	public AdminWorkerController(WorkerService workerService) {
		this.workerService = workerService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public WorkerResponse create(@Valid @RequestBody CreateWorkerRequest request) {
		return workerService.createWorker(request);
	}

	@GetMapping
	public List<WorkerResponse> list() {
		return workerService.listWorkers();
	}
}
