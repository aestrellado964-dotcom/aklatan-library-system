import java.time.LocalDate;

/**
 * ABSTRACTION (interface): the contract for anything that can be lent out.
 * It says WHAT must be possible, not HOW each item does it.
 */
public interface Borrowable {

    /** Lends the item to a member on the given date. */
    void borrow(Member member, LocalDate date) throws LibraryException;

    /** Takes the item back and returns the fine owed (0 if on time). */
    double giveBack(LocalDate date) throws LibraryException;

    boolean isAvailable();
}
