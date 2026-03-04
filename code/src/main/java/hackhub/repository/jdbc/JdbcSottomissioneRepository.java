package hackhub.repository.jdbc;

import hackhub.infrastructure.db.DBConnection;
import hackhub.model.entity.Sottomissione;
import hackhub.repository.SottomissioneRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcSottomissioneRepository implements SottomissioneRepository {

    private final DBConnection dbConnection;

    public JdbcSottomissioneRepository(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public Optional<Sottomissione> findById(UUID id) {
        String sql = "SELECT * FROM sottomissione WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante il recupero della sottomissione con id " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Sottomissione s) {
        String sql = """
                INSERT INTO sottomissione
                    (link_repo, link_demo, descrizione, data_invio, id_team, id_hackathon, vincitore, valutato)
                VALUES (?, ?, ?, ?, ?, ?, false, false)
                RETURNING id
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, s.getLinkRepo());
            stmt.setString(2, s.getLinkDemo());
            stmt.setString(3, s.getDescrizione());
            stmt.setTimestamp(4, Timestamp.valueOf(s.getDataInvio()));
            stmt.setObject(5, s.getIdTeam());
            stmt.setObject(6, s.getIdHackathon());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                s.setId((UUID) rs.getObject("id"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio della sottomissione: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByHackathonAndTeam(UUID idHackathon, UUID idTeam) {
        String sql = "SELECT 1 FROM sottomissione WHERE id_hackathon = ? AND id_team = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idHackathon);
            stmt.setObject(2, idTeam);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la verifica duplicato sottomissione: " + e.getMessage(), e);
        }
    }

    @Override
    public long countNonValutate(UUID idHackathon) {
        String sql = "SELECT COUNT(*) FROM sottomissione WHERE id_hackathon = ? AND valutato = false";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idHackathon);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getLong(1) : 0L;

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il conteggio delle sottomissioni non valutate: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Sottomissione> findVincitore(UUID idHackathon) {
        String sql = """
                SELECT s.*
                FROM sottomissione s
                JOIN valutazione v ON v.id_sottomissione = s.id
                WHERE s.id_hackathon = ?
                ORDER BY v.voto DESC
                LIMIT 1
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idHackathon);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca della sottomissione vincitrice: " + e.getMessage(), e);
        }
    }

    @Override
    public void markAsVincitore(UUID idSottomissione) {
        String sql = "UPDATE sottomissione SET vincitore = true WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idSottomissione);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento del flag vincitore: " + e.getMessage(), e);
        }
    }

    @Override
    public void markAsValutata(UUID idSottomissione) {
        String sql = "UPDATE sottomissione SET valutato = true WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idSottomissione);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento del flag valutato: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Sottomissione> findSottomissioniDaValutare(UUID idHackathon, UUID idGiudice) {
        // Restituisce sottomissioni non ancora valutate per l'hackathon.
        // idGiudice usato per futuri controlli di assegnazione (non implementato in questa iterazione).
        String sql = "SELECT * FROM sottomissione WHERE id_hackathon = ? AND valutato = false";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idHackathon);
            ResultSet rs = stmt.executeQuery();

            List<Sottomissione> risultato = new ArrayList<>();
            while (rs.next()) {
                risultato.add(mapRow(rs));
            }
            return risultato;

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero delle sottomissioni da valutare: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Sottomissione> findAllByHackathon(UUID idHackathon) {
        String sql = "SELECT * FROM sottomissione WHERE id_hackathon = ? ORDER BY data_invio ASC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idHackathon);
            ResultSet rs = stmt.executeQuery();

            List<Sottomissione> risultato = new ArrayList<>();
            while (rs.next()) {
                risultato.add(mapRow(rs));
            }
            return risultato;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante il recupero di tutte le sottomissioni per hackathon " + idHackathon + ": " + e.getMessage(), e);
        }
    }

    private Sottomissione mapRow(ResultSet rs) throws SQLException {
        return new Sottomissione(
                (UUID) rs.getObject("id"),
                rs.getString("link_repo"),
                rs.getString("link_demo"),
                rs.getString("descrizione"),
                rs.getTimestamp("data_invio").toLocalDateTime(),
                (UUID) rs.getObject("id_team"),
                (UUID) rs.getObject("id_hackathon"),
                rs.getBoolean("vincitore"),
                rs.getBoolean("valutato")
        );
    }
}
