package br.edu.utfpr.api_emprestimo.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;

import java.time.LocalDate;

public record NewLoanDTO(
        String idUsuario,
        String idLivro,

        @JsonFormat(pattern = "dd-MM-yyyy")
        @Future(message = ("Data de devolução esperada deve ser no futuro"))
        LocalDate dataDevolucaoEsperada
) {
}
