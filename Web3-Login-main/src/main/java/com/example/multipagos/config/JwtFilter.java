package com.example.multipagos.config;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.multipagos.util.JwtUtil;
import com.example.multipagos.repository.UserRepository;
import com.example.multipagos.model.User;
import com.example.multipagos.model.UserCompany;

import io.jsonwebtoken.Claims;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepo;

    /* ============================================================
       🔥 IGNORAR RUTAS PÚBLICAS
    ============================================================ */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.startsWith("/api/public/");
    }

    /* ============================================================
       🔐 FILTRAR SOLO RUTAS PRIVADAS (JWT requerido)
    ============================================================ */
    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");

        // Si NO hay token → continuar pero sin usuario autenticado
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        String token = header.substring(7);

        try {
            if (!jwtUtil.validate(token)) {
                chain.doFilter(req, res);
                return;
            }

            Claims claims = jwtUtil.getClaims(token);
            String username = claims.getSubject();

            if (username == null) {
                chain.doFilter(req, res);
                return;
            }

            // Cargar usuario real desde la BD
            User user = userRepo.findByUsername(username).orElse(null);
            if (user == null) {
                chain.doFilter(req, res);
                return;
            }

            /* ============================================================
               🟦 ROLES desde claims → sino desde BD
            ============================================================ */
            List<String> roles = claims.get("roles", List.class);

            if (roles == null) {
                roles = user.getCompanies().stream()
                        .map(uc -> uc.getRole().name())
                        .distinct()
                        .collect(Collectors.toList());
            }

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toList());

            /* ============================================================
               🟩 companyIds desde claims → sino desde BD
            ============================================================ */
            List<Integer> companyIdsInt = claims.get("companyIds", List.class);
            List<Long> companyIds = new ArrayList<>();

            if (companyIdsInt != null) {
                companyIds = companyIdsInt.stream()
                        .map(Integer::longValue)
                        .collect(Collectors.toList());
            } else {
                companyIds = user.getCompanies().stream()
                        .map(uc -> uc.getCompany().getId())
                        .collect(Collectors.toList());
            }

            /* ============================================================
               🟨 Setear autenticación en Spring Security
            ============================================================ */
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            authorities
                    );

            // adjuntar datos adicionales
            Map<String, Object> details = new HashMap<>();
            details.put("companyIds", companyIds);
            auth.setDetails(details);

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception ex) {
            // token inválido → ignorar, no romper nada
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(req, res);
    }
}
