package it.unina.multicloud.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

// classe modello, rappresenta una riga della tabella utente.
public class Utente {
    private String matricola;   // pk
    private String nome;
    private String cognome;
    private String email;
    private String passwordHash;   //  è l'hash, non la password vera
    private Timestamp dataRegistrazione;
    private List<String> corsiLaurea = new ArrayList<>(); // inizializzata subito per non beccare NullPointer dopo

    public Utente() { } // costruttore vuoto, usato quando riempio i campi con i setter (es leggendo dal db)

    // questo invece lo uso in registrazione, quando ho gia' i 4 dati principali
    public Utente(String matricola, String nome, String cognome, String email) {
        this.matricola = matricola;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
    }

    // da qui in poi solo getter e setter standard
    public String getMatricola() { return matricola; }
    public void setMatricola(String matricola) { this.matricola = matricola; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Timestamp getDataRegistrazione() { return dataRegistrazione; }
    public void setDataRegistrazione(Timestamp d) { this.dataRegistrazione = d; }

    public List<String> getCorsiLaurea() { return corsiLaurea; }
    public void setCorsiLaurea(List<String> corsiLaurea) { this.corsiLaurea = corsiLaurea; }

   
    public String getNomeCompleto() { return nome + " " + cognome; }

    @Override
    public String toString() { return getNomeCompleto() + " (" + matricola + ")"; } // comodo per debug
}