package com.numbericsuserportal.ai.controller;

import com.numbericsuserportal.ai.dto.ChatRequestDto;
import com.numbericsuserportal.ai.dto.ChatResponseDto;
import com.numbericsuserportal.ai.service.AnthropicChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/chat")
@CrossOrigin(origins = "*")
public class PublicChatController {

    @Autowired
    private AnthropicChatService anthropicChatService;

    @PostMapping
    public ResponseEntity<ChatResponseDto> chat(@RequestBody ChatRequestDto request) {
        ChatResponseDto response = anthropicChatService.chatPublic(request);
        if (!response.isSuccess()) {
            String err = response.getError() != null ? response.getError() : "";
            if (err.contains("not configured") || err.contains("Anthropic API")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }
            if (err.contains("Message is required")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
        }
        return ResponseEntity.ok(response);
    }
}

