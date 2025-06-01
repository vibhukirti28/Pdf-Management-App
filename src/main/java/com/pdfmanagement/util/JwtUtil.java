package com.pdfmanagement.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    /**
     * The secret key used for signing and verifying JWT tokens.
     * <p>
     * This key should be kept private and secure, as it is critical for the integrity and security
     * of the JWT authentication mechanism. Exposing this key can compromise the security of the application.
     * </p>
     * <p>
     * Note: Consider storing this key in a secure location such as environment variables or a secrets manager,
     * rather than hardcoding it in the source code.
     * </p>
     */
    private final String SECRET_KEY_STRING = "aae1dfb349b3fdcc83f85616321586294efdafc36c70c80096987ee0629768a29ee119a6f6d2fa5b692c80023a1b4b6de9a79aea1d29cbc06ec45d34c3db29b5123686df8de7a01551f252eb9f71246e5f81d45fb215724f2096febb5bd32001ad22e1d852e90288926fcf6b1fbee9e49abc03bf53bd4906a1835a516a9c06f0";

    private final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));

    private final long JWT_EXPIRATION = 1000 * 60 * 60 * 10; // 10 hours

    /**
     * Extracts the email (subject) from the provided JWT token.
     *
     * @param token the JWT token from which to extract the email
     * @return the email address (subject) contained in the token
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the provided JWT token.
     *
     * @param token the JWT token from which to extract the expiration date
     * @return the expiration {@link Date} of the token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the provided JWT token using the given claims resolver function.
     *
     * @param <T>            The type of the claim to be extracted.
     * @param token          The JWT token from which the claim is to be extracted.
     * @param claimsResolver A function that takes the Claims object and returns the desired claim.
     * @return The extracted claim of type T.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the provided JWT token.
     *
     * @param token the JWT token from which to extract claims
     * @return the {@link io.jsonwebtoken.Claims} object containing all claims present in the token
     * @throws io.jsonwebtoken.JwtException if the token is invalid or cannot be parsed
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if the provided JWT token has expired.
     *
     * @param token the JWT token to check
     * @return true if the token is expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generates a JWT token for the specified email address.
     *
     * <p>The generated token includes the email as the subject, the current time as the issue date,
     * and an expiration date based on the configured JWT expiration period. The token is signed
     * using the configured secret key and the HS512 signature algorithm.</p>
     *
     * @param email the email address to be set as the subject of the JWT token
     * @return a signed JWT token as a String
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Validates the given JWT token by checking if the email extracted from the token
     * matches the provided email and if the token has not expired.
     *
     * @param token the JWT token to validate
     * @param email the email address to compare with the one extracted from the token
     * @return true if the token is valid (email matches and token is not expired), false otherwise
     */
    public boolean validateToken(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }
}
