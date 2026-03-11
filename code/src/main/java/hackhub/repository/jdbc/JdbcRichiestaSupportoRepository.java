package hackhub.repository.jdbc;

import hackhub.exception.PersistenceException;
import hackhub.infrastructure.db.DBConnection;
import hackhub.model.entity.RichiestaSupporto;
import hackhub.repository.RichiestaSupportoRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcRichiestaSupportoRepository implements RichiestaSupportoRepository {

    private final DBConnection dbConnection;

    public JdbcRichiestaSupportoRepository(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void save(RichiestaSupporto richiesta) {
        String sql = """
                INSERT INTO richiesta_supporto
                    (id_team, id_hackathon, id_mentore, motivo, data_invio)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, richiesta.getIdTeam());
            stmt.setObject(2, richiesta.getIdHackathon());
            stmt.setObject(3, richiesta.getIdMentore());
            stmt.setString(4, richiesta.getMotivo());
            stmt.setTimestamp(5, Timestamp.valueOf(richiesta.getDataInvio()));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                richiesta.setId((UUID) rs.getObject("id"));
            }

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Errore durante il salvataggio della richiesta di supporto: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<RichiestaSupporto> findById(UUID id) {
        String sql = "SELECT * FROM richiesta_supporto WHERE id = ?";

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
                    "Errore durante il recupero della richiesta di supporto con id " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<RichiestaSupporto> findByHackathon(UUID idHackathon) {
        String sql = "SELECT * FROM richiesta_supporto WHERE id_hackathon = ? ORDER BY data_invio DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idHackathon);
            ResultSet rs = stmt.executeQuery();

            List<RichiestaSupporto> risultato = new ArrayList<>();
            while (rs.next()) {
                risultato.add(mapRow(rs));
            }
            return risultato;

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Errore durante il recupero delle richieste di supporto per hackathon " + idHackathon + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<RichiestaSupporto> findByTeam(UUID idTeam) {
        String sql = "SELECT * FROM richiesta_supporto WHERE id_team = ? ORDER BY data_invio DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idTeam);
            ResultSet rs = stmt.executeQuery();

            List<RichiestaSupporto> risultato = new ArrayList<>();
            while (rs.next()) {
                risultato.add(mapRow(rs));
            }
            return risultato;

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Errore durante il recupero delle richieste di supporto per team " + idTeam + ": " + e.getMessage(), e);
        }
    }

    private RichiestaSupporto mapRow(ResultSet rs) throws SQLException {
        return new RichiestaSupporto(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("id_team"),
                (UUID) rs.getObject("id_hackathon"),
                (UUID) rs.getObject("id_mentore"),
                rs.getString("motivo"),
                rs.getTimestamp("data_invio").toLocalDateTime()
        );
    }
}
