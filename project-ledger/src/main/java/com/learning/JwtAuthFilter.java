package com.learning;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenStore tokenStore;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository, TokenStore tokenStore) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.tokenStore = tokenStore;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return !path.startsWith("/api/") || path.equals("/api/login");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录");
            return;
        }
        String token = header.substring("Bearer ".length()).trim();
        try {
            JwtService.Payload payload = jwtService.parse(token);
            if (!tokenStore.exists(payload.jti())) {
                writeUnauthorized(response, "通行证已失效");
                return;
            }
            AppUser user = userRepository.findByUsername(payload.username()).orElse(null);
            if (user == null) {
                writeUnauthorized(response, "未登录");
                return;
            }
            LedgerUserPrincipal principal = new LedgerUserPrincipal(user);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);
        } catch (BusinessException ex) {
            writeUnauthorized(response, ex.getMessage());
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
