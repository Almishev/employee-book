package com.trackng.hours.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.trackng.hours.dao.WorkedHoursDayBreak;
import com.trackng.hours.dao.WorkedHoursPeriodResponse;
import com.trackng.hours.dao.WorkedHoursSummary;
import com.trackng.hours.dao.WorkEntryRequest;
import com.trackng.hours.enums.WorkerRole;
import com.trackng.hours.model.WorkEntry;
import com.trackng.hours.model.Worker;
import com.trackng.hours.repository.WorkEntryRepository;
import com.trackng.hours.repository.WorkerRepository;
import com.trackng.hours.security.WorkerPrincipal;

@Service
public class WorkEntryService {

	private final WorkEntryRepository repository;
	private final WorkerRepository workerRepository;

	public WorkEntryService(WorkEntryRepository repository, WorkerRepository workerRepository) {
		this.repository = repository;
		this.workerRepository = workerRepository;
	}

	public WorkEntry create(WorkEntryRequest request) {
		WorkerPrincipal me = currentPrincipal();
		if (me.role() == WorkerRole.ADMIN) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Администраторът не записва работни часове.");
		}
		validateTimes(request.workStart(), request.workEnd());
		Worker worker = workerRepository.findById(me.id())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
		WorkEntry entry = new WorkEntry();
		entry.setWorker(worker);
		applyTimesAndTransport(entry, request);
		return repository.save(entry);
	}

	public List<WorkEntry> list(Integer year, Integer month) {
		WorkerPrincipal me = currentPrincipal();
		if (year == null && month == null) {
			if (me.role() == WorkerRole.ADMIN) {
				return repository.findAllOrderedByWorkerName();
			}
			return repository.findByWorkerIdOrdered(me.id());
		}
		if (year == null || month == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Подайте и year, и month, или нито едно.");
		}
		LocalDateTime from = LocalDateTime.of(year, month, 1, 0, 0);
		LocalDateTime to = from.plusMonths(1);
		if (me.role() == WorkerRole.ADMIN) {
			return repository.findByWorkStartRangeOrdered(from, to);
		}
		return repository.findByWorkerIdAndWorkStartRange(me.id(), from, to);
	}

	public WorkEntry getById(Long id) {
		WorkEntry entry = repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		assertCanAccess(entry);
		return entry;
	}

	public WorkEntry update(Long id, WorkEntryRequest request) {
		validateTimes(request.workStart(), request.workEnd());
		WorkEntry entry = repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		assertCanAccess(entry);
		applyTimesAndTransport(entry, request);
		return repository.save(entry);
	}

	public void delete(Long id) {
		WorkEntry entry = repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		assertCanAccess(entry);
		repository.deleteById(id);
	}

	private record PeriodBounds(LocalDateTime rangeStart, LocalDateTime rangeEndExclusive) {
	}

	private PeriodBounds validatePeriod(LocalDate from, LocalDate to) {
		if (from == null || to == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Подайте from и to (дата).");
		}
		if (to.isBefore(from)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Крайната дата не може да е преди началната.");
		}
		long maxSpanDays = 366L * 3;
		if (ChronoUnit.DAYS.between(from, to) > maxSpanDays) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Периодът е твърде дълъг (макс. около 3 години).");
		}
		return new PeriodBounds(from.atStartOfDay(), to.plusDays(1).atStartOfDay());
	}

	/**
	 * Админ: {@code workerId} липсва = всички работници поотделно + общо; иначе един избран. Работник: винаги само себе си.
	 */
	public WorkedHoursPeriodResponse summarizeWorkedHours(LocalDate from, LocalDate to, Long workerIdParam) {
		PeriodBounds bounds = validatePeriod(from, to);
		WorkerPrincipal me = currentPrincipal();
		if (me.role() == WorkerRole.ADMIN && workerIdParam == null) {
			List<WorkedHoursSummary> rows = new ArrayList<>();
			for (Worker w : workerRepository.findAll(Sort.by("name"))) {
				List<WorkEntry> entries = repository.findByWorkerIdAndWorkStartRange(
						w.getId(), bounds.rangeStart(), bounds.rangeEndExclusive());
				Totals t = sumCompletedWork(entries);
				rows.add(WorkedHoursSummary.of(
						w.getId(), w.getName(), from, to, t.minutes(), t.complete(), completedMinutesByDay(entries)));
			}
			return WorkedHoursPeriodResponse.ofAll(from, to, rows);
		}
		long targetWorkerId = resolveSingleWorkerIdForPeriod(workerIdParam, me);
		List<WorkEntry> entries = repository.findByWorkerIdAndWorkStartRange(
				targetWorkerId, bounds.rangeStart(), bounds.rangeEndExclusive());
		Totals t = sumCompletedWork(entries);
		Worker worker = workerRepository.findById(targetWorkerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		WorkedHoursSummary one = WorkedHoursSummary.of(
				targetWorkerId,
				worker.getName(),
				from,
				to,
				t.minutes(),
				t.complete(),
				completedMinutesByDay(entries));
		return WorkedHoursPeriodResponse.ofSingle(one);
	}

	private record DayAccum(long minutes, int count) {
	}

	private static List<WorkedHoursDayBreak> completedMinutesByDay(List<WorkEntry> entries) {
		Map<LocalDate, DayAccum> byDay = new TreeMap<>();
		for (WorkEntry e : entries) {
			LocalDateTime end = e.getWorkEnd();
			if (end == null) {
				continue;
			}
			long mins = Duration.between(e.getWorkStart(), end).toMinutes();
			if (mins <= 0) {
				continue;
			}
			LocalDate d = e.getWorkStart().toLocalDate();
			byDay.merge(d, new DayAccum(mins, 1), (a, b) -> new DayAccum(a.minutes + b.minutes, a.count + b.count));
		}
		List<WorkedHoursDayBreak> out = new ArrayList<>(byDay.size());
		for (Map.Entry<LocalDate, DayAccum> en : byDay.entrySet()) {
			DayAccum a = en.getValue();
			out.add(WorkedHoursDayBreak.of(en.getKey(), a.minutes, a.count));
		}
		return out;
	}

	private record Totals(long minutes, int complete) {
	}

	private static Totals sumCompletedWork(List<WorkEntry> entries) {
		long totalMinutes = 0;
		int complete = 0;
		for (WorkEntry e : entries) {
			LocalDateTime end = e.getWorkEnd();
			if (end == null) {
				continue;
			}
			long mins = Duration.between(e.getWorkStart(), end).toMinutes();
			if (mins <= 0) {
				continue;
			}
			totalMinutes += mins;
			complete++;
		}
		return new Totals(totalMinutes, complete);
	}

	private long resolveSingleWorkerIdForPeriod(Long workerIdParam, WorkerPrincipal me) {
		if (me.role() == WorkerRole.ADMIN) {
			if (workerIdParam == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Подайте workerId или използвай режим „всички“ без параметър.");
			}
			if (!workerRepository.existsById(workerIdParam)) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Няма работник с този ID.");
			}
			return workerIdParam;
		}
		if (workerIdParam != null && !workerIdParam.equals(me.id())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Можеш да виждаш само своите часове.");
		}
		return me.id();
	}

	/**
	 * Експорт: админ без {@code workerId} = всички записи в периода (всички работници). Иначе един работник.
	 */
	public byte[] exportPeriodAsCsv(LocalDate from, LocalDate to, Long workerIdParam) {
		PeriodBounds bounds = validatePeriod(from, to);
		WorkerPrincipal me = currentPrincipal();
		List<WorkEntry> entries;
		if (me.role() == WorkerRole.ADMIN && workerIdParam == null) {
			entries = new ArrayList<>(repository.findByWorkStartRangeOrdered(bounds.rangeStart(), bounds.rangeEndExclusive()));
		} else {
			long wid = resolveSingleWorkerIdForPeriod(workerIdParam, me);
			entries = new ArrayList<>(repository.findByWorkerIdAndWorkStartRange(wid, bounds.rangeStart(), bounds.rangeEndExclusive()));
		}
		entries.sort(Comparator
				.comparing((WorkEntry e) -> e.getWorker() != null ? e.getWorker().getName() : "", String.CASE_INSENSITIVE_ORDER)
				.thenComparing(WorkEntry::getWorkStart));
		DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
		StringBuilder sb = new StringBuilder();
		sb.append('\uFEFF');
		sb.append("Дата;Час на започване;Час на приключване;Превоз;Име\n");
		for (WorkEntry e : entries) {
			LocalDateTime start = e.getWorkStart();
			LocalDateTime end = e.getWorkEnd();
			String name = e.getWorker() != null ? e.getWorker().getName() : "";
			sb.append(csvField(dateFmt.format(start.toLocalDate())));
			sb.append(';');
			sb.append(csvField(timeFmt.format(start.toLocalTime())));
			sb.append(';');
			sb.append(end != null ? csvField(timeFmt.format(end.toLocalTime())) : "");
			sb.append(';');
			sb.append(csvField(transportLabel(e.getTransportType().name())));
			sb.append(';');
			sb.append(csvField(name));
			sb.append('\n');
		}
		return sb.toString().getBytes(StandardCharsets.UTF_8);
	}

	private static String transportLabel(String code) {
		return switch (code) {
			case "PERSONAL" -> "Личен";
			case "COMPANY" -> "Служебен";
			default -> code;
		};
	}

	private static String csvField(String raw) {
		if (raw == null) {
			return "";
		}
		boolean needQuote = raw.indexOf(';') >= 0 || raw.indexOf('"') >= 0 || raw.indexOf('\n') >= 0 || raw.indexOf('\r') >= 0;
		String escaped = raw.replace("\"", "\"\"");
		if (needQuote) {
			return "\"" + escaped + "\"";
		}
		return escaped;
	}

	private void assertCanAccess(WorkEntry entry) {
		WorkerPrincipal me = currentPrincipal();
		if (me.role() == WorkerRole.ADMIN) {
			return;
		}
		if (!entry.getWorker().getId().equals(me.id())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нямате достъп до този запис.");
		}
	}

	private static WorkerPrincipal currentPrincipal() {
		Authentication a = SecurityContextHolder.getContext().getAuthentication();
		if (a == null || !(a.getPrincipal() instanceof WorkerPrincipal wp)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		return wp;
	}

	private static void applyTimesAndTransport(WorkEntry entry, WorkEntryRequest request) {
		entry.setWorkStart(request.workStart());
		entry.setWorkEnd(request.workEnd());
		entry.setTransportType(request.transportType());
	}

	private static void validateTimes(LocalDateTime workStart, LocalDateTime workEnd) {
		if (workEnd != null && workEnd.isBefore(workStart)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "workEnd не може да е преди workStart.");
		}
	}
}
