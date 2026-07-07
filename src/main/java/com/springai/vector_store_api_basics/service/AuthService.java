package com.springai.vector_store_api_basics.service;

import com.springai.vector_store_api_basics.model.User;
import com.springai.vector_store_api_basics.repository.UserRepository;
import com.springai.vector_store_api_basics.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Map<String, String> register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException(
                "Username already exists");
        }
        User user = new User(username,
            passwordEncoder.encode(password));
        userRepository.save(user);
        return Map.of("message",
            "User registered successfully!");
    }

    public Map<String, String> login(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() ->
                new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Wrong password");
        }

        String token = jwtUtil.generateToken(username);
        return Map.of(
            "token", token,
            "username", username,
            "message", "Login successful!"
        );
    }
}