package com.trackng.hours.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.trackng.hours.model.Worker;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;

@Component
public class JwtService {

	@Value("${app.jwt.secret}")
	private String secret;

	@Value("${app.jwt.expiration-ms}")
	private long expirationMs;

	private SecretKey key;

	@PostConstruct
	void init() {
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			throw new IllegalStateException("app.jwt.secret трябва да е поне 32 байта (UTF-8).");
		}
		this.key = Keys.hmacShaKeyFor(bytes);
	}

	public String generateToken(Worker worker) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + expirationMs);
		return Jwts.builder()
				.subject(String.valueOf(worker.getId()))
				.claim("role", worker.getRole().name())
				.issuedAt(now)
				.expiration(exp)
				.signWith(key)
				.compact();
	}

	public Claims parseClaims(String token) {
		try {
			return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
		} catch (RuntimeException e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Невалиден или изтекъл токен.");
		}
	}

	public Long parseWorkerId(String token) {
		return Long.parseLong(parseClaims(token).getSubject());
	}
}
