package hackhub.repository.jdbc;

import hackhub.infrastructure.db.DBConnection;
import hackhub.model.entity.Organizzatore;
import hackhub.repository.OrganizzatoreRepository;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class JdbcOrganizzatoreRepository implements OrganizzatoreRepository {

    private final DBConnection dbConnection;

    public JdbcOrganizzatoreRepository(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public Optional<Organizzatore> findById(UUID id) {
        String sql = "SELECT * FROM organizzatore WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dell'organizzatore con id " + id + ": " + e.getMessage(), e);
        }
    }

    private Organizzatore mapRow(ResultSet rs) throws SQLException {
        return new Organizzatore(
            (UUID) rs.getObject("id"),
            rs.getString("nome"),
            rs.getString("cognome"),
            rs.getString("email")
        );
    }
}
