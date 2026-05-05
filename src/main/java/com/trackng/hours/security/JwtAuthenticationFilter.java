package com.trackng.hours.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.trackng.hours.model.Worker;
import com.trackng.hours.repository.WorkerRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Profile("!test")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final WorkerRepository workerRepository;

	public JwtAuthenticationFilter(JwtService jwtService, WorkerRepository workerRepository) {
		this.jwtService = jwtService;
		this.workerRepository = workerRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String path = contextRelativePath(request);
		String method = request.getMethod();

		if (path.startsWith("/api/auth/login") && "POST".equalsIgnoreCase(method)) {
			filterChain.doFilter(request, response);
			return;
		}
		if (!path.startsWith("/api/")) {
			filterChain.doFilter(request, response);
			return;
		}

		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Липсва Bearer токен.\"}");
			return;
		}
		String token = header.substring(7).trim();
		try {
			Long id = jwtService.parseWorkerId(token);
			Worker worker = workerRepository.findById(id)
					.orElseThrow(() -> new IllegalArgumentException("unknown worker"));
			WorkerPrincipal principal = new WorkerPrincipal(
					worker.getId(),
					worker.getName(),
					worker.getPhoneNumber(),
					worker.getRole());
			var auth = new UsernamePasswordAuthenticationToken(
					principal,
					null,
					List.of(new SimpleGrantedAuthority("ROLE_" + worker.getRole().name())));
			SecurityContextHolder.getContext().setAuthentication(auth);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"Невалиден токен.\"}");
			return;
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * Без context path (напр. /myapp), за да работи съвпадението с /api/... при server.servlet.context-path.
	 */
	private static String contextRelativePath(HttpServletRequest request) {
		String servletPath = request.getServletPath();
		if (servletPath != null && !servletPath.isEmpty()) {
			String pathInfo = request.getPathInfo();
			return pathInfo != null ? servletPath + pathInfo : servletPath;
		}
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		if (ctx != null && !ctx.isEmpty() && uri != null && uri.startsWith(ctx)) {
			String rest = uri.substring(ctx.length());
			return rest.isEmpty() ? "/" : rest;
		}
		return uri != null ? uri : "/";
	}
}
