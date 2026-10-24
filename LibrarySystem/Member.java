import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ABSTRACTION + ENCAPSULATION: a library member. Private fields, validated setters.
 * Student, Teacher and Guest extend it and decide their own borrowing rules (POLYMORPHISM).
 */
public abstract class Member {

    private final String id;
    private String name;
    private String email;
    private final List<LibraryItem> borrowed = new ArrayList<>();

    protected Member(String id, String name, String email) throws LibraryException {
        this.id = id;
        setName(name);
        setEmail(email);
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws LibraryException {
        if (name == null || name.trim().isEmpty()) {
            throw new LibraryException("Name is required.");
        }
        this.name = name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) throws LibraryException {
        if (!isValidEmail(email)) {
            throw new LibraryException("Enter a valid email address, for example name@school.edu.");
        }
        this.email = email.trim();
    }

    public List<LibraryItem> getBorrowedItems() {
        return Collections.unmodifiableList(borrowed);
    }

    public boolean canBorrow() {
        return borrowed.size() < getBorrowLimit();
    }

    public void addBorrowed(LibraryItem item) {
        borrowed.add(item);
    }

    public void removeBorrowed(LibraryItem item) {
        borrowed.remove(item);
    }

    // ---------- Abstract rules, one answer per member type ----------

    public abstract String getMemberType();

    public abstract int getBorrowLimit();

    /** Multiplies the item's fine: 1.0 = full price, 0.5 = half price. */
    public abstract double getFineMultiplier();

    @Override
    public String toString() {
        return id + " - " + name + " (" + getMemberType() + ", " + borrowed.size() + "/" + getBorrowLimit() + ")";
    }
}
