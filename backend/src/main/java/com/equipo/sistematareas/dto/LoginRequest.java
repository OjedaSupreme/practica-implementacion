package com.equipo.sistematareas.dto;

/** Credenciales que envía el cliente a {@code POST /api/auth/login}. */
public record LoginRequest(String username, String password) {
}
