public class Book {

    private int bookId;
    private String title;
    private String author;
    private String category;
    private String deweyDecimal;
    private boolean isAvailable;

    // Constructor
    public Book(int bookId, String title, String author, String category, String deweyDecimal, boolean isAvailable) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.deweyDecimal = deweyDecimal;
        this.isAvailable = isAvailable;
    }

    // Getters
    public int getBookId()         { return bookId; }
    public String getTitle()       { return title; }
    public String getAuthor()      { return author; }
    public String getCategory()    { return category; }
    public String getDeweyDecimal(){ return deweyDecimal; }
    public boolean isAvailable()   { return isAvailable; }

    // Setters
    public void setTitle(String title)             { this.title = title; }
    public void setAuthor(String author)           { this.author = author; }
    public void setCategory(String category)       { this.category = category; }
    public void setDeweyDecimal(String dewey)      { this.deweyDecimal = dewey; }
    public void setAvailable(boolean available)    { this.isAvailable = available; }

    @Override
    public String toString() {
        return String.format("| %-5d | %-30s | %-22s | %-12s | %-20s | %-10s |",
                bookId, title, author, category, deweyDecimal,
                isAvailable ? "Available" : "Borrowed");
    }
}
