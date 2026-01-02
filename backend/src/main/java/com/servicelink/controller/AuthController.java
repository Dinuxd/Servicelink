package com.servicelink.controller;

import com.servicelink.dto.AuthRequest;
import com.servicelink.dto.AuthResponse;
import com.servicelink.model.User;
import com.servicelink.security.JwtUtil;
import com.servicelink.service.UserService;
import com.servicelink.service.SequenceGeneratorService;
import com.servicelink.security.SecurityUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final SequenceGeneratorService seq;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, JwtUtil jwtUtil, SequenceGeneratorService seq) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.seq = seq;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestParam String name,
                                         @RequestParam String email,
                                         @RequestParam String username,
                                         @RequestParam String password,
                                         @RequestParam(name = "role", required = false) String role) {
        String roleName = "ROLE_USER";
        if ("provider".equalsIgnoreCase(role)) {
            roleName = "ROLE_PROVIDER";
        } else if ("admin".equalsIgnoreCase(role)) {
            roleName = "ROLE_ADMIN";
        }
        User created = userService.registerWithRole(name, email, username, password, roleName);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponse> adminLogin(@Valid @RequestBody AuthRequest request) {
        return loginWithRole("ADMIN", request);
    }

    @PostMapping("/provider/login")
    public ResponseEntity<AuthResponse> providerLogin(@Valid @RequestBody AuthRequest request) {
        return loginWithRole("PROVIDER", request);
    }

    @PostMapping("/customer/login")
    public ResponseEntity<AuthResponse> customerLogin(@Valid @RequestBody AuthRequest request) {
        return loginWithRole("CUSTOMER", request);
    }

    private ResponseEntity<AuthResponse> loginWithRole(String requiredRole, AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
            );
            SecurityUserDetails principal = (SecurityUserDetails) authentication.getPrincipal();
            if (!principal.getRoleNames().stream().anyMatch(r -> r.equalsIgnoreCase(requiredRole) || r.equalsIgnoreCase("ROLE_" + requiredRole))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
            String token = jwtUtil.generateToken(principal);
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}
