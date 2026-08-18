package com.openclassrooms.datashare.mapper;

import com.openclassrooms.datashare.dto.RegisterRequestDTO;
import com.openclassrooms.datashare.dto.RegisterResponseDTO;
import com.openclassrooms.datashare.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {

    public User toEntity(RegisterRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setEmail(dto.getEmail());

        return user;
    }

    public RegisterResponseDTO toResponseDto(User user) {
        if (user == null) {
            return null;
        }

        return new RegisterResponseDTO(user.getId(), user.getEmail());
    }
}