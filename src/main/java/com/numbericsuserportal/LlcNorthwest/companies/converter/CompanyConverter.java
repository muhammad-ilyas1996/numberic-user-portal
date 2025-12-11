package com.numbericsuserportal.LlcNorthwest.companies.converter;

import com.numbericsuserportal.LlcNorthwest.companies.dto.CompanyDTO;
import com.numbericsuserportal.LlcNorthwest.companies.entity.CompanyEntity;
import com.numbericsuserportal.LlcNorthwest.companies.entity.CompanyJurisdictionEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CompanyConverter {

    public CompanyEntity toEntity(CompanyDTO dto) {
        CompanyEntity entity = new CompanyEntity();
        entity.setExternalId(dto.getId());
        entity.setName(dto.getName());
        entity.setEntityType(dto.getEntityType());
        entity.setHomeState(dto.getHomeState());

        // Convert jurisdictions
        if (dto.getJurisdictions() != null && !dto.getJurisdictions().isEmpty()) {
            List<CompanyJurisdictionEntity> jurisdictionEntities = new ArrayList<>();
            for (String jurisdictionName : dto.getJurisdictions()) {
                CompanyJurisdictionEntity jurisdictionEntity = new CompanyJurisdictionEntity();
                jurisdictionEntity.setJurisdictionName(jurisdictionName);
                jurisdictionEntity.setCompany(entity);
                jurisdictionEntities.add(jurisdictionEntity);
            }
            entity.setJurisdictions(jurisdictionEntities);
        }

        return entity;
    }

    public CompanyDTO toDTO(CompanyEntity entity) {
        CompanyDTO dto = new CompanyDTO();
        dto.setId(entity.getExternalId());
        dto.setCreatedAt(entity.getCreatedOn() != null ? entity.getCreatedOn().toString() : null);
        dto.setUpdatedAt(entity.getModifiedOn() != null ? entity.getModifiedOn().toString() : null);
        dto.setName(entity.getName());
        dto.setEntityType(entity.getEntityType());
        dto.setHomeState(entity.getHomeState());

        // Convert jurisdictions
        if (entity.getJurisdictions() != null && !entity.getJurisdictions().isEmpty()) {
            List<String> jurisdictions = entity.getJurisdictions().stream()
                    .map(CompanyJurisdictionEntity::getJurisdictionName)
                    .collect(Collectors.toList());
            dto.setJurisdictions(jurisdictions);
        }

        return dto;
    }

    public void updateEntity(CompanyEntity entity, CompanyDTO dto) {
        entity.setName(dto.getName());
        entity.setEntityType(dto.getEntityType());
        entity.setHomeState(dto.getHomeState());

        // Update jurisdictions - remove old ones and add new ones
        if (entity.getJurisdictions() != null) {
            entity.getJurisdictions().clear();
        } else {
            entity.setJurisdictions(new ArrayList<>());
        }

        if (dto.getJurisdictions() != null && !dto.getJurisdictions().isEmpty()) {
            for (String jurisdictionName : dto.getJurisdictions()) {
                CompanyJurisdictionEntity jurisdictionEntity = new CompanyJurisdictionEntity();
                jurisdictionEntity.setJurisdictionName(jurisdictionName);
                jurisdictionEntity.setCompany(entity);
                entity.getJurisdictions().add(jurisdictionEntity);
            }
        }
    }
}

