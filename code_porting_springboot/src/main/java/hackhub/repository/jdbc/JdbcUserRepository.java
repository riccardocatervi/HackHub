package hackhub.repository.jdbc;

import hackhub.infrastructure.db.DBConnection;
import org.springframework.stereotype.Repository;
import hackhub.model.OAuthProvider;
import hackhub.model.entity.Utente;
import hackhub.repository.UserRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcUserRepository implements UserRepository {

    private final DBConnection dbConnection;

    public JdbcUserRepository(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public Optional<Utente> findById(UUID id) {
        String sql = "SELECT * FROM utente WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dell'utente con id " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Utente> findByEmail(String email) {
        String sql = "SELECT * FROM utente WHERE email = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dell'utente per email: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM utente WHERE email = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la verifica email: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Utente> findByOAuth(OAuthProvider provider, String externalId) {
        String sql = "SELECT * FROM utente WHERE oauth_provider = ? AND oauth_external_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, provider.name());
            stmt.setString(2, externalId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dell'utente OAuth: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Utente> findAvailableForHackathon(UUID idHackathon) {
        // Restituisce utenti che non sono membri accettati di nessun team per l'hackathon
        String sql = """
                SELECT u.*
                FROM utente u
                WHERE u.id NOT IN (
                    SELECT mt.id_utente
                    FROM membro_team mt
                    JOIN team t ON t.id = mt.id_team
                    WHERE t.id_hackathon = ?
                )
                ORDER BY u.cognome, u.nome
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idHackathon);
            ResultSet rs = stmt.executeQuery();

            List<Utente> disponibili = new ArrayList<>();
            while (rs.next()) {
                disponibili.add(mapRow(rs));
            }
            return disponibili;

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero degli utenti disponibili: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isUserAvailable(UUID idUtente, UUID idHackathon) {
        String sql = """
                SELECT NOT EXISTS (
                    SELECT 1
                    FROM membro_team mt
                    JOIN team t ON t.id = mt.id_team
                    WHERE mt.id_utente = ? AND t.id_hackathon = ?
                )
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idUtente);
            stmt.setObject(2, idHackathon);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getBoolean(1);

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la verifica disponibilità utente: " + e.getMessage(), e);
        }
    }

    @Override
    public void salva(Utente utente) {
        String sql = """
                INSERT INTO utente (nome, cognome, email, hashed_password, oauth_provider, oauth_external_id)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utente.getNome());
            stmt.setString(2, utente.getCognome());
            stmt.setString(3, utente.getEmail());
            stmt.setString(4, utente.getHashedPassword());

            if (utente.getOauthProvider() != null) {
                stmt.setString(5, utente.getOauthProvider().name());
                stmt.setString(6, utente.getOauthExternalId());
            } else {
                stmt.setNull(5, Types.VARCHAR);
                stmt.setNull(6, Types.VARCHAR);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                utente.setId((UUID) rs.getObject("id"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio dell'utente: " + e.getMessage(), e);
        }
    }

    private Utente mapRow(ResultSet rs) throws SQLException {
        String providerStr = rs.getString("oauth_provider");
        OAuthProvider provider = (providerStr != null) ? OAuthProvider.valueOf(providerStr) : null;

        return new Utente(
                (UUID) rs.getObject("id"),
                rs.getString("nome"),
                rs.getString("cognome"),
                rs.getString("email"),
                rs.getString("hashed_password"),
                provider,
                rs.getString("oauth_external_id")
        );
    }
}
