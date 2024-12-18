package br.edu.utfpr.api_biblioteca.controllers;

import br.edu.utfpr.api_biblioteca.dtos.BookDTO;
import br.edu.utfpr.api_biblioteca.dtos.NewBookDTO;
import br.edu.utfpr.api_biblioteca.dtos.UpdateTotalCopiesDTO;
import br.edu.utfpr.api_biblioteca.models.Book;
import br.edu.utfpr.api_biblioteca.repositories.BookRepository;
import br.edu.utfpr.api_biblioteca.services.BookIdSequenceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/books")
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

    @PostMapping
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

        bookRepository.save(book);

        return ResponseEntity.status(HttpStatus.CREATED).body("Livro " + book.getTitle() +" cadastrado");
    }

    @GetMapping
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
                    book.getAvailableCopies()
            );
            books.add(bookDTO);
        }
        return ResponseEntity.ok(books);
    }

    @GetMapping(path = "/title/{title}")
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
                        book.getAvailableCopies()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookDTOs);
    }

    @GetMapping(path = "/author/{author}")
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
                        book.getAvailableCopies()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookDTOs);
    }

    @GetMapping(path="/isbn/{isbn}")
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
                book.getAvailableCopies()
        );
        return ResponseEntity.ok(bookDTO);
    }

    @PatchMapping(path="/updateCopies/{id}")
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
                book.getAvailableCopies()
        ));
    }

    @PatchMapping(path = "/updateCopies/loan/{id}")
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
                    book.getAvailableCopies()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PatchMapping(path = "/updateCopies/return/{id}")
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
                    book.getAvailableCopies()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable(name = "id") String id) {
        Optional<Book> optBook = bookRepository.findById(id);
        if (optBook.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Livro não encontrado");
        }
        bookRepository.deleteById(id);
        return ResponseEntity.ok("Livro deletado com sucesso");
    }

    @PutMapping(path="/{id}")
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
                book.getAvailableCopies()
        ));
    }

}
