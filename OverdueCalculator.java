import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class OverdueCalculator {

    // Fee per day overdue (in Philippine Peso)
    private static final double FEE_PER_DAY = 50.00;

    /**
     * Calculates the overdue fee based on due date and return date.
     * If returnDate is null, today's date is used (for active borrows).
     *
     * @param dueDate    the date the book was supposed to be returned
     * @param returnDate the actual return date (null if not yet returned)
     * @return overdue fee amount (0.0 if not overdue)
     */
    public static double calculateFee(LocalDate dueDate, LocalDate returnDate) {
        LocalDate checkDate = (returnDate != null) ? returnDate : LocalDate.now();

        if (!checkDate.isAfter(dueDate)) {
            return 0.0; // No overdue
        }

        long daysOverdue = ChronoUnit.DAYS.between(dueDate, checkDate);
        return daysOverdue * FEE_PER_DAY;
    }

    /**
     * Returns the number of overdue days.
     */
    public static long getDaysOverdue(LocalDate dueDate, LocalDate returnDate) {
        LocalDate checkDate = (returnDate != null) ? returnDate : LocalDate.now();
        long days = ChronoUnit.DAYS.between(dueDate, checkDate);
        return Math.max(0, days);
    }

    /**
     * Returns a formatted overdue summary string.
     */
    public static String getOverdueSummary(LocalDate dueDate, LocalDate returnDate) {
        long days = getDaysOverdue(dueDate, returnDate);
        double fee  = calculateFee(dueDate, returnDate);

        if (days == 0) {
            return "  Status     : On time / Not overdue";
        } else {
            return String.format(
                "  Days Overdue: %d day(s)%n  Overdue Fee : PHP %.2f (PHP %.2f/day)",
                days, fee, FEE_PER_DAY
            );
        }
    }

    public static double getFeePerDay() {
        return FEE_PER_DAY;
    }
}
