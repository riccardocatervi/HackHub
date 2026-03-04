package hackhub.repository.jdbc;

import hackhub.infrastructure.db.DBConnection;
import hackhub.model.entity.Segnalazione;
import hackhub.repository.SegnalazioneRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcSegnalazioneRepository implements SegnalazioneRepository {

    private final DBConnection dbConnection;

    public JdbcSegnalazioneRepository(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void save(Segnalazione segnalazione) {
        String sql = """
                INSERT INTO segnalazione (id_team, id_mentore, id_hackathon, descrizione, prove)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, segnalazione.getTeamId());
            stmt.setObject(2, segnalazione.getMentoreId());
            stmt.setObject(3, segnalazione.getHackathonId());
            stmt.setString(4, segnalazione.getDescrizione());
            stmt.setString(5, segnalazione.getProve());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                segnalazione.setId((UUID) rs.getObject("id"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante il salvataggio della segnalazione: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Segnalazione> findById(UUID segnalazioneId) {
        String sql = "SELECT * FROM segnalazione WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, segnalazioneId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante il recupero della segnalazione con id " + segnalazioneId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Segnalazione> findByHackathon(UUID idHackathon) {
        String sql = "SELECT * FROM segnalazione WHERE id_hackathon = ? ORDER BY data_invio DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idHackathon);
            ResultSet rs = stmt.executeQuery();

            List<Segnalazione> risultato = new ArrayList<>();
            while (rs.next()) {
                risultato.add(mapRow(rs));
            }
            return risultato;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante il recupero delle segnalazioni per hackathon " + idHackathon + ": " + e.getMessage(), e);
        }
    }

    private Segnalazione mapRow(ResultSet rs) throws SQLException {
        return new Segnalazione(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("id_team"),
                (UUID) rs.getObject("id_mentore"),
                (UUID) rs.getObject("id_hackathon"),
                rs.getString("descrizione"),
                rs.getString("prove"),
                rs.getTimestamp("data_invio").toLocalDateTime()
        );
    }
}
