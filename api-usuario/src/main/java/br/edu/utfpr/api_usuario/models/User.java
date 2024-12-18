package br.edu.utfpr.api_usuario.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("User")
public class User {

    @Id
    private String id;


    @Indexed(unique = true, name = "unique_username_index")
    private String username;


    private String password;

    private String fullName;

    private double activeDebt;

    // Construtores
    public User() {}

    public User(String id, String username, String password, String fullName, double activeDebt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.activeDebt = activeDebt;
    }

    // Getters e Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public double getActiveDebt() {
        return activeDebt;
    }

    public void setActiveDebt(double activeDebt) {
        this.activeDebt = activeDebt;
    }
}
