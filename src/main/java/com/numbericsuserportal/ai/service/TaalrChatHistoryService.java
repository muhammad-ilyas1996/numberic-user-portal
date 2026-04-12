package com.numbericsuserportal.ai.service;

import com.numbericsuserportal.ai.config.AnthropicProperties;
import com.numbericsuserportal.ai.entity.TaalrChatMessageEntity;
import com.numbericsuserportal.ai.repo.TaalrChatMessageRepository;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Persists multi-turn chat for Messages API fallback ({@code /v1/messages}). Managed Agents use Anthropic session instead.
 */
@Service
public class TaalrChatHistoryService {

    @Autowired
    private TaalrChatMessageRepository repository;

    @Autowired
    private AnthropicProperties anthropicProperties;

    @Transactional
    public Long appendUserMessage(User user, String content) {
        TaalrChatMessageEntity row = new TaalrChatMessageEntity();
        row.setUserId(user.getUserId());
        row.setRole("user");
        row.setContent(content);
        String audit = user.getEmail() != null ? user.getEmail() : String.valueOf(user.getUserId());
        row.setCreatedBy(audit);
        row.setModifiedBy(audit);
        return repository.save(row).getId();
    }

    @Transactional
    public void appendAssistantMessage(User user, String content) {
        TaalrChatMessageEntity row = new TaalrChatMessageEntity();
        row.setUserId(user.getUserId());
        row.setRole("assistant");
        row.setContent(content);
        String audit = user.getEmail() != null ? user.getEmail() : String.valueOf(user.getUserId());
        row.setCreatedBy(audit);
        row.setModifiedBy(audit);
        repository.save(row);
    }

    @Transactional
    public void deleteMessageById(Long id) {
        if (id != null) {
            repository.deleteById(id);
        }
    }

    /** Messages API fallback: wipe multi-turn history for a user (e.g. together with Managed Agents session reset). */
    @Transactional
    public void clearHistoryForUser(Long userId) {
        if (userId != null) {
            repository.deleteByUserId(userId);
        }
    }

    /**
     * Builds {@code messages} for Anthropic: chronological, trimmed to max length, starting with {@code user}.
     */
    public List<Map<String, String>> buildMessagesPayload(Long userId) {
        List<TaalrChatMessageEntity> rows = repository.findByUserIdOrderByIdAsc(userId);
        int max = Math.max(2, anthropicProperties.getMessagesHistoryMaxMessages());
        if (rows.size() > max) {
            rows = new ArrayList<>(rows.subList(rows.size() - max, rows.size()));
        }
        while (!rows.isEmpty() && "assistant".equalsIgnoreCase(rows.get(0).getRole())) {
            rows = new ArrayList<>(rows.subList(1, rows.size()));
        }
        List<Map<String, String>> out = new ArrayList<>();
        for (TaalrChatMessageEntity m : rows) {
            Map<String, String> one = new LinkedHashMap<>();
            one.put("role", m.getRole());
            one.put("content", m.getContent());
            out.add(one);
        }
        return out;
    }
}
