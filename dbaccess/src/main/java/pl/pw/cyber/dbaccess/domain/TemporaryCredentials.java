package pl.pw.cyber.dbaccess.domain;

import java.util.regex.Pattern;

public record TemporaryCredentials(
  String username,
  String password
) {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9]{6,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{12,}$");

    public TemporaryCredentials {
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Invalid username. It must be at least 6 characters, lowercase letters and digits only.");
        }

        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("Invalid password. It must be at least 12 characters and include upper/lowercase letters, a digit, and a special character.");
        }
    }
}
