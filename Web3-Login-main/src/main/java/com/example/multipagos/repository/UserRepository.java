package com.example.multipagos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.multipagos.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);
}
