package hackhub.builder;

import hackhub.exception.MaxTeamSizeExceedException;
import hackhub.exception.ValidationException;
import hackhub.model.entity.Invito;
import hackhub.model.entity.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <strong>Pattern GoF: Builder</strong>
 * <p>
 * Costruisce in modo fluente un {@link Team} insieme alla lista degli
 * {@link Invito} da inviare ai potenziali membri, applicando i vincoli
 * di dominio durante la fase di costruzione:
 * <ul>
 *   <li>Il numero di utenti invitati non può superare
 *       {@code dimensioneMaxTeam - 1} (il leader occupa uno slot).</li>
 *   <li>Ogni membro può essere invitato una sola volta per sessione di build.</li>
 *   <li>Il leader non può essere incluso tra gli invitati.</li>
 * </ul>
 * <p>
 * Il metodo {@link #build()} restituisce un {@link EquipeCreationResult}
 * che raggruppa {@code Team} e {@code List<Invito>} da persistere atomicamente.
 * <p>
 * Utilizzo:
 * <pre>{@code
 * EquipeCreationResult risultato = new EquipeBuilder()
 *     .nome("Team Alpha")
 *     .descrizione("Il nostro progetto di AI")
 *     .leader(idLeader)
 *     .hackathon(idHackathon, dimensioneMaxTeam)
 *     .invitaMembro(idUtente1)
 *     .invitaMembro(idUtente2)
 *     .build();
 * }</pre>
 */
public class EquipeBuilder {

    private String nome;
    private String descrizione;
    private UUID idLeader;
    private UUID idHackathon;
    private int dimensioneMaxTeam;

    private final List<UUID> idsUtentiInvitati = new ArrayList<>();

    public EquipeBuilder nome(String nome) {
        this.nome = nome;
        return this;
    }

    public EquipeBuilder descrizione(String descrizione) {
        this.descrizione = descrizione;
        return this;
    }

    public EquipeBuilder leader(UUID idLeader) {
        this.idLeader = idLeader;
        return this;
    }

    public EquipeBuilder hackathon(UUID idHackathon, int dimensioneMaxTeam) {
        this.idHackathon = idHackathon;
        this.dimensioneMaxTeam = dimensioneMaxTeam;
        return this;
    }

    /**
     * Aggiunge un utente alla lista degli invitati, verificando il vincolo
     * sulla dimensione massima del team e l'unicità dell'invito.
     *
     * @param idUtente identificativo dell'utente da invitare
     * @return il builder corrente (fluent API)
     * @throws MaxTeamSizeExceedException se il numero di invitati supererebbe il massimo consentito
     * @throws ValidationException        se si tenta di invitare il leader o un utente già invitato
     */
    public EquipeBuilder invitaMembro(UUID idUtente) {
        if (idUtente.equals(idLeader)) {
            throw new ValidationException(
                    "Il leader non può essere aggiunto come membro invitato: " + idUtente);
        }
        if (idsUtentiInvitati.contains(idUtente)) {
            throw new ValidationException(
                    "Utente già presente nella lista inviti: " + idUtente);
        }
        // Il leader occupa sempre uno slot: posizioni disponibili = max - 1
        if (idsUtentiInvitati.size() >= dimensioneMaxTeam - 1) {
            throw new MaxTeamSizeExceedException(
                    "Numero massimo di inviti raggiunto per questo hackathon " +
                            "(dimensione max team: " + dimensioneMaxTeam + ").");
        }
        idsUtentiInvitati.add(idUtente);
        return this;
    }

    /**
     * Valida i parametri obbligatori, costruisce il {@link Team} e genera
     * gli {@link Invito} per ciascun utente selezionato.
     * L'id del team sarà {@code null} fino alla persistenza (il DB lo assegna).
     *
     * @return il risultato della costruzione (team + inviti)
     * @throws ValidationException se mancano parametri obbligatori
     */
    public EquipeCreationResult build() {
        validaParametriObbligatori();

        // Il team viene creato senza id: sarà valorizzato dopo il salvataggio nel DB
        Team team = new Team(null, nome, descrizione, idLeader, idHackathon);

        // Gli inviti non hanno ancora l'idTeam: verrà impostato dopo il save del team
        List<Invito> inviti = idsUtentiInvitati.stream()
                .map(idUtente -> new Invito(null, idUtente, idHackathon))
                .toList();

        return new EquipeCreationResult(team, inviti);
    }

    private void validaParametriObbligatori() {
        if (nome == null || nome.isBlank()) {
            throw new ValidationException("Il nome del team è obbligatorio.");
        }
        if (idLeader == null) {
            throw new ValidationException("Il leader del team è obbligatorio.");
        }
        if (idHackathon == null) {
            throw new ValidationException("L'hackathon di riferimento è obbligatorio.");
        }
        if (dimensioneMaxTeam <= 0) {
            throw new ValidationException("La dimensione massima del team deve essere maggiore di zero.");
        }
    }
}
