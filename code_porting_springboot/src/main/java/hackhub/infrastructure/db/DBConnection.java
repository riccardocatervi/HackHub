package hackhub.infrastructure.db;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Incapsula l'accesso al DataSource gestito da Spring Boot (HikariCP).
 * Fornisce connessioni JDBC ai repository che le richiedono tramite try-with-resources.
 */
@Component
public class DBConnection {

    private final DataSource dataSource;

    public DBConnection(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Restituisce una connessione dal pool gestito dal DataSource.
     * Il chiamante è responsabile della chiusura della connessione (try-with-resources).
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
