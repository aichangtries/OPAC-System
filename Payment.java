import java.time.LocalDateTime;

public class Payment {

    private int paymentId;
    private int borrowerId;
    private Integer transactionId;
    private double amount;
    private String remarks;
    private LocalDateTime paidAt;

    public Payment(int paymentId, int borrowerId, Integer transactionId,
                   double amount, String remarks, LocalDateTime paidAt) {
        this.paymentId = paymentId;
        this.borrowerId = borrowerId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.remarks = remarks;
        this.paidAt = paidAt;
    }

    public int getPaymentId() { return paymentId; }
    public int getBorrowerId() { return borrowerId; }
    public Integer getTransactionId() { return transactionId; }
    public double getAmount() { return amount; }
    public String getRemarks() { return remarks; }
    public LocalDateTime getPaidAt() { return paidAt; }

    @Override
    public String toString() {
        return String.format("| %-5d | Borrower %-4d | TX %-5s | PHP %-8.2f | %-19s |",
                paymentId,
                borrowerId,
                transactionId != null ? transactionId.toString() : "—",
                amount,
                paidAt != null ? paidAt.toString() : "-");
    }
}
