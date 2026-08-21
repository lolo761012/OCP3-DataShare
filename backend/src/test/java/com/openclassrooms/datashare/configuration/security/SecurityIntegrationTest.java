package com.openclassrooms.datashare.configuration.security;

import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.repository.UserRepository;
import com.openclassrooms.datashare.service.JwtService;
import com.openclassrooms.datashare.support.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityIntegrationTest extends AbstractIntegrationTest {

    private static final String FILES_URL = "/api/files";
    private static final String EMAIL = "secured-user@example.com";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    private String validToken() {
        User user = new User();
        user.setEmail(EMAIL);
        user.setPasswordHash(passwordEncoder.encode("password123"));
        userRepository.save(user);
        return jwtService.generateToken(user);
    }

    @Test
    void protectedRoute_withoutToken_returns401() throws Exception {
        mockMvc.perform(get(FILES_URL))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.details").value(FILES_URL));
    }

    @Test
    void protectedRoute_withMalformedToken_returns401() throws Exception {
        mockMvc.perform(get(FILES_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer garbage"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRoute_withExpiredToken_returns401() throws Exception {
        long originalExpirationMs = (long) ReflectionTestUtils.getField(jwtService, "expirationMs");
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        String expiredToken;
        try {
            expiredToken = validToken();
        } finally {
            ReflectionTestUtils.setField(jwtService, "expirationMs", originalExpirationMs);
        }

        mockMvc.perform(get(FILES_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRoute_withValidToken_passesSecurity() throws Exception {
        String token = validToken();

        mockMvc.perform(get(FILES_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void anonymousUpload_withoutToken_passesSecurity() throws Exception {
        mockMvc.perform(post(FILES_URL))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void anonymousUpload_withInvalidToken_returns401NotSilentlyAnonymous() throws Exception {
        mockMvc.perform(post(FILES_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer garbage"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}