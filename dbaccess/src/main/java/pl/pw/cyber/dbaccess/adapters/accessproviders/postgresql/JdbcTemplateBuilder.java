package pl.pw.cyber.dbaccess.adapters.accessproviders.postgresql;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import pl.pw.cyber.dbaccess.domain.ResolvedDatabase;

class JdbcTemplateBuilder {

   static NamedParameterJdbcTemplate from(ResolvedDatabase resolvedDatabase) {
        return new NamedParameterJdbcTemplate(DataSourceBuilder.from(resolvedDatabase));
    }
}
