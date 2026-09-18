package com.equityledger.security.config;

import com.equityledger.security.domain.AppUser;
import com.equityledger.security.domain.Role;
import com.equityledger.security.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.save(new AppUser("admin", passwordEncoder.encode("admin123"), Role.ADMIN));
            userRepository.save(new AppUser("operator", passwordEncoder.encode("op123"), Role.OPERATOR));
            userRepository.save(new AppUser("viewer", passwordEncoder.encode("view123"), Role.VIEWER));
        }
    }
}