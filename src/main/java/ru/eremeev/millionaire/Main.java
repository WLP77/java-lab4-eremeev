package ru.eremeev.millionaire;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                GameFrame.applyLookAndFeel();
                GameFrame frame = new GameFrame();
                frame.setVisible(true);
                frame.startGame();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Ошибка запуска", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
