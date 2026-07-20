package it.unina.multicloud;

import it.unina.multicloud.gui.LoginRegisterFrame;
import it.unina.multicloud.gui.UITheme;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        UITheme.apply(); // imposta i colori/font prima di aprire qualsiasi finestra
        SwingUtilities.invokeLater(() -> new LoginRegisterFrame().setVisible(true));
    }
}
