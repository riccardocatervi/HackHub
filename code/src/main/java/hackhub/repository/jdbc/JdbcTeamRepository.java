package hackhub.repository.jdbc;

import hackhub.exception.PersistenceException;
import hackhub.infrastructure.db.DBConnection;
import hackhub.model.entity.MembroTeam;
import hackhub.model.entity.Team;
import hackhub.repository.TeamRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
            throw new PersistenceException("Errore durante il recupero del team con id " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Team team) {
        String sql = """
                INSERT INTO team (nome, descrizione, id_leader, id_hackathon)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, team.getNome());
            stmt.setString(2, team.getDescrizione());
            stmt.setObject(3, team.getIdLeader());
            stmt.setObject(4, team.getIdHackathon());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                team.setId((UUID) rs.getObject("id"));
            }

        } catch (SQLException e) {
            throw new PersistenceException("Errore durante il salvataggio del team: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Team> findByHackathonAndMembro(UUID idHackathon, UUID idUtente) {
        String sql = """
                SELECT t.*
                FROM team t
                JOIN membro_team mt ON mt.id_team = t.id
                WHERE t.id_hackathon = ? AND mt.id_utente = ?
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idHackathon);
            stmt.setObject(2, idUtente);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new PersistenceException("Errore durante la ricerca del team per membro: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Team> findByHackathon(UUID idHackathon) {
        String sql = "SELECT * FROM team WHERE id_hackathon = ? ORDER BY nome ASC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idHackathon);
            ResultSet rs = stmt.executeQuery();

            List<Team> risultato = new ArrayList<>();
            while (rs.next()) {
                risultato.add(mapRow(rs));
            }
            return risultato;

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Errore durante il recupero dei team per hackathon " + idHackathon + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void addMembro(UUID idTeam, UUID idUtente) {
        String sql = "INSERT INTO membro_team (id_utente, id_team) VALUES (?, ?) ON CONFLICT DO NOTHING";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idUtente);
            stmt.setObject(2, idTeam);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Errore durante l'aggiunta del membro al team " + idTeam + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<MembroTeam> findMembri(UUID idTeam) {
        String sql = """
                SELECT u.id AS id_utente, u.nome, u.cognome, u.email
                FROM membro_team mt
                JOIN utente u ON u.id = mt.id_utente
                WHERE mt.id_team = ?
                ORDER BY u.cognome, u.nome
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idTeam);
            ResultSet rs = stmt.executeQuery();

            List<MembroTeam> membri = new ArrayList<>();
            while (rs.next()) {
                membri.add(new MembroTeam(
                        (UUID) rs.getObject("id_utente"),
                        idTeam,
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getString("email")
                ));
            }
            return membri;

        } catch (SQLException e) {
            throw new PersistenceException("Errore durante il recupero dei membri del team " + idTeam + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void updateSqualificato(UUID idTeam, boolean squalificato) {
        String sql = "UPDATE team SET squalificato = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, squalificato);
            stmt.setObject(2, idTeam);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Errore durante l'aggiornamento del flag squalificato per il team " + idTeam + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Team> findByMembro(UUID idMembro) {
        String sql = """
                SELECT t.*
                FROM team t
                JOIN membro_team mt ON mt.id_team = t.id
                WHERE mt.id_utente = ?
                ORDER BY t.id DESC
                LIMIT 1
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idMembro);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Errore durante la ricerca del team per membro " + idMembro + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void removeMembro(UUID idTeam, UUID idMembro) {
        String sql = "DELETE FROM membro_team WHERE id_team = ? AND id_utente = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idTeam);
            stmt.setObject(2, idMembro);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Errore durante la rimozione del membro " + idMembro + " dal team " + idTeam + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void updateLeader(UUID idTeam, UUID nuovoLeaderId) {
        String sql = "UPDATE team SET id_leader = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, nuovoLeaderId);
            stmt.setObject(2, idTeam);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PersistenceException(
                    "Errore durante l'aggiornamento del leader del team " + idTeam + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Team team) {
        String sqlMembri = "DELETE FROM membro_team WHERE id_team = ?";
        String sqlTeam = "DELETE FROM team WHERE id = ?";

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmtMembri = conn.prepareStatement(sqlMembri)) {
                    stmtMembri.setObject(1, team.getId());
                    stmtMembri.executeUpdate();
                }
                try (PreparedStatement stmtTeam = conn.prepareStatement(sqlTeam)) {
                    stmtTeam.setObject(1, team.getId());
                    stmtTeam.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new PersistenceException(
                    "Errore durante l'eliminazione del team " + team.getId() + ": " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    private Team mapRow(ResultSet rs) throws SQLException {
        Team team = new Team(
                (UUID) rs.getObject("id"),
                rs.getString("nome"),
                rs.getString("descrizione"),
                (UUID) rs.getObject("id_leader"),
                (UUID) rs.getObject("id_hackathon")
        );
        // Il flag squalificato viene letto se la colonna esiste nel result set
        try {
            team.setSqualificato(rs.getBoolean("squalificato"));
        } catch (SQLException ignored) {
            // La colonna potrebbe non essere presente in query parziali (JOIN senza SELECT *)
        }
        return team;
    }
}
