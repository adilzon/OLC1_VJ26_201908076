package golite;

import golite.gui.MainWindow;
import javax.swing.SwingUtilities;

public class GoLite {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow ventana = new MainWindow();
            ventana.setVisible(true);
        });
    }
}