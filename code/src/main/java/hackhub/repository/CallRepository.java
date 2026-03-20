package hackhub.repository;

import hackhub.model.StatoCall;
import hackhub.model.entity.Call;

import java.util.List;
import java.util.UUID;

/**
 * Interfaccia repository per le call pianificate tra mentor e team.
 * Estende GenericRepository per ereditare il metodo findById.
 * Usata nei casi d'uso 'Pianificare call con un team' e
 * 'Gestire invito a call da parte di un mentore'.
 */
public interface CallRepository extends GenericRepository<Call, UUID> {

    /**
     * Persiste una nuova call e imposta l'id generato dal DB sull'entità.
     *
     * @param call l'entità da salvare
     */
    public void save(Call call);

    /**
     * Recupera tutte le call associate a una richiesta di supporto.
     *
     * @param idRichiesta id della richiesta di supporto
     * @return lista delle call, eventualmente vuota
     */
    public List<Call> findByRichiesta(UUID idRichiesta);

    // -----------------------------------------------------------------------
    // Caso d'uso: Gestire invito a call da parte di un mentore (it.5)
    // -----------------------------------------------------------------------

    /**
     * Aggiorna lo stato di una call nel database.
     * Chiamato quando il leader del team accetta o rifiuta l'invito.
     *
     * @param idCall     id della call da aggiornare
     * @param nuovoStato il nuovo stato (ACCETTATA o RIFIUTATA)
     */
    public void updateStato(UUID idCall, StatoCall nuovoStato);

    /**
     * Recupera tutte le call associate alle richieste di supporto di un team.
     * Permette al leader di visualizzare gli inviti a call ricevuti.
     *
     * @param idTeam id del team
     * @return lista delle call per il team, eventualmente vuota
     */
    public List<Call> findByTeamId(UUID idTeam);
}
