package hackhub.infrastructure.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Carica la configurazione del database da un file di proprietà nel classpath.
 * <p>
 * Ordine di precedenza (12-factor app):
 * 1. Variabili d'ambiente DB_URL / DB_USER / DB_PASS  (produzione, CI/CD)
 * 2. Proprietà nel file application.properties         (sviluppo locale)
 * <p>
 * In questo modo nessuna credenziale viene hardcodata nel codice sorgente.
 */
public class DBConfig {

    private static final String ENV_URL = "DB_URL";
    private static final String ENV_USER = "DB_USER";
    private static final String ENV_PASS = "DB_PASS";

    private final String url;
    private final String username;
    private final String password;

    private DBConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Crea un'istanza caricando la configurazione dal file specificato nel classpath.
     * Le variabili d'ambiente sovrascrivono i valori nel file se presenti.
     *
     * @param filename nome del file nel classpath (es. "application.properties")
     * @throws RuntimeException se il file non è trovato o mancano proprietà obbligatorie
     */
    public static DBConfig fromClasspath(String filename) {
        Properties props = caricaProperties(filename);
        String url = conOverrideEnv(ENV_URL, richiedi(props, "db.url", filename));
        String user = conOverrideEnv(ENV_USER, richiedi(props, "db.username", filename));
        String pass = conOverrideEnv(ENV_PASS, richiedi(props, "db.password", filename));
        return new DBConfig(url, user, pass);
    }

    private static Properties caricaProperties(String filename) {
        Properties props = new Properties();
        try (InputStream is = DBConfig.class.getClassLoader().getResourceAsStream(filename)) {
            if (is == null) {
                throw new RuntimeException(
                        "File di configurazione '" + filename + "' non trovato nel classpath. " +
                                "Copia 'application.properties.example' in 'application.properties' " +
                                "e configura i valori corretti."
                );
            }
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Errore nel caricamento di '" + filename + "': " + e.getMessage(), e
            );
        }
        return props;
    }

    private static String richiedi(Properties props, String chiave, String filename) {
        String valore = props.getProperty(chiave);
        if (valore == null || valore.isBlank()) {
            throw new RuntimeException(
                    "Proprietà obbligatoria '" + chiave + "' mancante in '" + filename + "'."
            );
        }
        return valore.trim();
    }

    /**
     * Restituisce il valore della variabile d'ambiente se impostata e non vuota,
     * altrimenti il valore di fallback dal file di configurazione.
     */
    private static String conOverrideEnv(String chiaveEnv, String fallback) {
        String envValue = System.getenv(chiaveEnv);
        return (envValue != null && !envValue.isBlank()) ? envValue.trim() : fallback;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
