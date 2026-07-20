package it.unina.multicloud.dao;

import it.unina.multicloud.db.DBConnection;
import it.unina.multicloud.model.ElementoMultimediale;
import it.unina.multicloud.model.Utente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// query Q22, Q24, Q25, Q27 - ascolti e statistiche
public class AscoltoDAO {

    // Q22 - upsert: se non ho mai ascoltato quell'elemento lo inserisco, se l'ho gia' ascoltato incremento il contatore
    public void registraAscolto(String matricolaUtente, int idElemento) throws SQLException {
        String sql = "INSERT INTO ascolti (matricola, idElementoMultimediale, numeroRiproduzioni, dataUltimoAscolto) " +
                "VALUES (?, ?, 1, CURRENT_TIMESTAMP) " +
                "ON CONFLICT (matricola, idElementoMultimediale) DO UPDATE SET " + // qui e' la parte "upsert"
                "numeroRiproduzioni = ascolti.numeroRiproduzioni + 1, dataUltimoAscolto = CURRENT_TIMESTAMP";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, matricolaUtente);
            ps.setInt(2, idElemento);
            ps.executeUpdate();
        }
    }

    // Q24 - usata nella pagina tendenze. urlCopertina ce l'ho messa dopo, all'inizio mancava e le copertine non si vedevano li'
    public List<ElementoMultimediale> topElementi(int limite) throws SQLException {
        String sql = "SELECT e.idElementoMultimediale, e.titolo, e.formato, e.matricolaAutore, e.urlCopertina, " +
                "SUM(a.numeroRiproduzioni) AS totaleRiproduzioni " +
                "FROM ascolti a JOIN elementoMultimediale e ON e.idElementoMultimediale = a.idElementoMultimediale " +
                "GROUP BY e.idElementoMultimediale, e.titolo, e.formato, e.matricolaAutore, e.urlCopertina " +
                "ORDER BY totaleRiproduzioni DESC LIMIT ?";
        List<ElementoMultimediale> list = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ElementoMultimediale e = new ElementoMultimediale();
                    e.setId(rs.getInt("idElementoMultimediale"));
                    e.setTitolo(rs.getString("titolo"));
                    e.setFormato(rs.getString("formato"));
                    e.setMatricolaAutore(rs.getString("matricolaAutore"));
                    e.setUrlCopertina(rs.getString("urlCopertina"));
                    e.setTotaleRiproduzioni(rs.getLong("totaleRiproduzioni"));
                    list.add(e);
                }
            }
        }
        return list;
    }

    // Q25
    public List<ElementoMultimediale> cronologia(String matricola) throws SQLException {
        String sql = "SELECT e.idElementoMultimediale, e.titolo, e.formato, a.numeroRiproduzioni, a.dataUltimoAscolto " +
                "FROM ascolti a JOIN elementoMultimediale e ON e.idElementoMultimediale = a.idElementoMultimediale " +
                "WHERE a.matricola = ? ORDER BY a.dataUltimoAscolto DESC";
        List<ElementoMultimediale> list = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, matricola);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ElementoMultimediale e = new ElementoMultimediale();
                    e.setId(rs.getInt("idElementoMultimediale"));
                    e.setTitolo(rs.getString("titolo"));
                    e.setFormato(rs.getString("formato"));
                    e.setTotaleRiproduzioni(rs.getLong("numeroRiproduzioni")); // qui riuso il campo anche se il nome non e' proprio identico
                    list.add(e);
                }
            }
        }
        return list;
    }

    // Q27 - qui uso un Object[] al posto di una classe apposita (utente, numero playlist), forse si poteva fare meglio
    public List<Object[]> utentiConPiuPlaylist() throws SQLException {
        String sql = "SELECT u.matricola, u.nome, u.cognome, COUNT(p.idPlaylist) AS numeroPlaylist " +
                "FROM utente u JOIN playlist p ON p.matricolaProprietario = u.matricola " +
                "GROUP BY u.matricola, u.nome, u.cognome ORDER BY numeroPlaylist DESC";
        List<Object[]> list = new ArrayList<>();
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Utente u = new Utente(rs.getString("matricola"), rs.getString("nome"), rs.getString("cognome"), null);
                list.add(new Object[]{u, rs.getInt("numeroPlaylist")}); // [0] = utente, [1] = numero playlist
            }
        }
        return list;
    }
}
