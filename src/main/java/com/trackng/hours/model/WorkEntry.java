package com.trackng.hours.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.trackng.hours.enums.TransportType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "work_entries")
public class WorkEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "worker_id", nullable = false)
	private Worker worker;

	@Column(nullable = false)
	private LocalDateTime workStart;

	private LocalDateTime workEnd;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private TransportType transportType;

	public Long getId() {
		return id;
	}

	@Transient
	@JsonProperty("employeeName")
	public String getEmployeeName() {
		return worker != null ? worker.getName() : null;
	}

	@JsonIgnore
	public Worker getWorker() {
		return worker;
	}

	@Transient
	@JsonProperty("workerId")
	public Long getWorkerId() {
		return worker != null ? worker.getId() : null;
	}

	public void setWorker(Worker worker) {
		this.worker = worker;
	}

	public LocalDateTime getWorkStart() {
		return workStart;
	}

	public void setWorkStart(LocalDateTime workStart) {
		this.workStart = workStart;
	}

	public LocalDateTime getWorkEnd() {
		return workEnd;
	}

	public void setWorkEnd(LocalDateTime workEnd) {
		this.workEnd = workEnd;
	}

	public TransportType getTransportType() {
		return transportType;
	}

	public void setTransportType(TransportType transportType) {
		this.transportType = transportType;
	}
}
