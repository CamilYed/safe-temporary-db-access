package pl.pw.cyber.dbaccess.adapters.generator;

import pl.pw.cyber.dbaccess.domain.TemporaryCredentials;
import pl.pw.cyber.dbaccess.domain.UserCredentialsGenerator;

import java.security.SecureRandom;

class SecureUserCredentialsGenerator implements UserCredentialsGenerator {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_+=<>?";
    private static final String ALL_ALLOWED = LOWER + UPPER + DIGITS + SPECIAL;

    private static final int USERNAME_LENGTH = 12;
    private static final int PASSWORD_LENGTH = 16;

    private final SecureRandom random = new SecureRandom();

    @Override
    public TemporaryCredentials generate() {
        String username = generateUsername();
        String password = generateSecurePassword();
        return new TemporaryCredentials(username, password);
    }

    private String generateUsername() {
        String allowed = LOWER + DIGITS;
        return generateRandomString(allowed, USERNAME_LENGTH);
    }

    private String generateSecurePassword() {
        String password;
        do {
            password = generateRandomString(ALL_ALLOWED, PASSWORD_LENGTH);
        } while (!isStrong(password));
        return password;
    }

    private boolean isStrong(String password) {
        return password.chars().anyMatch(Character::isUpperCase)
          && password.chars().anyMatch(Character::isLowerCase)
          && password.chars().anyMatch(Character::isDigit)
          && password.chars().anyMatch(c -> SPECIAL.indexOf((char) c) >= 0)
          && password.chars().distinct().count() >= 10;
    }

    private String generateRandomString(String characterSet, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(randomChar(characterSet));
        }
        return sb.toString();
    }

    private char randomChar(String set) {
        return set.charAt(random.nextInt(set.length()));
    }
}
