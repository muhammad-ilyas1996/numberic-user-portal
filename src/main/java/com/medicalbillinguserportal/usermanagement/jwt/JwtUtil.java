package com.medicalbillinguserportal.usermanagement.jwt;

import com.medicalbillinguserportal.usermanagement.domain.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.util.stream.Collectors;
@Component
public class JwtUtil {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private static final long EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24h

    public JwtUtil(
            @Value("${jwt.keystore.path}") String ksPath,
            @Value("${jwt.keystore.password}") String ksPassword,
            @Value("${jwt.key.alias}") String alias,
            ResourceLoader resourceLoader
    ) {
        try {
            Resource res = resourceLoader.getResource(ksPath);
            KeyStore ks = KeyStore.getInstance("JKS");
            try (InputStream is = res.getInputStream()) {
                ks.load(is, ksPassword.toCharArray());
            }
            this.privateKey = (PrivateKey) ks.getKey(alias, ksPassword.toCharArray());
            this.publicKey = ks.getCertificate(alias).getPublicKey();
        } catch (Exception e) {
            throw new RuntimeException("Keystore Not Loaded", e);
        }
    }

    public String generateToken(User user) {
        String userType = user.getUserType().name().toUpperCase();
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getUserId())
                .claim("userType", userType)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token);
    }

    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

