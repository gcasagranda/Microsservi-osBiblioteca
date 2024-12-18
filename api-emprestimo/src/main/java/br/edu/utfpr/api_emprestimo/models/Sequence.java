package br.edu.utfpr.api_emprestimo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("UserIdSequence")
public class Sequence {

    @Id
    private String id;
    private long seq;

    public long getSeq() {
        return seq;
    }
}
