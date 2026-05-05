package com.trackng.hours.config;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.trackng.hours.util.Utf8MojibakeRepair;

/**
 * При старт оправя вече записани в БД имена с UTF-8→Latin-1 mojibake (директно по JDBC, без JPA lifecycle).
 */
@Component
@Order(1)
public class WorkerNameDatabaseHeal implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;

	public WorkerNameDatabaseHeal(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		List<IdName> rows = jdbcTemplate.query(
				"SELECT id, name FROM workers",
				(rs, rowNum) -> new IdName(rs.getLong("id"), rs.getString("name")));
		for (IdName row : rows) {
			if (row.name == null) {
				continue;
			}
			String fixed = Utf8MojibakeRepair.repairMisreadUtf8AsLatin1(row.name);
			if (!fixed.equals(row.name)) {
				jdbcTemplate.update("UPDATE workers SET name = ? WHERE id = ?", fixed, row.id);
			}
		}
	}

	private record IdName(long id, String name) {
	}
}
