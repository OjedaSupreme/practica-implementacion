package com.equipo.sistematareas.security;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.equipo.sistematareas.controller.AuthController;
import com.equipo.sistematareas.model.EstadoTarea;
import com.equipo.sistematareas.model.PrioridadTarea;
import com.equipo.sistematareas.model.Tarea;
import com.equipo.sistematareas.repository.TareaRepository;

import jakarta.servlet.http.Cookie;

/**
 * Pruebas de seguridad de la autenticación JWT: rutas protegidas, tokens inválidos o expirados,
 * autorización por rol, renovación con refresh token y cierre de sesión.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadAutenticacionTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private TareaRepository tareaRepository;

	private Long idTarea;

	@BeforeEach
	void preparar() {
		tareaRepository.deleteAll();
		idTarea = tareaRepository
				.save(new Tarea("Preparar reporte", "Autenticación", EstadoTarea.PENDIENTE, PrioridadTarea.ALTA, "Ana"))
				.getId();
	}

	// ---------- Login ----------

	@Test
	@DisplayName("Login correcto: 200 con access token y cookie HttpOnly del refresh token")
	void loginCorrecto() throws Exception {
		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"Admin123*\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken", notNullValue()))
				.andExpect(jsonPath("$.tokenType", is("Bearer")))
				.andExpect(jsonPath("$.expiresIn", is(900)))
				.andExpect(jsonPath("$.rol", is("ADMIN")))
				.andExpect(cookie().exists(AuthController.COOKIE_REFRESH))
				.andExpect(cookie().httpOnly(AuthController.COOKIE_REFRESH, true))
				.andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Strict")));
	}

	@Test
	@DisplayName("Login con contraseña incorrecta: 401 CREDENCIALES_INVALIDAS")
	void loginContrasenaIncorrecta() throws Exception {
		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"admin\",\"password\":\"incorrecta\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.codigo", is("CREDENCIALES_INVALIDAS")));
	}

	// ---------- Rutas protegidas ----------

	@Test
	@DisplayName("Sin encabezado Authorization: 401 SIN_CREDENCIALES")
	void sinCredencialesResponde401() throws Exception {
		mockMvc.perform(get("/api/tareas"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
				.andExpect(jsonPath("$.codigo", is("SIN_CREDENCIALES")));
	}

	@Test
	@DisplayName("Token manipulado (rol cambiado a ADMIN sin volver a firmar): 401 TOKEN_INVALIDO")
	void tokenManipuladoResponde401() throws Exception {
		String token = login("jesus", "Usuario123*");
		String[] partes = token.split("\\.");
		String payload = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);
		String payloadFalso = payload.replace("\"rol\":\"USUARIO\"", "\"rol\":\"ADMIN\"");
		String alterado = partes[0] + "."
				+ Base64.getUrlEncoder().withoutPadding().encodeToString(payloadFalso.getBytes(StandardCharsets.UTF_8))
				+ "." + partes[2];

		mockMvc.perform(delete("/api/tareas/{id}", idTarea).header(HttpHeaders.AUTHORIZATION, "Bearer " + alterado))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.codigo", is("TOKEN_INVALIDO")));
	}

	@Test
	@DisplayName("Texto que no es un JWT: 401 TOKEN_INVALIDO")
	void tokenMalFormadoResponde401() throws Exception {
		mockMvc.perform(get("/api/tareas").header(HttpHeaders.AUTHORIZATION, "Bearer esto.no.esuntoken"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.codigo", is("TOKEN_INVALIDO")));
	}

	@Test
	@DisplayName("Token expirado: 401 TOKEN_EXPIRADO")
	void tokenExpiradoResponde401() throws Exception {
		String expirado = jwtService.generarToken("jesus", "USUARIO", Duration.ofSeconds(-60));

		mockMvc.perform(get("/api/tareas").header(HttpHeaders.AUTHORIZATION, "Bearer " + expirado))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("invalid_token")))
				.andExpect(jsonPath("$.codigo", is("TOKEN_EXPIRADO")));
	}

	@Test
	@DisplayName("Token válido: 200 en ruta protegida")
	void tokenValidoPermiteAcceso() throws Exception {
		String token = login("jesus", "Usuario123*");

		mockMvc.perform(get("/api/tareas").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].titulo", is("Preparar reporte")));
	}

	// ---------- Autorización por rol ----------

	@Test
	@DisplayName("Rol USUARIO intenta eliminar: 403 ACCESO_DENEGADO")
	void usuarioNoPuedeEliminar() throws Exception {
		String token = login("jesus", "Usuario123*");

		mockMvc.perform(delete("/api/tareas/{id}", idTarea).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.codigo", is("ACCESO_DENEGADO")));
	}

	@Test
	@DisplayName("Rol ADMIN elimina: 204")
	void adminPuedeEliminar() throws Exception {
		String token = login("admin", "Admin123*");

		mockMvc.perform(delete("/api/tareas/{id}", idTarea).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNoContent());
	}

	// ---------- Renovación y cierre de sesión ----------

	@Test
	@DisplayName("Refresh: entrega un access token nuevo y rota la cookie; el refresh viejo ya no sirve")
	void refreshRotaElToken() throws Exception {
		MvcResult login = loginCompleto("jesus", "Usuario123*");
		Cookie refreshOriginal = login.getResponse().getCookie(AuthController.COOKIE_REFRESH);

		MvcResult renovado = mockMvc.perform(post("/api/auth/refresh").cookie(refreshOriginal))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken", notNullValue()))
				.andExpect(cookie().value(AuthController.COOKIE_REFRESH, not(refreshOriginal.getValue())))
				.andReturn();
		assertNotEquals(refreshOriginal.getValue(),
				renovado.getResponse().getCookie(AuthController.COOKIE_REFRESH).getValue());

		// Reutilizar el refresh token anterior se rechaza (detección de robo).
		mockMvc.perform(post("/api/auth/refresh").cookie(refreshOriginal))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Logout revoca el refresh token")
	void logoutRevocaRefresh() throws Exception {
		Cookie refresh = loginCompleto("admin", "Admin123*").getResponse().getCookie(AuthController.COOKIE_REFRESH);

		mockMvc.perform(post("/api/auth/logout").cookie(refresh))
				.andExpect(status().isNoContent())
				.andExpect(cookie().maxAge(AuthController.COOKIE_REFRESH, 0));

		mockMvc.perform(post("/api/auth/refresh").cookie(refresh))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("Refresh sin cookie: 401")
	void refreshSinCookie() throws Exception {
		mockMvc.perform(post("/api/auth/refresh"))
				.andExpect(status().isUnauthorized());
	}

	// ---------- utilidades ----------

	private String login(String usuario, String contrasena) throws Exception {
		String cuerpo = loginCompleto(usuario, contrasena).getResponse().getContentAsString();
		return cuerpo.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
	}

	private MvcResult loginCompleto(String usuario, String contrasena) throws Exception {
		return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"" + usuario + "\",\"password\":\"" + contrasena + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
	}
}
