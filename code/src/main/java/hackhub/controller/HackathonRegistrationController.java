package hackhub.controller;

import hackhub.dto.UnsubscriptionSuccessDTO;
import hackhub.service.TeamRegistrationService;

import java.util.UUID;

/**
 * Coordinatore GRASP per il caso d'uso "Gestire iscrizione del team all'Hackathon".
 * <p>
 * Riceve la richiesta di disiscrizione dal layer di presentazione, esegue la validazione
 * formale degli argomenti e delega tutta la logica di business a {@link TeamRegistrationService}.
 * <p>
 * In un contesto con autenticazione JWT, l'id del richiedente (requesterId) verrebbe
 * estratto dal token; in questa implementazione viene passato esplicitamente come parametro.
 */
public class HackathonRegistrationController {

    private final TeamRegistrationService teamRegistrationService;

    public HackathonRegistrationController(TeamRegistrationService teamRegistrationService) {
        this.teamRegistrationService = teamRegistrationService;
    }

    /**
     * Avvia il processo di disiscrizione del team dall'hackathon.
     * Solo il leader del team può effettuare questa operazione.
     * L'hackathon deve essere in stato IN_ISCRIZIONE.
     *
     * @param idTeam      id del team da disiscrivere
     * @param requesterId id dell'utente che effettua la richiesta (leader del team)
     * @return DTO di conferma con i dettagli dell'avvenuta disiscrizione
     */
    public UnsubscriptionSuccessDTO unsubscribeTeam(UUID idTeam, UUID requesterId) {
        if (idTeam == null || requesterId == null) {
            throw new IllegalArgumentException("idTeam e requesterId non possono essere null.");
        }
        return teamRegistrationService.processTeamUnsubscription(idTeam, requesterId);
    }
}
