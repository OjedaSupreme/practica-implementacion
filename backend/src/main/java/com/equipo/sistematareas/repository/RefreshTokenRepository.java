package com.equipo.sistematareas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.equipo.sistematareas.model.RefreshToken;
import com.equipo.sistematareas.model.Usuario;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByToken(String token);

	List<RefreshToken> findByUsuarioAndRevocadoFalse(Usuario usuario);
}
