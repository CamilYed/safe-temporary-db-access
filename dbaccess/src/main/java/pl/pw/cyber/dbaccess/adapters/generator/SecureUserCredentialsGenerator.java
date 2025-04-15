package pl.pw.cyber.dbaccess.adapters.generator;

import pl.pw.cyber.dbaccess.domain.TemporaryCredentials;
import pl.pw.cyber.dbaccess.domain.UserCredentialsGenerator;

import java.security.SecureRandom;

/**
 * Secure implementation of the {@link UserCredentialsGenerator} port,
 * responsible for generating cryptographically secure, random usernames and passwords.
 * <p>
 * This class acts as an outbound adapter in a hexagonal architecture, providing
 * strong temporary credentials for systems like PostgreSQL, cloud resources,
 * or ephemeral environments.
 *
 * <h3>Username format</h3>
 * - 12 characters long<br>
 * - Lowercase letters and digits only<br>
 * - Example: {@code ab3kz9t1qv8d}
 *
 * <h3>Password format</h3>
 * - 16 characters long<br>
 * - Randomly generated from a secure character set<br>
 * - Includes at least one of each:
 *   <ul>
 *     <li>Uppercase letter (A–Z)</li>
 *     <li>Lowercase letter (a–z)</li>
 *     <li>Digit (0–9)</li>
 *     <li>Special character ({@code !@#$%^&*()-_+=<>?})</li>
 *   </ul>
 * - Must contain at least 10 unique characters
 * - Guaranteed to pass password strength and entropy criteria
 * - Example: {@code G7$pxR!dKmZ&20#b}
 *
 * <h3>Security details</h3>
 * - Uses {@link SecureRandom} for strong cryptographic randomness
 * - Designed to achieve high Shannon entropy (typically ≥ 60 bits per password)
 * - Safe against brute-force and pattern-predictable generation
 *
 * <h3>Usage example</h3>
 * <pre>{@code
 * UserCredentialsGenerator generator = new SecureUserCredentialsGenerator();
 * TemporaryCredentials creds = generator.generate();
 * System.out.println(creds.username()); // e.g. "user7g5m9dzq"
 * System.out.println(creds.password()); // e.g. "G7$pxR!dKmZ&20#b"
 * }</pre>
 *
 * @see TemporaryCredentials
 * @see UserCredentialsGenerator
 * @author Kamil Jędrzejuk
 */
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
        var username = generateUsername();
        var password = generateSecurePassword();
        return new TemporaryCredentials(username, password);
    }

    private String generateUsername() {
        var allowed = LOWER + DIGITS;
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
          && password.chars().distinct().count() >= 12;
    }

    private String generateRandomString(String characterSet, int length) {
        var sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(randomChar(characterSet));
        }
        return sb.toString();
    }

    private char randomChar(String set) {
        return set.charAt(random.nextInt(set.length()));
    }
}
