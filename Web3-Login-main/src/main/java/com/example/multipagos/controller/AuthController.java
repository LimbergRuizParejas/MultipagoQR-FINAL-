package com.example.multipagos.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.example.multipagos.dto.RegisterDto;
import com.example.multipagos.dto.LoginDto;
import com.example.multipagos.dto.AuthResponse;
import com.example.multipagos.service.AuthService;

import com.example.multipagos.model.Role;
import com.example.multipagos.model.User;
import com.example.multipagos.model.UserCompany;

import com.example.multipagos.repository.UserRepository;
import com.example.multipagos.repository.UserCompanyRepository;
import com.example.multipagos.util.JwtUtil;

import java.util.*;

@RestController
@RequestMapping("/api/public/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCompanyRepository userCompanyRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /* ============================================================
       ✅ REGISTER
    ============================================================ */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto dto) {
        try {
            authService.register(dto);
            return ResponseEntity.ok(Map.of("message", "User registered successfully"));

        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /* ============================================================
       ✅ LOGIN
    ============================================================ */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto dto) {
        try {
            AuthResponse auth = authService.login(dto);
            return ResponseEntity.ok(auth);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(401).body(Map.of("error", ex.getMessage()));
        }
    }

    /* ============================================================
       🟩 NUEVO — /me/context
       GET /api/public/auth/me/context
       Necesario para el Gateway y para validar el JWT.
    ============================================================ */
    @GetMapping("/me/context")
    public ResponseEntity<?> context(@RequestHeader(value = "Authorization", required = false) String header) {

        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing or invalid token"));
        }

        try {
            String token = header.substring(7);
            var claims = jwtUtil.getClaims(token);

            Map<String, Object> map = new HashMap<>();
            map.put("username", claims.getSubject());
            map.put("companyIds", claims.get("companyIds", List.class));
            map.put("roles", claims.get("roles", List.class));

            return ResponseEntity.ok(map);

        } catch (Exception ex) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
    }

    /* ============================================================
       ⚠ CAMBIO DE ROL
    ============================================================ */
    @PostMapping("/change-role")
    public ResponseEntity<?> changeUserRole(
            @RequestParam String username,
            @RequestParam Long companyId,
            @RequestParam String role) {

        try {
            Optional<User> optUser = userRepository.findByUsername(username);
            if (optUser.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            User user = optUser.get();

            Optional<UserCompany> optUserCompany =
                    userCompanyRepository.findByUserIdAndCompanyId(user.getId(), companyId);

            if (optUserCompany.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not linked to company"));
            }
            UserCompany userCompany = optUserCompany.get();

            Role newRole;
            try {
                newRole = Role.valueOf(role.toUpperCase());
            } catch (Exception ex) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid role: " + role));
            }

            userCompany.setRole(newRole);
            userCompanyRepository.save(userCompany);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Role updated successfully",
                            "username", username,
                            "companyId", companyId,
                            "newRole", newRole.name()
                    )
            );

        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
