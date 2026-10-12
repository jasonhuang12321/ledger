package com.learning;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> matches(password, user.getPassword()))
                .orElse(null);
    }

    public void register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new BusinessException("用户名已存在");
        }
        userRepository.save(new AppUser(username, passwordEncoder.encode(password), "USER"));
    }

    private boolean matches(String raw, String stored) {
        if (isBcrypt(stored)) {
            return passwordEncoder.matches(raw, stored);
        }
        return stored != null && stored.equals(raw);
    }

    private boolean isBcrypt(String stored) {
        return stored != null && stored.startsWith("$2");
    }
}
