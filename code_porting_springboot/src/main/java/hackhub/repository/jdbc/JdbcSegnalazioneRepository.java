package hackhub.repository.jdbc;

import hackhub.exception.PersistenceException;
import org.springframework.stereotype.Repository;
import hackhub.infrastructure.db.DBConnection;
import hackhub.model.StatoSegnalazione;
import hackhub.model.entity.Segnalazione;
import hackhub.repository.SegnalazioneRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcSegnalazioneRepository implements SegnalazioneRepository {

    private final DBConnection dbConnection;

    public JdbcSegnalazioneRepository(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void save(Segnalazione segnalazione) {
        String sql = """
                INSERT INTO segnalazione (id_team, id_mentore, id_hackathon, descrizione, prove, stato)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, segnalazione.getTeamId());
            stmt.setObject(2, segnalazione.getMentoreId());
            stmt.setObject(3, segnalazione.getHackathonId());
            stmt.setString(4, segnalazione.getDescrizione());
            stmt.setString(5, segnalazione.getProve());
            stmt.setString(6, segnalazione.getStato().name());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                segnalazione.setId((UUID) rs.getObject("id"));
            }

        } catch (SQLException e) {
            throw new PersistenceException(
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
            throw new PersistenceException(
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
            throw new PersistenceException(
                    "Errore durante il recupero delle segnalazioni per hackathon " + idHackathon + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void updateStato(UUID idSegnalazione, StatoSegnalazione stato) {
        String sql = "UPDATE segnalazione SET stato = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, stato.name());
            stmt.setObject(2, idSegnalazione);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Errore durante l'aggiornamento dello stato della segnalazione " + idSegnalazione + ": " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    private Segnalazione mapRow(ResultSet rs) throws SQLException {
        String statoStr = rs.getString("stato");
        StatoSegnalazione stato = (statoStr != null)
                ? StatoSegnalazione.valueOf(statoStr)
                : StatoSegnalazione.PENDENTE;

        return new Segnalazione(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("id_team"),
                (UUID) rs.getObject("id_mentore"),
                (UUID) rs.getObject("id_hackathon"),
                rs.getString("descrizione"),
                rs.getString("prove"),
                rs.getTimestamp("data_invio").toLocalDateTime(),
                stato
        );
    }
}
