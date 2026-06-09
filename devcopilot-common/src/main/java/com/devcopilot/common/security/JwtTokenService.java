package com.devcopilot.common.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class JwtTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final byte[] secret;
    private final long expirationSeconds;

    public JwtTokenService(String secret, long expirationSeconds) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 characters");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public String createToken(Long userId, String username) {
        long expiresAt = Instant.now().plusSeconds(expirationSeconds).getEpochSecond();
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("sub=" + userId + ";username=" + username + ";exp=" + expiresAt);
        String signature = sign(header + "." + payload);
        return header + "." + payload + "." + signature;
    }

    public Optional<JwtPrincipal> verify(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }
        String expectedSignature = sign(parts[0] + "." + parts[1]);
        if (!constantTimeEquals(expectedSignature, parts[2])) {
            return Optional.empty();
        }
        String payload = new String(URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
        Long userId = null;
        String username = null;
        long expiresAt = 0L;
        for (String item : payload.split(";")) {
            String[] pair = item.split("=", 2);
            if (pair.length != 2) {
                continue;
            }
            if ("sub".equals(pair[0])) {
                userId = Long.valueOf(pair[1]);
            } else if ("username".equals(pair[0])) {
                username = pair[1];
            } else if ("exp".equals(pair[0])) {
                expiresAt = Long.parseLong(pair[1]);
            }
        }
        if (userId == null || username == null || expiresAt <= Instant.now().getEpochSecond()) {
            return Optional.empty();
        }
        return Optional.of(new JwtPrincipal(userId, username));
    }

    private String encode(String value) {
        return URL_ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign JWT token", ex);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null || left.length() != right.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < left.length(); i++) {
            result |= left.charAt(i) ^ right.charAt(i);
        }
        return result == 0;
    }
}
