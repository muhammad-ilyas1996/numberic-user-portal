package com.numbericsuserportal.usermanagement.controller;

import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Depends on how roles are granted
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> userDtos = users.stream().map(user -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("userId", user.getUserId());
            dto.put("firstName", user.getFirstName());
            dto.put("lastName", user.getLastName());
            dto.put("email", user.getEmail());
            dto.put("userType", user.getUserType().name());
            dto.put("createdAt", user.getCreatedAt());
            dto.put("isActive", user.getIsActive());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/users/stats")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        List<User> users = userRepository.findAll();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", users.size());
        
        long activeUsers = users.stream().filter(User::getIsActive).count();
        stats.put("activeUsers", activeUsers);
        
        Map<String, Long> userTypes = users.stream()
            .collect(Collectors.groupingBy(u -> u.getUserType().name(), Collectors.counting()));
        stats.put("userTypes", userTypes);
        
        return ResponseEntity.ok(stats);
    }
}
