package br.edu.utfpr.api_usuario.models;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("Role")
public class Role {
    @Id
    private String id;
    private String name;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
