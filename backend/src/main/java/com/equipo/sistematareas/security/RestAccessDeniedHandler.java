package com.equipo.sistematareas.security;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Se invoca cuando el usuario sí está autenticado pero su rol no tiene permiso (autorización).
 * Responde 403 Forbidden en JSON.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		RespuestaError.escribir(request, response, HttpServletResponse.SC_FORBIDDEN, "Forbidden", "ACCESO_DENEGADO",
				"Tu rol no tiene permiso para realizar esta operación.");
	}
}
