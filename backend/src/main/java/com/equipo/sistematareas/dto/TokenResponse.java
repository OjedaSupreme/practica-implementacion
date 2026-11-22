package com.equipo.sistematareas.dto;

/**
 * Respuesta de login y refresh. El refresh token no viaja aquí: se entrega en una cookie HttpOnly.
 *
 * @param accessToken JWT firmado que se envía en el encabezado Authorization
 * @param tokenType siempre "Bearer"
 * @param expiresIn segundos de vida del access token
 * @param username usuario autenticado
 * @param rol rol del usuario
 */
public record TokenResponse(String accessToken, String tokenType, long expiresIn, String username, String rol) {
}
