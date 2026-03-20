package hackhub.service;

import hackhub.dto.AuthResponseDTO;
import hackhub.dto.LoginFormDTO;
import hackhub.dto.OAuthUserInfoDTO;
import hackhub.dto.UserRegistrationResponseDTO;
import hackhub.exception.InvalidCredentialsException;
import hackhub.model.OAuthProvider;
import hackhub.model.entity.Utente;
import hackhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test unitari per AuthService (caso d'uso: Autenticarsi alla piattaforma).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — Autenticarsi alla piattaforma")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private OAuthService oauthService;
    @Mock private UtenteService utenteService;
    @Mock private SessionManager sessionManager;

    private AuthService service;

    private static final UUID ID_UTENTE     = UUID.randomUUID();
    private static final String EMAIL       = "mario.rossi@test.it";
    private static final String PASSWORD    = "password123";
    private static final String HASH        = "hash:abc123";
    private static final String SESSION_TOK = "tok-" + UUID.randomUUID();

    private OAuth2Config configGoogle;
    private OAuth2Config configGithub;

    @BeforeEach
    void setUp() {
        configGoogle = new OAuth2Config(OAuthProvider.GOOGLE, "gid", "gsecret", "http://cb/google");
        configGithub = new OAuth2Config(OAuthProvider.GITHUB, "ghid", "ghsecret", "http://cb/github");

        service = new AuthService(
                userRepository,
                passwordEncoder,
                oauthService,
                utenteService,
                sessionManager,
                Map.of(OAuthProvider.GOOGLE, configGoogle, OAuthProvider.GITHUB, configGithub)
        );
    }

    // =========================================================================
    // authenticate()
    // =========================================================================

    @Nested
    @DisplayName("authenticate()")
    class Authenticate {

        @Test
        @DisplayName("Credenziali corrette → restituisce AuthResponseDTO con token di sessione")
        void flussoCo_restituisceDTO() {
            // Given
            Utente utente = utenteLocale();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(utente));
            when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(true);
            when(sessionManager.creaSessione(ID_UTENTE)).thenReturn(SESSION_TOK);

            LoginFormDTO dto = new LoginFormDTO(EMAIL, PASSWORD);

            // When
            AuthResponseDTO result = service.authenticate(dto);

            // Then
            assertNotNull(result);
            assertEquals(ID_UTENTE, result.idUtente());
            assertEquals("Mario", result.nome());
            assertEquals("Rossi", result.cognome());
            assertEquals(EMAIL, result.email());
            assertEquals(SESSION_TOK, result.sessionToken());
            verify(sessionManager).creaSessione(ID_UTENTE);
        }

        @Test
        @DisplayName("Utente non trovato per email → lancia InvalidCredentialsException")
        void emailNonTrovata_lancia() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThrows(InvalidCredentialsException.class,
                    () -> service.authenticate(new LoginFormDTO(EMAIL, PASSWORD)));
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("Account OAuth2 (senza password) → lancia InvalidCredentialsException")
        void accountOAuth_lancia() {
            Utente utenteOAuth = new Utente(
                    ID_UTENTE, "Mario", "Rossi", EMAIL,
                    null, OAuthProvider.GOOGLE, "google-ext-id");
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(utenteOAuth));

            assertThrows(InvalidCredentialsException.class,
                    () -> service.authenticate(new LoginFormDTO(EMAIL, PASSWORD)));
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("Password errata → lancia InvalidCredentialsException")
        void passwordErrata_lancia() {
            Utente utente = utenteLocale();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(utente));
            when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(false);

            assertThrows(InvalidCredentialsException.class,
                    () -> service.authenticate(new LoginFormDTO(EMAIL, PASSWORD)));
            verify(sessionManager, never()).creaSessione(any());
        }
    }

    // =========================================================================
    // getOAuth2AuthorizationUrl()
    // =========================================================================

    @Nested
    @DisplayName("getOAuth2AuthorizationUrl()")
    class GetOAuth2AuthorizationUrl {

        @Test
        @DisplayName("Provider GOOGLE configurato → restituisce URL contenente accounts.google.com")
        void google_restituisceUrl() {
            String urlAtteso = "https://accounts.google.com/o/oauth2/v2/auth?...";
            when(oauthService.buildAuthorizationUrl(
                    eq(OAuthProvider.GOOGLE), anyString(), anyString(), anyString()))
                    .thenReturn(urlAtteso);

            String url = service.getOAuth2AuthorizationUrl("google");

            assertNotNull(url);
            assertEquals(urlAtteso, url);
            verify(oauthService).buildAuthorizationUrl(
                    eq(OAuthProvider.GOOGLE), eq("gid"), eq("http://cb/google"), anyString());
        }

        @Test
        @DisplayName("Provider GITHUB configurato → restituisce URL contenente github.com")
        void github_restituisceUrl() {
            String urlAtteso = "https://github.com/login/oauth/authorize?...";
            when(oauthService.buildAuthorizationUrl(
                    eq(OAuthProvider.GITHUB), anyString(), anyString(), anyString()))
                    .thenReturn(urlAtteso);

            String url = service.getOAuth2AuthorizationUrl("GITHUB");

            assertEquals(urlAtteso, url);
        }

        @Test
        @DisplayName("Provider sconosciuto → lancia IllegalArgumentException")
        void providerSconosciuto_lancia() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.getOAuth2AuthorizationUrl("FACEBOOK"));
        }
    }

    // =========================================================================
    // gestisciCallbackOAuth2()
    // =========================================================================

    @Nested
    @DisplayName("gestisciCallbackOAuth2()")
    class GestisciCallbackOAuth2 {

        @Test
        @DisplayName("Nuovo utente Google → crea account, genera sessione, restituisce AuthResponseDTO")
        void nuovoUtenteGoogle_flussoCo() {
            // Given
            String codice = "auth-code-abc";
            String accessToken = "ya29.xxx";
            OAuthUserInfoDTO userInfo =
                    new OAuthUserInfoDTO("Mario", "Rossi", EMAIL, OAuthProvider.GOOGLE, "g-ext-id");
            UserRegistrationResponseDTO regResult =
                    new UserRegistrationResponseDTO(ID_UTENTE, "Mario", "Rossi", EMAIL);

            when(oauthService.scambiaCodiceConToken(
                    codice, OAuthProvider.GOOGLE, "gid", "gsecret", "http://cb/google"))
                    .thenReturn(accessToken);
            when(oauthService.fetchUserInfo(OAuthProvider.GOOGLE, accessToken))
                    .thenReturn(userInfo);
            when(utenteService.registraDaOAuth(userInfo)).thenReturn(regResult);
            when(sessionManager.creaSessione(ID_UTENTE)).thenReturn(SESSION_TOK);

            // When
            AuthResponseDTO result = service.gestisciCallbackOAuth2(codice, "GOOGLE");

            // Then
            assertNotNull(result);
            assertEquals(ID_UTENTE, result.idUtente());
            assertEquals("Mario", result.nome());
            assertEquals(EMAIL, result.email());
            assertEquals(SESSION_TOK, result.sessionToken());
        }

        @Test
        @DisplayName("Utente GitHub già registrato → recupera account esistente, genera sessione")
        void utenteGitHubEsistente_flussoCo() {
            // Given
            String codice = "gh-code-xyz";
            String accessToken = "gho.xxx";
            OAuthUserInfoDTO userInfo =
                    new OAuthUserInfoDTO("Anna", "Verdi", "anna@test.it", OAuthProvider.GITHUB, "gh-ext-99");
            UUID idAnna = UUID.randomUUID();
            UserRegistrationResponseDTO regResult =
                    new UserRegistrationResponseDTO(idAnna, "Anna", "Verdi", "anna@test.it");
            String tokAnna = "tok-anna";

            when(oauthService.scambiaCodiceConToken(
                    codice, OAuthProvider.GITHUB, "ghid", "ghsecret", "http://cb/github"))
                    .thenReturn(accessToken);
            when(oauthService.fetchUserInfo(OAuthProvider.GITHUB, accessToken)).thenReturn(userInfo);
            when(utenteService.registraDaOAuth(userInfo)).thenReturn(regResult);
            when(sessionManager.creaSessione(idAnna)).thenReturn(tokAnna);

            // When
            AuthResponseDTO result = service.gestisciCallbackOAuth2(codice, "github");

            // Then
            assertEquals(idAnna, result.idUtente());
            assertEquals(tokAnna, result.sessionToken());
        }

        @Test
        @DisplayName("Provider non configurato → lancia IllegalArgumentException")
        void providerNonConfigurato_lancia() {
            // Crea service con mappa vuota (nessun provider configurato)
            AuthService serviceVuoto = new AuthService(
                    userRepository, passwordEncoder, oauthService,
                    utenteService, sessionManager, Map.of());

            assertThrows(IllegalArgumentException.class,
                    () -> serviceVuoto.gestisciCallbackOAuth2("code", "GOOGLE"));
        }
    }

    // =========================================================================
    // generaSessioneUtente()
    // =========================================================================

    @Nested
    @DisplayName("generaSessioneUtente()")
    class GeneraSessioneUtente {

        @Test
        @DisplayName("Genera un token di sessione valido per l'utente indicato")
        void generaToken_restituisceString() {
            when(sessionManager.creaSessione(ID_UTENTE)).thenReturn(SESSION_TOK);

            String token = service.generaSessioneUtente(ID_UTENTE);

            assertEquals(SESSION_TOK, token);
            verify(sessionManager).creaSessione(ID_UTENTE);
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private Utente utenteLocale() {
        return new Utente(ID_UTENTE, "Mario", "Rossi", EMAIL, HASH, null, null);
    }

    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
