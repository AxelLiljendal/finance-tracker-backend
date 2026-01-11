package com.github.axelliljendal.finance_tracker.auth;

import com.github.axelliljendal.finance_tracker.auth.dto.LoginRequest;
import com.github.axelliljendal.finance_tracker.auth.dto.RegisterRequest;
import com.github.axelliljendal.finance_tracker.jwt.JwtUtil;
import com.github.axelliljendal.finance_tracker.user.User;
import com.github.axelliljendal.finance_tracker.user.UserService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            Set<String> roles = request.getRoles() != null && !request.getRoles().isEmpty()
                    ? request.getRoles()
                    : Set.of("USER");

            User user = userService.createUser(
                    request.getEmail(),
                    request.getPassword(),
                    roles
            );

            String token = jwtUtil.generateToken(user.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponse(token, user.getEmail()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            String email = authentication.getName();
            String token = JwtUtil.generateToken(email);

            return ResponseEntity.ok(new AuthResponse(token, email));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        }
    }
}
