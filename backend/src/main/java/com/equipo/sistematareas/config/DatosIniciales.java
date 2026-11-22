package com.equipo.sistematareas.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.equipo.sistematareas.model.Rol;
import com.equipo.sistematareas.model.Usuario;
import com.equipo.sistematareas.repository.UsuarioRepository;

/**
 * Crea dos usuarios de prueba al arrancar (la base H2 está en memoria y empieza vacía).
 * Solo para desarrollo: en producción los usuarios se registran por un proceso controlado.
 */
@Configuration
public class DatosIniciales {

	@Bean
	CommandLineRunner crearUsuarios(UsuarioRepository usuarios, PasswordEncoder encoder) {
		return args -> {
			if (!usuarios.existsByUsername("admin")) {
				usuarios.save(new Usuario("admin", encoder.encode("Admin123*"), Rol.ADMIN));
			}
			if (!usuarios.existsByUsername("jesus")) {
				usuarios.save(new Usuario("jesus", encoder.encode("Usuario123*"), Rol.USUARIO));
			}
		};
	}
}
