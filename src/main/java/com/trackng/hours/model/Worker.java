package com.trackng.hours.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trackng.hours.enums.WorkerRole;
import com.trackng.hours.util.Utf8MojibakeRepair;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "workers")
public class Worker {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(nullable = false, unique = true, length = 32)
	private String phoneNumber;

	@JsonIgnore
	@Column(nullable = false, length = 120)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private WorkerRole role;

	public Long getId() {
		return id;
	}

	/**
	 * Връща поправен UTF-8 при нужда, без да променя persistence полето (избягва dirty flush при четене).
	 */
	public String getName() {
		return Utf8MojibakeRepair.repairMisreadUtf8AsLatin1(name);
	}

	public void setName(String name) {
		this.name = name == null ? null : Utf8MojibakeRepair.repairMisreadUtf8AsLatin1(name);
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public WorkerRole getRole() {
		return role;
	}

	public void setRole(WorkerRole role) {
		this.role = role;
	}
}
