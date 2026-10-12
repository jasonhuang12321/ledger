package com.learning;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    public record Payload(String username, String jti) {
    }

    private final SecretKey key;
    private final long expireMs;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expire-ms}") long expireMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMs = expireMs;
    }

    public long expireMs() {
        return expireMs;
    }

    public String create(AppUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getUsername())
                .claim("uid", user.getId())
                .claim("role", user.getRole())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expireMs)))
                .signWith(key)
                .compact();
    }

    public String usernameFrom(String token) {
        return parse(token).username();
    }

    public Payload parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new Payload(claims.getSubject(), claims.getId());
        } catch (ExpiredJwtException ex) {
            throw new BusinessException("通行证过期");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessException("通行证无效");
        }
    }
}
