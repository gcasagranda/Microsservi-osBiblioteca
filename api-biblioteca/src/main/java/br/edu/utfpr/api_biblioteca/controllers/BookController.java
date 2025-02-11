package br.edu.utfpr.api_biblioteca.controllers;

import br.edu.utfpr.api_biblioteca.dtos.BookDTO;
import br.edu.utfpr.api_biblioteca.dtos.NewBookDTO;
import br.edu.utfpr.api_biblioteca.dtos.UpdateTotalCopiesDTO;
import br.edu.utfpr.api_biblioteca.models.Book;
import br.edu.utfpr.api_biblioteca.repositories.BookRepository;
import br.edu.utfpr.api_biblioteca.services.BookIdSequenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/books")
@Tag(name = "Livros", description = "Operações relacionadas a livros")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookIdSequenceService bookIdSequenceService;

    public void updateTotalCopies(Book book, int newTotalCopiesValue){
        int oldTotalCopiesValue = book.getTotalCopies();
        int oldAvailableCopiesValue = book.getAvailableCopies();
        int dif = newTotalCopiesValue - oldTotalCopiesValue;
        book.setTotalCopies(newTotalCopiesValue);
        book.setAvailableCopies(oldAvailableCopiesValue + dif);
        bookRepository.save(book);
    }

    public void updateAvailableCopies(Book book, Boolean loan) {
        int oldAvailableCopiesValue = book.getAvailableCopies();
        int totalCopiesValue = book.getTotalCopies();

        if (loan) {
            if (oldAvailableCopiesValue == 0) {
                throw new IllegalStateException("Erro: Não há mais cópias disponíveis para empréstimo.");
            }
            book.setAvailableCopies(oldAvailableCopiesValue - 1);
        } else {
            if (oldAvailableCopiesValue == totalCopiesValue) {
                throw new IllegalStateException("Erro: Todas as cópias já estão disponíveis. Não é possível devolver mais.");
            }
            book.setAvailableCopies(oldAvailableCopiesValue + 1);
        }
        bookRepository.save(book);
    }

    @PostMapping(path = "/")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @Operation(
            summary = "Cadastra novo livro no acervo (acesso exclusivo do ADMIN).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do novo Livro",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = NewBookDTO.class)
                    )
            ),
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Livro {titulo} cadastrado"
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Livro já cadastrado"
                    )
            }
    )
    public ResponseEntity<?> createBook(@Valid @RequestBody NewBookDTO newBookDTO) {
        Optional<Book> foundBook = bookRepository.findByIsbn(newBookDTO.isbn());
        if (foundBook.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Livro já cadastrado");
        }
        Book book = new Book();
        book.setTitle(newBookDTO.title());
        book.setAuthor(newBookDTO.author());
        book.setIsbn(newBookDTO.isbn());
        book.setYear(newBookDTO.year());
        book.setTotalCopies(newBookDTO.totalCopies());
        book.setAvailableCopies(newBookDTO.totalCopies());
        book.setId(String.valueOf(bookIdSequenceService.generateSequence(Book.class.getSimpleName())));
        book.setReservedCopies(0);

        bookRepository.save(book);

        return ResponseEntity.status(HttpStatus.CREATED).body("Livro " + book.getTitle() +" cadastrado");
    }

    @GetMapping(path = "/")
    @Operation(
            summary = "Lista todos os livros do acervo",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Retorna array list dos livros no acervo",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = BookDTO.class)
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        List<BookDTO> books = new ArrayList<>();
        for (Book book : bookRepository.findAll()) {
            BookDTO bookDTO = new BookDTO(
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getYear(),
                    book.getIsbn(),
                    book.getTotalCopies(),
                    book.getAvailableCopies(),
                    book.getReservedCopies()
            );
            books.add(bookDTO);
        }
        return ResponseEntity.ok(books);
    }

    @GetMapping(path = "/title/{title}")
    @Operation(
            summary = "Retorna resultados da busca de livros pelo título.",
            parameters = {
                    @Parameter(
                            name = "title",
                            description = "título a ser buscado",
                            in = ParameterIn.PATH,
                            required = true,
                            schema = @Schema(type = "String")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Array list dos livros encontrados",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = BookDTO.class)
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Nenhum livro encontrado"
                    )
            }
    )
    public ResponseEntity<?> getBooksByTitle(@PathVariable(name="title") String title) {
        List<Book> foundBooks = bookRepository.findByTitle(".*" + title + ".*", "i");
        if (foundBooks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Nenhum livro encontrado com o título: " + title);
        }
        List<BookDTO> bookDTOs = foundBooks.stream()
                .map(book -> new BookDTO(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getYear(),
                        book.getIsbn(),
                        book.getTotalCopies(),
                        book.getAvailableCopies(),
                        book.getReservedCopies()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookDTOs);
    }

    @GetMapping(path = "/author/{author}")
    @Operation(
            summary = "Retorna resultados da busca de livros pelo autor.",
            parameters = {
                    @Parameter(
                            name = "author",
                            description = "autor a ser buscado",
                            in = ParameterIn.PATH,
                            required = true,
                            schema = @Schema(type = "String")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Array list dos livros encontrados",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(
                                            schema = @Schema(implementation = BookDTO.class)
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Nenhum livro encontrado"
                    )
            }
    )
    public ResponseEntity<?> getBooksByAuthor(@PathVariable(name="author") String author) {
        List<Book> foundBooks = bookRepository.findByAuthor(".*" + author + ".*", "i");
        if (foundBooks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Nenhum livro encontrado com o autor: " + author);
        }
        List<BookDTO> bookDTOs = foundBooks.stream()
                .map(book -> new BookDTO(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getYear(),
                        book.getIsbn(),
                        book.getTotalCopies(),
                        book.getAvailableCopies(),
                        book.getReservedCopies()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookDTOs);
    }

    @GetMapping(path="/isbn/{isbn}")
    @Operation(
            summary = "Retorna resultados da busca de livros pelo isbn.",
            parameters = {
                    @Parameter(
                            name = "isbn",
                            description = "isbn a ser buscado",
                            in = ParameterIn.PATH,
                            required = true,
                            schema = @Schema(type = "String")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Descrição do livro encontrado",
                            content = @Content(
                                    schema = @Schema(implementation = BookDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Nenhum livro encontrado"
                    )
            }
    )
    public ResponseEntity<?> getBooksByIsbn(@PathVariable(name="isbn") String isbn) {
        Optional<Book> foundBook = bookRepository.findByIsbn(isbn);
        if (foundBook.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum livro encontrado com o ISBN: "  + isbn);
        Book book = foundBook.get();
        BookDTO bookDTO = new BookDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getYear(),
                book.getIsbn(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getReservedCopies()
        );
        return ResponseEntity.ok(bookDTO);
    }

    @GetMapping(path="/id/{id}")
    @Operation(
            summary = "Retorna resultados da busca de livros pelo id.",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "id a ser buscado",
                            in = ParameterIn.PATH,
                            required = true,
                            schema = @Schema(type = "String")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Descrição do livro encontrado",
                            content = @Content(
                                    schema = @Schema(implementation = BookDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Nenhum livro encontrado"
                    )
            }
    )
    public ResponseEntity<BookDTO> getBooksById(@PathVariable(name="id") String id) {
        Optional<Book> foundBook = bookRepository.findById(id);
        if (foundBook.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        Book book = foundBook.get();
        BookDTO bookDTO = new BookDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getYear(),
                book.getIsbn(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getReservedCopies()
        );
        return ResponseEntity.ok(bookDTO);
    }

    @PatchMapping(path="/updateCopies/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @Operation(
            summary = "Atualiza número de cópias totais de um livro (acesso exclusivo do ADMIN).",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "id do livro a ser atualizado",
                            in = ParameterIn.PATH,
                            required = true,
                            schema = @Schema(type = "String")
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novo número de cópias totais",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UpdateTotalCopiesDTO.class)
                    )
            ),
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Dados do livro atualizado",
                            content = @Content(
                                schema = @Schema(implementation = BookDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Livro não encontrado"
                    )
            }
    )
    public ResponseEntity<?> updateCopies(@PathVariable("id") String id, @RequestBody UpdateTotalCopiesDTO updateTotalCopiesDTO) {
        Optional<Book> foundBook = bookRepository.findById(id);
        if (foundBook.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Livro não encontrado");
        Book book = foundBook.get();
        int newTotalCopiesValue = updateTotalCopiesDTO.newTotalCopiesValue();
        updateTotalCopies(book, newTotalCopiesValue);
        return ResponseEntity.ok(new BookDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getYear(),
                book.getIsbn(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getReservedCopies()
        ));
    }

    @PostMapping(path = "/updateCopies/loan/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @Operation(
            summary = "Diminui em 1 o número de cópias disponíveis de um livro (realização de empréstimo)(apenas ADMIN).",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "id do livro a ser alterado",
                            in = ParameterIn.PATH,
                            required = true,
                            schema = @Schema(type = "String")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Descrição do livro atualizado",
                            content = @Content(
                                    schema = @Schema(implementation = BookDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Livro não encontrado"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Erro ao realizar empréstimo"
                    )
            }
    )
    public ResponseEntity<?> updateAvailableCopiesLoan(@PathVariable("id") String id) {
        Optional<Book> foundBook = bookRepository.findById(id);
        if (foundBook.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro: Livro não encontrado");
        }

        Book book = foundBook.get();
        try {
            updateAvailableCopies(book, true); // Tenta realizar o empréstimo
            return ResponseEntity.ok(new BookDTO(
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getYear(),
                    book.getIsbn(),
                    book.getTotalCopies(),
                    book.getAvailableCopies(),
                    book.getReservedCopies()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping(path = "/updateCopies/return/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @Operation(
            summary = "Aumenta em 1 o número de cópias disponíveis de um livro (realização de devolução)(Apenas ADMIN).",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "id do livro a ser alterado",
                            in = ParameterIn.PATH,
                            required = true,
                            schema = @Schema(type = "String")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Descrição do livro atualizado",
                            content = @Content(
                                    schema = @Schema(implementation = BookDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Livro não encontrado"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Erro ao realizar devolução"
                    )
            }
    )
    public ResponseEntity<?> updateAvailableCopiesReturn(@PathVariable("id") String id) {
        Optional<Book> foundBook = bookRepository.findById(id);
        if (foundBook.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro: Livro não encontrado");
        }

        Book book = foundBook.get();
        try {
            updateAvailableCopies(book, false); // Tenta realizar a devolução
            return ResponseEntity.ok(new BookDTO(
                    book.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getYear(),
                    book.getIsbn(),
                    book.getTotalCopies(),
                    book.getAvailableCopies(),
                    book.getReservedCopies()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @Operation(
            summary = "Deleta um livro pelo id (apenas ADMIN).",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "id do livro a ser deletado",
                            in = ParameterIn.PATH,
                            required = true,
                            schema = @Schema(type = "String")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Livro deletado com sucesso"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Nenhum livro encontrado"
                    )
            }
    )
    public ResponseEntity<String> deleteBook(@PathVariable(name = "id") String id) {
        Optional<Book> optBook = bookRepository.findById(id);
        if (optBook.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Livro não encontrado");
        }
        bookRepository.deleteById(id);
        return ResponseEntity.ok("Livro deletado com sucesso");
    }

    @PutMapping(path="/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    @Operation(
            summary = "Atualiza um livro no acervo pelo seu id (acesso exclusivo do ADMIN).",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "id do livro a ser atualizado",
                            in = ParameterIn.PATH,
                            required = true,
                            schema = @Schema(type = "String")
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados do Livro",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = NewBookDTO.class)
                    )
            ),
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Dados atualizados do livro",
                            content = @Content(
                                    schema = @Schema(implementation = BookDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Livro não encontrado"
                    )
            }
    )
    public ResponseEntity<?> updateBook(@PathVariable("id") String id, @Valid @RequestBody NewBookDTO bookDTO) {
        Optional<Book> optBook = bookRepository.findById(id);
        if (optBook.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Livro não encontrado");
        Book book = optBook.get();
        book.setTitle(bookDTO.title());
        book.setAuthor(bookDTO.author());
        book.setYear(bookDTO.year());
        book.setIsbn(bookDTO.isbn());
        updateTotalCopies(book, bookDTO.totalCopies());
        bookRepository.save(book);
        return ResponseEntity.ok(new BookDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getYear(),
                book.getIsbn(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getReservedCopies()
        ));
    }

    @PostMapping(path="/updateReserved/{id}/{bool}")
    @PreAuthorize("hasAuthority('SCOPE_BASIC')")
    @Operation(
            summary = "Atualiza o número de cópias reservadas de um livro (reserva) (acesso exclusivo BASIC).",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "id do livro a ser atualizado",
                            in = ParameterIn.PATH,
                            required = true,
                            schema = @Schema(type = "String")
                    ),
                    @Parameter(
                            name = "bool",
                            description = "boolean indicando se cópias devem ser aumentadas (true) ou diminuidas (false) em 1",
                            in = ParameterIn.PATH,
                            required = true,
                            schema = @Schema(type = "Boolean")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Cópias reservadas atualizadas"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Livro não encontrado"
                    )
            }
    )
    public ResponseEntity<?> updateReservedBook(@PathVariable("id") String id, @PathVariable("bool") boolean bool) {
        Optional<Book> optBook = bookRepository.findById(id);
        if(optBook.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Livro não encontrado");
        }
        Book book = optBook.get();
        if (bool) {
            book.setReservedCopies(book.getReservedCopies() + 1);
        }
        else {
            book.setReservedCopies(book.getReservedCopies() - 1);
        }
        bookRepository.save(book);
        return ResponseEntity.ok("Cópias reservadas atualizadas");
    }


}
