import java.time.LocalDate;

/** One line in the ledger: a borrow or a return. Immutable, so every field is private final. */
public class Transaction {

    private final String id;
    private final String action; // "Borrowed" or "Returned"
    private final LocalDate date;
    private final String itemId;
    private final String itemTitle;
    private final String memberId;
    private final String memberName;
    private final int daysLate;
    private final double fine;

    public Transaction(String id, String action, LocalDate date, LibraryItem item, Member member,
                       int daysLate, double fine) {
        this.id = id;
        this.action = action;
        this.date = date;
        this.itemId = item.getId();
        this.itemTitle = item.getTitle();
        this.memberId = member.getId();
        this.memberName = member.getName();
        this.daysLate = daysLate;
        this.fine = fine;
    }

    public String getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public int getDaysLate() {
        return daysLate;
    }

    public double getFine() {
        return fine;
    }
}
