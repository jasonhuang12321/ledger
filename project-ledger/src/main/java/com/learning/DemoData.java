package com.learning;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DemoData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ensure("xiaoming", "USER");
        ensure("xiaohong", "USER");
        ensure("admin", "ADMIN");
    }

    private void ensure(String username, String role) {
        userRepository.findByUsername(username).ifPresentOrElse(user -> {
            if (user.getRole() == null || user.getRole().isBlank()) {
                user.setRole(role);
                userRepository.save(user);
            }
        }, () -> userRepository.save(new AppUser(username, passwordEncoder.encode("123456"), role)));
    }
}
