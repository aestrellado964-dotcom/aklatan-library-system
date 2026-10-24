/** Student: up to 3 items, pays the full fine. */
public class Student extends Member {

    public Student(String id, String name, String email) throws LibraryException {
        super(id, name, email);
    }

    @Override
    public String getMemberType() {
        return "Student";
    }

    @Override
    public int getBorrowLimit() {
        return 3;
    }

    @Override
    public double getFineMultiplier() {
        return 1.0;
    }
}
