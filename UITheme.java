package it.unina.multicloud.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// classe di appoggio solo per lo stile grafico, colori presi dal logo dell'app. niente logica qui dentro
public final class UITheme {
    public static final Color NAVY        = new Color(5, 45, 96);
    public static final Color NAVY_DARK   = new Color(3, 28, 61);
    public static final Color NAVY_LIGHT  = new Color(16, 66, 130);
    public static final Color GOLD        = new Color(212, 176, 118);
    public static final Color GOLD_DARK   = new Color(178, 143, 85);
    public static final Color WHITE       = new Color(250, 250, 248);
    public static final Color GREY_TEXT   = new Color(90, 100, 115);
    public static final Color CARD_BG     = Color.WHITE;
    public static final Color ERROR       = new Color(196, 60, 60);

    public static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD, 26);
    public static final Font FONT_SUB     = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_LABEL   = new Font("SansSerif", Font.BOLD, 12);
    public static final Font FONT_FIELD   = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_BUTTON  = new Font("SansSerif", Font.BOLD, 14);

    private UITheme() { } // solo metodi statici, non ha senso creare oggetti di questa classe

    // chiamato una volta sola in Main, prima di aprire la prima finestra
    public static void apply() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // stile del sistema operativo
        } catch (Exception ignored) { }
        UIManager.put("ToolTip.background", NAVY);
        UIManager.put("ToolTip.foreground", Color.WHITE);
    }

    // 3 "fabbriche" di pulsanti diversi, per non riscrivere ogni volta gli stessi settaggi
    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, GOLD, NAVY_DARK, GOLD_DARK); // il pulsante principale, oro
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, NAVY_LIGHT, Color.WHITE, NAVY); // azioni secondarie
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, new Color(220, 90, 90), Color.WHITE, ERROR); // per elimina/azioni distruttive, rosso
        return b;
    }

    private static void styleButton(JButton b, Color bg, Color fg, Color border) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(FONT_BUTTON);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR)); // manina al passaggio del mouse
        b.setOpaque(true);
        b.setBorderPainted(false);
    }

    public static JTextField field() {
        JTextField f = new JTextField();
        styleField(f);
        return f;
    }

    public static JPasswordField passwordField() {
        JPasswordField f = new JPasswordField();
        styleField(f);
        return f;
    }

    private static void styleField(JTextField f) {
        f.setFont(FONT_FIELD);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, GOLD_DARK), // solo bordo sotto, tipo material design
                new EmptyBorder(6, 4, 6, 4)));
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(NAVY);
        return l;
    }

    public static JPanel navyPanel() {
        JPanel p = new JPanel();
        p.setBackground(NAVY);
        return p;
    }

    // per i titoli tipo "UninaMultiCloud" + sottotitolo sotto, usato nelle intestazioni delle finestre
    public static void applyNiceHeader(JLabel title, JLabel subtitle) {
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);
        if (subtitle != null) {
            subtitle.setFont(FONT_SUB);
            subtitle.setForeground(GOLD);
        }
    }
}

