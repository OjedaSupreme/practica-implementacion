package com.equipo.sistematareas.dto;

/** Resultado interno de un login o refresh: la respuesta JSON más el refresh token para la cookie. */
public record SesionTokens(TokenResponse respuesta, String refreshToken, long refreshMaxAgeSegundos) {
}
