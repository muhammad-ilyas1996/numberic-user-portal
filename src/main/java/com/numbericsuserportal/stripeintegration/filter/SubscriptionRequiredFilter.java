package com.numbericsuserportal.stripeintegration.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.numbericsuserportal.usermanagement.domain.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * SubscriptionRequiredFilter
 *
 * Defense-in-depth filter that enforces subscription requirements on
 * business API endpoints. If a fully-authenticated user has no active
 * subscription (TRIAL or ACTIVE), the filter returns HTTP 403 with a
 * machine-readable code of "SUBSCRIPTION_REQUIRED" so the frontend can
 * redirect to /choose-plan.
 *
 * This is the BACKEND layer of enforcement. The frontend ProtectedRoute
 * component provides the UX layer.
 */
@Component
public class SubscriptionRequiredFilter extends OncePerRequestFilter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Allowed subscription statuses that grant dashboard access. */
    private static final List<String> ALLOWED_STATUSES = Arrays.asList("TRIAL", "ACTIVE");

    /**
     * URI prefixes that require an active subscription.
     * Everything else (auth, subscription management, public endpoints) is exempt.
     */
    private static final List<String> GATED_PREFIXES = Arrays.asList(
            "/api/invoice",
            "/api/tax",
            "/api/llc-northwest",
            "/api/receipt",
            "/api/kintsugi",
            "/api/twilio",
            "/api/company",
            "/api/dashboard",
            "/api/menus",
            "/api/payment-methods"
    );

    /** Paths that are ALWAYS allowed even if they match a gated prefix. */
    private static final List<String> ALWAYS_ALLOWED = Arrays.asList(
            "/api/subscription/plans",
            "/api/subscription/add-to-user",
            "/api/subscription/status",
            "/api/user-management/me",
            "/api/auth"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Only apply gate to gated paths
        if (!isGatedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get the authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof User)) {
            // Spring Security will handle unauthenticated requests separately
            filterChain.doFilter(request, response);
            return;
        }

        User user = (User) auth.getPrincipal();

        String status = user.getSubscriptionStatus();
        if (status == null || !ALLOWED_STATUSES.contains(status.toUpperCase())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            String body = MAPPER.writeValueAsString(Map.of(
                    "success", false,
                    "code", "SUBSCRIPTION_REQUIRED",
                    "message", "An active subscription is required to access this resource."
            ));
            response.getWriter().write(body);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isGatedPath(String path) {
        for (String allowed : ALWAYS_ALLOWED) {
            if (path.startsWith(allowed)) return false;
        }
        for (String prefix : GATED_PREFIXES) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }
}
