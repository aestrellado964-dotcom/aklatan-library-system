/** Guest: 1 item at a time, pays double the fine. */
public class Guest extends Member {

    public Guest(String id, String name, String email) throws LibraryException {
        super(id, name, email);
    }

    @Override
    public String getMemberType() {
        return "Guest";
    }

    @Override
    public int getBorrowLimit() {
        return 1;
    }

    @Override
    public double getFineMultiplier() {
        return 2.0;
    }
}
