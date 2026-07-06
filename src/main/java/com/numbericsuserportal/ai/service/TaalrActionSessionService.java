package com.numbericsuserportal.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.ai.action.TaalrActionChannel;
import com.numbericsuserportal.ai.action.TaalrPendingAction;
import com.numbericsuserportal.ai.action.dto.TaalrActionRequest;
import com.numbericsuserportal.ai.action.dto.TaalrSessionContext;
import com.numbericsuserportal.ai.config.TaalrActionProperties;
import com.numbericsuserportal.ai.entity.TaalrActionSessionEntity;
import com.numbericsuserportal.ai.repo.TaalrActionSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class TaalrActionSessionService {

    @Autowired
    private TaalrActionSessionRepository repository;

    @Autowired
    private TaalrActionProperties properties;

    @Autowired
    private ObjectMapper objectMapper;

    public Optional<TaalrActionSessionEntity> findActiveSession(TaalrActionRequest request) {
        LocalDateTime now = LocalDateTime.now();
        if (request.getUserId() != null) {
            return repository.findFirstByUserIdAndIsActiveTrueAndExpiresAtAfterOrderByModifiedOnDesc(
                    request.getUserId(), now);
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            return repository.findFirstByPhoneNumberAndIsActiveTrueAndExpiresAtAfterOrderByModifiedOnDesc(
                    request.getPhoneNumber().trim(), now);
        }
        return Optional.empty();
    }

    public TaalrSessionContext loadContext(TaalrActionSessionEntity session) {
        if (session == null || session.getContextJson() == null || session.getContextJson().isBlank()) {
            TaalrSessionContext ctx = new TaalrSessionContext();
            if (session != null && session.getPendingAction() != null) {
                ctx.setPendingAction(session.getPendingAction());
            }
            return ctx;
        }
        try {
            TaalrSessionContext ctx = objectMapper.readValue(session.getContextJson(), TaalrSessionContext.class);
            if (ctx.getPendingAction() == null && session.getPendingAction() != null) {
                ctx.setPendingAction(session.getPendingAction());
            }
            return ctx;
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse Taalr session context id={}: {}", session.getId(), e.getMessage());
            TaalrSessionContext ctx = new TaalrSessionContext();
            ctx.setPendingAction(session.getPendingAction());
            return ctx;
        }
    }

    @Transactional
    public TaalrActionSessionEntity saveSession(TaalrActionRequest request, TaalrPendingAction action,
            TaalrSessionContext context) {
        deactivateExisting(request);
        TaalrActionSessionEntity entity = new TaalrActionSessionEntity();
        entity.setUserId(request.getUserId());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setChannel(request.getChannel() != null ? request.getChannel() : TaalrActionChannel.APP_CHAT);
        entity.setPendingAction(action);
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(properties.getSessionTtlMinutes()));
        entity.setIsActive(true);
        if (context != null) {
            context.setPendingAction(action);
            try {
                entity.setContextJson(objectMapper.writeValueAsString(context));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize Taalr session context", e);
            }
        }
        return repository.save(entity);
    }

    @Transactional
    public TaalrActionSessionEntity updateSession(TaalrActionSessionEntity session, TaalrPendingAction action,
            TaalrSessionContext context) {
        session.setPendingAction(action);
        session.setExpiresAt(LocalDateTime.now().plusMinutes(properties.getSessionTtlMinutes()));
        if (context != null) {
            context.setPendingAction(action);
            try {
                session.setContextJson(objectMapper.writeValueAsString(context));
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize Taalr session context", e);
            }
        }
        return repository.save(session);
    }

    @Transactional
    public void touchSession(TaalrActionSessionEntity session) {
        if (session == null) {
            return;
        }
        session.setExpiresAt(LocalDateTime.now().plusMinutes(properties.getSessionTtlMinutes()));
        repository.save(session);
    }

    @Transactional
    public void clearSession(TaalrActionRequest request) {
        findActiveSession(request).ifPresent(session -> {
            session.setIsActive(false);
            repository.save(session);
        });
    }

    private void deactivateExisting(TaalrActionRequest request) {
        findActiveSession(request).ifPresent(session -> {
            session.setIsActive(false);
            repository.save(session);
        });
    }
}
