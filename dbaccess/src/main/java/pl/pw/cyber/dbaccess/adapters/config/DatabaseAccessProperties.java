package pl.pw.cyber.dbaccess.adapters.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "db")
public record DatabaseAccessProperties(
  Map<String, DataSourceDefinition> databases
) {
    public record DataSourceDefinition(String envPrefix) {}
}
