import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * ABSTRACTION (abstract class): the shared idea of "something the library lends".
 * ENCAPSULATION: every field is private and reached only through validated
 * getters and setters.
 * Book, Magazine and DVD extend this class (INHERITANCE) and each supplies its
 * own loan period and fine rules (POLYMORPHISM).
 */
public abstract class LibraryItem implements Borrowable {

    private final String id;
    private String title;
    private String creator;
    private int year;
    private Member borrower;
    private LocalDate borrowedOn;
    private LocalDate dueDate;

    protected LibraryItem(String id, String title, String creator, int year) throws LibraryException {
        this.id = id;
        setTitle(title);
        setCreator(creator);
        setYear(year);
    }

    // ---------- Encapsulation: controlled access ----------

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) throws LibraryException {
        if (title == null || title.trim().isEmpty()) {
            throw new LibraryException("Title is required.");
        }
        this.title = title.trim();
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) throws LibraryException {
        if (creator == null || creator.trim().isEmpty()) {
            throw new LibraryException("Creator is required (author, publisher or director).");
        }
        this.creator = creator.trim();
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) throws LibraryException {
        int max = LocalDate.now().getYear() + 1;
        if (year < 1000 || year > max) {
            throw new LibraryException("Year must be a whole number between 1000 and " + max + ".");
        }
        this.year = year;
    }

    public Member getBorrower() {
        return borrower;
    }

    public LocalDate getBorrowedOn() {
        return borrowedOn;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    // ---------- Borrowable implementation ----------

    @Override
    public boolean isAvailable() {
        return borrower == null;
    }

    @Override
    public void borrow(Member member, LocalDate date) throws LibraryException {
        if (!isAvailable()) {
            throw new LibraryException("\"" + title + "\" is already borrowed.");
        }
        this.borrower = member;
        this.borrowedOn = date;
        this.dueDate = date.plusDays(getLoanDays()); // each subclass decides the loan period
    }

    /** Whole days past the due date (0 when on time or not on loan). */
    public int daysLate(LocalDate date) {
        if (dueDate == null) {
            return 0;
        }
        return (int) Math.max(0, ChronoUnit.DAYS.between(dueDate, date));
    }

    @Override
    public double giveBack(LocalDate date) throws LibraryException {
        if (isAvailable()) {
            throw new LibraryException("\"" + title + "\" is not on loan.");
        }
        if (date.isBefore(borrowedOn)) {
            throw new LibraryException("Return date cannot be before the borrow date.");
        }
        // calculateFine is polymorphic; getFineMultiplier is polymorphic too.
        double fine = calculateFine(daysLate(date)) * borrower.getFineMultiplier();
        this.borrower = null;
        this.borrowedOn = null;
        this.dueDate = null;
        return Math.round(fine * 100.0) / 100.0;
    }

    // ---------- Abstract behaviour every item type must define ----------

    public abstract String getItemType();

    public abstract int getLoanDays();

    public abstract double getFinePerDay();

    /** Type-specific detail such as page count or running time. */
    public abstract String getExtraInfo();

    /** Default fine rule. Subclasses may override it (see DVD). */
    public double calculateFine(int daysLate) {
        return daysLate <= 0 ? 0 : daysLate * getFinePerDay();
    }

    @Override
    public String toString() {
        return id + " - " + title;
    }
}
