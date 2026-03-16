package com.example.capgemini_backend.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String signingKeyBase64;
    private String encryptionKeyBase64;
    private String issuer;
    private long expirationMinutes;

    public String getSigningKeyBase64() {
        return signingKeyBase64;
    }

    public void setSigningKeyBase64(String signingKeyBase64) {
        this.signingKeyBase64 = signingKeyBase64;
    }

    public String getEncryptionKeyBase64() {
        return encryptionKeyBase64;
    }

    public void setEncryptionKeyBase64(String encryptionKeyBase64) {
        this.encryptionKeyBase64 = encryptionKeyBase64;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(long expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }
}
