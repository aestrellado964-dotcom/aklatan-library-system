/** INHERITANCE: Book is a LibraryItem with a page count. Loan 14 days, PHP 5.00 per day late. */
public class Book extends LibraryItem {

    private int pages;

    public Book(String id, String title, String author, int year, int pages) throws LibraryException {
        super(id, title, author, year);
        setPages(pages);
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) throws LibraryException {
        if (pages <= 0) {
            throw new LibraryException("Pages must be a whole number greater than 0.");
        }
        this.pages = pages;
    }

    @Override
    public String getItemType() {
        return "Book";
    }

    @Override
    public int getLoanDays() {
        return 14;
    }

    @Override
    public double getFinePerDay() {
        return 5.00;
    }

    @Override
    public String getExtraInfo() {
        return pages + " pages";
    }
}
