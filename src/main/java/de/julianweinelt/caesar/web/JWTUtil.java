package de.julianweinelt.caesar.web;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@SuppressWarnings({"unused", "RedundantSuppression"})
@Slf4j
public class JWTUtil {

    private static final JWTVerifier verifier = JWT.require(Algorithm.HMAC256("caesar-backend")).build();


    public static String token(UUID user) {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 30);
        Date expiration = calendar.getTime();
        return JWT.create()
                .withSubject(user.toString())
                .withIssuer("caesar")
                .withNotBefore(now)
                .withIssuedAt(now)
                .withExpiresAt(expiration)
                .sign(Algorithm.HMAC256("caesar-backend"));
    }

    public static DecodedJWT decode(String token) {
        try {
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            log.error("Failed to decode JWT token: {}", e.getMessage());
            return null;
        }
    }

    public static boolean verify(String token) {
        try {
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            log.error("Failed to verify JWT token: {}", e.getMessage());
            return false;
        }
    }
}