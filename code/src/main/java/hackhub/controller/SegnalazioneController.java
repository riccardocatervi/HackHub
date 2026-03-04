package hackhub.controller;

import hackhub.dto.ModuloSegnalazioneDTO;
import hackhub.service.SegnalazioneService;

import java.util.UUID;

public class SegnalazioneController {

    private final SegnalazioneService segnalazioneService;

    public SegnalazioneController(SegnalazioneService segnalazioneService) {
        this.segnalazioneService = segnalazioneService;
    }

    public ModuloSegnalazioneDTO getDatiModulo(UUID hackathonId) {
        // TODO: Implementare nel caso d'uso 'Segnalare violazione del regolamento'
        return null;
    }
}
