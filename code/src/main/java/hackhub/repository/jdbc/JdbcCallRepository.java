package hackhub.repository.jdbc;

import hackhub.exception.PersistenceException;
import hackhub.infrastructure.db.DBConnection;
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
public class JdbcCallRepository implements CallRepository {

    private final DBConnection dbConnection;

    public JdbcCallRepository(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void save(Call call) {
        String sql = """
                INSERT INTO call_pianificata
                    (id_richiesta_supporto, data_call, ora_call, link_call, descrizione, data_creazione)
                VALUES (?, ?, ?, ?, ?, ?)
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

    private Call mapRow(ResultSet rs) throws SQLException {
        return new Call(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("id_richiesta_supporto"),
                rs.getDate("data_call").toLocalDate(),
                rs.getTime("ora_call").toLocalTime(),
                rs.getString("link_call"),
                rs.getString("descrizione"),
                rs.getTimestamp("data_creazione").toLocalDateTime()
        );
    }
}
