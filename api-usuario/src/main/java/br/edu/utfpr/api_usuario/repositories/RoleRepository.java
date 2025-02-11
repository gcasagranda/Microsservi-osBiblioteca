package br.edu.utfpr.api_usuario.repositories;

import br.edu.utfpr.api_usuario.models.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoleRepository extends MongoRepository<Role, String> {

    Optional<Role> findByName(String name);
}
