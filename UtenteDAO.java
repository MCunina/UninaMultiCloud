package it.unina.multicloud.dao;

import it.unina.multicloud.db.DBConnection;
import it.unina.multicloud.model.Utente;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

// query Q1-Q5, tutto quello che riguarda gli utenti
public class UtenteDAO {

    // Q1 - registra utente + corso di laurea insieme, in transazione (o vanno bene tutti e due o niente)
    public void registraUtente(Utente u, String corsoLaurea) throws SQLException {
        Connection conn = DBConnection.getConnection();
        boolean prevAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false); // disattivo il commit automatico, decido io quando salvare
        try {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO utente (matricola, nome, cognome, email, password) VALUES (?,?,?,?,?)")) {
                ps.setString(1, u.getMatricola());
                ps.setString(2, u.getNome());
                ps.setString(3, u.getCognome());
                ps.setString(4, u.getEmail());
                ps.setString(5, u.getPasswordHash()); // gia' hashata prima di arrivare qui
                ps.executeUpdate();
            }
            if (corsoLaurea != null && !corsoLaurea.isBlank()) { // il corso e' facoltativo
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO corsoLaurea (matricola, corsoLaurea) VALUES (?,?)")) {
                    ps.setString(1, u.getMatricola());
                    ps.setString(2, corsoLaurea);
                    ps.executeUpdate();
                }
            }
            conn.commit(); // solo se sono arrivato fin qui salvo per davvero tutto insieme
        } catch (SQLException e) {
            conn.rollback(); // se qualcosa va storto annullo anche il primo insert, niente a meta'
            throw e;
        } finally {
            conn.setAutoCommit(prevAutoCommit); // rimetto autocommit come stava prima
        }
    }

    // Q2 - login. ritorna null se email non esiste o password sbagliata (non distinguo i due casi, per sicurezza)
    public Utente login(String email, String passwordChiara) throws SQLException {
        String sql = "SELECT matricola, nome, cognome, password FROM utente WHERE email = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password");
                    if (hash.equals(sha256(passwordChiara))) { // confronto hash vs hash, mai chiaro vs chiaro
                        Utente u = new Utente(rs.getString("matricola"), rs.getString("nome"),
                                rs.getString("cognome"), email);
                        u.setPasswordHash(hash);
                        u.setCorsiLaurea(getCorsiLaurea(u.getMatricola()));
                        return u;
                    }
                }
            }
        }
        return null;
    }

    // Q3 - cambio password
    public void aggiornaPassword(String matricola, String nuovaPasswordChiara) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "UPDATE utente SET password = ? WHERE matricola = ?")) {
            ps.setString(1, sha256(nuovaPasswordChiara));
            ps.setString(2, matricola);
            ps.executeUpdate();
        }
    }

    // Q4 - un utente puo' avere piu' corsi, per questo e' una tabella a parte e non una colonna sola
    public List<String> getCorsiLaurea(String matricola) throws SQLException {
        List<String> corsi = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "SELECT corsoLaurea FROM corsoLaurea WHERE matricola = ?")) {
            ps.setString(1, matricola);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) corsi.add(rs.getString(1));
            }
        }
        return corsi;
    }

    // Q5
    public void eliminaUtente(String matricola) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "DELETE FROM utente WHERE matricola = ?")) {
            ps.setString(1, matricola);
            ps.executeUpdate();
        }
    }

    // controlli usati in fase di registrazione, prima di inserire, cosi' do un messaggio piu' chiaro
    public boolean matricolaEsiste(String matricola) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "SELECT 1 FROM utente WHERE matricola = ?")) {
            ps.setString(1, matricola);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); } // true se trova almeno una riga
        }
    }

    public boolean emailEsiste(String email) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "SELECT 1 FROM utente WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    // usato dalla pagina profilo, prende i dati + i corsi di laurea associati
    public Utente getByMatricola(String matricola) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "SELECT matricola, nome, cognome, email, dataRegistrazione FROM utente WHERE matricola = ?")) {
            ps.setString(1, matricola);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Utente u = new Utente(rs.getString("matricola"), rs.getString("nome"),
                            rs.getString("cognome"), rs.getString("email"));
                    u.setDataRegistrazione(rs.getTimestamp("dataRegistrazione"));
                    u.setCorsiLaurea(getCorsiLaurea(matricola));
                    return u;
                }
            }
        }
        return null;
    }

    public static String sha256(String testoChiaro) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(testoChiaro.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b)); // ogni byte diventa 2 cifre hex
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e); // non dovrebbe succedere mai, sha-256 e utf-8 ci sono sempre
        }
    }
}