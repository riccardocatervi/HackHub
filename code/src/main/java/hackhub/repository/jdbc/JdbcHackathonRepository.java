package hackhub.repository.jdbc;

import hackhub.infrastructure.db.DBConnection;
import hackhub.model.entity.Address;
import hackhub.model.entity.Hackathon;
import hackhub.model.state.StatoHackathon;
import hackhub.repository.HackathonRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcHackathonRepository implements HackathonRepository {

    private final DBConnection dbConnection;

    public JdbcHackathonRepository(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void save(Hackathon hackathon) {
        String sqlHackathon = """
                INSERT INTO hackathon (
                    nome, data_inizio, data_fine, scadenza_iscrizioni, scadenza_sottomissioni,
                    premio, dimensione_max_team, regolamento, stato,
                    id_organizzatore, id_giudice,
                    via, numero_civico, citta, cap, provincia
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        // Operazione su due tabelle: usa transazione esplicita.
        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                UUID idGenerato = inserisciHackathon(conn, sqlHackathon, hackathon);
                hackathon.setId(idGenerato);
                inserisciMentori(conn, idGenerato, hackathon.getIdsMentori());
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il salvataggio dell'hackathon: " + e.getMessage(), e);
        }
    }

    private UUID inserisciHackathon(Connection conn, String sql, Hackathon h) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            Address luogo = h.getLuogo();
            stmt.setString(1, h.getNome());
            stmt.setTimestamp(2, Timestamp.valueOf(h.getDataInizio()));
            stmt.setTimestamp(3, Timestamp.valueOf(h.getDataFine()));
            stmt.setTimestamp(4, Timestamp.valueOf(h.getScadenzaIscrizioni()));
            stmt.setTimestamp(5, Timestamp.valueOf(h.getScadenzaSottomissioni()));
            stmt.setDouble(6, h.getPremio());
            stmt.setInt(7, h.getDimensioneMaxTeam());
            stmt.setString(8, h.getRegolamento());
            stmt.setString(9, h.getStatoEnum().name());
            stmt.setObject(10, h.getIdOrganizzatore());
            stmt.setObject(11, h.getIdGiudice());

            if (luogo != null) {
                stmt.setString(12, luogo.getVia());
                stmt.setInt(13, luogo.getNumeroCivico());
                stmt.setString(14, luogo.getCitta());
                stmt.setString(15, luogo.getCap());
                stmt.setString(16, luogo.getProvincia());
            } else {
                stmt.setNull(12, Types.VARCHAR);
                stmt.setNull(13, Types.INTEGER);
                stmt.setNull(14, Types.VARCHAR);
                stmt.setNull(15, Types.VARCHAR);
                stmt.setNull(16, Types.VARCHAR);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return (UUID) rs.getObject("id");
            }
            throw new SQLException("INSERT hackathon non ha restituito l'id generato.");
        }
    }

    private void inserisciMentori(Connection conn, UUID idHackathon, List<UUID> idsMentori)
            throws SQLException {
        if (idsMentori == null || idsMentori.isEmpty()) return;

        String sql = "INSERT INTO hackathon_mentori (id_hackathon, id_mentore) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (UUID idMentore : idsMentori) {
                stmt.setObject(1, idHackathon);
                stmt.setObject(2, idMentore);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    @Override
    public Optional<Hackathon> findById(UUID id) {
        String sql = """
                SELECT h.*,
                       array_remove(array_agg(hm.id_mentore), NULL) AS mentori
                FROM hackathon h
                LEFT JOIN hackathon_mentori hm ON hm.id_hackathon = h.id
                WHERE h.id = ?
                GROUP BY h.id
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dell'hackathon con id " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Hackathon> findAllInIscrizione() {
        String sql = """
                SELECT h.*,
                       array_remove(array_agg(hm.id_mentore), NULL) AS mentori
                FROM hackathon h
                LEFT JOIN hackathon_mentori hm ON hm.id_hackathon = h.id
                WHERE h.stato = 'IN_ISCRIZIONE'
                GROUP BY h.id
                ORDER BY h.scadenza_iscrizioni ASC
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Hackathon> risultato = new ArrayList<>();
            while (rs.next()) {
                risultato.add(mapRow(rs));
            }
            return risultato;

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero degli hackathon in iscrizione: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateStato(UUID id, StatoHackathon stato) {
        String sql = "UPDATE hackathon SET stato = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, stato.name());
            stmt.setObject(2, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento dello stato dell'hackathon: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateVincitoreEPremio(UUID idHackathon, UUID idTeamVincitore, boolean premioDisbursed) {
        String sql = "UPDATE hackathon SET id_team_vincitore = ?, premio_disbursed = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idTeamVincitore);
            stmt.setBoolean(2, premioDisbursed);
            stmt.setObject(3, idHackathon);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante l'aggiornamento del vincitore e del premio per l'hackathon " + idHackathon + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Hackathon> findByOrganizzatore(UUID idOrganizzatore) {
        String sql = """
                SELECT h.*,
                       array_remove(array_agg(hm.id_mentore), NULL) AS mentori
                FROM hackathon h
                LEFT JOIN hackathon_mentori hm ON hm.id_hackathon = h.id
                WHERE h.id_organizzatore = ?
                GROUP BY h.id
                ORDER BY h.data_inizio DESC
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, idOrganizzatore);
            ResultSet rs = stmt.executeQuery();

            List<Hackathon> risultato = new ArrayList<>();
            while (rs.next()) {
                risultato.add(mapRow(rs));
            }
            return risultato;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante il recupero degli hackathon dell'organizzatore " +
                            idOrganizzatore + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void updateMentori(UUID idHackathon, List<UUID> nuoviIdsMentori) {
        String sqlElimina = "DELETE FROM hackathon_mentori WHERE id_hackathon = ?";
        String sqlInserisci = "INSERT INTO hackathon_mentori (id_hackathon, id_mentore) VALUES (?, ?)";

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Rimuove tutte le associazioni precedenti
                try (PreparedStatement stmtDel = conn.prepareStatement(sqlElimina)) {
                    stmtDel.setObject(1, idHackathon);
                    stmtDel.executeUpdate();
                }
                // Inserisce le nuove associazioni
                if (nuoviIdsMentori != null && !nuoviIdsMentori.isEmpty()) {
                    try (PreparedStatement stmtIns = conn.prepareStatement(sqlInserisci)) {
                        for (UUID idMentore : nuoviIdsMentori) {
                            stmtIns.setObject(1, idHackathon);
                            stmtIns.setObject(2, idMentore);
                            stmtIns.addBatch();
                        }
                        stmtIns.executeBatch();
                    }
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante l'aggiornamento dei mentori per l'hackathon " +
                            idHackathon + ": " + e.getMessage(), e);
        }
    }

    // -----------------------------------------------------------------------
    // Caso d'uso: Visualizzare informazioni pubbliche sugli hackathon
    // -----------------------------------------------------------------------

    @Override
    public List<Hackathon> findAllAvailable() {
        String sql = """
                SELECT h.*,
                       array_remove(array_agg(hm.id_mentore), NULL) AS mentori
                FROM hackathon h
                LEFT JOIN hackathon_mentori hm ON hm.id_hackathon = h.id
                WHERE h.stato != 'CONCLUSO'
                GROUP BY h.id
                ORDER BY h.data_inizio DESC
                """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Hackathon> risultato = new ArrayList<>();
            while (rs.next()) {
                risultato.add(mapRow(rs));
            }
            return risultato;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Errore durante il recupero degli hackathon disponibili: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Hackathon> findByIdAndDisponibile(UUID id) {
        String sql = """
                SELECT h.*,
                       array_remove(array_agg(hm.id_mentore), NULL) AS mentori
                FROM hackathon h
                LEFT JOIN hackathon_mentori hm ON hm.id_hackathon = h.id
                WHERE h.id = ? AND h.stato != 'CONCLUSO'
                GROUP BY h.id
                """;

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
                    "Errore durante il recupero dell'hackathon disponibile con id " + id +
                            ": " + e.getMessage(), e);
        }
    }

    private Hackathon mapRow(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        String via = rs.getString("via");

        Address luogo = null;
        if (via != null) {
            luogo = new Address(
                    via,
                    rs.getInt("numero_civico"),
                    rs.getString("citta"),
                    rs.getString("cap"),
                    rs.getString("provincia")
            );
        }

        List<UUID> idMentori = new ArrayList<>();
        Array mentoriArray = rs.getArray("mentori");
        if (mentoriArray != null) {
            Object[] arr = (Object[]) mentoriArray.getArray();
            for (Object obj : arr) {
                idMentori.add((UUID) obj);
            }
        }

        StatoHackathon stato = StatoHackathon.valueOf(rs.getString("stato"));

        Hackathon hackathon = new Hackathon(
                id,
                rs.getString("nome"),
                rs.getTimestamp("data_inizio").toLocalDateTime(),
                rs.getTimestamp("data_fine").toLocalDateTime(),
                rs.getTimestamp("scadenza_iscrizioni").toLocalDateTime(),
                rs.getTimestamp("scadenza_sottomissioni").toLocalDateTime(),
                rs.getDouble("premio"),
                luogo,
                rs.getInt("dimensione_max_team"),
                rs.getString("regolamento"),
                (UUID) rs.getObject("id_organizzatore"),
                (UUID) rs.getObject("id_giudice"),
                idMentori,
                stato.creaIstanza()
        );

        // Ricostruzione dei campi aggiuntivi (opzionali, potrebbero non essere presenti)
        try {
            hackathon.setIdTeamVincitore((UUID) rs.getObject("id_team_vincitore"));
            hackathon.setPremioDisbursed(rs.getBoolean("premio_disbursed"));
        } catch (SQLException ignored) {
            // Colonne non presenti in query parziali: si mantengono i valori di default
        }

        return hackathon;
    }
}
