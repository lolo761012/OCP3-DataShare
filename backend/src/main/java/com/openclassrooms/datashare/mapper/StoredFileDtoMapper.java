package com.openclassrooms.datashare.mapper;
import com.openclassrooms.datashare.dto.StoredFileDTO;
import com.openclassrooms.datashare.entities.StoredFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface StoredFileDtoMapper {
    @Mapping(target = "status", constant = "VALID")
    @Mapping(
            target = "passwordProtected",
            expression = "java(storedFile.getPasswordHash() != null)"
    )
    StoredFileDTO toDto(StoredFile storedFile);
    List<StoredFileDTO> toDtoList(List<StoredFile> storedFiles);


}
