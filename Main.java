import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point. Builds the GUI on the Event Dispatch Thread (EDT).
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Cross-platform look and feel so button colours (mouse hover) render everywhere
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("Look and feel not applied: " + ex.getMessage());
            }
            new StudentFormFrame().setVisible(true);
        });
    }
}
