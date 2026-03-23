package hackhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto di ingresso dell'applicazione HackHub.
 * <p>
 * Il wiring delle dipendenze è gestito automaticamente da Spring Boot
 * tramite component scanning e constructor injection.
 * La configurazione del database è in {@code application.properties}.
 */
@SpringBootApplication
public class HackHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(HackHubApplication.class, args);
    }
}
