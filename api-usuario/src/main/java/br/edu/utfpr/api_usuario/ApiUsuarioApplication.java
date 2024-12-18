package br.edu.utfpr.api_usuario;

import br.edu.utfpr.api_usuario.models.User;
import br.edu.utfpr.api_usuario.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.ArrayList;
import java.util.List;


@SpringBootApplication
@EnableMongoRepositories
public class ApiUsuarioApplication {

	@Autowired
	UserRepository userRepository;

	List<User> userList = new ArrayList<User>();

	public static void main(String[] args) {
		SpringApplication.run(ApiUsuarioApplication.class, args);
	}

}
