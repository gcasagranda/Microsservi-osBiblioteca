package br.edu.utfpr.api_biblioteca.dtos;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

public record NewBookDTO(

        @NotBlank(message = "Título é obrigatório")
        @Length(min = 2, max = 100, message = "Título deve ter entre 2 e 100 caracteres")
        String title,

        @NotBlank(message = "Autor é obrigatório")
        @Length(min = 2, max = 50, message = "Autor deve ter entre 2 e 50 caracteres")
        String author,

        @NotBlank(message = "Ano de publicação é obrigatório")
        @Pattern(regexp = "^[0-9]{4}$", message = "Ano deve estar no formato aaaa")
        String year,

        @NotBlank(message = "ISBN é obrigatório")
        @Length(min = 10, max = 13, message = "ISBN deve ter entre 10 e 13 caracteres")
        String isbn,

        @NotNull(message = "Quantidade de cópias totais é obrigatória")
        @PositiveOrZero(message = "Total de cópias deve ser um valor positivo ou zero")
        int totalCopies
) {
}