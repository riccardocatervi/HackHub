package hackhub.service;

import hackhub.dto.ModuloSegnalazioneDTO;
import hackhub.repository.SegnalazioneRepository;
import hackhub.repository.TeamRepository;

import java.util.UUID;

public class SegnalazioneService {

    private final SegnalazioneRepository segnalazioneRepository;
    private final TeamRepository         teamRepository;
    private final NotificationsService   notificationsService;

    public SegnalazioneService(SegnalazioneRepository segnalazioneRepository,
                               TeamRepository teamRepository,
                               NotificationsService notificationsService) {
        this.segnalazioneRepository = segnalazioneRepository;
        this.teamRepository         = teamRepository;
        this.notificationsService   = notificationsService;
    }

    public ModuloSegnalazioneDTO getTeamsDisponibili(UUID hackathonId) {
        // TODO: Implementare nel caso d'uso 'Segnalare violazione del regolamento'
        return null;
    }
}
