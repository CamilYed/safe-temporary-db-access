package pl.pw.cyber.dbaccess.domain;

public record ResolvedDatabase(
  String name,
  String url,
  String username,
  String password
) {}
