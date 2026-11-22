package com.equipo.sistematareas.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.equipo.sistematareas.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Genera y valida los JSON Web Tokens de acceso (firmados con HMAC-SHA256).
 */
@Service
public class JwtService {

	private final SecretKey clave;
	private final String emisor;
	private final Duration vidaAcceso;

	public JwtService(
			@Value("${jwt.secreto}") String secretoBase64,
			@Value("${jwt.emisor}") String emisor,
			@Value("${jwt.expiracion-acceso-ms}") long expiracionAccesoMs) {
		this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
		this.emisor = emisor;
		this.vidaAcceso = Duration.ofMillis(expiracionAccesoMs);
	}

	/** Crea un access token con la vida configurada (15 minutos por defecto). */
	public String generarToken(Usuario usuario) {
		return generarToken(usuario.getUsername(), usuario.getRol().name(), vidaAcceso);
	}

	/** Crea un access token con una vida específica. Las pruebas lo usan para fabricar tokens ya expirados. */
	public String generarToken(String username, String rol, Duration vida) {
		Instant ahora = Instant.now();
		return Jwts.builder()
				.id(UUID.randomUUID().toString())          // jti: identificador único del token
				.issuer(emisor)                             // iss
				.subject(username)                          // sub: a quién pertenece
				.claim("rol", rol)                          // claim privado con el rol
				.issuedAt(Date.from(ahora))                 // iat
				.expiration(Date.from(ahora.plus(vida)))    // exp
				.signWith(clave, Jwts.SIG.HS256)            // firma HMAC-SHA256
				.compact();
	}

	/**
	 * Verifica firma, emisor y expiración. Si algo falla lanza una subclase de
	 * {@link io.jsonwebtoken.JwtException} (por ejemplo {@code ExpiredJwtException} o
	 * {@code SignatureException}).
	 */
	public Claims validar(String token) {
		return Jwts.parser()
				.verifyWith(clave)
				.requireIssuer(emisor)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public long getVidaAccesoSegundos() {
		return vidaAcceso.toSeconds();
	}
}
