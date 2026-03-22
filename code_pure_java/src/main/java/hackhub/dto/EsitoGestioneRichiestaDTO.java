package hackhub.dto;

import java.util.UUID;

/**
 * DTO di conferma restituito al mentore al termine della gestione di una richiesta di supporto
 * (accettazione o rifiuto). Se la richiesta è stata rifiutata, {@code motivazione} contiene
 * la motivazione inserita dal mentore; altrimenti è {@code null}.
 * Usato nel caso d'uso 'Prendere in carico una richiesta di supporto'.
 *
 * @param idRichiesta id della richiesta gestita
 * @param idTeam      id del team richiedente
 * @param nomeTeam    nome del team richiedente
 * @param motivazione motivazione del rifiuto (null se la richiesta è stata accettata)
 * @param messaggio   messaggio descrittivo dell'esito dell'operazione
 */
public record EsitoGestioneRichiestaDTO(
        UUID idRichiesta,
        UUID idTeam,
        String nomeTeam,
        String motivazione,
        String messaggio
) {
}
