package br.edu.utfpr.api_usuario.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record NewUserDTO(

    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 20, message = "Username deve ter entre 3 e 20 caracteres")
    String username,

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 20, message = "Senha deve ter entre 6 e 20 caracteres")
    String password,

    @NotBlank(message = "Nome Completo é obrigatório")
    @Size(min = 5, max = 50, message = "Nome Completo deve ter entre 5 e 50 caracteres")
    String fullName

) {
}
