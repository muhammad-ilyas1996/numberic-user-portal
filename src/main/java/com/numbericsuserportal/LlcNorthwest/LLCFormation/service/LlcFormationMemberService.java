package com.numbericsuserportal.LlcNorthwest.LLCFormation.service;

import com.numbericsuserportal.LlcNorthwest.LLCFormation.dto.FormationMemberDTO;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormation;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.entity.LlcFormationMember;
import com.numbericsuserportal.LlcNorthwest.LLCFormation.repo.LlcFormationMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class LlcFormationMemberService {

    @Autowired
    private LlcFormationMemberRepository memberRepository;

    @Transactional
    public List<LlcFormationMember> replaceMembers(Long formationId, LlcFormation formation, List<FormationMemberDTO> members) {
        if (members == null || members.isEmpty()) {
            return memberRepository.findByFormationIdOrderByIdAsc(formationId);
        }

        validateMembers(formation, members);
        memberRepository.deleteByFormationId(formationId);

        List<LlcFormationMember> rows = new ArrayList<>();
        int idx = 0;
        for (FormationMemberDTO m : members) {
            LlcFormationMember row = new LlcFormationMember();
            row.setFormationId(formationId);
            row.setFirstName(req(m.getFirstName(), "member.firstName"));
            row.setLastName(req(m.getLastName(), "member.lastName"));
            row.setDob(parseDate(m.getDob()));
            row.setSsnLast4Enc(blankToNull(m.getSsnLast4()));
            row.setOwnershipPct(reqPct(m.getOwnershipPct()));
            row.setTitle(blankToNull(m.getTitle()));
            row.setPrimaryMember(Boolean.TRUE.equals(m.getPrimaryMember()) || idx == 0);
            rows.add(memberRepository.save(row));
            idx++;
        }
        return rows;
    }

    @Transactional(readOnly = true)
    public List<LlcFormationMember> list(Long formationId) {
        return memberRepository.findByFormationIdOrderByIdAsc(formationId);
    }

    @Transactional
    public LlcFormationMember add(Long formationId, LlcFormation formation, FormationMemberDTO dto) {
        List<FormationMemberDTO> combined = toDtoList(memberRepository.findByFormationIdOrderByIdAsc(formationId));
        combined.add(dto);
        List<LlcFormationMember> saved = replaceMembers(formationId, formation, combined);
        return saved.get(saved.size() - 1);
    }

    @Transactional
    public List<LlcFormationMember> remove(Long formationId, LlcFormation formation, Long memberId) {
        List<FormationMemberDTO> combined = toDtoList(memberRepository.findByFormationIdOrderByIdAsc(formationId));
        combined.removeIf(m -> Objects.equals(m.getId(), memberId));
        validateMembers(formation, combined);
        memberRepository.deleteByFormationId(formationId);
        List<LlcFormationMember> rows = new ArrayList<>();
        int idx = 0;
        for (FormationMemberDTO m : combined) {
            LlcFormationMember row = new LlcFormationMember();
            row.setFormationId(formationId);
            row.setFirstName(req(m.getFirstName(), "member.firstName"));
            row.setLastName(req(m.getLastName(), "member.lastName"));
            row.setDob(parseDate(m.getDob()));
            row.setSsnLast4Enc(blankToNull(m.getSsnLast4()));
            row.setOwnershipPct(reqPct(m.getOwnershipPct()));
            row.setTitle(blankToNull(m.getTitle()));
            row.setPrimaryMember(idx == 0);
            rows.add(memberRepository.save(row));
            idx++;
        }
        return rows;
    }

    public void validateMembers(LlcFormation formation, List<FormationMemberDTO> members) {
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("At least one member is required");
        }
        String ownershipType = formation.getOwnershipType() == null ? "single" : formation.getOwnershipType().trim().toLowerCase();
        if ("single".equals(ownershipType) && members.size() != 1) {
            throw new IllegalArgumentException("Single-member LLC must have exactly one member");
        }
        if ("multi".equals(ownershipType) && members.size() < 2) {
            throw new IllegalArgumentException("Multi-member LLC must have at least two members");
        }

        int totalPct = 0;
        for (FormationMemberDTO m : members) {
            req(m.getFirstName(), "member.firstName");
            req(m.getLastName(), "member.lastName");
            totalPct += reqPct(m.getOwnershipPct());
        }
        if (totalPct != 100) {
            throw new IllegalArgumentException("Total ownership percentage must equal 100");
        }
    }

    private List<FormationMemberDTO> toDtoList(List<LlcFormationMember> rows) {
        List<FormationMemberDTO> list = new ArrayList<>();
        for (LlcFormationMember row : rows) {
            FormationMemberDTO dto = new FormationMemberDTO();
            dto.setId(row.getId());
            dto.setFirstName(row.getFirstName());
            dto.setLastName(row.getLastName());
            dto.setDob(row.getDob() == null ? null : row.getDob().toString());
            dto.setSsnLast4(row.getSsnLast4Enc());
            dto.setOwnershipPct(row.getOwnershipPct());
            dto.setTitle(row.getTitle());
            dto.setPrimaryMember(row.getPrimaryMember());
            list.add(dto);
        }
        return list;
    }

    private String req(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private int reqPct(Integer pct) {
        if (pct == null || pct < 0 || pct > 100) {
            throw new IllegalArgumentException("ownershipPct must be between 0 and 100");
        }
        return pct;
    }

    private String blankToNull(String v) {
        return v == null || v.trim().isEmpty() ? null : v.trim();
    }

    private LocalDate parseDate(String d) {
        if (d == null || d.trim().isEmpty()) return null;
        return LocalDate.parse(d.trim());
    }
}

