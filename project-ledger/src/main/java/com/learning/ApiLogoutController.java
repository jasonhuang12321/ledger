package com.learning;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiLogoutController {

    private static final Logger log = LoggerFactory.getLogger(ApiLogoutController.class);
    private final JwtService jwtService;
    private final TokenStore tokenStore;

    public ApiLogoutController(JwtService jwtService, TokenStore tokenStore) {
        this.jwtService = jwtService;
        this.tokenStore = tokenStore;
    }

    @PostMapping("/api/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length()).trim();
            JwtService.Payload payload = jwtService.parse(token);
            tokenStore.remove(payload.jti());
            log.info("接口退出 username={}", payload.username());
        }
        return Map.of("ok", true);
    }
}
