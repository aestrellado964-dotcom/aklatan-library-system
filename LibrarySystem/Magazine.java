/** INHERITANCE: Magazine is a LibraryItem with an issue number. Loan 7 days, PHP 3.00 per day late. */
public class Magazine extends LibraryItem {

    private int issueNo;

    public Magazine(String id, String title, String publisher, int year, int issueNo) throws LibraryException {
        super(id, title, publisher, year);
        setIssueNo(issueNo);
    }

    public int getIssueNo() {
        return issueNo;
    }

    public void setIssueNo(int issueNo) throws LibraryException {
        if (issueNo <= 0) {
            throw new LibraryException("Issue no. must be a whole number greater than 0.");
        }
        this.issueNo = issueNo;
    }

    @Override
    public String getItemType() {
        return "Magazine";
    }

    @Override
    public int getLoanDays() {
        return 7;
    }

    @Override
    public double getFinePerDay() {
        return 3.00;
    }

    @Override
    public String getExtraInfo() {
        return "Issue " + issueNo;
    }
}
