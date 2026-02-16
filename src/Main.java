// Import the custom DashboardPanel class from the gui package
import gui.DashboardPanel;

// Import the custom LoginPanel class from the gui package (currently commented out in use)
import gui.LoginPanel;

// Exception class for unsupported Look and Feel configurations
import javax.swing.UnsupportedLookAndFeelException;

public class Main {
    public static void main(String[] args) {

        // Attempt to set the look and feel to Nimbus (a modern Swing UI theme)
        try {
            javax.swing.UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
            // Exceptions are caught but not handled—could log or print error for better debugging
        }

        // Ensures that all Swing components are created on the Event Dispatch Thread (EDT)
        // This is the recommended approach for thread safety in Swing applications
        javax.swing.SwingUtilities.invokeLater(() -> {

            // Uncomment the line below to show the LoginPanel instead of directly opening the dashboard
//            LoginPanel loginPanel = new LoginPanel(); // Launches the login GUI
            
            // For now, bypasses login and opens the Dashboard directly with "Admin" user
//            new DashboardPanel("Admin");
            LoginPanel LoginPanel = new LoginPanel(); // Launch the login GUI
          

        });
    }
}

