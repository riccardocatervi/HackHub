package hackhub.rest;

import hackhub.controller.RegistrationController;
import hackhub.dto.UserRegistrationDTO;
import hackhub.dto.UserRegistrationResponseDTO;
import hackhub.model.OAuthProvider;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint REST per il caso d'uso 'Registrarsi alla piattaforma'.
 * Delega al coordinatore GRASP {@link RegistrationController}.
 */
@RestController
@RequestMapping("/api/registrazione")
public class RegistrationRestController {

    private final RegistrationController registrationController;

    public RegistrationRestController(RegistrationController registrationController) {
        this.registrationController = registrationController;
    }

    /**
     * POST /api/registrazione — Registrazione tramite form standard (email + password).
     */
    @PostMapping
    public ResponseEntity<UserRegistrationResponseDTO> registra(
            @RequestBody UserRegistrationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registrationController.compilaFormRegistrazione(dto));
    }

    /**
     * POST /api/registrazione/oauth — Completamento registrazione tramite OAuth2.
     */
    @PostMapping("/oauth")
    public ResponseEntity<UserRegistrationResponseDTO> registraOAuth(
            @RequestParam("provider") OAuthProvider provider,
            @RequestParam("accessToken") String accessToken) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registrationController.completaRegistrazioneOAuth(provider, accessToken));
    }
}
