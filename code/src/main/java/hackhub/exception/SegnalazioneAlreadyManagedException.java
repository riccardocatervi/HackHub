package hackhub.exception;

import java.util.UUID;

/**
 * Lanciata quando si tenta di gestire una segnalazione già processata
 * (stato diverso da PENDENTE).
 */
public class SegnalazioneAlreadyManagedException extends RuntimeException {

    public SegnalazioneAlreadyManagedException(UUID idSegnalazione) {
        super("La segnalazione " + idSegnalazione + " è già stata gestita e non può essere modificata.");
    }
}
