package com.myga.learning.backend.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/** Creates and validates signed (HS256) JWT access tokens. */
@Service
public class JwtService {

    private static final String PASSWORD_VERSION_CLAIM = "pv";

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /** The password version is stamped into the token so it can be invalidated. */
    public String generateToken(UserDetails userDetails, String role, int passwordVersion) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("role", role)
                .claim(PASSWORD_VERSION_CLAIM, passwordVersion)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                && !isExpired(token)
                && matchesPasswordVersion(token, userDetails);
    }

    /**
     * A token is only good for the password it was issued under. Changing or
     * resetting the password bumps the account's version, which leaves every
     * token stamped with an earlier one invalid - immediately, and with no
     * dependence on clock resolution.
     */
    private boolean matchesPasswordVersion(String token, UserDetails userDetails) {
        if (!(userDetails instanceof AuthenticatedUser)) {
            return true;
        }
        Integer tokenVersion = extractClaim(token, claims -> claims.get(PASSWORD_VERSION_CLAIM, Integer.class));
        int current = ((AuthenticatedUser) userDetails).getPasswordVersion();
        return (tokenVersion == null ? 0 : tokenVersion) == current;
    }

    private boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return resolver.apply(claims);
    }
}
