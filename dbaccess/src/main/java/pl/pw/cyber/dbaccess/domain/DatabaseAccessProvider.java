package pl.pw.cyber.dbaccess.domain;

public interface DatabaseAccessProvider {

    void createTemporaryUser(CreateTemporaryUserRequest request);

    void revokeTemporaryUser(String username, String targetDatabase);
}
