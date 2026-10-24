/** Teacher: up to 6 items, pays half the fine. */
public class Teacher extends Member {

    public Teacher(String id, String name, String email) throws LibraryException {
        super(id, name, email);
    }

    @Override
    public String getMemberType() {
        return "Teacher";
    }

    @Override
    public int getBorrowLimit() {
        return 6;
    }

    @Override
    public double getFineMultiplier() {
        return 0.5;
    }
}
