package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.dto.LoginRequestDTO;
import com.openclassrooms.datashare.dto.LoginResponseDTO;
import com.openclassrooms.datashare.dto.RegisterRequestDTO;
import com.openclassrooms.datashare.dto.RegisterResponseDTO;
import com.openclassrooms.datashare.entities.User;
import com.openclassrooms.datashare.mapper.UserDtoMapper;
import com.openclassrooms.datashare.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserDtoMapper userDtoMapper;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO request) {

        User user = userDtoMapper.toEntity(request);

        User savedUser = userService.register(
                user,
                request.getPassword()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userDtoMapper.toResponseDto(savedUser));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {

        String token = userService.login(
                request.getEmail(),
                request.getPassword()
        );

        return ResponseEntity.ok(
                new LoginResponseDTO(token)
        );
    }
}