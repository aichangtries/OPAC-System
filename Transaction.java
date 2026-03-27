import java.time.LocalDate;

public class Transaction {

    private int transactionId;
    private int bookId;
    private int borrowerId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double overdueFee;

    // Constructor for new borrow
    public Transaction(int transactionId, int bookId, int borrowerId,
                       LocalDate borrowDate, LocalDate dueDate) {
        this.transactionId = transactionId;
        this.bookId = bookId;
        this.borrowerId = borrowerId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = null;
        this.overdueFee = 0.0;
    }

    // Constructor for loading existing transaction (with return info)
    public Transaction(int transactionId, int bookId, int borrowerId,
                       LocalDate borrowDate, LocalDate dueDate,
                       LocalDate returnDate, double overdueFee) {
        this.transactionId = transactionId;
        this.bookId = bookId;
        this.borrowerId = borrowerId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.overdueFee = overdueFee;
    }

    // Getters
    public int getTransactionId()    { return transactionId; }
    public int getBookId()           { return bookId; }
    public int getBorrowerId()       { return borrowerId; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate()    { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public double getOverdueFee()    { return overdueFee; }

    // Setters
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public void setOverdueFee(double fee)           { this.overdueFee = fee; }

    @Override
    public String toString() {
        return String.format("| %-5d | %-6d | %-10d | %-12s | %-12s | %-12s | PHP %-8.2f |",
                transactionId, bookId, borrowerId, borrowDate, dueDate,
                returnDate != null ? returnDate.toString() : "Not returned",
                overdueFee);
    }
}
