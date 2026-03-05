package hackhub.repository.jdbc;

import hackhub.infrastructure.db.DBConnection;
import hackhub.model.entity.Valutazione;
import hackhub.repository.ValutazioneRepository;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class JdbcValutazioneRepository implements ValutazioneRepository {

    private final DBConnection dbConnection;

    public JdbcValutazioneRepository(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public Optional<Valutazione> findById(UUID valutazioneId) {
        String sql = "SELECT * FROM valutazione WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, valutazioneId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante il recupero della valutazione con id " + valutazioneId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsBySottomissione(UUID idSottomissione) {
        String sql = "SELECT 1 FROM valutazione WHERE id_sottomissione = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idSottomissione);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante la verifica valutazione per sottomissione " + idSottomissione + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Valutazione valutazione) {
        String sql = """
                INSERT INTO valutazione (voto, giudizio_scritto, id_sottomissione, id_giudice)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, valutazione.getPunteggio());
            stmt.setString(2, valutazione.getGiudizioScritto());
            stmt.setObject(3, valutazione.getIdSottomissione());
            stmt.setObject(4, valutazione.getIdGiudice());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                valutazione.setId((UUID) rs.getObject("id"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante il salvataggio della valutazione: " + e.getMessage(), e);
        }
    }

    private Valutazione mapRow(ResultSet rs) throws SQLException {
        return new Valutazione(
                (UUID) rs.getObject("id"),
                rs.getDouble("voto"),
                rs.getString("giudizio_scritto"),
                (UUID) rs.getObject("id_sottomissione"),
                (UUID) rs.getObject("id_giudice")
        );
    }
}
