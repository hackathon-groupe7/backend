package com.example.capgemini_backend.security;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final byte[] signingKeyBytes;
    private final byte[] encryptionKeyBytes;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKeyBytes = decodeBase64Key(jwtProperties.getSigningKeyBase64(), "signing");
        this.encryptionKeyBytes = decodeBase64Key(jwtProperties.getEncryptionKeyBase64(), "encryption");
        validateKeyLengths();
    }

    public String generateToken(String subject, List<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwtProperties.getExpirationMinutes() * 60L);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
            .issuer(jwtProperties.getIssuer())
            .subject(subject)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(exp))
            .jwtID(UUID.randomUUID().toString())
            .claim("roles", roles)
            .build();

        try {
            SignedJWT signedJwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(),
                claims
            );
            signedJwt.sign(new MACSigner(signingKeyBytes));

            JWEObject encryptedJwt = new JWEObject(
                new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
                    .contentType("JWT")
                    .build(),
                new Payload(signedJwt)
            );
            encryptedJwt.encrypt(new DirectEncrypter(encryptionKeyBytes));
            return encryptedJwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Unable to generate JWT", e);
        }
    }

    public JwtPrincipal parseAndValidate(String token) {
        try {
            JWEObject encryptedJwt = JWEObject.parse(token);
            encryptedJwt.decrypt(new DirectDecrypter(encryptionKeyBytes));
            SignedJWT signedJwt = encryptedJwt.getPayload().toSignedJWT();
            if (signedJwt == null) {
                throw new IllegalArgumentException("Invalid JWT payload format");
            }

            if (!signedJwt.verify(new MACVerifier(signingKeyBytes))) {
                throw new IllegalArgumentException("JWT signature verification failed");
            }

            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
            validateClaims(claims);

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.getClaim("roles");
            return new JwtPrincipal(claims.getSubject(), roles == null ? List.of() : roles);
        } catch (ParseException | JOSEException ex) {
            throw new IllegalArgumentException("Invalid JWT token", ex);
        }
    }

    private void validateClaims(JWTClaimsSet claims) {
        if (claims.getExpirationTime() == null || claims.getExpirationTime().before(new Date())) {
            throw new IllegalArgumentException("JWT token is expired");
        }
        if (claims.getIssuer() == null || !claims.getIssuer().equals(jwtProperties.getIssuer())) {
            throw new IllegalArgumentException("JWT issuer mismatch");
        }
        if (claims.getSubject() == null || claims.getSubject().isBlank()) {
            throw new IllegalArgumentException("JWT subject missing");
        }
    }

    private byte[] decodeBase64Key(String rawBase64, String keyName) {
        if (rawBase64 == null || rawBase64.isBlank()) {
            throw new IllegalArgumentException(keyName + " key is missing");
        }
        String normalized = rawBase64.trim();

        try {
            return Base64.getDecoder().decode(normalized);
        } catch (IllegalArgumentException decodeFailure) {
            return normalized.getBytes(StandardCharsets.UTF_8);
        }
    }

    private void validateKeyLengths() {
        if (signingKeyBytes.length < 32) {
            throw new IllegalArgumentException("Signing key must be at least 32 bytes");
        }
        if (encryptionKeyBytes.length != 32) {
            throw new IllegalArgumentException("Encryption key must be exactly 32 bytes for A256GCM");
        }
        if (java.util.Arrays.equals(signingKeyBytes, encryptionKeyBytes)) {
            throw new IllegalArgumentException("Signing and encryption keys must be different");
        }
    }
}
