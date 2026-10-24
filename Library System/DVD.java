/**
 * INHERITANCE: DVD is a LibraryItem with a running time. Loan 3 days, PHP 20.00 per day late.
 * POLYMORPHISM: DVD overrides calculateFine so its fine never goes above PHP 300.00.
 */
public class DVD extends LibraryItem {

    private static final double FINE_CAP = 300.00;

    private int minutes;

    public DVD(String id, String title, String director, int year, int minutes) throws LibraryException {
        super(id, title, director, year);
        setMinutes(minutes);
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) throws LibraryException {
        if (minutes <= 0) {
            throw new LibraryException("Minutes must be a whole number greater than 0.");
        }
        this.minutes = minutes;
    }

    @Override
    public String getItemType() {
        return "DVD";
    }

    @Override
    public int getLoanDays() {
        return 3;
    }

    @Override
    public double getFinePerDay() {
        return 20.00;
    }

    @Override
    public String getExtraInfo() {
        return minutes + " min";
    }

    @Override
    public double calculateFine(int daysLate) {
        return Math.min(super.calculateFine(daysLate), FINE_CAP);
    }
}
