package com.numbericsuserportal.usermanagement.jwt;


import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.validate(token)) {
                Claims claims = jwtUtil.parse(token).getBody();

                String username = claims.getSubject();
                String userType = String.valueOf(claims.get("userType")); // ADMIN / PATIENT

                User userEntity = userRepository.findByUsername(username)
                        .orElse(null);
                // Spring Security authority
                List<GrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_" + userType));

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userEntity, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(req, res);
    }
}
