package br.edu.utfpr.api_emprestimo.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record LoanDTO(
        String id,
        String idUsuario,
        String idLivro,
        @JsonFormat(pattern = "dd-MM-yyyy")
        LocalDate dataEmprestimo,
        @JsonFormat(pattern = "dd-MM-yyyy")
        LocalDate dataDevolucaoEsperada,
        @JsonFormat(pattern = "dd-MM-yyyy")
        LocalDate dataRealDevolucao,
        boolean emprestimoAtivo,
        boolean atrasado
) {
}
