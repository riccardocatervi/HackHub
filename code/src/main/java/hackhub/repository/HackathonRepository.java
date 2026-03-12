package hackhub.repository;

import hackhub.model.entity.Hackathon;
import hackhub.model.state.StatoHackathon;

import java.util.List;
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
}
