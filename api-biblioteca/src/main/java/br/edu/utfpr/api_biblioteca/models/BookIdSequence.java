package br.edu.utfpr.api_biblioteca.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("UserIdSequence")
public class BookIdSequence {

    @Id
    private String id;
    private long seq;

    public long getSeq() {
        return seq;
    }
}
