package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.dto.LoginRequestDTO;
import com.openclassrooms.datashare.dto.RegisterRequestDTO;
import com.openclassrooms.datashare.repository.UserRepository;
import com.openclassrooms.datashare.support.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends AbstractIntegrationTest {

    private static final String REGISTER_URL = "/api/auth/register";
    private static final String LOGIN_URL = "/api/auth/login";
    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "password123";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    private void registerUser() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail(EMAIL);
        dto.setPassword(PASSWORD);

        mockMvc.perform(post(REGISTER_URL)
                .content(objectMapper.writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void register_missingFields_returns400() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO();

        mockMvc.perform(post(REGISTER_URL)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail("not-an-email");
        dto.setPassword(PASSWORD);

        mockMvc.perform(post(REGISTER_URL)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordTooShort_returns400() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail(EMAIL);
        dto.setPassword("short");

        mockMvc.perform(post(REGISTER_URL)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        registerUser();

        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail(EMAIL);
        dto.setPassword(PASSWORD);

        mockMvc.perform(post(REGISTER_URL)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void register_success_returns201WithIdAndEmail() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail(EMAIL);
        dto.setPassword(PASSWORD);

        mockMvc.perform(post(REGISTER_URL)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void login_success_returns200WithToken() throws Exception {
        registerUser();

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail(EMAIL);
        dto.setPassword(PASSWORD);

        mockMvc.perform(post(LOGIN_URL)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        registerUser();

        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail(EMAIL);
        dto.setPassword("wrong-password");

        mockMvc.perform(post(LOGIN_URL)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_unknownEmail_returns401() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("unknown@example.com");
        dto.setPassword(PASSWORD);

        mockMvc.perform(post(LOGIN_URL)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_missingFields_returns400() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();

        mockMvc.perform(post(LOGIN_URL)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}