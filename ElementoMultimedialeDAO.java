package it.unina.multicloud.dao;

import it.unina.multicloud.db.DBConnection;
import it.unina.multicloud.model.ElementoMultimediale;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// query Q6-Q11 + Q26 e Q28
public class ElementoMultimedialeDAO {

    // Q6 - carica un elemento nuovo e mi torna l'id generato dal db (serve subito dopo per aggiungerlo a una playlist)
    public int carica(ElementoMultimediale e) throws SQLException {
        String sql = "INSERT INTO elementoMultimediale " +
                "(titolo, descrizione, formato, durata, dataCreazione, urlFile, urlCopertina, matricolaAutore) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        // RETURN_GENERATED_KEYS dice al driver di darmi indietro l'id auto generato (serial) dopo l'insert
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getTitolo());
            ps.setString(2, e.getDescrizione());
            ps.setString(3, e.getFormato());
            if (e.getDurata() != null) ps.setInt(4, e.getDurata()); else ps.setNull(4, Types.INTEGER); // se non c'e' la durata, null vero e proprio, non 0
            ps.setDate(5, e.getDataCreazione());
            ps.setString(6, e.getUrlFile());
            ps.setString(7, e.getUrlCopertina());
            ps.setString(8, e.getMatricolaAutore());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    // Q7 - modifica titolo/descrizione, ma solo se e' davvero l'autore (vedi la and nella where)
    public boolean modifica(int idElemento, String matricolaAutore, String nuovoTitolo, String nuovaDescrizione) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "UPDATE elementoMultimediale SET titolo = ?, descrizione = ? WHERE idElementoMultimediale = ? AND matricolaAutore = ?")) {
            ps.setString(1, nuovoTitolo);
            ps.setString(2, nuovaDescrizione);
            ps.setInt(3, idElemento);
            ps.setString(4, matricolaAutore);
            return ps.executeUpdate() > 0; // se non era l'autore la where non trova righe e torna 0 = false
        }
    }

    // Q8
    public boolean elimina(int idElemento, String matricolaAutore) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "DELETE FROM elementoMultimediale WHERE idElementoMultimediale = ? AND matricolaAutore = ?")) {
            ps.setInt(1, idElemento);
            ps.setString(2, matricolaAutore);
            return ps.executeUpdate() > 0;
        }
    }

    // Q9 - piu' recenti prima
    public List<ElementoMultimediale> getByAutore(String matricola) throws SQLException {
        String sql = "SELECT idElementoMultimediale, titolo, formato, dataCreazione, descrizione, durata, " +
                "urlFile, urlCopertina, matricolaAutore, dataCaricamento " +
                "FROM elementoMultimediale WHERE matricolaAutore = ? ORDER BY dataCreazione DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, matricola);
            try (ResultSet rs = ps.executeQuery()) { return mapAll(rs); }
        }
    }

    // Q10 - ilike invece di like perche' voglio che non sia case sensitive (postgres, LIKE normale distingue maiuscole)
    public List<ElementoMultimediale> cercaPerTitolo(String parolaChiave) throws SQLException {
        String sql = "SELECT idElementoMultimediale, titolo, formato, matricolaAutore, descrizione, durata, " +
                "dataCreazione, urlFile, urlCopertina, dataCaricamento " +
                "FROM elementoMultimediale WHERE titolo ILIKE ? ORDER BY dataCreazione DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, "%" + parolaChiave + "%"); // % = jolly, cosi' trovo la parola anche in mezzo al titolo
            try (ResultSet rs = ps.executeQuery()) { return mapAll(rs); }
        }
    }

    // non e' una delle query della traccia, mi serve solo per riempire il feed della home
    public List<ElementoMultimediale> getAll() throws SQLException {
        String sql = "SELECT idElementoMultimediale, titolo, formato, matricolaAutore, descrizione, durata, " +
                "dataCreazione, urlFile, urlCopertina, dataCaricamento " +
                "FROM elementoMultimediale ORDER BY dataCaricamento DESC";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return mapAll(rs);
        }
    }

    // Q11 - qui il join serve per prendere nome/cognome dell'autore e sommare le riproduzioni
    public ElementoMultimediale getDettaglio(int idElemento) throws SQLException {
        String sql = "SELECT e.idElementoMultimediale, e.titolo, e.formato, e.durata, e.dataCreazione, " +
                "e.descrizione, e.urlFile, e.urlCopertina, e.matricolaAutore, e.dataCaricamento, " +
                "u.nome, u.cognome, COALESCE(SUM(a.numeroRiproduzioni), 0) AS totaleRiproduzioni " +
                "FROM elementoMultimediale e " +
                "JOIN utente u ON u.matricola = e.matricolaAutore " +
                "LEFT JOIN ascolti a ON a.idElementoMultimediale = e.idElementoMultimediale " + // left join: tengo l'elemento anche se non e' mai stato ascoltato
                "WHERE e.idElementoMultimediale = ? " +
                "GROUP BY e.idElementoMultimediale, e.titolo, e.formato, e.durata, e.dataCreazione, " +
                "e.descrizione, e.urlFile, e.urlCopertina, e.matricolaAutore, e.dataCaricamento, u.nome, u.cognome";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idElemento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ElementoMultimediale e = map(rs);
                    e.setNomeAutore(rs.getString("nome"));
                    e.setCognomeAutore(rs.getString("cognome"));
                    e.setTotaleRiproduzioni(rs.getLong("totaleRiproduzioni")); // coalesce sopra evita che venga null se 0 ascolti
                    return e;
                }
            }
        }
        return null;
    }

    // Q26
    public java.util.Map<String, Integer> distribuzionePerFormato() throws SQLException {
        java.util.Map<String, Integer> m = new java.util.LinkedHashMap<>();
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT formato, COUNT(*) AS numeroElementi FROM elementoMultimediale GROUP BY formato")) {
            while (rs.next()) m.put(rs.getString("formato"), rs.getInt("numeroElementi"));
        }
        return m;
    }

    // Q28 - stesso trucco del left join ma al contrario: prendo solo dove il join NON ha trovato niente
    public List<ElementoMultimediale> maiRiprodottiDaGiorni(int giorniInattivita) throws SQLException {
        String sql = "SELECT e.idElementoMultimediale, e.titolo, e.matricolaAutore, e.dataCreazione, e.formato, " +
                "e.descrizione, e.durata, e.urlFile, e.urlCopertina, e.dataCaricamento " +
                "FROM elementoMultimediale e " +
                "LEFT JOIN ascolti a ON a.idElementoMultimediale = e.idElementoMultimediale " +
                "WHERE a.idElementoMultimediale IS NULL AND e.dataCreazione <= CURRENT_DATE - ?::int";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, giorniInattivita);
            try (ResultSet rs = ps.executeQuery()) { return mapAll(rs); }
        }
    }

    // due metodi privati per non riscrivere sempre la stessa mappatura riga -> oggetto
    private List<ElementoMultimediale> mapAll(ResultSet rs) throws SQLException {
        List<ElementoMultimediale> list = new ArrayList<>();
        while (rs.next()) list.add(map(rs));
        return list;
    }

    private ElementoMultimediale map(ResultSet rs) throws SQLException {
        ElementoMultimediale e = new ElementoMultimediale();
        e.setId(rs.getInt("idElementoMultimediale"));
        e.setTitolo(rs.getString("titolo"));
        e.setFormato(rs.getString("formato"));
        e.setMatricolaAutore(rs.getString("matricolaAutore"));
        // i try/catch qui sotto servono perche' non tutte le query selezionano tutte le colonne
        try { e.setDescrizione(rs.getString("descrizione")); } catch (SQLException ignored) { }
        try { int d = rs.getInt("durata"); e.setDurata(rs.wasNull() ? null : d); } catch (SQLException ignored) { }
        try { e.setDataCreazione(rs.getDate("dataCreazione")); } catch (SQLException ignored) { }
        try { e.setUrlFile(rs.getString("urlFile")); } catch (SQLException ignored) { }
        try { e.setUrlCopertina(rs.getString("urlCopertina")); } catch (SQLException ignored) { }
        try { e.setDataCaricamento(rs.getTimestamp("dataCaricamento")); } catch (SQLException ignored) { }
        return e;
    }
}


