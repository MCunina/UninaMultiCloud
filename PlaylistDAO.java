package it.unina.multicloud.dao;

import it.unina.multicloud.db.DBConnection;
import it.unina.multicloud.model.ElementoMultimediale;
import it.unina.multicloud.model.Playlist;
import it.unina.multicloud.model.Utente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// query Q12-Q21 + Q29, tutto quello che riguarda le playlist
public class PlaylistDAO {

    // Q12
    public int crea(String titolo, String visibilita, String matricolaProprietario) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "INSERT INTO playlist (titolo, visibilita, matricolaProprietario) VALUES (?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, titolo);
            ps.setString(2, visibilita);
            ps.setString(3, matricolaProprietario);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    // Q13 - la posizione non la calcolo in java, la faccio calcolare al db con una select dentro l'insert
    public void aggiungiElemento(int idPlaylist, int idElemento) throws SQLException {
        String sql = "INSERT INTO composizionePlaylist (idPlaylist, idElementoMultimediale, posizione) " +
                "SELECT ?, ?, COALESCE(MAX(posizione), 0) + 1 FROM composizionePlaylist WHERE idPlaylist = ?";
        // coalesce serve per quando la playlist e' ancora vuota (max su niente sarebbe null, +1 di null resta null)
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idPlaylist);
            ps.setInt(2, idElemento);
            ps.setInt(3, idPlaylist);
            ps.executeUpdate();
            // nota: c'e' un trigger sul db che aggiorna da solo numeroElementi in playlist, qui non tocco niente
        }
    }

    // Q14
    public void rimuoviElemento(int idPlaylist, int idElemento) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "DELETE FROM composizionePlaylist WHERE idPlaylist = ? AND idElementoMultimediale = ?")) {
            ps.setInt(1, idPlaylist);
            ps.setInt(2, idElemento);
            ps.executeUpdate();
        }
    }

    // Q15 - ordine giusto grazie a order by posizione
    public List<ElementoMultimediale> contenuto(int idPlaylist) throws SQLException {
        String sql = "SELECT cp.posizione, e.idElementoMultimediale, e.titolo, e.formato, e.durata, " +
                "e.matricolaAutore, e.descrizione, e.dataCreazione, e.urlFile, e.urlCopertina, e.dataCaricamento " +
                "FROM composizionePlaylist cp " +
                "JOIN elementoMultimediale e ON e.idElementoMultimediale = cp.idElementoMultimediale " +
                "WHERE cp.idPlaylist = ? ORDER BY cp.posizione";
        List<ElementoMultimediale> list = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idPlaylist);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ElementoMultimediale e = new ElementoMultimediale();
                    e.setId(rs.getInt("idElementoMultimediale"));
                    e.setTitolo(rs.getString("titolo"));
                    e.setFormato(rs.getString("formato"));
                    int d = rs.getInt("durata");
                    e.setDurata(rs.wasNull() ? null : d); // wasNull controlla se l'ultima getInt era in realta' un null
                    e.setMatricolaAutore(rs.getString("matricolaAutore"));
                    e.setDescrizione(rs.getString("descrizione"));
                    e.setDataCreazione(rs.getDate("dataCreazione"));
                    e.setUrlFile(rs.getString("urlFile"));
                    e.setUrlCopertina(rs.getString("urlCopertina"));
                    e.setDataCaricamento(rs.getTimestamp("dataCaricamento"));
                    list.add(e);
                }
            }
        }
        return list;
    }

    // Q16 - stesso trucco delle altre: solo il proprietario puo' farlo (and nella where)
    public boolean cambiaVisibilita(int idPlaylist, String nuovaVisibilita, String matricolaProprietario) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "UPDATE playlist SET visibilita = ? WHERE idPlaylist = ? AND matricolaProprietario = ?")) {
            ps.setString(1, nuovaVisibilita);
            ps.setInt(2, idPlaylist);
            ps.setString(3, matricolaProprietario);
            return ps.executeUpdate() > 0;
        }
    }

    // Q17
    public boolean elimina(int idPlaylist, String matricolaProprietario) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "DELETE FROM playlist WHERE idPlaylist = ? AND matricolaProprietario = ?")) {
            ps.setInt(1, idPlaylist);
            ps.setString(2, matricolaProprietario);
            return ps.executeUpdate() > 0;
        }
    }

    // Q18 - non controllo qui se la playlist e' davvero condivisa, ci pensa un trigger sul database
    public void concediAccesso(int idPlaylist, String matricolaOspite) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "INSERT INTO accessoPlaylist (idPlaylist, matricola) VALUES (?,?)")) {
            ps.setInt(1, idPlaylist);
            ps.setString(2, matricolaOspite);
            ps.executeUpdate();
        }
    }

    // Q19
    public void revocaAccesso(int idPlaylist, String matricolaOspite) throws SQLException {
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(
                "DELETE FROM accessoPlaylist WHERE idPlaylist = ? AND matricola = ?")) {
            ps.setInt(1, idPlaylist);
            ps.setString(2, matricolaOspite);
            ps.executeUpdate();
        }
    }

    // Q20
    public List<Utente> utentiAutorizzati(int idPlaylist) throws SQLException {
        String sql = "SELECT u.matricola, u.nome, u.cognome FROM accessoPlaylist ap " +
                "JOIN utente u ON u.matricola = ap.matricola WHERE ap.idPlaylist = ?";
        List<Utente> list = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idPlaylist);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Utente(rs.getString("matricola"), rs.getString("nome"), rs.getString("cognome"), null));
                }
            }
        }
        return list;
    }

    // Q21 - la piu' complicata: 3 casi in or, proprietario / pubblica / condivisa con permesso
    public List<Playlist> visibiliPerUtente(String matricolaUtente) throws SQLException {
        String sql = "SELECT DISTINCT p.idPlaylist, p.titolo, p.visibilita, p.matricolaProprietario, " +
                "p.dataCreazione, p.numeroElementi " +
                "FROM playlist p " +
                "LEFT JOIN accessoPlaylist ap ON ap.idPlaylist = p.idPlaylist AND ap.matricola = ? " +
                "WHERE p.matricolaProprietario = ? OR p.visibilita = 'PUBBLICA' " +
                "OR (p.visibilita = 'CONDIVISA' AND ap.matricola IS NOT NULL)"; // il join sopra mi dice se e' autorizzato
        List<Playlist> list = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, matricolaUtente); // usata nel join
            ps.setString(2, matricolaUtente); // usata nella where, stessa matricola due volte
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // Q29
    public Long durataTotaleSecondi(int idPlaylist) throws SQLException {
        String sql = "SELECT SUM(e.durata) AS durataTotaleSecondi FROM playlist p " +
                "JOIN composizionePlaylist cp ON cp.idPlaylist = p.idPlaylist " +
                "JOIN elementoMultimediale e ON e.idElementoMultimediale = cp.idElementoMultimediale " +
                "WHERE p.idPlaylist = ? GROUP BY p.idPlaylist";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idPlaylist);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long v = rs.getLong(1);
                    return rs.wasNull() ? null : v; // Long e non long, per poter dire "non disponibile"
                }
            }
        }
        return null;
    }

    private Playlist map(ResultSet rs) throws SQLException {
        Playlist p = new Playlist();
        p.setId(rs.getInt("idPlaylist"));
        p.setTitolo(rs.getString("titolo"));
        p.setVisibilita(rs.getString("visibilita"));
        p.setMatricolaProprietario(rs.getString("matricolaProprietario"));
        p.setDataCreazione(rs.getDate("dataCreazione"));
        p.setNumeroElementi(rs.getInt("numeroElementi"));
        return p;
    }
}

