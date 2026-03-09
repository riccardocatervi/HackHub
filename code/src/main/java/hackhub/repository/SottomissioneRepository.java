package hackhub.repository;

import hackhub.model.entity.Sottomissione;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SottomissioneRepository extends GenericRepository<Sottomissione, UUID> {

    /** Persiste una nuova sottomissione e imposta l'id generato dal DB sull'entità passata. */
    void save(Sottomissione sottomissione);

    /** Verifica se il team ha già inviato una sottomissione per l'hackathon specificato. */
    boolean existsByHackathonAndTeam(UUID idHackathon, UUID idTeam);

    /**
     * Conta le sottomissioni non ancora valutate per l'hackathon specificato.
     * Con il flag {@code valutato} su Sottomissione la query è diretta.
     */
    long countNonValutate(UUID idHackathon);

    /**
     * Trova la sottomissione con il voto più alto per l'hackathon.
     * Utilizzata per determinare il vincitore prima della proclamazione.
     */
    Optional<Sottomissione> findVincitore(UUID idHackathon);

    /** Imposta il flag vincitore = true sulla sottomissione specificata. */
    void markAsVincitore(UUID idSottomissione);

    /** Imposta il flag valutato = true sulla sottomissione specificata. */
    void markAsValutata(UUID idSottomissione);

    /** Restituisce le sottomissioni ancora da valutare per un hackathon. */
    List<Sottomissione> findSottomissioniDaValutare(UUID idHackathon, UUID idGiudice);

    /**
     * Restituisce tutte le sottomissioni (valutate e non) per un hackathon.
     * Usato per la dashboard del giudice che mostra lo stato completo.
     */
    List<Sottomissione> findAllByHackathon(UUID idHackathon);
}
