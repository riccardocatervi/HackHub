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

    @Override
    public List<Mentore> findAllAvailable(UUID idHackathon) {
        String sql = """
                SELECT m.*
                FROM mentore m
                WHERE m.id NOT IN (
                    SELECT hm.id_mentore
                    FROM hackathon_mentori hm
                    WHERE hm.id_hackathon = ?
                )
                ORDER BY m.cognome, m.nome
                """;

        List<Mentore> mentori = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idHackathon);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                mentori.add(mapRow(rs));
            }
            return mentori;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante il recupero dei mentori disponibili per l'hackathon " +
                            idHackathon + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Mentore> findAllByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        // Costruisce i placeholder per la clausola IN
        String placeholders = ids.stream()
                .map(id -> "?")
                .reduce((a, b) -> a + ", " + b)
                .orElse("?");

        String sql = "SELECT * FROM mentore WHERE id IN (" + placeholders + ") ORDER BY cognome, nome";
        List<Mentore> mentori = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < ids.size(); i++) {
                stmt.setObject(i + 1, ids.get(i));
            }
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                mentori.add(mapRow(rs));
            }
            return mentori;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante il recupero dei mentori per id: " + e.getMessage(), e);
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
