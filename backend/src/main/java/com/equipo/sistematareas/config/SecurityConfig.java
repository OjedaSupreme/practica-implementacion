package com.equipo.sistematareas.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.equipo.sistematareas.repository.UsuarioRepository;
import com.equipo.sistematareas.security.JwtAuthenticationFilter;
import com.equipo.sistematareas.security.JwtService;
import com.equipo.sistematareas.security.RestAccessDeniedHandler;
import com.equipo.sistematareas.security.RestAuthenticationEntryPoint;

/**
 * Cadena de filtros de seguridad: API sin estado (stateless) protegida con JWT y autorización por rol.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
			RestAuthenticationEntryPoint entryPoint, RestAccessDeniedHandler accessDeniedHandler) throws Exception {
		JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService);

		http
				// API REST sin sesión de servidor: no se usa JSESSIONID, cada petición trae su token.
				.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.headers(h -> h.frameOptions(f -> f.sameOrigin())) // necesario para la consola H2

				// Reglas de autorización, de la más específica a la más general.
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/refresh", "/api/auth/logout")
						.permitAll()
						.requestMatchers("/h2-console/**", "/error").permitAll()
						.requestMatchers(HttpMethod.DELETE, "/api/tareas/**").hasRole("ADMIN")
						.requestMatchers("/api/**").hasAnyRole("ADMIN", "USUARIO")
						.anyRequest().authenticated())

				// Respuestas JSON para 401 (no autenticado) y 403 (sin permiso).
				.exceptionHandling(e -> e
						.authenticationEntryPoint(entryPoint)
						.accessDeniedHandler(accessDeniedHandler))

				// El filtro JWT se ejecuta antes del filtro estándar de usuario/contraseña.
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/** Carga usuarios desde la base de datos para que AuthenticationManager valide el login. */
	@Bean
	public UserDetailsService userDetailsService(UsuarioRepository usuarios) {
		return username -> usuarios.findByUsername(username)
				.map(u -> User.withUsername(u.getUsername())
						.password(u.getPasswordHash())
						.roles(u.getRol().name())
						.disabled(!u.isActivo())
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
	}

	/** BCrypt aplica sal aleatoria y un factor de costo; las contraseñas nunca se guardan en claro. */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
		return configuracion.getAuthenticationManager();
	}

	/** CORS para el frontend Angular en desarrollo. */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of("http://localhost:4200"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		config.setExposedHeaders(List.of("WWW-Authenticate"));
		config.setAllowCredentials(true); // permite la cookie HttpOnly del refresh token
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", config);
		return source;
	}
}
