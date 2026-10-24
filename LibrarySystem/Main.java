import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.time.LocalDate;

/** Entry point. Loads a few sample records so the app opens in a working state. */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // The default look and feel is fine.
        }
        Library library = new Library();
        try {
            loadSampleData(library);
        } catch (LibraryException e) {
            System.err.println("Sample data could not be loaded: " + e.getMessage());
        }
        SwingUtilities.invokeLater(() -> new LibraryGUI(library).setVisible(true));
    }

    private static void loadSampleData(Library lib) throws LibraryException {
        lib.addItem("Book", "Noli Me Tangere", "Jose Rizal", 1887, 438);                       // BK-001
        lib.addItem("Book", "El Filibusterismo", "Jose Rizal", 1891, 381);                     // BK-002
        lib.addItem("Book", "Clean Code", "Robert C. Martin", 2008, 464);                      // BK-003
        lib.addItem("Book", "Introduction to Algorithms", "Thomas H. Cormen", 2009, 1312);     // BK-004
        lib.addItem("Magazine", "National Geographic", "National Geographic Society", 2025, 10); // MG-001
        lib.addItem("DVD", "Heneral Luna", "Jerrold Tarog", 2015, 118);                        // DV-001

        lib.registerMember("Student", "Maria Santos", "maria.santos@example.com");   // M-001
        lib.registerMember("Student", "Juan Dela Cruz", "juan.delacruz@example.com"); // M-002
        lib.registerMember("Teacher", "Ana Reyes", "ana.reyes@example.com");         // M-003
        lib.registerMember("Guest", "Luis Lopez", "luis.lopez@example.com");         // M-004

        LocalDate today = LocalDate.now();
        lib.borrowItem("BK-002", "M-001", today.minusDays(30));
        lib.returnItem("BK-002", today.minusDays(25));
        lib.borrowItem("MG-001", "M-001", today.minusDays(20));
        lib.borrowItem("BK-003", "M-002", today.minusDays(20)); // now overdue
        lib.returnItem("MG-001", today.minusDays(8));           // 5 days late
        lib.borrowItem("DV-001", "M-003", today.minusDays(3));
    }
}
