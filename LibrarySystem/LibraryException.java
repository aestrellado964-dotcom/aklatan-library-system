/**
 * Custom checked exception for every rule the library system enforces
 * (invalid input, borrow limits, already-borrowed items, and so on).
 * Inheritance: LibraryException extends Exception.
 */
public class LibraryException extends Exception {
    public LibraryException(String message) {
        super(message);
    }
}
