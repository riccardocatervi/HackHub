package hackhub.controller;

import hackhub.dto.*;
import hackhub.service.TeamService;

import java.util.List;
import java.util.UUID;

/**
 * Coordinatore GRASP per i casi d'uso relativi alla gestione dei team:
 * creazione, risposta agli inviti e gestione della partecipazione (abbandono).
 * Delega tutta la logica di dominio a {@link TeamService}.
 */
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * Restituisce la lista degli hackathon aperti alle iscrizioni.
     * Punto di ingresso del flusso: l'utente sceglie un hackathon prima di creare il team.
     *
     * @return lista di hackathon in stato IN_ISCRIZIONE
     */
    public List<HackathonResponseDTO> getHackathonDisponibili() {
        return teamService.getHackathonDisponibili();
    }

    /**
     * Restituisce i dati necessari per il form di creazione team:
     * informazioni sull'hackathon e lista degli utenti invitabili.
     *
     * @param idHackathon id dell'hackathon selezionato
     * @param idLeader    id dell'utente che vuole creare il team
     * @return DTO con dati dell'hackathon e utenti disponibili
     */
    public TeamFormDTO getCreateTeamForm(UUID idHackathon, UUID idLeader) {
        return teamService.getCreateTeamForm(idHackathon, idLeader);
    }

    /**
     * Crea un nuovo team, aggiunge il leader come membro e invia gli inviti
     * agli utenti selezionati.
     *
     * @param dto dati del form compilato dal leader
     * @return DTO di conferma con i dati del team creato e gli inviti emessi
     */
    public TeamResponseDTO createTeam(CreateTeamRequestDTO dto) {
        return teamService.createTeam(dto);
    }

    /**
     * Registra la risposta di un utente a un invito ricevuto.
     * Se accettato, l'utente viene aggiunto come membro effettivo del team.
     *
     * @param dto DTO con id invito, id utente e risposta (accettato/rifiutato)
     * @return DTO con lo stato aggiornato dell'invito
     */
    public InvitationResponseDTO rispondiInvito(RispostaInvitoDTO dto) {
        return teamService.rispondiInvito(dto);
    }

    // -----------------------------------------------------------------------
    // Caso d'uso: Gestire iscrizione al team (UC2)
    // -----------------------------------------------------------------------

    /**
     * Recupera i dettagli di partecipazione corrente di un membro al suo team.
     * Punto di ingresso del caso d'uso "Gestire iscrizione al team".
     *
     * @param idMembro id dell'utente autenticato
     * @return DTO con team, hackathon, membri e flag di ruolo
     */
    public DettagliPartecipazioneDTO richiediDettagliPartecipazione(UUID idMembro) {
        return teamService.ottieniDettagliTeamPerMembro(idMembro);
    }

    /**
     * Processa la richiesta confermata di abbandono del team.
     * Se il membro è leader, viene eletto un nuovo leader casuale.
     *
     * @param idTeam   id del team da abbandonare
     * @param idMembro id dell'utente che abbandona
     * @return DTO di conferma con messaggio esplicativo
     */
    public GestioneTeamResponseDTO confermaAbbandonoTeam(UUID idTeam, UUID idMembro) {
        return teamService.abbandonaTeam(idTeam, idMembro);
    }
}
