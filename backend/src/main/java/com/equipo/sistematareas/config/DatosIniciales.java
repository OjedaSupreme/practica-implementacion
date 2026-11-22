package com.equipo.sistematareas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.equipo.sistematareas.model.Rol;
import com.equipo.sistematareas.model.Usuario;
import com.equipo.sistematareas.repository.UsuarioRepository;

/**
 * Crea los dos usuarios iniciales si todavía no existen.
 * En desarrollo usan contraseñas conocidas; en producción las contraseñas llegan por variables de entorno
 * (APP_ADMIN_PASSWORD y APP_USUARIO_PASSWORD) y nunca se escriben en el código.
 */
@Configuration
public class DatosIniciales {

	@Bean
	CommandLineRunner crearUsuarios(UsuarioRepository usuarios, PasswordEncoder encoder,
			@Value("${app.usuarios.admin-password}") String claveAdmin,
			@Value("${app.usuarios.usuario-password}") String claveUsuario) {
		return args -> {
			if (!usuarios.existsByUsername("admin")) {
				usuarios.save(new Usuario("admin", encoder.encode(claveAdmin), Rol.ADMIN));
			}
			if (!usuarios.existsByUsername("jesus")) {
				usuarios.save(new Usuario("jesus", encoder.encode(claveUsuario), Rol.USUARIO));
			}
		};
	}
}
