package pl.pw.cyber.dbaccess.web.accessrequest;

public record AccessRequestJson(String permissionLevel, int durationMinutes, String targetDatabase) {
}
