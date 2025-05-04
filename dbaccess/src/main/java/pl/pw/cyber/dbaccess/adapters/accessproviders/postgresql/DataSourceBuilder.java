package pl.pw.cyber.dbaccess.adapters.accessproviders.postgresql;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import pl.pw.cyber.dbaccess.domain.ResolvedDatabase;

import javax.sql.DataSource;

class DataSourceBuilder {

   static DataSource from(ResolvedDatabase resolvedDatabase) {
        return new DriverManagerDataSource(
          resolvedDatabase.url(),
          resolvedDatabase.username(),
          resolvedDatabase.password()
        );
    }

}
