package hackhub.infrastructure.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Incapsula i parametri di connessione JDBC a PostgreSQL.
 * Progettato per essere sostituito da un DataSource Spring nella migrazione futura.
 */
public class DBConnection {

    private final String url;
    private final String username;
    private final String password;

    public DBConnection(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Restituisce una nuova connessione fisica al database.
     * Il chiamante è responsabile della chiusura della connessione (try-with-resources).
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
