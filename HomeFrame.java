package it.unina.multicloud.gui;

import it.unina.multicloud.dao.AscoltoDAO;
import it.unina.multicloud.dao.ElementoMultimedialeDAO;
import it.unina.multicloud.model.ElementoMultimediale;
import it.unina.multicloud.model.Utente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

// la schermata principale dopo il login: menu a sx, barra di ricerca sopra, elenco al centro
public class HomeFrame extends JFrame {

    private final Utente utenteCorrente; // chi ha fatto login, arriva dal costruttore
    private final ElementoMultimedialeDAO elementoDAO = new ElementoMultimedialeDAO();
    private final AscoltoDAO ascoltoDAO = new AscoltoDAO();

    private JPanel listaPanel;       // dove finiscono le card degli elementi
    private JLabel contatoreLabel;   // "elementi caricati: n"
    private JTextField ricercaField;

    public HomeFrame(Utente utente) {
        super("UninaMultiCloud - Home");
        this.utenteCorrente = utente;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // dispose e non exit, altrimenti chiude tutto il programma
        setSize(1000, 680);
        setMinimumSize(new Dimension(760, 520));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildFeed(), BorderLayout.CENTER);

        caricaFeed(null); // primo caricamento appena si apre la finestra, nessuna ricerca ancora
    }

    // ---- menu laterale ----

    private JPanel buildSidebar() {
        JPanel side = UITheme.navyPanel();
        side.setPreferredSize(new Dimension(210, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(24, 16, 24, 16));

        JLabel logo = new JLabel("\u2601 UninaMultiCloud");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 16));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(logo);
        side.add(Box.createVerticalStrut(30));

        // ogni voce del menu e' collegata a un metodo diverso con la lambda
        side.add(menuButton("FEED", e -> caricaFeed(ricercaField != null ? ricercaField.getText() : null)));
        side.add(menuButton("+ CARICA", e -> apriUpload()));
        side.add(menuButton("MY PLAYLISTS", e -> new PlaylistDialog(this, utenteCorrente).setVisible(true)));
        side.add(menuButton("TENDENZE", e -> mostraTendenze()));
        side.add(menuButton("DATI E PRIVACY", e -> new ProfileDialog(this, utenteCorrente).setVisible(true)));

        side.add(Box.createVerticalGlue()); // spazio elastico, spinge il logout in fondo

        JButton logout = UITheme.dangerButton("LOG OUT");
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logout.addActionListener(e -> {
            dispose();
            new LoginRegisterFrame().setVisible(true);
        });
        side.add(logout);

        return side;
    }

    // per non ripetere lo stesso stile per ogni voce del menu
    private JButton menuButton(String text, java.awt.event.ActionListener listener) {
        JButton b = new JButton(text);
        b.setForeground(Color.WHITE);
        b.setBackground(UITheme.NAVY);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(UITheme.FONT_BUTTON);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(listener);
        side_hover(b);
        return b;
    }

    // effetto oro al passaggio del mouse
    private void side_hover(JButton b) {
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { b.setForeground(UITheme.GOLD); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { b.setForeground(Color.WHITE); }
        });
    }

    // ---- barra in alto ----

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.WHITE);
        top.setBorder(new EmptyBorder(16, 20, 16, 20));

        ricercaField = new JTextField();
        ricercaField.setFont(UITheme.FONT_FIELD);
        ricercaField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.GOLD_DARK, 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        ricercaField.putClientProperty("JTextField.placeholderText", "Ricerca...");
        ricercaField.addActionListener(e -> caricaFeed(ricercaField.getText())); // invio = cerca

        JButton cerca = UITheme.secondaryButton("Cerca");
        cerca.addActionListener(e -> caricaFeed(ricercaField.getText()));

        JPanel searchWrap = new JPanel(new BorderLayout(8, 0));
        searchWrap.setOpaque(false);
        searchWrap.add(ricercaField, BorderLayout.CENTER);
        searchWrap.add(cerca, BorderLayout.EAST);

        JLabel benvenuto = new JLabel("Ciao, " + utenteCorrente.getNome() + " \uD83D\uDC4B");
        benvenuto.setFont(new Font("SansSerif", Font.BOLD, 16));
        benvenuto.setForeground(UITheme.NAVY);
        benvenuto.setBorder(new EmptyBorder(0, 20, 0, 0));

        top.add(searchWrap, BorderLayout.CENTER);
        top.add(benvenuto, BorderLayout.EAST);
        return top;
    }

    // ---- feed ----

    private JScrollPane buildFeed() {
        listaPanel = new JPanel();
        listaPanel.setLayout(new BoxLayout(listaPanel, BoxLayout.Y_AXIS)); // le card impilate una sotto l'altra
        listaPanel.setBackground(UITheme.WHITE);
        listaPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.WHITE);

        contatoreLabel = new JLabel("Elementi caricati: 0");
        contatoreLabel.setForeground(UITheme.GREY_TEXT);
        contatoreLabel.setBorder(new EmptyBorder(0, 20, 0, 0));
        wrapper.add(contatoreLabel, BorderLayout.NORTH);
        wrapper.add(listaPanel, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // se parolaChiave e' vuota prendo tutto, altrimenti cerco per titolo. richiamato anche dopo un upload
    private void caricaFeed(String parolaChiave) {
        listaPanel.removeAll();
        listaPanel.add(caricamentoLabel()); // messaggio "caricamento..." mentre gira la query
        listaPanel.revalidate();
        listaPanel.repaint();

        new SwingWorker<List<ElementoMultimediale>, Void>() {
            Exception failure;
            @Override protected List<ElementoMultimediale> doInBackground() {
                try {
                    if (parolaChiave == null || parolaChiave.isBlank()) {
                        return elementoDAO.getAll();
                    }
                    return elementoDAO.cercaPerTitolo(parolaChiave.trim());
                } catch (SQLException ex) {
                    failure = ex;
                    return List.of();
                }
            }
            @Override protected void done() {
                listaPanel.removeAll(); // tolgo il "caricamento..." e ricostruisco tutto
                if (failure != null) {
                    listaPanel.add(new JLabel("Errore nel caricamento: " + failure.getMessage()));
                } else {
                    try {
                        List<ElementoMultimediale> risultati = get();
                        contatoreLabel.setText("Elementi caricati: " + risultati.size());
                        if (risultati.isEmpty()) {
                            listaPanel.add(new JLabel("Nessun elemento trovato."));
                        }
                        for (ElementoMultimediale el : risultati) {
                            listaPanel.add(creaCard(el));
                            listaPanel.add(Box.createVerticalStrut(10)); // spaziatura tra una card e l'altra
                        }
                    } catch (Exception ex) {
                        listaPanel.add(new JLabel("Errore imprevisto: " + ex.getMessage()));
                    }
                }
                listaPanel.revalidate();
                listaPanel.repaint();
            }
        }.execute();
    }

    private JLabel caricamentoLabel() {
        JLabel l = new JLabel("Caricamento...");
        l.setForeground(UITheme.GREY_TEXT);
        return l;
    }

    // costruisce il rettangolo grafico per un singolo elemento, usato sia dal feed che da tendenze
    private JPanel creaCard(ElementoMultimediale el) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(12, 16, 12, 16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel icona = creaMiniatura(el, 48);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel titolo = new JLabel(el.getTitolo());
        titolo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titolo.setForeground(UITheme.NAVY);
        JLabel meta = new JLabel(el.getFormato() + " \u00b7 autore: " + el.getMatricolaAutore());
        meta.setFont(UITheme.FONT_SUB);
        meta.setForeground(UITheme.GREY_TEXT);
        info.add(titolo);
        info.add(meta);

        JPanel azioni = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        azioni.setOpaque(false);
        JButton play = UITheme.primaryButton("\u25B6 Play");
        play.addActionListener(e -> apriPlayer(el.getId()));
        JButton aggiungi = UITheme.secondaryButton("+ Playlist");
        aggiungi.addActionListener(e -> aggiungiAPlaylist(el.getId()));
        azioni.add(play);
        azioni.add(aggiungi);

        // il pulsante elimina si vede solo se l'elemento e' mio (stesso controllo che fa gia' la query Q8 nella WHERE)
        if (el.getMatricolaAutore().equals(utenteCorrente.getMatricola())) {
            JButton elimina = UITheme.dangerButton("Elimina");
            elimina.addActionListener(e -> eliminaElemento(el));
            azioni.add(elimina);
        }

        card.add(icona, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
        card.add(azioni, BorderLayout.EAST);
        return card;
    }

    // prova a caricare la copertina vera da disco, se non ci riesce torna alla vecchia emoji
    private JLabel creaMiniatura(ElementoMultimediale el, int dimensione) {
        String percorso = el.getUrlCopertina();
        if (percorso != null && !percorso.isBlank()) {
            try {
                java.io.File file = new java.io.File(percorso);
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(file);
                if (img != null) {
                    Image scalata = img.getScaledInstance(dimensione, dimensione, Image.SCALE_SMOOTH);
                    JLabel miniatura = new JLabel(new ImageIcon(scalata));
                    miniatura.setPreferredSize(new Dimension(dimensione, dimensione));
                    return miniatura;
                }
            } catch (Exception ignored) {
                // file non leggibile o corrotto, non deve mai far crashare l'app: passo all'emoji sotto
            }
        }
        JLabel icona = new JLabel("Video".equals(el.getFormato()) ? "\uD83C\uDFA5" : "\uD83C\uDFB5");
        icona.setFont(new Font("SansSerif", Font.PLAIN, (int) (dimensione * 0.55)));
        icona.setHorizontalAlignment(SwingConstants.CENTER);
        icona.setPreferredSize(new Dimension(dimensione, dimensione));
        return icona;
    }

    // premuto play su una card: prendo il dettaglio completo, segno l'ascolto, apro il player
    private void apriPlayer(int idElemento) {
        try {
            ElementoMultimediale dettaglio = elementoDAO.getDettaglio(idElemento);
            if (dettaglio == null) return;
            ascoltoDAO.registraAscolto(utenteCorrente.getMatricola(), idElemento);
            new PlayerDialog(this, dettaglio).setVisible(true);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore: " + ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aggiungiAPlaylist(int idElemento) {
        new PlaylistDialog(this, utenteCorrente, idElemento).setVisible(true); // passando l'id scatta la modalita' "aggiungi"
    }

    // Q8 - elimina un elemento multimediale, solo se chi ha cliccato ne e' davvero l'autore
    private void eliminaElemento(ElementoMultimediale el) {
        int conferma = JOptionPane.showConfirmDialog(this,
                "Eliminare definitivamente \"" + el.getTitolo() + "\"?\nVerra' rimosso anche da eventuali playlist.",
                "Conferma eliminazione", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conferma != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = elementoDAO.elimina(el.getId(), utenteCorrente.getMatricola());
            if (ok) {
                caricaFeed(ricercaField != null ? ricercaField.getText() : null); // ricarico il feed cosi' la card sparisce subito
            } else {
                // ps.executeUpdate() e' tornato 0: o l'id non esiste piu', o la matricola non corrisponde all'autore
                JOptionPane.showMessageDialog(this, "Impossibile eliminare: non risulti l'autore di questo elemento.",
                        "Operazione non riuscita", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore durante l'eliminazione: " + ex.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void apriUpload() {
        UploadDialog dialog = new UploadDialog(this, utenteCorrente);
        dialog.setVisible(true);
        if (dialog.isCaricamentoRiuscito()) {
            caricaFeed(null); // ricarico il feed cosi' vedo subito il nuovo elemento
        }
    }

    // stesso identico schema di caricaFeed, ma con topElementi al posto di getAll
    private void mostraTendenze() {
        listaPanel.removeAll();
        listaPanel.add(caricamentoLabel());
        listaPanel.revalidate();
        new SwingWorker<List<ElementoMultimediale>, Void>() {
            Exception failure;
            @Override protected List<ElementoMultimediale> doInBackground() {
                try { return ascoltoDAO.topElementi(20); }
                catch (SQLException ex) { failure = ex; return List.of(); }
            }
            @Override protected void done() {
                listaPanel.removeAll();
                if (failure != null) {
                    listaPanel.add(new JLabel("Errore nel caricamento: " + failure.getMessage()));
                } else {
                    try {
                        List<ElementoMultimediale> top = get();
                        contatoreLabel.setText("Tendenze - top " + top.size());
                        if (top.isEmpty()) listaPanel.add(new JLabel("Nessun ascolto registrato ancora."));
                        for (ElementoMultimediale el : top) {
                            JPanel card = creaCard(el); // riuso la stessa card del feed normale
                            listaPanel.add(card);
                            listaPanel.add(Box.createVerticalStrut(10));
                        }
                    } catch (Exception ignored) { }
                }
                listaPanel.revalidate();
                listaPanel.repaint();
            }
        }.execute();
    }
}
