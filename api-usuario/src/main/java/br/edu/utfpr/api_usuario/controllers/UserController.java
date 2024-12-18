package br.edu.utfpr.api_usuario.controllers;

import br.edu.utfpr.api_usuario.dtos.DebtDTO;
import br.edu.utfpr.api_usuario.dtos.NewUserDTO;
import br.edu.utfpr.api_usuario.dtos.UserDTO;
import br.edu.utfpr.api_usuario.models.User;
import br.edu.utfpr.api_usuario.repositories.UserRepository;
import br.edu.utfpr.api_usuario.services.UserIdSequenceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserIdSequenceService userIdSequenceService;

    @PostMapping
    public ResponseEntity<String> createUser(@Valid @RequestBody NewUserDTO newUserDTO) {
        Optional<User> existingUser = userRepository.findByUsername(newUserDTO.username());

        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username já cadastrado");
        }

        User user = new User();
        user.setUsername(newUserDTO.username());
        user.setPassword(newUserDTO.password());
        user.setFullName(newUserDTO.fullName());
        user.setId(String.valueOf(userIdSequenceService.generateSequence(User.class.getSimpleName())));

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário #" + user.getId() + " cadastrado com sucesso");
    }


    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            UserDTO userDTO = new UserDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getActiveDebt()
            );
            users.add(userDTO);
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping(path="/{id}")
    public ResponseEntity<?> getUser(@PathVariable(name= "id") String id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            UserDTO userDTO = new UserDTO(
                    id,
                    user.get().getUsername(),
                    user.get().getFullName(),
                    user.get().getActiveDebt()
            );
            return ResponseEntity.ok(userDTO);
        }
        else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
        }
    }

    @PatchMapping(path="{id}/activedebt")
    public ResponseEntity<?> activeDebt(@PathVariable(name = "id") String id, @RequestBody DebtDTO debtDTO) {
        if (debtDTO.debtIncreasedValue()<=0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("O valor aumentado da dívida deve ser maior que 0");
        }
        Optional<User> optUser = userRepository.findById(id);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encrontrado");
        }
        User user = optUser.get();
        double newDebt = user.getActiveDebt() + debtDTO.debtIncreasedValue();
        user.setActiveDebt(newDebt);
        userRepository.save(user);
        return ResponseEntity.ok(new UserDTO(
                id,
                user.getUsername(),
                user.getFullName(),
                user.getActiveDebt()
        ));
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<?> updateUser(@PathVariable(name = "id") String id, @Valid @RequestBody NewUserDTO updatedUserDTO) {
        Optional<User> optUser = userRepository.findById(id);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
        }
        User user = optUser.get();
        Optional<User> existingUser = userRepository.findByUsername(updatedUserDTO.username());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username já cadastrado.");
        }
        user.setFullName(updatedUserDTO.fullName());
        user.setUsername(updatedUserDTO.username());
        user.setPassword(updatedUserDTO.password());
        userRepository.save(user);
        return ResponseEntity.ok(new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getActiveDebt()
        ));
    }

    @GetMapping(path="/{username}/activedebt")
    public ResponseEntity<String> activeDebtByUsername(@PathVariable(name = "username") String username) {
        Optional<User> optUser = userRepository.findByUsername(username);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
        }
        User user = optUser.get();
        return ResponseEntity.status(HttpStatus.FOUND).body("Dívida ativa:" + user.getActiveDebt());
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable(name = "id") String id) {
        Optional<User> optUser = userRepository.findById(id);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("Usuário deletado com sucesso");
    }

}
