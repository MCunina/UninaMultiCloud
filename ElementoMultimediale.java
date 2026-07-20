package it.unina.multicloud.model;

import java.sql.Date;
import java.sql.Timestamp;

public class ElementoMultimediale {
    private int id;
    private String titolo;
    private String descrizione;
    private String formato;          // "Audio" | "Video", il controllo vero e' un CHECK sul db
    private Integer durata;          // secondi - Integer e non int perche' puo' essere null
    private Date dataCreazione;
    private String urlFile;
    private String urlCopertina;     // puo' essere vuota, non tutti mettono la copertina
    private String matricolaAutore;
    private Timestamp dataCaricamento;

    // questi 3 non sono colonne vere, arrivano solo dalle query che fanno il join con utente/ascolti
    private String nomeAutore;
    private String cognomeAutore;
    private long totaleRiproduzioni;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitolo() { return titolo; }
    public void setTitolo(String titolo) { this.titolo = titolo; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public String getFormato() { return formato; }
    public void setFormato(String formato) { this.formato = formato; }

    public Integer getDurata() { return durata; }
    public void setDurata(Integer durata) { this.durata = durata; }

    public Date getDataCreazione() { return dataCreazione; }
    public void setDataCreazione(Date dataCreazione) { this.dataCreazione = dataCreazione; }

    public String getUrlFile() { return urlFile; }
    public void setUrlFile(String urlFile) { this.urlFile = urlFile; }

    public String getUrlCopertina() { return urlCopertina; }
    public void setUrlCopertina(String urlCopertina) { this.urlCopertina = urlCopertina; }

    public String getMatricolaAutore() { return matricolaAutore; }
    public void setMatricolaAutore(String matricolaAutore) { this.matricolaAutore = matricolaAutore; }

    public Timestamp getDataCaricamento() { return dataCaricamento; }
    public void setDataCaricamento(Timestamp d) { this.dataCaricamento = d; }

    public String getNomeAutore() { return nomeAutore; }
    public void setNomeAutore(String nomeAutore) { this.nomeAutore = nomeAutore; }

    public String getCognomeAutore() { return cognomeAutore; }
    public void setCognomeAutore(String cognomeAutore) { this.cognomeAutore = cognomeAutore; }

    public long getTotaleRiproduzioni() { return totaleRiproduzioni; }
    public void setTotaleRiproduzioni(long t) { this.totaleRiproduzioni = t; }

    // trasforma i secondi in mm:ss per non farli vedere brutti nella gui (tipo 185 -> 03:05)
    public String getDurataFormattata() {
        if (durata == null) return "--:--";
        int m = durata / 60;   // divisione intera, i minuti
        int s = durata % 60;   // resto della divisione, i secondi che avanzano
        return String.format("%02d:%02d", m, s); // %02d = sempre 2 cifre, tipo 03 invece di 3
    }

    @Override
    public String toString() { return titolo + " [" + formato + "]"; }
}
