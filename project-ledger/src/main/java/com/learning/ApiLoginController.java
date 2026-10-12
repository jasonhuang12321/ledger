package com.learning;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiLoginController {

    private static final Logger log = LoggerFactory.getLogger(ApiLoginController.class);
    private final UserService userService;
    private final JwtService jwtService;
    private final TokenStore tokenStore;

    public ApiLoginController(UserService userService, JwtService jwtService, TokenStore tokenStore) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.tokenStore = tokenStore;
    }

    @PostMapping("/api/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody ApiLoginForm form) {
        AppUser user = userService.login(form.getUsername(), form.getPassword());
        if (user == null) {
            log.warn("接口登录失败 username={}", form.getUsername());
            return ResponseEntity.status(401).body(Map.of("error", "用户名或密码错误"));
        }
        String token = jwtService.create(user);
        JwtService.Payload payload = jwtService.parse(token);
        tokenStore.save(payload.jti(), user.getUsername(), jwtService.expireMs());
        log.info("接口登录成功 username={} role={}", user.getUsername(), user.getRole());
        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "role", user.getRole() == null ? "USER" : user.getRole()
        ));
    }
}
