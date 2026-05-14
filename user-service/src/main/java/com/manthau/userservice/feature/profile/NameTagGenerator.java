package com.manthau.userservice.feature.profile;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class NameTagGenerator {

    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final int TAG_LENGTH = 6;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        char[] chars = new char[TAG_LENGTH];
        for (int i = 0; i < TAG_LENGTH; i++) {
            chars[i] = ALPHABET[random.nextInt(ALPHABET.length)];
        }
        return new String(chars);
    }
}
