package it.unina.multicloud.gui;

import it.unina.multicloud.dao.ElementoMultimedialeDAO;
import it.unina.multicloud.model.ElementoMultimediale;
import it.unina.multicloud.model.Utente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.sql.Date;
import java.sql.SQLException;

// il form per caricare un nuovo elemento (titolo, descrizione, tipo, file, copertina)
public class UploadDialog extends JDialog {

    private final Utente autore;
    private final ElementoMultimedialeDAO elementoDAO = new ElementoMultimedialeDAO();
    private boolean caricamentoRiuscito = false; // letto da HomeFrame dopo la chiusura per sapere se ricaricare il feed

    private File fileSelezionato;
    private File copertinaSelezionata;
    private final JLabel fileLabel = new JLabel("Nessun file selezionato");
    private final JLabel copertinaLabel = new JLabel("Nessuna copertina");

    public UploadDialog(Frame owner, Utente autore) {
        super(owner, "Carica nuovo elemento multimediale", true); // true = modale, blocca la finestra sotto
        this.autore = autore;
        setSize(480, 660); // alzata un po', altrimenti il tasto conferma finiva fuori dalla finestra
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.WHITE);

        add(buildForm(), BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.WHITE);
        panel.setBorder(new EmptyBorder(24, 26, 24, 26));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(5, 0, 5, 0);

        JLabel titoloSchermata = new JLabel("Elemento Multimediale");
        titoloSchermata.setFont(new Font("SansSerif", Font.BOLD, 18));
        titoloSchermata.setForeground(UITheme.NAVY);

        JTextField titoloField = UITheme.field();
        JTextArea descrizioneArea = new JTextArea(3, 20);
        descrizioneArea.setLineWrap(true);
        descrizioneArea.setWrapStyleWord(true); // va a capo per parola intera, non a meta'
        descrizioneArea.setFont(UITheme.FONT_FIELD);
        JScrollPane descScroll = new JScrollPane(descrizioneArea);

        JComboBox<String> tipoCombo = new JComboBox<>(new String[]{"Audio", "Video"});
        tipoCombo.setFont(UITheme.FONT_FIELD);

        JTextField durataField = UITheme.field();
        durataField.setToolTipText("Durata in secondi (opzionale)");

        JButton scegliFile = UITheme.secondaryButton("Carica Audio o Video");
        scegliFile.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Seleziona il file multimediale");
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                fileSelezionato = chooser.getSelectedFile();
                fileLabel.setText(fileSelezionato.getName());
                // guardo l'estensione e imposto da sola audio/video, cosi' non resta sempre su audio se uno si scorda
                String tipoRilevato = rilevaFormatoDaEstensione(fileSelezionato.getName());
                if (tipoRilevato != null) {
                    tipoCombo.setSelectedItem(tipoRilevato);
                }
            }
        });
        fileLabel.setForeground(UITheme.GREY_TEXT);
        fileLabel.setFont(UITheme.FONT_SUB);

        JButton scegliCopertina = UITheme.secondaryButton("Inserisci copertina");
        scegliCopertina.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Seleziona la copertina (immagine)");
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                copertinaSelezionata = chooser.getSelectedFile();
                copertinaLabel.setText(copertinaSelezionata.getName());
            }
        });
        copertinaLabel.setForeground(UITheme.GREY_TEXT);
        copertinaLabel.setFont(UITheme.FONT_SUB);

        JLabel errore = new JLabel(" ");
        errore.setForeground(UITheme.ERROR);
        errore.setFont(UITheme.FONT_SUB);

        int y = 0;
        c.gridy = y++; panel.add(titoloSchermata, c);
        c.gridy = y++; panel.add(Box.createVerticalStrut(10), c);
        c.gridy = y++; panel.add(UITheme.label("INSERISCI TITOLO"), c);
        c.gridy = y++; panel.add(titoloField, c);
        c.gridy = y++; panel.add(UITheme.label("INSERISCI DESCRIZIONE"), c);
        c.gridy = y++; panel.add(descScroll, c);
        c.gridy = y++; panel.add(UITheme.label("SELEZIONA TIPO DI ELEMENTO"), c);
        c.gridy = y++; panel.add(tipoCombo, c);
        c.gridy = y++; panel.add(UITheme.label("DURATA (secondi, opzionale)"), c);
        c.gridy = y++; panel.add(durataField, c);
        c.gridy = y++; panel.add(scegliFile, c);
        c.gridy = y++; panel.add(fileLabel, c);
        c.gridy = y++; panel.add(scegliCopertina, c);
        c.gridy = y++; panel.add(copertinaLabel, c);
        c.gridy = y++; panel.add(errore, c);

        JButton conferma = UITheme.primaryButton("Conferma");
        c.gridy = y; c.insets = new Insets(16, 0, 6, 0);
        panel.add(conferma, c);

        conferma.addActionListener(e -> {
            String titolo = titoloField.getText().trim();
            String descrizione = descrizioneArea.getText().trim();
            String tipo = (String) tipoCombo.getSelectedItem();

            if (titolo.isEmpty()) { errore.setText("Il titolo è obbligatorio."); return; }
            if (fileSelezionato == null) { errore.setText("Seleziona il file da caricare."); return; }

            Integer durata = null;
            if (!durataField.getText().trim().isEmpty()) {
                try {
                    durata = Integer.parseInt(durataField.getText().trim());
                } catch (NumberFormatException ex) {
                    errore.setText("La durata deve essere un numero intero di secondi.");
                    return;
                }
            }
            final Integer durataFinal = durata; // serve final per poterla usare dentro la classe anonima sotto

            conferma.setEnabled(false);
            errore.setText(" ");
            new SwingWorker<Void, Void>() {
                Exception failure;
                @Override protected Void doInBackground() {
                    try {
                        ElementoMultimediale el = new ElementoMultimediale();
                        el.setTitolo(titolo);
                        el.setDescrizione(descrizione.isEmpty() ? null : descrizione);
                        el.setFormato(tipo);
                        el.setDurata(durataFinal);
                        el.setDataCreazione(Date.valueOf(java.time.LocalDate.now()));
                        el.setUrlFile(fileSelezionato.getAbsolutePath());
                        el.setUrlCopertina(copertinaSelezionata != null ? copertinaSelezionata.getAbsolutePath() : null);
                        el.setMatricolaAutore(autore.getMatricola());
                        elementoDAO.carica(el);
                    } catch (SQLException ex) {
                        failure = ex;
                    }
                    return null;
                }
                @Override protected void done() {
                    conferma.setEnabled(true);
                    if (failure != null) {
                        errore.setText("Errore: " + failure.getMessage());
                        return;
                    }
                    caricamentoRiuscito = true;
                    JOptionPane.showMessageDialog(UploadDialog.this,
                            "Elemento caricato con successo!", "MESSAGGIO CONFERMA",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            }.execute();
        });

        return panel;
    }

    public boolean isCaricamentoRiuscito() { return caricamentoRiuscito; }

    // guarda l'estensione del file e prova a indovinare se e' audio o video
    private static String rilevaFormatoDaEstensione(String nomeFile) {
        String nome = nomeFile.toLowerCase();
        String[] estensioniVideo = {".mp4", ".mov", ".mkv", ".avi", ".webm", ".m4v", ".wmv"};
        String[] estensioniAudio = {".mp3", ".wav", ".m4a", ".aac", ".flac", ".ogg", ".wma"};
        for (String ext : estensioniVideo) if (nome.endsWith(ext)) return "Video";
        for (String ext : estensioniAudio) if (nome.endsWith(ext)) return "Audio";
        return null; // estensione sconosciuta, resta quello che c'era gia' nella tendina
    }
}
