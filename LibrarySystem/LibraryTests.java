import java.time.LocalDate;

/**
 * Console test log for the Testing section of the documentation.
 * Run with:  java LibraryTests
 */
public class LibraryTests {

    interface Action {
        void run() throws Exception;
    }

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean ok, String detail) {
        if (ok) {
            passed++;
        } else {
            failed++;
        }
        System.out.printf("%-5s %s%s%n", ok ? "PASS" : "FAIL", name, ok ? "" : "  -> " + detail);
    }

    private static void expectError(String name, Action action, String messagePart) {
        try {
            action.run();
            check(name, false, "no error was thrown");
        } catch (LibraryException e) {
            check(name, e.getMessage().contains(messagePart), e.getMessage());
        } catch (Exception e) {
            check(name, false, e.toString());
        }
    }

    private static Library base() throws LibraryException {
        Library lib = new Library();
        lib.addItem("Book", "Noli Me Tangere", "Jose Rizal", 1887, 438);        // BK-001
        lib.addItem("Book", "El Filibusterismo", "Jose Rizal", 1891, 381);      // BK-002
        lib.addItem("Book", "Clean Code", "Robert C. Martin", 2008, 464);       // BK-003
        lib.addItem("Book", "Introduction to Algorithms", "Cormen", 2009, 1312); // BK-004
        lib.addItem("Magazine", "National Geographic", "NatGeo Society", 2025, 10); // MG-001
        lib.addItem("DVD", "Heneral Luna", "Jerrold Tarog", 2015, 118);         // DV-001
        lib.registerMember("Student", "Maria Santos", "maria@example.com");     // M-001
        lib.registerMember("Student", "Juan Dela Cruz", "juan@example.com");    // M-002
        lib.registerMember("Teacher", "Ana Reyes", "ana@example.com");          // M-003
        lib.registerMember("Guest", "Luis Lopez", "luis@example.com");          // M-004
        return lib;
    }

    public static void main(String[] args) throws Exception {
        LocalDate d = LocalDate.of(2026, 1, 1);

        // Adding and validation
        Library fresh = new Library();
        LibraryItem first = fresh.addItem("Book", "Noli Me Tangere", "Jose Rizal", 1887, 438);
        check("Add a valid book gets id BK-001", first.getId().equals("BK-001"), first.getId());
        expectError("Blank title is rejected", () -> new Library().addItem("Book", "  ", "X", 2000, 10),
                "Title is required");
        expectError("Year out of range is rejected", () -> new Library().addItem("Book", "T", "X", 3000, 10),
                "Year must be");
        expectError("Zero pages is rejected", () -> new Library().addItem("Book", "T", "X", 2000, 0),
                "Pages must be");
        expectError("Duplicate item is rejected", () -> base().addItem("Book", "clean code", "ROBERT C. MARTIN", 2008, 464),
                "already in the catalog");
        expectError("Invalid email is rejected", () -> new Library().registerMember("Student", "Ana", "not-an-email"),
                "valid email");
        expectError("Duplicate email is rejected", () -> {
            Library lib = base();
            lib.registerMember("Guest", "Another", "MARIA@example.com");
        }, "already registered");

        // Borrowing rules
        Library lib = base();
        lib.borrowItem("BK-001", "M-001", d);
        check("Book is due 14 days after borrowing", lib.findItem("BK-001").getDueDate().equals(d.plusDays(14)),
                String.valueOf(lib.findItem("BK-001").getDueDate()));
        expectError("Borrowed item cannot be borrowed again", () -> lib.borrowItem("BK-001", "M-002", d),
                "already borrowed");
        expectError("Borrowed item cannot be removed", () -> lib.removeItem("BK-001"), "cannot be removed");
        expectError("Student cannot go past 3 items", () -> {
            Library l = base();
            l.borrowItem("BK-001", "M-001", d);
            l.borrowItem("BK-002", "M-001", d);
            l.borrowItem("BK-003", "M-001", d);
            l.borrowItem("BK-004", "M-001", d);
        }, "limit of 3");
        expectError("Member with an overdue item cannot borrow", () -> {
            Library l = base();
            l.borrowItem("BK-001", "M-001", d);
            l.borrowItem("BK-002", "M-001", d.plusDays(20));
        }, "overdue");
        expectError("Return date before borrow date is rejected", () -> {
            Library l = base();
            l.borrowItem("BK-001", "M-001", d);
            l.returnItem("BK-001", d.minusDays(1));
        }, "before the borrow date");

        // Fines (polymorphism)
        Library fines = base();
        fines.borrowItem("BK-001", "M-001", d);
        Transaction late = fines.returnItem("BK-001", d.plusDays(14 + 6));
        check("Student book, 6 days late = PHP 30.00", late.getFine() == 30.0, String.valueOf(late.getFine()));

        Library teacher = base();
        teacher.borrowItem("BK-001", "M-003", d);
        check("Teacher pays half = PHP 15.00",
                teacher.returnItem("BK-001", d.plusDays(14 + 6)).getFine() == 15.0, "");

        Library guest = base();
        guest.borrowItem("BK-001", "M-004", d);
        check("Guest pays double = PHP 60.00",
                guest.returnItem("BK-001", d.plusDays(14 + 6)).getFine() == 60.0, "");

        Library dvd = base();
        dvd.borrowItem("DV-001", "M-001", d);
        check("DVD fine is capped at PHP 300.00",
                dvd.returnItem("DV-001", d.plusDays(3 + 30)).getFine() == 300.0, "");

        Library onTime = base();
        onTime.borrowItem("MG-001", "M-001", d);
        check("On-time return has no fine", onTime.returnItem("MG-001", d.plusDays(7)).getFine() == 0.0, "");

        LibraryItem[] items = {
                new Book("a", "T", "C", 2000, 1), new Magazine("b", "T", "C", 2000, 1), new DVD("c", "T", "C", 2000, 1)
        };
        check("Polymorphism: calculateFine(5) is 25 / 15 / 100",
                items[0].calculateFine(5) == 25 && items[1].calculateFine(5) == 15 && items[2].calculateFine(5) == 100,
                "");

        // Search
        check("Search is case-insensitive", base().searchItems("CLEAN").size() == 1, "");
        check("Search matches creator", base().searchItems("rizal").size() == 2, "");

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
