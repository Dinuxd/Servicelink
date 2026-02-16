package com.servicelink.service;

import com.servicelink.model.User;
import com.servicelink.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SequenceGeneratorService seq;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, SequenceGeneratorService seq) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.seq = seq;
    }

    @Transactional
    public User register(String name, String email, String username, String password) {
        return registerWithRole(name, email, username, password, "ROLE_USER");
    }

    @Transactional
    public User registerWithRole(String name, String email, String username, String password, String roleName) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (username != null && userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already in use");
        }
        User user = new User();
        user.setId(seq.generateSequence("users"));
        user.setName(name);
        user.setEmail(email);
        // Default username to email when not provided so Security principal is stable
        user.setUsername(username != null ? username : email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoleNames(List.of(roleName));
        user.setActive(true);
        return userRepository.save(user);
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    public User getByEmail(String identifier) {
        // Accept either email or username for lookups since auth principal uses username
        return userRepository.findByUsernameOrEmail(identifier, identifier).orElse(null);
    }

    public User getById(@NonNull Long id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User findByUsernameOrEmail(String identifier) {
        return userRepository.findByUsernameOrEmail(identifier, identifier).orElse(null);
    }
}