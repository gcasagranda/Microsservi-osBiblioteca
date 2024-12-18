package br.edu.utfpr.api_biblioteca.repositories;

import br.edu.utfpr.api_biblioteca.models.Book;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends MongoRepository<Book, String> {

    Optional<Book> findByIsbn(String isbn);

    @Query("{ 'title': { $regex: ?0, $options: ?1 } }")
    List<Book> findByTitle(String regex, String options);

    @Query("{ 'author': { $regex: ?0, $options: ?1 } }")
    List<Book> findByAuthor(String regex, String options);

}
