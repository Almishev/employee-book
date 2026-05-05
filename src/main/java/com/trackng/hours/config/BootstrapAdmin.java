package com.trackng.hours.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.trackng.hours.enums.WorkerRole;
import com.trackng.hours.model.Worker;
import com.trackng.hours.repository.WorkerRepository;
import com.trackng.hours.service.WorkerService;
import com.trackng.hours.util.BulgarianLatinToCyrillic;

@Component
@Order(0)
public class BootstrapAdmin implements ApplicationRunner {

	private final WorkerRepository workerRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.bootstrap.admin-phone}")
	private String adminPhone;

	@Value("${app.bootstrap.admin-password}")
	private String adminPassword;

	@Value("${app.bootstrap.admin-name}")
	private String adminName;

	public BootstrapAdmin(WorkerRepository workerRepository, PasswordEncoder passwordEncoder) {
		this.workerRepository = workerRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(ApplicationArguments args) {
		String phone = WorkerService.normalizePhone(adminPhone);
		if (workerRepository.existsByPhoneNumber(phone)) {
			return;
		}
		Worker admin = new Worker();
		admin.setName(BulgarianLatinToCyrillic.convertName(adminName.trim()));
		admin.setPhoneNumber(phone);
		admin.setPasswordHash(passwordEncoder.encode(adminPassword));
		admin.setRole(WorkerRole.ADMIN);
		workerRepository.save(admin);
	}
}
