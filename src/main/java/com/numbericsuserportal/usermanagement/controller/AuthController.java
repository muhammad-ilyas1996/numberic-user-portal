package com.numbericsuserportal.usermanagement.controller;

import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.service.AuthService;
import com.numbericsuserportal.usermanagement.jwt.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            User user = authService.authenticate(req.getUsername(), req.getPassword());

            // Optional: patient portal me sirf patient ko allow karna ho:
            // if (!user.getUserType().equals(User.UserType.patient)) { return 403; }

            String token = jwtUtil.generateToken(user);
            return ResponseEntity.ok(new TokenResponse(token));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class LoginRequest {
    private String username;
    private String password;
}


@Data
@AllArgsConstructor
@NoArgsConstructor
class TokenResponse {
    private String token;
}

