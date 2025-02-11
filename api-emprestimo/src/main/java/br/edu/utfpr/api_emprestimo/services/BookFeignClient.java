package br.edu.utfpr.api_emprestimo.services;

import br.edu.utfpr.api_emprestimo.dtos.BookDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name="api-biblioteca", url="localhost:8082")
public interface BookFeignClient {

    @GetMapping("/books/id/{id}")
    ResponseEntity<BookDTO> getBooksById(@PathVariable(name="id") String id);

    @PostMapping(path = "/books/updateCopies/loan/{id}")
    ResponseEntity<?> updateAvailableCopiesLoan(@PathVariable("id") String id);

    @PostMapping(path = "/books/updateCopies/return/{id}")
    ResponseEntity<?> updateAvailableCopiesReturn(@PathVariable("id") String id);

    @PostMapping(path = "/books/updateReserved/{id}/{bool}")
    ResponseEntity<?> updateReservedBook(@PathVariable("id") String id, @PathVariable("bool") boolean bool);
}
