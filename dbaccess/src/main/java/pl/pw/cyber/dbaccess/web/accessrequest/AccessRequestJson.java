package pl.pw.cyber.dbaccess.web.accessrequest;

import io.swagger.v3.oas.annotations.media.Schema;

public record AccessRequestJson(
  @Schema(example = "READ_ONLY", description = "The level of access permission requested")
  String permissionLevel,

  @Schema(example = "10", description = "Duration of access in minutes")
  int durationMinutes,

  @Schema(example = "test1", description = "Target database name")
  String targetDatabase
) {}
