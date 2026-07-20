package it.unina.multicloud.gui;

import it.unina.multicloud.model.ElementoMultimediale;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

// finestra del player. per i .wav suona davvero dentro l'app, per il resto apre il player del sistema
public class PlayerDialog extends JDialog {

    private Clip clip; // tenuto come campo cosi' posso fermarlo da un altro punto (es chiudendo la finestra)

    public PlayerDialog(Frame owner, ElementoMultimediale elemento) {
        super(owner, "Player - " + elemento.getTitolo(), true);
        setSize(460, 320);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UITheme.NAVY);
        setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(30, 30, 20, 30));

        JLabel icona = creaCopertinaOEmoji(elemento, 140);
        icona.setAlignmentX(Component.CENTER_ALIGNMENT);
        icona.setForeground(UITheme.GOLD);

        JLabel titolo = new JLabel(elemento.getTitolo(), SwingConstants.CENTER);
        titolo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titolo.setForeground(Color.WHITE);
        titolo.setAlignmentX(Component.CENTER_ALIGNMENT);

        String autore = elemento.getNomeAutore() != null
                ? elemento.getNomeAutore() + " " + elemento.getCognomeAutore()
                : elemento.getMatricolaAutore(); // se il dettaglio non ha fatto il join, mostro almeno la matricola
        JLabel sub = new JLabel(autore + "   \u00b7   " + elemento.getDurataFormattata()
                + "   \u00b7   " + elemento.getTotaleRiproduzioni() + " riproduzioni", SwingConstants.CENTER);
        sub.setForeground(UITheme.GOLD);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(icona);
        content.add(Box.createVerticalStrut(10));
        content.add(titolo);
        content.add(Box.createVerticalStrut(4));
        content.add(sub);
        content.add(Box.createVerticalStrut(24));

        JPanel controlli = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        controlli.setOpaque(false);
        JButton playBtn = UITheme.primaryButton("\u25B6 Riproduci");
        controlli.add(playBtn);
        content.add(controlli);

        JLabel stato = new JLabel(" ", SwingConstants.CENTER);
        stato.setForeground(Color.WHITE);
        stato.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(Box.createVerticalStrut(8));
        content.add(stato);

        File file = elemento.getUrlFile() != null ? new File(elemento.getUrlFile()) : null;

        // un solo pulsante che funziona per qualsiasi formato: prova prima il player integrato
        // (solo wav, javax.sound non decodifica mp3/mp4 senza librerie in piu'), se non va apre
        // il player di sistema, che invece i formati compressi li apre benissimo
        playBtn.addActionListener(e -> {
            if (file == null || !file.exists()) {
                stato.setText("File non trovato sul disco locale: " + (file == null ? "percorso mancante" : file.getAbsolutePath()));
                return;
            }
            if (file.getName().toLowerCase().endsWith(".wav")) {
                try {
                    stopClip(); // se stava gia' suonando qualcosa lo fermo prima
                    AudioInputStream ais = AudioSystem.getAudioInputStream(file);
                    clip = AudioSystem.getClip();
                    clip.open(ais);
                    clip.start();
                    stato.setText("In riproduzione nel player integrato...");
                    return;
                } catch (Exception ex) {
                    stato.setText("Player integrato non disponibile (" + ex.getMessage() + "), apro il player di sistema...");
                    // non faccio return, continuo sotto e provo comunque col player di sistema
                }
            }
            try {
                Desktop.getDesktop().open(file); // stesso effetto del doppio click sul file da esplora risorse
                stato.setText("Aperto nel player predefinito del sistema.");
            } catch (Exception ex) {
                stato.setText("Impossibile aprire il file: " + ex.getMessage());
            }
        });

        add(content, BorderLayout.CENTER);

        // se chiudo la finestra mentre suona un wav, lo fermo, altrimenti continua a suonare da solo
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { stopClip(); }
        });
    }

    // stessa idea di creaMiniatura in HomeFrame, duplicata qui perche' sono due classi diverse
    private JLabel creaCopertinaOEmoji(ElementoMultimediale elemento, int dimensione) {
        String percorso = elemento.getUrlCopertina();
        if (percorso != null && !percorso.isBlank()) {
            try {
                java.io.File file = new java.io.File(percorso);
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(file);
                if (img != null) {
                    Image scalata = img.getScaledInstance(dimensione, dimensione, Image.SCALE_SMOOTH);
                    JLabel l = new JLabel(new ImageIcon(scalata), SwingConstants.CENTER);
                    l.setPreferredSize(new Dimension(dimensione, dimensione));
                    return l;
                }
            } catch (Exception ignored) {
                // se non riesco a leggerla passo all'emoji, niente crash
            }
        }
        JLabel icona = new JLabel("Video".equals(elemento.getFormato()) ? "\uD83C\uDFA5" : "\uD83C\uDFB5", SwingConstants.CENTER);
        icona.setFont(new Font("SansSerif", Font.PLAIN, 60));
        return icona;
    }

    private void stopClip() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }
}
