package it.unina.multicloud.gui;

import it.unina.multicloud.dao.UtenteDAO;
import it.unina.multicloud.model.Utente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

// prima finestra che si vede, con le due tab accedi/registrati
public class LoginRegisterFrame extends JFrame {

    private final UtenteDAO utenteDAO = new UtenteDAO();

    public LoginRegisterFrame() {
        super("UninaMultiCloud - Accedi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 620);
        setMinimumSize(new Dimension(420, 560));
        setLocationRelativeTo(null); // centra la finestra nello schermo
        getContentPane().setBackground(UITheme.NAVY);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BUTTON);
        tabs.addTab("ACCEDI", buildLoginPanel());
        tabs.addTab("REGISTRATI", buildRegisterPanel());
        tabs.setBorder(new EmptyBorder(10, 20, 20, 20));
        tabs.setOpaque(false);
        add(tabs, BorderLayout.CENTER);
    }

    // logo + sottotitolo in alto
    private JPanel buildHeader() {
        JPanel header = UITheme.navyPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(28, 20, 12, 20));

        JLabel logo = new JLabel("\u2601 UninaMultiCloud", SwingConstants.CENTER);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = new JLabel("Condividi e ascolta contenuti multimediali", SwingConstants.CENTER);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        UITheme.applyNiceHeader(logo, sub);
        header.add(logo);
        header.add(Box.createVerticalStrut(6)); // spazio vuoto tra le due label
        header.add(sub);
        return header;
    }

    // ---- login ----

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.WHITE);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(6, 0, 6, 0);

        JTextField emailField = UITheme.field();
        JPasswordField passField = UITheme.passwordField();
        JLabel errore = new JLabel(" "); // spazio vuoto invece di stringa vuota, cosi' la label non collassa a 0 altezza
        errore.setForeground(UITheme.ERROR);
        errore.setFont(UITheme.FONT_SUB);

        c.gridy = 0; panel.add(UITheme.label("EMAIL (@unina.it / @studenti.unina.it)"), c);
        c.gridy = 1; panel.add(emailField, c);
        c.gridy = 2; panel.add(Box.createVerticalStrut(10), c);
        c.gridy = 3; panel.add(UITheme.label("PASSWORD"), c);
        c.gridy = 4; panel.add(passField, c);
        c.gridy = 5; panel.add(errore, c);

        JButton loginBtn = UITheme.primaryButton("ACCEDI");
        c.gridy = 6; c.insets = new Insets(20, 0, 6, 0); panel.add(loginBtn, c);

        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String pass = new String(passField.getPassword());
            if (email.isEmpty() || pass.isEmpty()) {
                errore.setText("Inserisci email e password.");
                return;
            }
            loginBtn.setEnabled(false); // disabilito il bottone finche' non ho la risposta, evita doppio click
            errore.setText(" ");
            // swingworker perche' la query va fatta fuori dall'edt, altrimenti la finestra si blocca
            new SwingWorker<Utente, Void>() {
                Exception failure;
                @Override protected Utente doInBackground() {
                    try {
                        return utenteDAO.login(email, pass);
                    } catch (SQLException ex) {
                        failure = ex;
                        return null;
                    }
                }
                @Override protected void done() {
                    loginBtn.setEnabled(true);
                    if (failure != null) {
                        errore.setText("Errore di connessione al database: " + failure.getMessage());
                        return;
                    }
                    try {
                        Utente u = get();
                        if (u == null) {
                            errore.setText("Credenziali non valide.");
                        } else {
                            SwingUtilities.getWindowAncestor(panel).dispose(); // chiudo questa finestra
                            new HomeFrame(u).setVisible(true); // e apro la home passando l'utente loggato
                        }
                    } catch (Exception ex) {
                        errore.setText("Errore imprevisto: " + ex.getMessage());
                    }
                }
            }.execute();
        });

        return wrapScroll(panel);
    }

    // ---- registrazione ----

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.WHITE);
        panel.setBorder(new EmptyBorder(20, 24, 24, 24));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(4, 0, 4, 0);

        JTextField matricolaField = UITheme.field();
        JTextField nomeField = UITheme.field();
        JTextField cognomeField = UITheme.field();
        JTextField emailField = UITheme.field();
        JTextField corsoField = UITheme.field();
        JPasswordField passField = UITheme.passwordField();
        JPasswordField confermaField = UITheme.passwordField();
        JLabel errore = new JLabel(" ");
        errore.setForeground(UITheme.ERROR);
        errore.setFont(UITheme.FONT_SUB);

        // y++ per non dover riscrivere il numero ogni volta, comodo quando aggiungo/tolgo un campo
        int y = 0;
        c.gridy = y++; panel.add(UITheme.label("MATRICOLA"), c);
        c.gridy = y++; panel.add(matricolaField, c);
        c.gridy = y++; panel.add(UITheme.label("NOME"), c);
        c.gridy = y++; panel.add(nomeField, c);
        c.gridy = y++; panel.add(UITheme.label("COGNOME"), c);
        c.gridy = y++; panel.add(cognomeField, c);
        c.gridy = y++; panel.add(UITheme.label("E-MAIL (dominio unina.it)"), c);
        c.gridy = y++; panel.add(emailField, c);
        c.gridy = y++; panel.add(UITheme.label("CORSO DI LAUREA"), c);
        c.gridy = y++; panel.add(corsoField, c);
        c.gridy = y++; panel.add(UITheme.label("PASSWORD (min. 8 caratteri)"), c);
        c.gridy = y++; panel.add(passField, c);
        c.gridy = y++; panel.add(UITheme.label("CONFERMA PASSWORD"), c);
        c.gridy = y++; panel.add(confermaField, c);
        c.gridy = y++; panel.add(errore, c);

        JButton registerBtn = UITheme.primaryButton("REGISTRATI");
        c.gridy = y; c.insets = new Insets(16, 0, 6, 0); panel.add(registerBtn, c);

        registerBtn.addActionListener(e -> {
            String matricola = matricolaField.getText().trim();
            String nome = nomeField.getText().trim();
            String cognome = cognomeField.getText().trim();
            String email = emailField.getText().trim();
            String corso = corsoField.getText().trim();
            String pass = new String(passField.getPassword());
            String conferma = new String(confermaField.getPassword());

            // validazioni lato client prima, per non fare un giro a vuoto sul database se manca qualcosa di ovvio
            if (matricola.isEmpty() || nome.isEmpty() || cognome.isEmpty() || email.isEmpty()) {
                errore.setText("Compila tutti i campi obbligatori.");
                return;
            }
            if (!email.endsWith("@unina.it") && !email.endsWith("@studenti.unina.it")) {
                errore.setText("L'email deve avere dominio @unina.it o @studenti.unina.it.");
                return;
            }
            if (pass.length() < 8) {
                errore.setText("La password deve avere almeno 8 caratteri.");
                return;
            }
            if (!pass.equals(conferma)) {
                errore.setText("Le due password non coincidono.");
                return;
            }

            registerBtn.setEnabled(false);
            errore.setText(" ");
            new SwingWorker<Void, Void>() {
                Exception failure;
                @Override protected Void doInBackground() {
                    try {
                        // controllo prima se matricola/email esistono gia', per un messaggio piu' chiaro
                        if (utenteDAO.matricolaEsiste(matricola)) {
                            failure = new SQLException("Matricola già registrata.");
                            return null;
                        }
                        if (utenteDAO.emailEsiste(email)) {
                            failure = new SQLException("Email già registrata.");
                            return null;
                        }
                        Utente u = new Utente(matricola, nome, cognome, email);
                        u.setPasswordHash(UtenteDAO.sha256(pass)); // hasho qui, non salvo mai la password vera
                        utenteDAO.registraUtente(u, corso);
                    } catch (SQLException ex) {
                        failure = ex;
                    }
                    return null;
                }
                @Override protected void done() {
                    registerBtn.setEnabled(true);
                    if (failure != null) {
                        errore.setText(failure.getMessage());
                        return;
                    }
                    JOptionPane.showMessageDialog(panel,
                            "Registrazione completata! Ora puoi accedere.",
                            "MESSAGGIO CONFERMA", JOptionPane.INFORMATION_MESSAGE);
                    // svuoto tutti i campi dopo una registrazione riuscita
                    matricolaField.setText("");
                    nomeField.setText("");
                    cognomeField.setText("");
                    emailField.setText("");
                    corsoField.setText("");
                    passField.setText("");
                    confermaField.setText("");
                }
            }.execute();
        });

        return wrapScroll(panel);
    }

    // avvolge il form in una scrollbar, utile per il tab registrati che ha tanti campi
    private JPanel wrapScroll(JPanel inner) {
        JScrollPane scroll = new JScrollPane(inner);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16); // scroll un po' piu' veloce del default
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(UITheme.WHITE);
        outer.add(scroll, BorderLayout.CENTER);
        return outer;
    }
}
