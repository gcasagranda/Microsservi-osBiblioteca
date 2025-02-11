package br.edu.utfpr.api_usuario.controllers;

import br.edu.utfpr.api_usuario.dtos.NewUserDTO;
import br.edu.utfpr.api_usuario.dtos.UserDTO;
import br.edu.utfpr.api_usuario.models.Role;
import br.edu.utfpr.api_usuario.models.User;
import br.edu.utfpr.api_usuario.repositories.RoleRepository;
import br.edu.utfpr.api_usuario.repositories.UserRepository;
import br.edu.utfpr.api_usuario.services.UserIdSequenceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

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

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private RoleRepository roleRepository;

    @PostMapping
    public ResponseEntity<String> createUser(@Valid @RequestBody NewUserDTO newUserDTO) {
        Optional<User> existingUser = userRepository.findByUsername(newUserDTO.username());
        Optional<Role> roleOptional = roleRepository.findByName("BASIC"); // Use a better variable name

        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username já cadastrado");
        }

        Role role = roleOptional.get();  // Now get the Role object after checking isPresent()

        User user = new User();
        user.setUsername(newUserDTO.username());
        user.setPassword(this.bCryptPasswordEncoder.encode(newUserDTO.password()));
        user.setFullName(newUserDTO.fullName());
        user.setId(String.valueOf(userIdSequenceService.generateSequence(User.class.getSimpleName())));

        List<Role> roles = new ArrayList<>(); // Create a new ArrayList
        roles.add(role); // Add the role to the list


        user.setRoles(roles); // Set the list of roles

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário #" + user.getId() + " cadastrado com sucesso");
    }


    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            UserDTO userDTO = new UserDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getFullName()
            );
            users.add(userDTO);
        }
        return ResponseEntity.ok(users);
    }


    @GetMapping(path="/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<?> getUserbyId(@PathVariable(name= "id") String id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            UserDTO userDTO = new UserDTO(
                    id,
                    user.get().getUsername(),
                    user.get().getFullName()
            );
            return ResponseEntity.ok(userDTO);
        }
        else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
        }
    }

    @GetMapping(path="/profile")
    @PreAuthorize("hasAuthority('SCOPE_BASIC')")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String id = jwt.getClaimAsString("sub");
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            UserDTO userDTO = new UserDTO(
                    id,
                    user.get().getUsername(),
                    user.get().getFullName()
            );
            return ResponseEntity.ok(userDTO);
        }
        else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
        }
    }


    @PutMapping(path = "/updateProfile")
    @PreAuthorize("hasAuthority('SCOPE_BASIC')")
    public ResponseEntity<?> updateUser(Authentication authentication, @Valid @RequestBody NewUserDTO updatedUserDTO) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String id = jwt.getClaimAsString("sub");
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
                user.getFullName()
        ));
    }

    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable(name = "id") String id) {
        Optional<User> optUser = userRepository.findById(id);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("Usuário deletado com sucesso");
    }

}
