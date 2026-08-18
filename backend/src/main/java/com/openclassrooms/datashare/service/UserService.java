package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.exception.EmailAlreadyUsedException;
import com.openclassrooms.datashare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(User user, String rawPassword) {
        Assert.notNull(user, "User must not be null");
        Assert.hasText(rawPassword, "Password must not be blank");

        log.info("Registering new user");

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyUsedException("Email is already used");
        }

        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        return userRepository.save(user);
    }
}