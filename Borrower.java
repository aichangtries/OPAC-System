public class Borrower {

    private int borrowerId;
    private String name;

    public Borrower(int borrowerId, String name) {
        this.borrowerId = borrowerId;
        this.name = name;
    }

    public int getBorrowerId() { return borrowerId; }
    public String getName()    { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return String.format("| %-5d | %-30s |", borrowerId, name);
    }
}
