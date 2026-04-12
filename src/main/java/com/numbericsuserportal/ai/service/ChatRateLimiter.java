package com.numbericsuserportal.ai.service;

import com.numbericsuserportal.ai.config.ChatRateLimitProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sliding-window rate limit per user for {@code /api/chat}. In-memory (single JVM); use Redis for multi-instance later.
 */
@Service
public class ChatRateLimiter {

    @Autowired
    private ChatRateLimitProperties properties;

    private final ConcurrentHashMap<Long, Deque<Long>> timestampsByUser = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Object> userLocks = new ConcurrentHashMap<>();

    /**
     * Records this request or throws 429 if the user exceeded the configured limit.
     */
    public void checkAllowed(Long userId) {
        if (!properties.isEnabled() || userId == null) {
            return;
        }
        int max = properties.getMaxMessages();
        int windowMin = properties.getWindowMinutes();
        if (max <= 0 || windowMin <= 0) {
            return;
        }
        long windowMs = windowMin * 60_000L;
        long now = System.currentTimeMillis();

        Object lock = userLocks.computeIfAbsent(userId, k -> new Object());
        synchronized (lock) {
            Deque<Long> dq = timestampsByUser.computeIfAbsent(userId, k -> new ArrayDeque<>());
            while (!dq.isEmpty() && dq.peekFirst() < now - windowMs) {
                dq.pollFirst();
            }
            if (dq.size() >= max) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        String.format(
                                "Chat rate limit: at most %d messages per %d minute(s). Try again later.",
                                max,
                                windowMin));
            }
            dq.addLast(now);
        }
    }
}
