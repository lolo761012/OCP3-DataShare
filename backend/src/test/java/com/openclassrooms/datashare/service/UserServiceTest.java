package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.exception.EmailAlreadyUsedException;
import com.openclassrooms.datashare.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL = "user@example.com";
    private static final String RAW_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encoded-password";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void register_withNullUser_throwsIllegalArgumentException() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(null, RAW_PASSWORD)
        );
    }

    @Test
    void register_withExistingEmail_throwsEmailAlreadyUsedException() {
        User user = new User();
        user.setEmail(EMAIL);

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(user));

        Assertions.assertThrows(
                EmailAlreadyUsedException.class,
                () -> userService.register(user, RAW_PASSWORD)
        );
    }

    @Test
    void register_success_encodesPasswordAndSaves() {
        User user = new User();
        user.setEmail(EMAIL);

        when(userRepository.findByEmail(EMAIL))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(RAW_PASSWORD))
                .thenReturn(ENCODED_PASSWORD);

        when(userRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.register(user, RAW_PASSWORD);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        verify(passwordEncoder).encode(RAW_PASSWORD);
        verify(userRepository).save(captor.capture());

        assertThat(captor.getValue().getPasswordHash())
                .isEqualTo(ENCODED_PASSWORD);

        assertThat(saved.getEmail())
                .isEqualTo(EMAIL);
    }
}
