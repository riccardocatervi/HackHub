package hackhub.rest;

import hackhub.controller.AuthController;
import hackhub.dto.AuthResponseDTO;
import hackhub.dto.LoginFormDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoint REST per il caso d'uso 'Autenticarsi alla piattaforma'.
 * Delega al coordinatore GRASP {@link AuthController}.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthController authController;

    public AuthRestController(AuthController authController) {
        this.authController = authController;
    }

    /**
     * POST /api/auth/login — Autenticazione tramite credenziali locali.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginFormDTO dto) {
        return ResponseEntity.ok(authController.effettuaLogin(dto));
    }

    /**
     * GET /api/auth/oauth2/{provider} — Genera l'URL di autorizzazione OAuth2.
     */
    @GetMapping("/oauth2/{provider}")
    public ResponseEntity<Map<String, String>> getOAuth2Url(@PathVariable String provider) {
        String url = authController.richiediLoginOAuth2(provider);
        return ResponseEntity.ok(Map.of("authorizationUrl", url));
    }

    /**
     * GET /api/auth/oauth2/{provider}/callback — Callback OAuth2 dal provider.
     */
    @GetMapping("/oauth2/{provider}/callback")
    public ResponseEntity<AuthResponseDTO> oauth2Callback(
            @PathVariable String provider,
            @RequestParam("code") String authorizationCode) {
        return ResponseEntity.ok(authController.oauth2Callback(authorizationCode, provider));
    }
}
