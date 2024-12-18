package br.edu.utfpr.api_emprestimo.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDate;

public record NewReservationDTO(
        String idUsuario,
        String idLivro,
        @JsonFormat(pattern = "dd-MM-yyyy")
        @FutureOrPresent (message = ("Data de empréstimo não pode ser no passado"))
        LocalDate dataEmprestimo,
        @JsonFormat(pattern = "dd-MM-yyyy")
        @Future (message = ("Data de devolução esperada deve ser no futuro"))
        LocalDate dataDevolucaoEsperada
) {
}
