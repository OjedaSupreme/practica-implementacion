package com.equipo.sistematareas.controller;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.equipo.sistematareas.dto.LoginRequest;
import com.equipo.sistematareas.dto.SesionTokens;
import com.equipo.sistematareas.dto.TokenResponse;
import com.equipo.sistematareas.service.AuthService;

/**
 * Endpoints públicos de autenticación. El refresh token viaja solo en una cookie HttpOnly, así que
 * JavaScript no puede leerlo aunque la página sufriera un ataque XSS.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	public static final String COOKIE_REFRESH = "refresh_token";

	private final AuthService authService;
	private final boolean cookieSegura;

	public AuthController(AuthService authService, @Value("${jwt.cookie-segura}") boolean cookieSegura) {
		this.authService = authService;
		this.cookieSegura = cookieSegura;
	}

	@PostMapping("/login")
	public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest datos) {
		return conCookie(authService.login(datos));
	}

	@PostMapping("/refresh")
	public ResponseEntity<TokenResponse> refresh(
			@CookieValue(name = COOKIE_REFRESH, required = false) String refreshToken) {
		return conCookie(authService.refrescar(refreshToken));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@CookieValue(name = COOKIE_REFRESH, required = false) String refreshToken) {
		authService.cerrarSesion(refreshToken);
		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, cookie("", 0).toString())
				.build();
	}

	/** Ruta protegida: devuelve quién es el usuario según el token recibido. */
	@GetMapping("/me")
	public Map<String, Object> me(Authentication autenticacion) {
		Map<String, Object> datos = new LinkedHashMap<>();
		datos.put("username", autenticacion.getName());
		datos.put("roles", autenticacion.getAuthorities().stream().map(Object::toString).toList());
		return datos;
	}

	/** Credenciales o refresh token incorrectos: 401 sin revelar cuál dato falló. */
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<Map<String, Object>> credencialesInvalidas(AuthenticationException e) {
		Map<String, Object> cuerpo = new LinkedHashMap<>();
		cuerpo.put("timestamp", Instant.now().toString());
		cuerpo.put("status", 401);
		cuerpo.put("error", "Unauthorized");
		cuerpo.put("codigo", "CREDENCIALES_INVALIDAS");
		cuerpo.put("mensaje", e.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.header(HttpHeaders.SET_COOKIE, cookie("", 0).toString())
				.body(cuerpo);
	}

	private ResponseEntity<TokenResponse> conCookie(SesionTokens sesion) {
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie(sesion.refreshToken(), sesion.refreshMaxAgeSegundos()).toString())
				.body(sesion.respuesta());
	}

	private ResponseCookie cookie(String valor, long maxAgeSegundos) {
		return ResponseCookie.from(COOKIE_REFRESH, valor)
				.httpOnly(true)          // inaccesible desde JavaScript
				.secure(cookieSegura)    // solo HTTPS en producción
				.sameSite("Strict")      // no se envía en peticiones de otros sitios (mitiga CSRF)
				.path("/api/auth")       // solo viaja a los endpoints de autenticación
				.maxAge(maxAgeSegundos)
				.build();
	}
}
