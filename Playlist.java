package it.unina.multicloud.model;

import java.sql.Date;

public class Playlist {

    public static final String PUBBLICA = "PUBBLICA";
    public static final String PRIVATA = "PRIVATA";
    public static final String CONDIVISA = "CONDIVISA";

    private int id;
    private String titolo;
    private String visibilita;   // uno dei 3 valori sopra
    private Date dataCreazione;
    private int numeroElementi;  // tenuto aggiornato da un trigger sul db, non lo tocco mai a mano da java
    private String matricolaProprietario;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitolo() { return titolo; }
    public void setTitolo(String titolo) { this.titolo = titolo; }

    public String getVisibilita() { return visibilita; }
    public void setVisibilita(String visibilita) { this.visibilita = visibilita; }

    public Date getDataCreazione() { return dataCreazione; }
    public void setDataCreazione(Date dataCreazione) { this.dataCreazione = dataCreazione; }

    public int getNumeroElementi() { return numeroElementi; }
    public void setNumeroElementi(int numeroElementi) { this.numeroElementi = numeroElementi; }

    public String getMatricolaProprietario() { return matricolaProprietario; }
    public void setMatricolaProprietario(String m) { this.matricolaProprietario = m; }

    @Override
    public String toString() {
        return titolo + " (" + visibilita + ", " + numeroElementi + " elementi)";
    }
}
