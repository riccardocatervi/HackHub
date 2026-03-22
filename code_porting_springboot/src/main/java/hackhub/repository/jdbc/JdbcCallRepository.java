package hackhub.repository.jdbc;

import hackhub.exception.PersistenceException;
import org.springframework.stereotype.Repository;
import hackhub.infrastructure.db.DBConnection;
import hackhub.model.StatoCall;
import hackhub.model.entity.Call;
import hackhub.repository.CallRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementazione JDBC del repository per le call pianificate.
 * Persiste i dati nella tabella {@code call_pianificata}.
 */
@Repository
public class JdbcCallRepository implements CallRepository {

    private final DBConnection dbConnection;

    public JdbcCallRepository(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void save(Call call) {
        String sql = """
                INSERT INTO call_pianificata
                    (id_richiesta_supporto, data_call, ora_call, link_call, descrizione, data_creazione, stato)
                VALUES (?, ?, ?, ?, ?, ?, ?::stato_call)
                RETURNING id
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, call.getIdRichiestaSupporto());
            stmt.setDate(2, Date.valueOf(call.getDataCall()));
            stmt.setTime(3, Time.valueOf(call.getOraCall()));
            stmt.setString(4, call.getLinkCall());
            stmt.setString(5, call.getDescrizione());
            stmt.setTimestamp(6, Timestamp.valueOf(call.getDataCreazione()));
            stmt.setString(7, call.getStato() != null ? call.getStato().name() : StatoCall.PENDENTE.name());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                call.setId((UUID) rs.getObject("id"));
            }

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Errore durante il salvataggio della call pianificata: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Call> findById(UUID id) {
        String sql = "SELECT * FROM call_pianificata WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Errore durante il recupero della call con id " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Call> findByRichiesta(UUID idRichiesta) {
        String sql = "SELECT * FROM call_pianificata WHERE id_richiesta_supporto = ? ORDER BY data_creazione DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idRichiesta);
            ResultSet rs = stmt.executeQuery();

            List<Call> risultato = new ArrayList<>();
            while (rs.next()) {
                risultato.add(mapRow(rs));
            }
            return risultato;

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Errore durante il recupero delle call per la richiesta " + idRichiesta + ": " + e.getMessage(), e);
        }
    }

    // -----------------------------------------------------------------------
    // Caso d'uso: Gestire invito a call da parte di un mentore (it.5)
    // -----------------------------------------------------------------------

    @Override
    public void updateStato(UUID idCall, StatoCall nuovoStato) {
        String sql = "UPDATE call_pianificata SET stato = ?::stato_call WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuovoStato.name());
            stmt.setObject(2, idCall);
            int righeAggiornate = stmt.executeUpdate();

            if (righeAggiornate == 0) {
                throw new PersistenceException(
                        "Aggiornamento stato call fallito: nessuna riga modificata per id " + idCall, null);
            }

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Errore durante l'aggiornamento dello stato della call " + idCall + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Call> findByTeamId(UUID idTeam) {
        String sql = """
                SELECT cp.*
                FROM call_pianificata cp
                JOIN richiesta_supporto rs ON rs.id = cp.id_richiesta_supporto
                WHERE rs.id_team = ?
                ORDER BY cp.data_call ASC, cp.ora_call ASC
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idTeam);
            ResultSet rs = stmt.executeQuery();

            List<Call> risultato = new ArrayList<>();
            while (rs.next()) {
                risultato.add(mapRow(rs));
            }
            return risultato;

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Errore durante il recupero delle call per il team " + idTeam + ": " + e.getMessage(), e);
        }
    }

    // -----------------------------------------------------------------------
    // Mapping
    // -----------------------------------------------------------------------

    private Call mapRow(ResultSet rs) throws SQLException {
        StatoCall stato = StatoCall.PENDENTE;
        try {
            String statoStr = rs.getString("stato");
            if (statoStr != null) {
                stato = StatoCall.valueOf(statoStr);
            }
        } catch (SQLException ignored) {
            // Colonna non presente in query parziali: usa il valore di default
        }

        return new Call(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("id_richiesta_supporto"),
                rs.getDate("data_call").toLocalDate(),
                rs.getTime("ora_call").toLocalTime(),
                rs.getString("link_call"),
                rs.getString("descrizione"),
                rs.getTimestamp("data_creazione").toLocalDateTime(),
                stato
        );
    }
}
