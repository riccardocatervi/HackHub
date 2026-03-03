package hackhub.repository.jdbc;

import hackhub.infrastructure.db.DBConnection;
import hackhub.model.entity.Team;
import hackhub.repository.TeamRepository;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class JdbcTeamRepository implements TeamRepository {

    private final DBConnection dbConnection;

    public JdbcTeamRepository(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public Optional<Team> findById(UUID id) {
        String sql = "SELECT * FROM team WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero del team con id " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Team> findByHackathonAndMembro(UUID idHackathon, UUID idMembro) {
        String sql = """
            SELECT t.*
            FROM team t
            JOIN membro_team mt ON mt.id_team = t.id
            WHERE t.id_hackathon = ? AND mt.id = ?
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idHackathon);
            stmt.setObject(2, idMembro);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca del team per membro: " + e.getMessage(), e);
        }
    }

    private Team mapRow(ResultSet rs) throws SQLException {
        return new Team(
            (UUID) rs.getObject("id"),
            rs.getString("nome"),
            rs.getString("descrizione"),
            (UUID) rs.getObject("id_leader"),
            (UUID) rs.getObject("id_hackathon")
        );
    }
}
