package hackhub.repository.jdbc;

import hackhub.infrastructure.db.DBConnection;
import hackhub.model.entity.Giudice;
import hackhub.repository.GiudiceRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcGiudiceRepository implements GiudiceRepository {

    private final DBConnection dbConnection;

    public JdbcGiudiceRepository(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public List<Giudice> findAllDisponibili() {
        String sql = "SELECT * FROM giudice WHERE disponibile = true ORDER BY cognome, nome";
        List<Giudice> giudici = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                giudici.add(mapRow(rs));
            }
            return giudici;

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dei giudici disponibili: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Giudice> findById(UUID id) {
        String sql = "SELECT * FROM giudice WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero del giudice con id " + id + ": " + e.getMessage(), e);
        }
    }

    private Giudice mapRow(ResultSet rs) throws SQLException {
        return new Giudice(
                (UUID) rs.getObject("id"),
                rs.getString("nome"),
                rs.getString("cognome"),
                rs.getString("email"),
                rs.getBoolean("disponibile")
        );
    }
}
