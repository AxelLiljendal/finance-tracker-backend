package com.github.axelliljendal.finance_tracker.jwt;

public class JwtSecretGenerator {
    public static void main(String[] args) {
        byte[] randomBytes = new byte[64];
        new java.security.SecureRandom().nextBytes(randomBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : randomBytes) {
            sb.append(String.format("%02x", b));
        }
        System.out.println("Generated JWT Secret: " + sb.toString());
    }
}
