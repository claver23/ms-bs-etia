package com.etlions.ms_bs_etia.auth.openapi;

import com.etlions.ms_bs_etia.auth.persistence.UserEntity;
import com.etlions.ms_bs_etia.auth.persistence.UserRepository;
import com.etlions.ms_bs_etia.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public record LoginRequest(String email, String password) {}
    public record RegisterRequest(String email, String password, String nombre) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        var user = userRepository.findByEmail(req.email())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of(
                    "isSuccess", false,
                    "message", "Credenciales invalidas"
            ));
        }

        if (!Boolean.TRUE.equals(user.getActivo())) {
            return ResponseEntity.status(403).body(Map.of(
                    "isSuccess", false,
                    "message", "Usuario desactivado"
            ));
        }

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(Map.of(
                "isSuccess", true,
                "data", Map.of(
                        "token", token,
                        "companyId", user.getCompanyId() != null ? user.getCompanyId() : "",
                        "nombre", user.getNombre() != null ? user.getNombre() : "",
                        "email", user.getEmail(),
                        "rol", user.getRol()
                )
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.email() == null || req.email().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("isSuccess", false, "message", "Email es obligatorio"));
        }
        if (req.password() == null || req.password().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("isSuccess", false, "message", "Password debe tener al menos 6 caracteres"));
        }
        if (userRepository.existsByEmail(req.email())) {
            return ResponseEntity.badRequest().body(Map.of("isSuccess", false, "message", "Ya existe un usuario con ese email"));
        }

        UserEntity user = UserEntity.builder()
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .nombre(req.nombre())
                .rol("USER")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(Map.of(
                "isSuccess", true,
                "data", Map.of(
                        "token", token,
                        "userId", user.getId(),
                        "companyId", "",
                        "email", user.getEmail()
                )
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("isSuccess", false, "message", "Token requerido"));
        }
        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of("isSuccess", false, "message", "Token invalido"));
        }
        return ResponseEntity.ok(Map.of(
                "isSuccess", true,
                "data", Map.of(
                        "userId", jwtService.extractUserId(token),
                        "email", jwtService.extractEmail(token),
                        "companyId", jwtService.extractCompanyId(token),
                        "rol", jwtService.extractRol(token)
                )
        ));
    }
}

