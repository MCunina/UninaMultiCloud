package it.unina.multicloud.gui;

import it.unina.multicloud.dao.ElementoMultimedialeDAO;
import it.unina.multicloud.dao.PlaylistDAO;
import it.unina.multicloud.model.ElementoMultimediale;
import it.unina.multicloud.model.Playlist;
import it.unina.multicloud.model.Utente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

// gestisce sia "le mie playlist" che "aggiungi questo elemento a una playlist" nella stessa classe
public class PlaylistDialog extends JDialog {

    private final Utente utenteCorrente;
    private final PlaylistDAO playlistDAO = new PlaylistDAO();
    private final ElementoMultimedialeDAO elementoDAO = new ElementoMultimedialeDAO();
    private final it.unina.multicloud.dao.AscoltoDAO ascoltoDAO = new it.unina.multicloud.dao.AscoltoDAO();
    private final int idElementoDaAggiungere; // -1 = nessun elemento, sto solo guardando le playlist

    private DefaultListModel<Playlist> listModel = new DefaultListModel<>();
    private JList<Playlist> playlistJList = new JList<>(listModel);
    private JPanel dettaglioPanel;

    // costruttore "normale", solo per guardare le playlist
    public PlaylistDialog(Frame owner, Utente utente) {
        this(owner, utente, -1); // -1 = sentinella, vuol dire "nessun elemento da aggiungere"
    }

    // questo invece si apre quando premo "+ Playlist" su un elemento
    public PlaylistDialog(Frame owner, Utente utente, int idElementoDaAggiungere) {
        super(owner, idElementoDaAggiungere >= 0 ? "Aggiungi a una playlist" : "Le mie playlist", true);
        this.utenteCorrente = utente;
        this.idElementoDaAggiungere = idElementoDaAggiungere;
        setSize(820, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.WHITE);

        add(buildSplit(), BorderLayout.CENTER);
        caricaPlaylist();
    }

    // pannello diviso in due: lista playlist a sx, dettaglio a dx
    private JSplitPane buildSplit() {
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(UITheme.WHITE);
        left.setBorder(new EmptyBorder(14, 14, 14, 8));

        playlistJList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel l = new JLabel(value.getTitolo() + "  [" + value.getVisibilita() + "]  (" + value.getNumeroElementi() + ")");
            l.setOpaque(true);
            l.setBorder(new EmptyBorder(8, 8, 8, 8));
            l.setBackground(isSelected ? UITheme.GOLD : Color.WHITE);
            l.setForeground(UITheme.NAVY);
            return l;
        });
        playlistJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) mostraDettaglio(playlistJList.getSelectedValue()); // valueIsAdjusting evita di richiamarlo piu' volte durante lo scroll
        });

        JButton nuova = UITheme.primaryButton("+ Nuova playlist");
        nuova.addActionListener(e -> creaPlaylist());

        left.add(new JScrollPane(playlistJList), BorderLayout.CENTER);
        left.add(nuova, BorderLayout.SOUTH);

        dettaglioPanel = new JPanel(new BorderLayout());
        dettaglioPanel.setBackground(UITheme.WHITE);
        dettaglioPanel.setBorder(new EmptyBorder(14, 8, 14, 14));
        dettaglioPanel.add(new JLabel("Seleziona una playlist a sinistra."), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, dettaglioPanel);
        split.setDividerLocation(300);
        return split;
    }

    private void caricaPlaylist() {
        new SwingWorker<List<Playlist>, Void>() {
            Exception failure;
            @Override protected List<Playlist> doInBackground() {
                try { return playlistDAO.visibiliPerUtente(utenteCorrente.getMatricola()); }
                catch (SQLException ex) { failure = ex; return List.of(); }
            }
            @Override protected void done() {
                listModel.clear();
                if (failure != null) {
                    JOptionPane.showMessageDialog(PlaylistDialog.this, "Errore: " + failure.getMessage());
                    return;
                }
                try {
                    for (Playlist p : get()) listModel.addElement(p);
                } catch (Exception ignored) { }
            }
        }.execute();
    }

    // form veloce con JOptionPane invece di costruire un intero dialog a mano
    private void creaPlaylist() {
        JTextField titoloField = UITheme.field();
        JComboBox<String> visibilitaCombo = new JComboBox<>(new String[]{Playlist.PRIVATA, Playlist.PUBBLICA, Playlist.CONDIVISA});
        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.add(UITheme.label("Titolo playlist"));
        form.add(titoloField);
        form.add(UITheme.label("Visibilità"));
        form.add(visibilitaCombo);

        int scelta = JOptionPane.showConfirmDialog(this, form, "Nuova playlist", JOptionPane.OK_CANCEL_OPTION);
        if (scelta != JOptionPane.OK_OPTION) return; // annullato, non faccio niente
        String titolo = titoloField.getText().trim();
        if (titolo.isEmpty()) return;
        String visibilita = (String) visibilitaCombo.getSelectedItem();

        new SwingWorker<Void, Void>() {
            Exception failure;
            @Override protected Void doInBackground() {
                try { playlistDAO.crea(titolo, visibilita, utenteCorrente.getMatricola()); }
                catch (SQLException ex) { failure = ex; }
                return null;
            }
            @Override protected void done() {
                if (failure != null) {
                    JOptionPane.showMessageDialog(PlaylistDialog.this, "Errore: " + failure.getMessage());
                } else {
                    caricaPlaylist(); // ricarico la lista cosi' compare subito la nuova playlist
                }
            }
        }.execute();
    }

    // ricostruisce da zero il pannello di destra ogni volta che cambia la playlist selezionata
    private void mostraDettaglio(Playlist p) {
        dettaglioPanel.removeAll();
        if (p == null) {
            dettaglioPanel.revalidate();
            dettaglioPanel.repaint();
            return;
        }
        boolean isProprietario = p.getMatricolaProprietario().equals(utenteCorrente.getMatricola());

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        JLabel titolo = new JLabel(p.getTitolo());
        titolo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titolo.setForeground(UITheme.NAVY);
        JLabel sub = new JLabel("Visibilità: " + p.getVisibilita() + "   \u00b7   Elementi: " + p.getNumeroElementi());
        sub.setForeground(UITheme.GREY_TEXT);
        header.add(titolo);
        header.add(sub);

        JPanel azioni = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        azioni.setOpaque(false);

        // questo bottone compare solo se sono in "modalita' aggiungi elemento"
        if (idElementoDaAggiungere >= 0) {
            JButton aggiungi = UITheme.primaryButton("Aggiungi elemento qui");
            aggiungi.addActionListener(e -> aggiungiElementoCorrente(p));
            azioni.add(aggiungi);
        }
        // questi altri invece solo se sono il proprietario della playlist
        if (isProprietario) {
            JButton cambiaVisibilita = UITheme.secondaryButton("Cambia visibilità");
            cambiaVisibilita.addActionListener(e -> cambiaVisibilita(p));
            JButton elimina = UITheme.dangerButton("Elimina playlist");
            elimina.addActionListener(e -> eliminaPlaylist(p));
            azioni.add(cambiaVisibilita);
            azioni.add(elimina);
            if (Playlist.CONDIVISA.equals(p.getVisibilita())) {
                JButton condividi = UITheme.secondaryButton("Condividi con...");
                condividi.addActionListener(e -> condividiPlaylist(p));
                azioni.add(condividi);
            }
        }

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(azioni, BorderLayout.SOUTH);

        DefaultListModel<ElementoMultimediale> contenutoModel = new DefaultListModel<>();
        JList<ElementoMultimediale> contenutoList = new JList<>(contenutoModel);
        contenutoList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel l = new JLabel((index + 1) + ". " + value.getTitolo() + " [" + value.getFormato() + "]");
            l.setOpaque(true);
            l.setBorder(new EmptyBorder(6, 8, 6, 8));
            l.setBackground(isSelected ? UITheme.GOLD : Color.WHITE);
            return l;
        });
        // doppio click su un brano lo fa partire subito
        contenutoList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) { // 2 = doppio click, non ci sono altri metodi da sovrascrivere qui
                    ElementoMultimediale sel = contenutoList.getSelectedValue();
                    if (sel != null) riproduciElemento(sel.getId());
                }
            }
        });

        JButton riproduci = UITheme.primaryButton("\u25B6 Riproduci");
        riproduci.addActionListener(e -> {
            ElementoMultimediale sel = contenutoList.getSelectedValue();
            if (sel == null) {
                JOptionPane.showMessageDialog(PlaylistDialog.this, "Seleziona prima un elemento dall'elenco.");
                return;
            }
            riproduciElemento(sel.getId());
        });

        JButton rimuovi = UITheme.dangerButton("Rimuovi elemento selezionato");
        rimuovi.addActionListener(e -> {
            ElementoMultimediale sel = contenutoList.getSelectedValue();
            if (sel == null) return;
            new SwingWorker<Void, Void>() {
                Exception failure;
                @Override protected Void doInBackground() {
                    try { playlistDAO.rimuoviElemento(p.getId(), sel.getId()); }
                    catch (SQLException ex) { failure = ex; }
                    return null;
                }
                @Override protected void done() {
                    if (failure != null) JOptionPane.showMessageDialog(PlaylistDialog.this, "Errore: " + failure.getMessage());
                    else { mostraDettaglio(p); caricaPlaylist(); } // ricarico tutto per aggiornare il contatore elementi
                }
            }.execute();
        });

        JPanel azioniContenuto = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        azioniContenuto.setOpaque(false);
        azioniContenuto.add(riproduci); // riproduci sempre visibile, chiunque puo' ascoltare
        if (isProprietario) azioniContenuto.add(rimuovi); // rimuovi solo per il proprietario

        dettaglioPanel.add(top, BorderLayout.NORTH);
        dettaglioPanel.add(new JScrollPane(contenutoList), BorderLayout.CENTER);
        dettaglioPanel.add(azioniContenuto, BorderLayout.SOUTH);

        new SwingWorker<List<ElementoMultimediale>, Void>() {
            Exception failure;
            @Override protected List<ElementoMultimediale> doInBackground() {
                try { return playlistDAO.contenuto(p.getId()); }
                catch (SQLException ex) { failure = ex; return List.of(); }
            }
            @Override protected void done() {
                if (failure == null) {
                    try { for (ElementoMultimediale el : get()) contenutoModel.addElement(el); }
                    catch (Exception ignored) { }
                }
            }
        }.execute();

        dettaglioPanel.revalidate();
        dettaglioPanel.repaint();
    }

    // stesso schema di HomeFrame.apriPlayer: dettaglio completo, registro l'ascolto, apro il player
    private void riproduciElemento(int idElemento) {
        new SwingWorker<ElementoMultimediale, Void>() {
            Exception failure;
            @Override protected ElementoMultimediale doInBackground() {
                try {
                    ElementoMultimediale dettaglio = elementoDAO.getDettaglio(idElemento);
                    if (dettaglio != null) ascoltoDAO.registraAscolto(utenteCorrente.getMatricola(), idElemento);
                    return dettaglio;
                } catch (SQLException ex) {
                    failure = ex;
                    return null;
                }
            }
            @Override protected void done() {
                if (failure != null) {
                    JOptionPane.showMessageDialog(PlaylistDialog.this, "Errore: " + failure.getMessage());
                    return;
                }
                try {
                    ElementoMultimediale dettaglio = get();
                    if (dettaglio != null) new PlayerDialog(getOwnerFrame(), dettaglio).setVisible(true);
                } catch (Exception ignored) { }
            }
        }.execute();
    }

    // PlaylistDialog e' un JDialog, non un JFrame: mi serve il Frame proprietario per aprire il PlayerDialog
    private Frame getOwnerFrame() {
        return this.getOwner() instanceof Frame ? (Frame) this.getOwner() : null;
    }

    // usato solo in "modalita' aggiungi", collega idElementoDaAggiungere alla playlist scelta
    private void aggiungiElementoCorrente(Playlist p) {
        new SwingWorker<Void, Void>() {
            Exception failure;
            @Override protected Void doInBackground() {
                try { playlistDAO.aggiungiElemento(p.getId(), idElementoDaAggiungere); }
                catch (SQLException ex) { failure = ex; }
                return null;
            }
            @Override protected void done() {
                if (failure != null) {
                    JOptionPane.showMessageDialog(PlaylistDialog.this, "Errore: " + failure.getMessage());
                } else {
                    JOptionPane.showMessageDialog(PlaylistDialog.this, "Elemento aggiunto alla playlist.");
                    dispose(); // chiudo la finestra, il compito e' finito
                }
            }
        }.execute();
    }

    private void cambiaVisibilita(Playlist p) {
        String[] opzioni = {Playlist.PRIVATA, Playlist.PUBBLICA, Playlist.CONDIVISA};
        String scelta = (String) JOptionPane.showInputDialog(this, "Nuova visibilità:", "Cambia visibilità",
                JOptionPane.PLAIN_MESSAGE, null, opzioni, p.getVisibilita());
        if (scelta == null) return; // annullato
        new SwingWorker<Void, Void>() {
            Exception failure;
            @Override protected Void doInBackground() {
                try { playlistDAO.cambiaVisibilita(p.getId(), scelta, utenteCorrente.getMatricola()); }
                catch (SQLException ex) { failure = ex; }
                return null;
            }
            @Override protected void done() {
                if (failure != null) JOptionPane.showMessageDialog(PlaylistDialog.this, "Errore: " + failure.getMessage());
                caricaPlaylist();
            }
        }.execute();
    }

    private void eliminaPlaylist(Playlist p) {
        int conferma = JOptionPane.showConfirmDialog(this, "Eliminare la playlist \"" + p.getTitolo() + "\"?",
                "Conferma eliminazione", JOptionPane.YES_NO_OPTION);
        if (conferma != JOptionPane.YES_OPTION) return; // chiede conferma prima, azione distruttiva
        new SwingWorker<Void, Void>() {
            Exception failure;
            @Override protected Void doInBackground() {
                try { playlistDAO.elimina(p.getId(), utenteCorrente.getMatricola()); }
                catch (SQLException ex) { failure = ex; }
                return null;
            }
            @Override protected void done() {
                if (failure != null) JOptionPane.showMessageDialog(PlaylistDialog.this, "Errore: " + failure.getMessage());
                dettaglioPanel.removeAll();
                dettaglioPanel.revalidate();
                dettaglioPanel.repaint();
                caricaPlaylist();
            }
        }.execute();
    }

    // uso una sintassi tipo "-matricola" per revocare, senza matricola per concedere. non elegantissimo ma funziona
    private void condividiPlaylist(Playlist p) {
        String matricolaOspite = JOptionPane.showInputDialog(this,
                "Matricola dell'utente a cui concedere l'accesso (o rimuovere con '-matricola'):");
        if (matricolaOspite == null || matricolaOspite.isBlank()) return;
        boolean revoca = matricolaOspite.trim().startsWith("-");
        String matricola = revoca ? matricolaOspite.trim().substring(1) : matricolaOspite.trim();

        new SwingWorker<Void, Void>() {
            Exception failure;
            @Override protected Void doInBackground() {
                try {
                    if (revoca) playlistDAO.revocaAccesso(p.getId(), matricola);
                    else playlistDAO.concediAccesso(p.getId(), matricola);
                } catch (SQLException ex) { failure = ex; }
                return null;
            }
            @Override protected void done() {
                if (failure != null) JOptionPane.showMessageDialog(PlaylistDialog.this, "Errore: " + failure.getMessage());
                else JOptionPane.showMessageDialog(PlaylistDialog.this, revoca ? "Accesso revocato." : "Accesso concesso.");
            }
        }.execute();
    }
}
