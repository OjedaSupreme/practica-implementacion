package com.equipo.sistematareas.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.equipo.sistematareas.dto.LoginRequest;
import com.equipo.sistematareas.dto.SesionTokens;
import com.equipo.sistematareas.dto.TokenResponse;
import com.equipo.sistematareas.model.RefreshToken;
import com.equipo.sistematareas.model.Usuario;
import com.equipo.sistematareas.repository.RefreshTokenRepository;
import com.equipo.sistematareas.repository.UsuarioRepository;
import com.equipo.sistematareas.security.JwtService;

/**
 * Lógica de inicio de sesión, renovación (refresh con rotación) y cierre de sesión.
 */
@Service
public class AuthService {

	private static final SecureRandom ALEATORIO = new SecureRandom();

	private final AuthenticationManager authenticationManager;
	private final UsuarioRepository usuarios;
	private final RefreshTokenRepository refreshTokens;
	private final JwtService jwtService;
	private final Duration vidaRefresh;

	public AuthService(AuthenticationManager authenticationManager, UsuarioRepository usuarios,
			RefreshTokenRepository refreshTokens, JwtService jwtService,
			@Value("${jwt.expiracion-refresh-ms}") long expiracionRefreshMs) {
		this.authenticationManager = authenticationManager;
		this.usuarios = usuarios;
		this.refreshTokens = refreshTokens;
		this.jwtService = jwtService;
		this.vidaRefresh = Duration.ofMillis(expiracionRefreshMs);
	}

	/** Valida usuario y contraseña (BCrypt) y emite un access token y un refresh token. */
	@Transactional
	public SesionTokens login(LoginRequest datos) {
		if (datos == null || datos.username() == null || datos.password() == null) {
			throw new BadCredentialsException("Credenciales incompletas");
		}
		try {
			// Spring Security busca al usuario y compara la contraseña con el hash BCrypt.
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(datos.username().trim(), datos.password()));
		} catch (AuthenticationException e) {
			// Mismo mensaje para usuario inexistente o contraseña incorrecta (no se revela cuál falló).
			throw new BadCredentialsException("Usuario o contraseña incorrectos");
		}

		Usuario usuario = usuarios.findByUsername(datos.username().trim())
				.orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
		return emitir(usuario);
	}

	/**
	 * Cambia un refresh token vigente por un par nuevo (rotación). Si se presenta un refresh token que
	 * ya había sido usado o revocado, se asume robo y se revocan todas las sesiones de ese usuario.
	 */
	@Transactional(noRollbackFor = BadCredentialsException.class) // conservar la revocación aunque se rechace
	public SesionTokens refrescar(String valorRefresh) {
		if (valorRefresh == null || valorRefresh.isBlank()) {
			throw new BadCredentialsException("Falta el refresh token");
		}
		RefreshToken actual = refreshTokens.findByToken(valorRefresh)
				.orElseThrow(() -> new BadCredentialsException("Refresh token desconocido"));

		if (actual.isRevocado()) {
			refreshTokens.findByUsuarioAndRevocadoFalse(actual.getUsuario()).forEach(RefreshToken::revocar);
			throw new BadCredentialsException("Refresh token reutilizado: se cerraron todas las sesiones");
		}
		if (!actual.estaVigente()) {
			throw new BadCredentialsException("Refresh token expirado");
		}

		actual.revocar(); // un refresh token sirve una sola vez
		return emitir(actual.getUsuario());
	}

	/** Revoca el refresh token; el access token deja de renovarse y caduca solo en minutos. */
	@Transactional
	public void cerrarSesion(String valorRefresh) {
		if (valorRefresh == null) {
			return;
		}
		refreshTokens.findByToken(valorRefresh).ifPresent(RefreshToken::revocar);
	}

	public long getVidaRefreshSegundos() {
		return vidaRefresh.toSeconds();
	}

	private SesionTokens emitir(Usuario usuario) {
		String accessToken = jwtService.generarToken(usuario);

		byte[] bytes = new byte[48];
		ALEATORIO.nextBytes(bytes);
		String valorRefresh = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		refreshTokens.save(new RefreshToken(valorRefresh, usuario, Instant.now().plus(vidaRefresh)));

		TokenResponse respuesta = new TokenResponse(accessToken, "Bearer", jwtService.getVidaAccesoSegundos(),
				usuario.getUsername(), usuario.getRol().name());
		return new SesionTokens(respuesta, valorRefresh, vidaRefresh.toSeconds());
	}
}
