package com.equipo.sistematareas.security;

import java.io.IOException;
import java.time.Instant;

import org.springframework.http.MediaType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Escribe respuestas de error de seguridad en JSON con un formato uniforme. */
final class RespuestaError {

	private RespuestaError() {
	}

	static void escribir(HttpServletRequest request, HttpServletResponse response, int status, String error,
			String codigo, String mensaje) throws IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		String json = "{"
				+ "\"timestamp\":\"" + Instant.now() + "\","
				+ "\"status\":" + status + ","
				+ "\"error\":\"" + error + "\","
				+ "\"codigo\":\"" + codigo + "\","
				+ "\"mensaje\":\"" + escapar(mensaje) + "\","
				+ "\"ruta\":\"" + escapar(request.getRequestURI()) + "\""
				+ "}";
		response.getWriter().write(json);
	}

	private static String escapar(String texto) {
		return texto.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
