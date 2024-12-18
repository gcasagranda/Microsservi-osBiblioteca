package br.edu.utfpr.api_biblioteca.dtos;

public record BookDTO(
        String id,
        String title,
        String author,
        String year,
        String isbn,
        int totalCopies,
        int availableCopies
) {
}
