package com.equipo.sistematareas.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Refresh token opaco guardado en la base de datos. A diferencia del JWT de acceso, se puede revocar
 * (cerrar sesión) porque el servidor conserva su estado.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String token;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;

	@Column(name = "expira_en", nullable = false)
	private Instant expiraEn;

	@Column(nullable = false)
	private boolean revocado = false;

	public RefreshToken() {
	}

	public RefreshToken(String token, Usuario usuario, Instant expiraEn) {
		this.token = token;
		this.usuario = usuario;
		this.expiraEn = expiraEn;
	}

	public boolean estaVigente() {
		return !revocado && Instant.now().isBefore(expiraEn);
	}

	public Long getId() {
		return id;
	}

	public String getToken() {
		return token;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public Instant getExpiraEn() {
		return expiraEn;
	}

	public boolean isRevocado() {
		return revocado;
	}

	public void revocar() {
		this.revocado = true;
	}
}
