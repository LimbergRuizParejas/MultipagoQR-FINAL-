package com.example.multipagos.service;

import com.example.multipagos.dto.LoginDto;
import com.example.multipagos.dto.RegisterDto;
import com.example.multipagos.dto.AuthResponse;
import com.example.multipagos.model.User;
import com.example.multipagos.model.Company;
import com.example.multipagos.model.UserCompany;
import com.example.multipagos.model.Role;

import com.example.multipagos.repository.UserRepository;
import com.example.multipagos.repository.CompanyRepository;
import com.example.multipagos.repository.UserCompanyRepository;

import com.example.multipagos.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserCompanyRepository userCompanyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;


    /* ============================================================
       REGISTRO GENERAL
       Se puede registrar usuario y asignarlo a una empresa
    ============================================================ */
    public AuthResponse register(RegisterDto dto) {

        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya existe.");
        }

        User newUser = new User();
        newUser.setUsername(dto.getUsername());
        newUser.setEmail(dto.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        userRepository.save(newUser);

        // Si se asigna empresa → registrar multi-tenant
        if (dto.getCompanyId() != null) {
            Company company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Empresa no encontrada."));

            Role role = dto.getRole() != null ? dto.getRole() : Role.USER;

            UserCompany relation = new UserCompany(newUser, company, role);
            userCompanyRepository.save(relation);
        }

        return buildAuthResponse(newUser);
    }


    /* ============================================================
       LOGIN
    ============================================================ */
    public AuthResponse login(LoginDto dto) {

        User user = userRepository
                .findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        return buildAuthResponse(user);
    }


    /* ============================================================
       ASIGNAR ROL (ADMIN FUNCTION)
    ============================================================ */
    public void assignRole(Long userId, Long companyId, Role role) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        UserCompany existing = userCompanyRepository
                .findByUserIdAndCompanyId(userId, companyId)
                .orElse(null);

        if (existing != null) {
            existing.setRole(role);
            userCompanyRepository.save(existing);
        } else {
            UserCompany relation = new UserCompany(user, company, role);
            userCompanyRepository.save(relation);
        }
    }


    /* ============================================================
       Construye respuesta con JWT, roles y companyIds
    ============================================================ */
    private AuthResponse buildAuthResponse(User user) {

        List<UserCompany> userCompanies = userCompanyRepository.findByUserId(user.getId());

        List<Long> companyIds = userCompanies
                .stream()
                .map(uc -> uc.getCompany().getId())
                .collect(Collectors.toList());

        List<String> roles = userCompanies
                .stream()
                .map(uc -> uc.getRole().name())
                .distinct()
                .collect(Collectors.toList());

        String token = jwtUtil.generateToken(
                user.getUsername(),
                companyIds,
                roles
        );

        return new AuthResponse(
                token,
                user.getUsername(),
                companyIds,
                roles
        );
    }

}
