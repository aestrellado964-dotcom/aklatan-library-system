import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The system's brain. It owns the items, members and transactions and enforces every rule.
 * The GUI never changes data directly; it always goes through this class.
 */
public class Library {

    private final List<LibraryItem> items = new ArrayList<>();
    private final List<Member> members = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();
    private final Map<String, Integer> counters = new HashMap<>();

    public List<LibraryItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<Member> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    // ---------- Items ----------

    public LibraryItem addItem(String type, String title, String creator, int year, int extra)
            throws LibraryException {
        if (type == null) {
            throw new LibraryException("Choose an item type.");
        }
        String prefix;
        LibraryItem item;
        switch (type) {
            case "Book":
                prefix = "BK";
                item = new Book(nextId(prefix), title, creator, year, extra);
                break;
            case "Magazine":
                prefix = "MG";
                item = new Magazine(nextId(prefix), title, creator, year, extra);
                break;
            case "DVD":
                prefix = "DV";
                item = new DVD(nextId(prefix), title, creator, year, extra);
                break;
            default:
                throw new LibraryException("Choose an item type.");
        }
        for (LibraryItem other : items) {
            if (other.getItemType().equals(item.getItemType())
                    && other.getTitle().equalsIgnoreCase(item.getTitle())
                    && other.getCreator().equalsIgnoreCase(item.getCreator())) {
                throw new LibraryException(other.getItemType() + " \"" + other.getTitle() + "\" by "
                        + other.getCreator() + " is already in the catalog (" + other.getId() + ").");
            }
        }
        counters.merge(prefix, 1, Integer::sum);
        items.add(item);
        return item;
    }

    public void removeItem(String id) throws LibraryException {
        LibraryItem item = findItem(id);
        if (item == null) {
            throw new LibraryException("Select an item to remove.");
        }
        if (!item.isAvailable()) {
            throw new LibraryException("\"" + item.getTitle() + "\" is on loan to "
                    + item.getBorrower().getName() + " and cannot be removed.");
        }
        items.remove(item);
    }

    public LibraryItem findItem(String id) {
        for (LibraryItem item : items) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    public List<LibraryItem> searchItems(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        List<LibraryItem> found = new ArrayList<>();
        for (LibraryItem item : items) {
            if (k.isEmpty()
                    || item.getId().toLowerCase().contains(k)
                    || item.getItemType().toLowerCase().contains(k)
                    || item.getTitle().toLowerCase().contains(k)
                    || item.getCreator().toLowerCase().contains(k)) {
                found.add(item);
            }
        }
        return found;
    }

    // ---------- Members ----------

    public Member registerMember(String type, String name, String email) throws LibraryException {
        if (type == null) {
            throw new LibraryException("Choose a member type.");
        }
        String id = String.format("M-%03d", counters.getOrDefault("M", 0) + 1);
        Member member;
        switch (type) {
            case "Student":
                member = new Student(id, name, email);
                break;
            case "Teacher":
                member = new Teacher(id, name, email);
                break;
            case "Guest":
                member = new Guest(id, name, email);
                break;
            default:
                throw new LibraryException("Choose a member type.");
        }
        for (Member other : members) {
            if (other.getEmail().equalsIgnoreCase(member.getEmail())) {
                throw new LibraryException("A member with email " + member.getEmail()
                        + " is already registered (" + other.getId() + ").");
            }
        }
        counters.merge("M", 1, Integer::sum);
        members.add(member);
        return member;
    }

    public Member findMember(String id) {
        for (Member member : members) {
            if (member.getId().equals(id)) {
                return member;
            }
        }
        return null;
    }

    // ---------- Circulation ----------

    public Transaction borrowItem(String itemId, String memberId, LocalDate date) throws LibraryException {
        if (date == null) {
            throw new LibraryException("Enter a valid borrow date (yyyy-mm-dd).");
        }
        LibraryItem item = findItem(itemId);
        if (item == null) {
            throw new LibraryException("Choose an item to borrow.");
        }
        Member member = findMember(memberId);
        if (member == null) {
            throw new LibraryException("Choose a member.");
        }
        for (LibraryItem held : member.getBorrowedItems()) {
            if (held.getDueDate().isBefore(date)) {
                throw new LibraryException(member.getName() + " has an overdue item (" + held.getTitle()
                        + "). Return it before borrowing again.");
            }
        }
        if (!member.canBorrow()) {
            throw new LibraryException(member.getName() + " has reached the " + member.getMemberType()
                    + " limit of " + member.getBorrowLimit() + " items.");
        }
        item.borrow(member, date);
        member.addBorrowed(item);
        return log("Borrowed", item, member, date, 0, 0);
    }

    public Transaction returnItem(String itemId, LocalDate date) throws LibraryException {
        if (date == null) {
            throw new LibraryException("Enter a valid return date (yyyy-mm-dd).");
        }
        LibraryItem item = findItem(itemId);
        if (item == null) {
            throw new LibraryException("Choose an item to return.");
        }
        Member member = item.getBorrower();
        if (member == null) {
            throw new LibraryException("\"" + item.getTitle() + "\" is not on loan.");
        }
        int late = item.daysLate(date);
        double fine = item.giveBack(date);
        member.removeBorrowed(item);
        return log("Returned", item, member, date, late, fine);
    }

    private Transaction log(String action, LibraryItem item, Member member, LocalDate date,
                            int daysLate, double fine) {
        int next = counters.getOrDefault("T", 0) + 1;
        counters.put("T", next);
        Transaction t = new Transaction(String.format("T-%03d", next), action, date, item, member, daysLate, fine);
        transactions.add(t);
        return t;
    }

    // ---------- Reports ----------

    public double totalFines() {
        double sum = 0;
        for (Transaction t : transactions) {
            sum += t.getFine();
        }
        return sum;
    }

    public List<LibraryItem> overdueItems(LocalDate asOf) {
        List<LibraryItem> overdue = new ArrayList<>();
        for (LibraryItem item : items) {
            if (!item.isAvailable() && item.getDueDate().isBefore(asOf)) {
                overdue.add(item);
            }
        }
        return overdue;
    }

    private int countItems(String type) {
        int n = 0;
        for (LibraryItem item : items) {
            if (item.getItemType().equals(type)) {
                n++;
            }
        }
        return n;
    }

    private int countMembers(String type) {
        int n = 0;
        for (Member member : members) {
            if (member.getMemberType().equals(type)) {
                n++;
            }
        }
        return n;
    }

    public String generateReport(LocalDate asOf) {
        int onLoan = 0;
        for (LibraryItem item : items) {
            if (!item.isAvailable()) {
                onLoan++;
            }
        }
        List<LibraryItem> overdue = overdueItems(asOf);
        StringBuilder sb = new StringBuilder();
        sb.append("AKLATAN LIBRARY REPORT (").append(asOf).append(")\n");
        sb.append("-----------------------------------\n");
        sb.append(String.format("Items          : %d (Books %d, Magazines %d, DVDs %d)%n",
                items.size(), countItems("Book"), countItems("Magazine"), countItems("DVD")));
        sb.append(String.format("Available      : %d%n", items.size() - onLoan));
        sb.append(String.format("On loan        : %d%n", onLoan));
        sb.append(String.format("Overdue        : %d%n", overdue.size()));
        sb.append(String.format("Members        : %d (Students %d, Teachers %d, Guests %d)%n",
                members.size(), countMembers("Student"), countMembers("Teacher"), countMembers("Guest")));
        sb.append(String.format("Transactions   : %d%n", transactions.size()));
        sb.append(String.format("Fines collected: PHP %.2f%n", totalFines()));
        if (!overdue.isEmpty()) {
            sb.append("Overdue items:\n");
            for (LibraryItem item : overdue) {
                sb.append(String.format("  %s \"%s\" - %s, %d day(s) late%n", item.getId(), item.getTitle(),
                        item.getBorrower().getName(), item.daysLate(asOf)));
            }
        }
        return sb.toString();
    }

    // ---------- Helpers for the GUI ----------

    private String nextId(String prefix) {
        return String.format("%s-%03d", prefix, counters.getOrDefault(prefix, 0) + 1);
    }

    /** A throw-away item used by the GUI to show each type's rules (polymorphic calls). */
    public static LibraryItem sampleItem(String type) {
        try {
            if ("Magazine".equals(type)) {
                return new Magazine("sample", "Sample", "Sample", 2000, 1);
            }
            if ("DVD".equals(type)) {
                return new DVD("sample", "Sample", "Sample", 2000, 1);
            }
            return new Book("sample", "Sample", "Sample", 2000, 1);
        } catch (LibraryException e) {
            return null;
        }
    }

    public static Member sampleMember(String type) {
        try {
            if ("Teacher".equals(type)) {
                return new Teacher("sample", "Sample", "s@example.com");
            }
            if ("Guest".equals(type)) {
                return new Guest("sample", "Sample", "s@example.com");
            }
            return new Student("sample", "Sample", "s@example.com");
        } catch (LibraryException e) {
            return null;
        }
    }
}
