package com.equipo.sistematareas.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Se invoca cuando una ruta protegida se pide sin una autenticación válida. Responde 401 en JSON
 * e indica el motivo: sin credenciales, token inválido o token expirado.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {

		Object motivo = request.getAttribute(JwtAuthenticationFilter.ATRIBUTO_ERROR);
		String codigo;
		String mensaje;

		if ("TOKEN_EXPIRADO".equals(motivo)) {
			codigo = "TOKEN_EXPIRADO";
			mensaje = "El token de acceso expiró. Solicita uno nuevo en /api/auth/refresh.";
			response.setHeader(HttpHeaders.WWW_AUTHENTICATE,
					"Bearer error=\"invalid_token\", error_description=\"The access token expired\"");
		} else if ("TOKEN_INVALIDO".equals(motivo)) {
			codigo = "TOKEN_INVALIDO";
			mensaje = "El token no es válido: la firma no coincide o el formato es incorrecto.";
			response.setHeader(HttpHeaders.WWW_AUTHENTICATE,
					"Bearer error=\"invalid_token\", error_description=\"Invalid signature or malformed token\"");
		} else {
			codigo = "SIN_CREDENCIALES";
			mensaje = "Se requiere autenticación. Envía el encabezado Authorization: Bearer <token>.";
			response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
		}

		RespuestaError.escribir(request, response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", codigo,
				mensaje);
	}
}
