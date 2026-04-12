package com.numbericsuserportal.ai.service;

import com.numbericsuserportal.ai.config.AnthropicProperties;
import com.numbericsuserportal.ai.entity.TaalrAgentSessionEntity;
import com.numbericsuserportal.ai.repo.TaalrAgentSessionRepository;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TaalrAgentSessionService {

    @Autowired
    private TaalrAgentSessionRepository repository;

    @Autowired
    private AnthropicProperties anthropicProperties;

    @Autowired
    private ManagedAgentsClient managedAgentsClient;

    /**
     * Returns existing {@code sesn_...} for user, or creates a new Managed Agents session and persists mapping.
     */
    @Transactional
    public String getOrCreateAnthropicSessionId(User user) throws Exception {
        Optional<TaalrAgentSessionEntity> existing = repository.findByUserId(user.getUserId());
        if (existing.isPresent()) {
            return existing.get().getAnthropicSessionId();
        }
        Map<String, String> meta = new HashMap<>();
        meta.put("portal_user_id", String.valueOf(user.getUserId()));
        if (user.getEmail() != null) {
            meta.put("user_email", user.getEmail());
        }
        String sessionId = managedAgentsClient.createSession(
                anthropicProperties.getAgentId().trim(),
                anthropicProperties.getEnvironmentId().trim(),
                meta
        );
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalStateException("Anthropic returned empty session id");
        }
        TaalrAgentSessionEntity row = new TaalrAgentSessionEntity();
        row.setUserId(user.getUserId());
        row.setAnthropicSessionId(sessionId);
        row.setAgentId(anthropicProperties.getAgentId());
        String audit = user.getEmail() != null ? user.getEmail() : String.valueOf(user.getUserId());
        row.setCreatedBy(audit);
        row.setModifiedBy(audit);
        repository.save(row);
        return sessionId;
    }

    /** Removes stored {@code sesn_} mapping so the next chat creates a new Anthropic session. */
    @Transactional
    public void clearSessionForUser(Long userId) {
        repository.deleteByUserId(userId);
    }
}
