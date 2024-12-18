package br.edu.utfpr.api_biblioteca.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("Book")
public class Book {

    @Id
    private String id;
    private String title;
    private String author;
    private String year;
    private String isbn;
    private int totalCopies;
    private int availableCopies;

    public Book() {
    }

    public Book(String id, String title, String author, String year, String isbn, int totalCopies, int availableCopies) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.isbn = isbn;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }
}
