package it.unina.multicloud.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Gestisce la connessione al db postgres
public final class DBConnection {
	//parametri per il collegamento
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "5432";
    private static final String DEFAULT_DB   = "postgres";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASS = "victus015";

    private static Connection connection; // unica connessione condivisa

    private DBConnection() { } // non ha senso istanziarla, e' tutta roba statica

    // synchronized cosi' se due parti del programma la chiamano insieme non si creano 2 connessioni per errore
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) { // la creo solo se non esiste gia' o si e' chiusa
            java.util.Properties p = loadOverrides();
            String host = p.getProperty("host", DEFAULT_HOST); // se non c'e' nel file, usa il default
            String port = p.getProperty("port", DEFAULT_PORT);
            String db   = p.getProperty("database", DEFAULT_DB);
            String user = p.getProperty("user", DEFAULT_USER);
            String pass = p.getProperty("password", DEFAULT_PASS);

            String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;
            try {
                Class.forName("org.postgresql.Driver"); // registra il driver jdbc di postgres
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver JDBC PostgreSQL non trovato nel classpath.", e);
            }
            connection = DriverManager.getConnection(url, user, pass);
        }
        return connection;
    }

    // legge db.properties dalla cartella di lavoro, se c'e'. altrimenti torna vuoto e si usano i default
    private static java.util.Properties loadOverrides() {
        java.util.Properties p = new java.util.Properties();
        java.io.File f = new java.io.File("db.properties");
        if (f.exists()) {
            try (java.io.FileInputStream in = new java.io.FileInputStream(f)) {
                p.load(in);
            } catch (java.io.IOException ignored) {
                // se il file non si legge bene pazienza, si va avanti coi default
            }
        }
        return p;
    }

    // non usato per ora da nessuna parte della gui ma tenuto pronto per quando serve chiudere tutto
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            } finally {
                connection = null;
            }
        }
    }
}
