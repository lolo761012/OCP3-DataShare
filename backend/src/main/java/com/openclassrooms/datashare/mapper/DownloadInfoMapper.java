package com.openclassrooms.datashare.mapper;

import com.openclassrooms.datashare.dto.DownloadInfoDTO;
import com.openclassrooms.datashare.entities.StoredFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DownloadInfoMapper {
    @Mapping(
            target = "passwordProtected",
            expression = "java(storedFile.getPasswordHash() != null)"
    )
    DownloadInfoDTO toDto(StoredFile storedFile);
}
