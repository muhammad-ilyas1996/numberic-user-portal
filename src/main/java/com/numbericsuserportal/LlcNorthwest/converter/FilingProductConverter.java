package com.numbericsuserportal.LlcNorthwest.converter;

import com.numbericsuserportal.LlcNorthwest.dto.FilingMethodDTO;
import com.numbericsuserportal.LlcNorthwest.dto.FilingProductDTO;
import com.numbericsuserportal.LlcNorthwest.dto.TimeFrameDTO;
import com.numbericsuserportal.LlcNorthwest.entity.FilingMethodEntity;
import com.numbericsuserportal.LlcNorthwest.entity.FilingProductEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FilingProductConverter {

    public FilingProductEntity toEntity(FilingProductDTO dto) {
        FilingProductEntity entity = new FilingProductEntity();
        entity.setExternalId(dto.getId());
        entity.setName(dto.getName());
        entity.setFilingName(dto.getFilingName());
        entity.setPrice(dto.getPrice());

        if (dto.getFilingMethods() != null && !dto.getFilingMethods().isEmpty()) {
            List<FilingMethodEntity> methods = new ArrayList<>();
            for (FilingMethodDTO methodDTO : dto.getFilingMethods()) {
                FilingMethodEntity methodEntity = toMethodEntity(methodDTO);
                methodEntity.setFilingProduct(entity);
                methods.add(methodEntity);
            }
            entity.setFilingMethods(methods);
        }

        return entity;
    }

    public FilingProductDTO toDTO(FilingProductEntity entity) {
        FilingProductDTO dto = new FilingProductDTO();
        dto.setId(entity.getExternalId());
        dto.setName(entity.getName());
        dto.setFilingName(entity.getFilingName());
        dto.setPrice(entity.getPrice());

        if (entity.getFilingMethods() != null && !entity.getFilingMethods().isEmpty()) {
            List<FilingMethodDTO> methods = new ArrayList<>();
            for (FilingMethodEntity methodEntity : entity.getFilingMethods()) {
                methods.add(toMethodDTO(methodEntity));
            }
            dto.setFilingMethods(methods);
        }

        return dto;
    }

    public void updateEntity(FilingProductEntity entity, FilingProductDTO dto) {
        entity.setName(dto.getName());
        entity.setFilingName(dto.getFilingName());
        entity.setPrice(dto.getPrice());
    }

    private FilingMethodEntity toMethodEntity(FilingMethodDTO dto) {
        FilingMethodEntity entity = new FilingMethodEntity();
        entity.setExternalId(dto.getId());
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setCost(dto.getCost());
        
        if (dto.getFiledIn() != null) {
            entity.setFiledInDays(dto.getFiledIn().getDays());
            entity.setFiledInHours(dto.getFiledIn().getHours());
        }
        
        if (dto.getDocsIn() != null) {
            entity.setDocsInDays(dto.getDocsIn().getDays());
            entity.setDocsInHours(dto.getDocsIn().getHours());
        }
        
        entity.setAgencyName(dto.getAgencyName());
        entity.setFilingDescription(dto.getFilingDescription());
        entity.setJurisdiction(dto.getJurisdiction());
        
        return entity;
    }

    private FilingMethodDTO toMethodDTO(FilingMethodEntity entity) {
        FilingMethodDTO dto = new FilingMethodDTO();
        dto.setId(entity.getExternalId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setCost(entity.getCost());
        
        TimeFrameDTO filedIn = new TimeFrameDTO();
        filedIn.setDays(entity.getFiledInDays());
        filedIn.setHours(entity.getFiledInHours());
        dto.setFiledIn(filedIn);
        
        TimeFrameDTO docsIn = new TimeFrameDTO();
        docsIn.setDays(entity.getDocsInDays());
        docsIn.setHours(entity.getDocsInHours());
        dto.setDocsIn(docsIn);
        
        dto.setAgencyName(entity.getAgencyName());
        dto.setFilingDescription(entity.getFilingDescription());
        dto.setJurisdiction(entity.getJurisdiction());
        
        return dto;
    }
}

