
package com.openclassrooms.datashare.service;

import com.openclassrooms.datashare.dto.StoredFileDTO;
import com.openclassrooms.datashare.entities.StoredFile;
import com.openclassrooms.datashare.handler.StoredFileNotFoundException;
import com.openclassrooms.datashare.mapper.StoredFileDtoMapper;
import com.openclassrooms.datashare.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class StoredFileService  {
    private final StoredFileRepository storedFileRepository;
    private final StoredFileDtoMapper storedFileDtoMapper;
    public StoredFileDTO uploadStoredFile(StoredFileDTO storedFileDTO){
        throw new UnsupportedOperationException("US01 upload not implemented yet");
    }

    public List<StoredFileDTO> listStoredFiles() {
        return storedFileDtoMapper.toDtoList(storedFileRepository.findAll());
    }



    public StoredFileDTO getStoredFileByToken(long id) {
        StoredFile storedFile = storedFileRepository.findById(id)
                .orElseThrow(() ->
                        new StoredFileNotFoundException(id));
        return storedFileDtoMapper.toDto(storedFile);
    }


    public void deleteStoredFile(long id) {
        StoredFile storedFile = storedFileRepository.findById(id)
                .orElseThrow(() ->
                        new StoredFileNotFoundException(id));

        storedFileRepository.delete(storedFile);
    }

}
