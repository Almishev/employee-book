package com.trackng.hours.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.trackng.hours.dao.WorkedHoursPeriodResponse;
import com.trackng.hours.dao.WorkEntryRequest;
import com.trackng.hours.model.WorkEntry;
import com.trackng.hours.service.WorkEntryService;

@RestController
@RequestMapping("/api/work-entries")
public class WorkEntryController {

	private final WorkEntryService service;

	public WorkEntryController(WorkEntryService service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public WorkEntry create(@Valid @RequestBody WorkEntryRequest request) {
		return service.create(request);
	}

	@GetMapping
	public List<WorkEntry> list(
			@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month) {
		return service.list(year, month);
	}

	@GetMapping("/worked-hours-summary")
	public WorkedHoursPeriodResponse workedHoursSummary(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) Long workerId) {
		return service.summarizeWorkedHours(from, to, workerId);
	}

	@GetMapping("/export")
	public ResponseEntity<byte[]> exportCsv(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(required = false) Long workerId) {
		byte[] body = service.exportPeriodAsCsv(from, to, workerId);
		String filename = "zapisi_" + from + "_" + to + ".csv";
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
				.contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
				.body(body);
	}

	@GetMapping("/{id}")
	public WorkEntry get(@PathVariable Long id) {
		return service.getById(id);
	}

	@PutMapping("/{id}")
	public WorkEntry update(@PathVariable Long id, @Valid @RequestBody WorkEntryRequest request) {
		return service.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		service.delete(id);
	}
}
