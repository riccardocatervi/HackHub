package hackhub.repository;

import hackhub.model.entity.Hackathon;
import hackhub.model.state.StatoHackathon;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HackathonRepository extends GenericRepository<Hackathon, UUID> {

    /**
     * Persiste un nuovo hackathon e imposta l'id generato dal DB sull'entità passata.
     */
    public void save(Hackathon hackathon);

    /**
     * Aggiorna il campo 'stato' di un hackathon nel database.
     */
    public void updateStato(UUID id, StatoHackathon stato);

    /**
     * Restituisce tutti gli hackathon in fase di iscrizione aperta.
     * Utilizzato per mostrare agli utenti registrati i contest disponibili.
     */
    public List<Hackathon> findAllInIscrizione();

    /**
     * Aggiorna l'id del team vincitore e il flag di avvenuta erogazione del premio.
     * Chiamato al termine della proclamazione, dopo il successo del pagamento.
     */
    public void updateVincitoreEPremio(UUID idHackathon, UUID idTeamVincitore, boolean premioDisbursed);

    /**
     * Restituisce la lista degli hackathon creati da un determinato organizzatore.
     * Utilizzato nel caso d'uso 'Gestire mentori di un hackathon'.
     *
     * @param idOrganizzatore l'id dell'organizzatore
     * @return lista degli hackathon di competenza dell'organizzatore
     */
    public List<Hackathon> findByOrganizzatore(UUID idOrganizzatore);

    /**
     * Sostituisce l'intera associazione dei mentori per un hackathon.
     * Elimina prima tutte le associazioni esistenti, poi inserisce quelle nuove.
     * Operazione atomica tramite transazione esplicita.
     *
     * @param idHackathon     l'id dell'hackathon da aggiornare
     * @param nuoviIdsMentori la nuova lista completa degli id dei mentori
     */
    public void updateMentori(UUID idHackathon, List<UUID> nuoviIdsMentori);

    // -----------------------------------------------------------------------
    // Caso d'uso: Visualizzare informazioni pubbliche sugli hackathon
    // -----------------------------------------------------------------------

    /**
     * Restituisce tutti gli hackathon pubblicamente disponibili,
     * ovvero quelli in stato IN_ISCRIZIONE, IN_CORSO o IN_VALUTAZIONE
     * (esclude lo stato CONCLUSO).
     * Utilizzato per la lista pubblica accessibile a qualsiasi visitatore o utente.
     *
     * @return lista degli hackathon disponibili, ordinata per data di inizio decrescente
     */
    public List<Hackathon> findAllAvailable();

    /**
     * Cerca un hackathon per id verificando che sia ancora disponibile
     * (stato diverso da CONCLUSO).
     * Restituisce {@link Optional#empty()} se l'hackathon non esiste
     * oppure è già concluso.
     *
     * @param id l'id dell'hackathon da recuperare
     * @return {@link Optional} con l'hackathon se trovato e disponibile
     */
    public Optional<Hackathon> findByIdAndDisponibile(UUID id);
}
