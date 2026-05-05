package com.trackng.hours.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trackng.hours.model.Worker;

public interface WorkerRepository extends JpaRepository<Worker, Long> {

	Optional<Worker> findByPhoneNumber(String phoneNumber);

	boolean existsByPhoneNumber(String phoneNumber);
}
