package br.edu.utfpr.api_usuario.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document("UserIdSequence")
public class UserIdSequence {

    @Id
    private String id;
    private long seq;

    public long getSeq() {
        return seq;
    }
}
