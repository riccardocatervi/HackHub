package hackhub.repository.jdbc;

import hackhub.infrastructure.db.DBConnection;
import hackhub.model.StatoInvito;
import hackhub.model.entity.Invito;
import hackhub.repository.InvitoRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcInvitoRepository implements InvitoRepository {

    private final DBConnection dbConnection;

    public JdbcInvitoRepository(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public Optional<Invito> findById(UUID id) {
        String sql = "SELECT * FROM invito WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dell'invito con id " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Invito invito) {
        String sql = """
                INSERT INTO invito (id_team, id_utente, id_hackathon, stato, data_invio)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, invito.getIdTeam());
            stmt.setObject(2, invito.getIdUtente());
            stmt.setObject(3, invito.getIdHackathon());
            stmt.setString(4, invito.getStato().name());
            stmt.setTimestamp(5, Timestamp.valueOf(invito.getDataInvio()));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                invito.setId((UUID) rs.getObject("id"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio dell'invito: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Invito> findByTeam(UUID idTeam) {
        String sql = "SELECT * FROM invito WHERE id_team = ? ORDER BY data_invio DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idTeam);
            ResultSet rs = stmt.executeQuery();

            List<Invito> inviti = new ArrayList<>();
            while (rs.next()) {
                inviti.add(mapRow(rs));
            }
            return inviti;

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero degli inviti del team " + idTeam + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Invito> findByTeamAndStato(UUID idTeam, StatoInvito stato) {
        String sql = "SELECT * FROM invito WHERE id_team = ? AND stato = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idTeam);
            stmt.setString(2, stato.name());
            ResultSet rs = stmt.executeQuery();

            List<Invito> inviti = new ArrayList<>();
            while (rs.next()) {
                inviti.add(mapRow(rs));
            }
            return inviti;

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero degli inviti per stato: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Invito> findByUtenteAndHackathon(UUID idUtente, UUID idHackathon) {
        String sql = "SELECT * FROM invito WHERE id_utente = ? AND id_hackathon = ? ORDER BY data_invio DESC LIMIT 1";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idUtente);
            stmt.setObject(2, idHackathon);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca dell'invito per utente/hackathon: " + e.getMessage(), e);
        }
    }

    @Override
    public void aggiornaStato(UUID idInvito, StatoInvito nuovoStato) {
        String sql = "UPDATE invito SET stato = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuovoStato.name());
            stmt.setObject(2, idInvito);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento dello stato dell'invito: " + e.getMessage(), e);
        }
    }

    @Override
    public int countAccettati(UUID idTeam) {
        String sql = "SELECT COUNT(*) FROM invito WHERE id_team = ? AND stato = 'ACCETTATO'";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idTeam);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il conteggio degli inviti accettati: " + e.getMessage(), e);
        }
    }

    private Invito mapRow(ResultSet rs) throws SQLException {
        return new Invito(
                (UUID) rs.getObject("id"),
                (UUID) rs.getObject("id_team"),
                (UUID) rs.getObject("id_utente"),
                (UUID) rs.getObject("id_hackathon"),
                StatoInvito.valueOf(rs.getString("stato")),
                rs.getTimestamp("data_invio").toLocalDateTime()
        );
    }
}
