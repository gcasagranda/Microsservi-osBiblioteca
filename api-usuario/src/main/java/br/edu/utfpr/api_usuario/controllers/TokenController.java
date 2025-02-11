package br.edu.utfpr.api_usuario.controllers;

import br.edu.utfpr.api_usuario.dtos.LoginRequest;
import br.edu.utfpr.api_usuario.dtos.LoginResponse;
import br.edu.utfpr.api_usuario.models.Role;
import br.edu.utfpr.api_usuario.models.User;
import br.edu.utfpr.api_usuario.repositories.UserRepository;
import br.edu.utfpr.api_usuario.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class TokenController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){

        Optional<User> userOptional = userRepository.findByUsername(loginRequest.username());

        if (userOptional.isEmpty() || !bCryptPasswordEncoder.matches(loginRequest.password(), userOptional.get().getPassword())) {
            throw new BadCredentialsException("user or password invalid");
        }

        User user = userOptional.get();

        var now = Instant.now();
        var expiresIn = 3000L;

        List<Role> roles = new ArrayList<>();
        if (user.getRoles() != null) { // Check if roles is not null
            for (Role roleRef : user.getRoles()) {
                Role role = roleRepository.findById(roleRef.getId()).orElse(null); // Fetch the Role object
                if (role != null) {
                    roles.add(role);
                }
            }
        }
        System.out.println("roles" + roles);
        String scopes = roles.stream()
                .filter(Objects::nonNull) // Filter out any null Role objects (if possible)
                .map(Role::getName)
                .collect(Collectors.joining(" "));

        System.out.println(scopes);
        var claims = JwtClaimsSet.builder()
                .issuer("br.edu.utfpr.security")
                .subject(user.getId())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .claim("scope", scopes)
                .build();


        System.out.println(claims.toString());
        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        System.out.println("Generated JWT: " + jwtValue);

        LoginResponse response = new LoginResponse(jwtValue, expiresIn);

        return ResponseEntity.ok(response);
    }

}
