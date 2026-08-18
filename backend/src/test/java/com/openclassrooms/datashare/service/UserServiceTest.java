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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private static final String TOKEN = "jwt-token";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    @Test
    void register_withNullUser_throwsIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.register(null, RAW_PASSWORD));
    }

    @Test
    void register_withExistingEmail_throwsEmailAlreadyUsedException() {
        User user = new User();
        user.setEmail(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        Assertions.assertThrows(EmailAlreadyUsedException.class, () -> userService.register(user, RAW_PASSWORD));
    }

    @Test
    void register_success_encodesPasswordAndSaves() {
        User user = new User();
        user.setEmail(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.register(user, RAW_PASSWORD);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo(ENCODED_PASSWORD);
        assertThat(saved.getEmail()).isEqualTo(EMAIL);
    }

    @Test
    void login_withBlankEmail_throwsIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.login(" ", RAW_PASSWORD));
    }

    @Test
    void login_withBlankPassword_throwsIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.login(EMAIL, " "));
    }

    @Test
    void login_success_returnsToken() {
        User user = new User();
        user.setEmail(EMAIL);
        user.setPasswordHash(ENCODED_PASSWORD);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn(TOKEN);

        String token = userService.login(EMAIL, RAW_PASSWORD);

        assertThat(token).isEqualTo(TOKEN);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_withBadCredentials_propagatesException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        Assertions.assertThrows(BadCredentialsException.class, () -> userService.login(EMAIL, RAW_PASSWORD));
    }
}