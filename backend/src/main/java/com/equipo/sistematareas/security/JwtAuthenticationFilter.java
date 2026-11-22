package com.equipo.sistematareas.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro (middleware) que se ejecuta una vez por petición, antes de los controladores.
 * Lee el encabezado {@code Authorization: Bearer <token>}, valida el JWT y, si es correcto,
 * registra al usuario autenticado en el {@link SecurityContextHolder}.
 *
 * Si el token falta, el filtro no hace nada: es Spring Security quien decide después si la ruta
 * exige autenticación y responde 401 mediante {@link RestAuthenticationEntryPoint}.
 *
 * No se declara como {@code @Component} a propósito: así Spring Boot no lo registra también como
 * filtro global del servidor y solo corre dentro de la cadena de Spring Security.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	/** Atributo de la petición donde se deja el motivo del rechazo para el EntryPoint. */
	public static final String ATRIBUTO_ERROR = "jwt.error";

	private static final String PREFIJO = "Bearer ";

	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String encabezado = request.getHeader(HttpHeaders.AUTHORIZATION);

		// 1. Sin encabezado Bearer: se continúa sin autenticar.
		if (encabezado == null || !encabezado.startsWith(PREFIJO)) {
			chain.doFilter(request, response);
			return;
		}

		String token = encabezado.substring(PREFIJO.length()).trim();

		try {
			// 2. Validar firma, emisor y expiración.
			Claims claims = jwtService.validar(token);
			String username = claims.getSubject();
			String rol = claims.get("rol", String.class);

			// 3. Construir la autenticación con el rol como autoridad (ROLE_ADMIN / ROLE_USUARIO).
			var autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + rol));
			var autenticacion = new UsernamePasswordAuthenticationToken(username, null, autoridades);
			autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

			// 4. Registrar al usuario en el contexto de seguridad de esta petición.
			SecurityContextHolder.getContext().setAuthentication(autenticacion);

		} catch (ExpiredJwtException e) {
			SecurityContextHolder.clearContext();
			request.setAttribute(ATRIBUTO_ERROR, "TOKEN_EXPIRADO");
		} catch (JwtException | IllegalArgumentException e) {
			// Firma alterada, token mal formado, emisor distinto, etc.
			SecurityContextHolder.clearContext();
			request.setAttribute(ATRIBUTO_ERROR, "TOKEN_INVALIDO");
		}

		chain.doFilter(request, response);
	}
}
