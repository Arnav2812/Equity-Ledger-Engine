package com.equityledger.security.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.equityledger.security.domain.AppUser;
import com.equityledger.security.jwt.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.get("username"), request.get("password"))
        );
        
        AppUser user = (AppUser) userDetailsService.loadUserByUsername(request.get("username"));
        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        return ResponseEntity.ok(Map.of("accessToken", accessToken, "refreshToken", refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (jwtService.isTokenValid(refreshToken)) {
            String username = jwtService.extractUsername(refreshToken);
            AppUser user = (AppUser) userDetailsService.loadUserByUsername(username);
            String newAccessToken = jwtService.generateAccessToken(user.getUsername(), user.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired refresh token"));
    }
}