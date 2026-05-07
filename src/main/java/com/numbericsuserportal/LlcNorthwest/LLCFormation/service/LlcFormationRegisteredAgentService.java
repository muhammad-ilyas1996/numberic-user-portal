package com.numbericsuserportal.LlcNorthwest.LLCFormation.service;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.UpdateStep4RegisteredAgentRequestDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormationRegisteredAgent;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationRegisteredAgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LlcFormationRegisteredAgentService {

    @Autowired
    private LlcFormationRegisteredAgentRepository repo;

    @Transactional
    public LlcFormationRegisteredAgent upsert(Long formationId, LlcFormation formation, UpdateStep4RegisteredAgentRequestDTO req) {
        if (req.getAgentType() == null || req.getAgentType().trim().isEmpty()) {
            throw new IllegalArgumentException("agentType is required");
        }
        String type = req.getAgentType().trim().toUpperCase();

        if (!type.equals("NUMBRICS_NW") && !type.equals("OWN")) {
            throw new IllegalArgumentException("agentType must be NUMBRICS_NW or OWN");
        }

        if (type.equals("OWN")) {
            validateOwnAgent(formation, req);
        }

        LlcFormationRegisteredAgent row = repo.findByFormationId(formationId).orElseGet(LlcFormationRegisteredAgent::new);
        row.setFormationId(formationId);
        row.setAgentType(type);
        row.setSource(type.equals("OWN") ? "USER_PROVIDED" : "NORTHWEST");

        if (type.equals("NUMBRICS_NW")) {
            row.setNorthwestRefId(req.getNorthwestRefId());
            row.setAgentNameSnapshot(req.getAgentNameSnapshot());
            row.setAgentAddressSnapshot(req.getAgentAddressSnapshot());
            row.setAgentName(null);
            row.setStreet(null);
            row.setCity(null);
            row.setState(null);
            row.setZip(null);
        } else {
            row.setNorthwestRefId(null);
            row.setAgentNameSnapshot(null);
            row.setAgentAddressSnapshot(null);
            row.setAgentName(req.getAgentName());
            row.setStreet(req.getStreet());
            row.setCity(req.getCity());
            row.setState(req.getState());
            row.setZip(req.getZip());
        }

        return repo.save(row);
    }

    private void validateOwnAgent(LlcFormation formation, UpdateStep4RegisteredAgentRequestDTO req) {
        if (req.getAgentName() == null || req.getAgentName().trim().isEmpty()) {
            throw new IllegalArgumentException("agentName is required for OWN agent");
        }
        if (req.getStreet() == null || req.getStreet().trim().isEmpty()) {
            throw new IllegalArgumentException("street is required for OWN agent");
        }
        if (req.getCity() == null || req.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("city is required for OWN agent");
        }
        if (req.getState() == null || req.getState().trim().isEmpty()) {
            throw new IllegalArgumentException("state is required for OWN agent");
        }
        if (req.getZip() == null || req.getZip().trim().isEmpty()) {
            throw new IllegalArgumentException("zip is required for OWN agent");
        }

        String street = req.getStreet().toLowerCase();
        if (street.contains("p.o.") || street.contains("po box") || street.contains("p o box")) {
            throw new IllegalArgumentException("Registered agent address must be a physical address (no P.O. Box)");
        }

        String formationState = formation.getJurisdiction() != null ? formation.getJurisdiction().trim().toUpperCase() : null;
        if (formationState != null && !formationState.isEmpty()) {
            String agentState = req.getState().trim().toUpperCase();
            if (!agentState.equals(formationState)) {
                throw new IllegalArgumentException("Registered agent address must be in the state of formation (" + formationState + ")");
            }
        }
    }
}

