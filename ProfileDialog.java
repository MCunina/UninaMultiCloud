package it.unina.multicloud.gui;

import it.unina.multicloud.dao.ElementoMultimedialeDAO;
import it.unina.multicloud.dao.UtenteDAO;
import it.unina.multicloud.model.ElementoMultimediale;
import it.unina.multicloud.model.Utente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

// pagina "dati e privacy": profilo, elementi caricati, cambio password
public class ProfileDialog extends JDialog {

    private final Utente utente;
    private final UtenteDAO utenteDAO = new UtenteDAO();
    private final ElementoMultimedialeDAO elementoDAO = new ElementoMultimedialeDAO();

    public ProfileDialog(Frame owner, Utente utente) {
        super(owner, "Dati e Privacy", true);
        this.utente = utente;
        setSize(480, 520);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UITheme.WHITE);
        setLayout(new BorderLayout());
        add(buildContent(), BorderLayout.CENTER);
    }

    private JComponent buildContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.WHITE);
        panel.setBorder(new EmptyBorder(24, 26, 24, 26));

        JLabel titolo = new JLabel(utente.getNomeCompleto());
        titolo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titolo.setForeground(UITheme.NAVY);
        titolo.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titolo);
        panel.add(Box.createVerticalStrut(4));
        panel.add(rigaDato("MATRICOLA", utente.getMatricola()));
        panel.add(rigaDato("E-MAIL", utente.getEmail()));
        panel.add(rigaDato("CORSO DI LAUREA", String.join(", ", utente.getCorsiLaurea()))); // piu' corsi separati da virgola
        panel.add(Box.createVerticalStrut(16));

        JLabel elementiTitolo = UITheme.label("ELEMENTI CARICATI");
        elementiTitolo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(elementiTitolo);
        panel.add(Box.createVerticalStrut(4));

        DefaultListModel<ElementoMultimediale> model = new DefaultListModel<>();
        JList<ElementoMultimediale> list = new JList<>(model);
        list.setCellRenderer((l, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value.getTitolo() + " [" + value.getFormato() + "] - " + value.getDataCreazione());
            lbl.setBorder(new EmptyBorder(4, 4, 4, 4));
            return lbl;
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setPreferredSize(new Dimension(400, 150));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        panel.add(scroll);
        panel.add(Box.createVerticalStrut(16));

        JLabel cambioPasswordLabel = UITheme.label("CAMBIA PASSWORD");
        cambioPasswordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPasswordField nuovaPass = UITheme.passwordField();
        nuovaPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton aggiornaBtn = UITheme.secondaryButton("Aggiorna password");
        aggiornaBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel stato = new JLabel(" ");
        stato.setAlignmentX(Component.LEFT_ALIGNMENT);
        stato.setForeground(UITheme.GREY_TEXT);

        aggiornaBtn.addActionListener(e -> {
            String nuova = new String(nuovaPass.getPassword());
            if (nuova.length() < 8) {
                stato.setText("La password deve avere almeno 8 caratteri.");
                stato.setForeground(UITheme.ERROR);
                return;
            }
            new SwingWorker<Void, Void>() {
                Exception failure;
                @Override protected Void doInBackground() {
                    try { utenteDAO.aggiornaPassword(utente.getMatricola(), nuova); }
                    catch (SQLException ex) { failure = ex; }
                    return null;
                }
                @Override protected void done() {
                    if (failure != null) {
                        stato.setForeground(UITheme.ERROR);
                        stato.setText("Errore: " + failure.getMessage());
                    } else {
                        stato.setForeground(new Color(60, 140, 80)); // verde, per dire che e' andata bene
                        stato.setText("Password aggiornata.");
                        nuovaPass.setText("");
                    }
                }
            }.execute();
        });

        panel.add(cambioPasswordLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(nuovaPass);
        panel.add(Box.createVerticalStrut(8));
        panel.add(aggiornaBtn);
        panel.add(Box.createVerticalStrut(6));
        panel.add(stato);

        // carico gli elementi dell'utente in background, come al solito
        new SwingWorker<List<ElementoMultimediale>, Void>() {
            Exception failure;
            @Override protected List<ElementoMultimediale> doInBackground() {
                try { return elementoDAO.getByAutore(utente.getMatricola()); }
                catch (SQLException ex) { failure = ex; return List.of(); }
            }
            @Override protected void done() {
                if (failure == null) {
                    try {
                        List<ElementoMultimediale> el = get();
                        for (ElementoMultimediale e : el) model.addElement(e);
                        elementiTitolo.setText("ELEMENTI CARICATI: " + el.size());
                    } catch (Exception ignored) { }
                }
            }
        }.execute();

        return new JScrollPane(panel);
    }

    // etichetta sopra + valore sotto, usato per matricola/email/corso cosi' non riscrivo 3 volte la stessa cosa
    private JPanel rigaDato(String label, String valore) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel l = UITheme.label(label);
        JLabel v = new JLabel(valore == null || valore.isBlank() ? "-" : valore);
        v.setFont(UITheme.FONT_FIELD);
        v.setForeground(UITheme.NAVY);
        JPanel inner = new JPanel(new GridLayout(2, 1));
        inner.setOpaque(false);
        inner.add(l);
        inner.add(v);
        p.add(inner, BorderLayout.CENTER);
        return p;
    }
}