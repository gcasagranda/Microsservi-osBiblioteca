package br.edu.utfpr.api_usuario.models;

import br.edu.utfpr.api_usuario.enums.RoleType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("User")
public class User {

    @Id
    private String id;


    @Indexed(unique = true, name = "unique_username_index")
    private String username;

    private String password;

    private String fullName;


    @DBRef
    private List<Role> roles;

    // Construtores
    public User() {}

    public User(String id, String username, String password, String fullName, List<Role> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.roles = roles; // Initialize roles
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


    public List<Role> getRoles() { // Corrected to return List<Role>
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<RoleType> getRoleTypes() { // Returns a List of RoleType
        if (roles != null && !roles.isEmpty()) {
            return roles.stream()
                    .map(role -> {
                        try {
                            return RoleType.valueOf(role.getName());
                        } catch (IllegalArgumentException e) {
                            return null; // Or handle the invalid role name appropriately
                        }
                    })
                    .filter(roleType -> roleType != null) // Filter out null values
                    .toList();
        }
        return List.of(); // Return empty list
    }
}
