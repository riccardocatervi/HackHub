package hackhub.repository;

import hackhub.model.entity.Sottomissione;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SottomissioneRepository extends GenericRepository<Sottomissione, UUID> {

    /** Persiste una nuova sottomissione e imposta l'id generato dal DB sull'entità passata. */
    public void save(Sottomissione sottomissione);

    /** Verifica se il team ha già inviato una sottomissione per l'hackathon specificato. */
    public boolean existsByHackathonAndTeam(UUID idHackathon, UUID idTeam);

    /**
     * Conta le sottomissioni non ancora valutate per l'hackathon specificato.
     * Con il flag {@code valutato} su Sottomissione la query è diretta.
     */
    public long countNonValutate(UUID idHackathon);

    /**
     * Trova la sottomissione con il voto più alto per l'hackathon.
     * Utilizzata per determinare il vincitore prima della proclamazione.
     */
    public Optional<Sottomissione> findVincitore(UUID idHackathon);

    /** Imposta il flag vincitore = true sulla sottomissione specificata. */
    public void markAsVincitore(UUID idSottomissione);

    /** Imposta il flag valutato = true sulla sottomissione specificata. */
    public void markAsValutata(UUID idSottomissione);

    /** Restituisce le sottomissioni ancora da valutare per un hackathon. */
    public List<Sottomissione> findSottomissioniDaValutare(UUID idHackathon, UUID idGiudice);

    /**
     * Restituisce tutte le sottomissioni (valutate e non) per un hackathon.
     * Usato per la dashboard del giudice che mostra lo stato completo.
     */
    public List<Sottomissione> findAllByHackathon(UUID idHackathon);

    /**
     * Recupera la sottomissione di un team specifico per un hackathon.
     * Utilizzata nel caso d'uso 'Aggiornare Sottomissione del Team'.
     *
     * @param idHackathon id dell'hackathon
     * @param idTeam      id del team
     * @return la sottomissione esistente, se presente
     */
    public Optional<Sottomissione> findByHackathonAndTeam(UUID idHackathon, UUID idTeam);

    /**
     * Aggiorna i campi modificabili di una sottomissione esistente.
     * Utilizzata nel caso d'uso 'Aggiornare Sottomissione del Team'.
     *
     * @param sottomissione entità con i nuovi valori e l'id già valorizzato
     */
    public void update(Sottomissione sottomissione);
}
