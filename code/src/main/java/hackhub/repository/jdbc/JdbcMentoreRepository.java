package hackhub.repository.jdbc;

import hackhub.infrastructure.db.DBConnection;
import hackhub.model.entity.Mentore;
import hackhub.repository.MentoreRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcMentoreRepository implements MentoreRepository {

    private final DBConnection dbConnection;

    public JdbcMentoreRepository(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public List<Mentore> findAllDisponibili() {
        String sql = "SELECT * FROM mentore WHERE disponibile = true ORDER BY cognome, nome";
        List<Mentore> mentori = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                mentori.add(mapRow(rs));
            }
            return mentori;

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dei mentori disponibili: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Mentore> findById(UUID id) {
        String sql = "SELECT * FROM mentore WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero del mentore con id " + id + ": " + e.getMessage(), e);
        }
    }

    private Mentore mapRow(ResultSet rs) throws SQLException {
        return new Mentore(
                (UUID) rs.getObject("id"),
                rs.getString("nome"),
                rs.getString("cognome"),
                rs.getString("email"),
                rs.getBoolean("disponibile")
        );
    }
}
