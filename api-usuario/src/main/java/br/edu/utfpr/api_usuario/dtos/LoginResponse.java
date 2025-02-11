package br.edu.utfpr.api_usuario.dtos;

public record LoginResponse(String token, Long expiresIn) {
}
