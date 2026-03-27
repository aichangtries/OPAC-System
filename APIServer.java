import com.sun.net.httpserver.*;
import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * REST API Server for OPAC System
 */
public class APIServer {
    static BookDAO bookDAO;
    static BorrowerDAO borrowerDAO;
    static TransactionDAO transactionDAO;
    static PaymentDAO paymentDAO;
    static java.sql.Connection dbConnection;
    static PrintWriter logFile;
    
    public static void main(String[] args) throws Exception {
        // Setup logging
        logFile = new PrintWriter(new FileWriter("api_server.log"), true);
        log("=== API Server Starting ===");
        
        log("Connecting to database...");
        java.sql.Connection conn = Database.connect();
        if (conn == null) {
            log("ERROR: Failed to connect to database!");
            return;
        }
        log("✅ Database connected");
        
        dbConnection = conn;
        bookDAO = new BookDAO(conn);
        borrowerDAO = new BorrowerDAO(conn);
        transactionDAO = new TransactionDAO(conn);
        paymentDAO = new PaymentDAO(conn);
        
        log("Creating HTTP server on port 5000...");
        HttpServer server = HttpServer.create(new java.net.InetSocketAddress(5000), 0);
        log("✅ HTTP server created");
        
        server.createContext("/api/books", APIServer::handleBooks);
        server.createContext("/api/borrowers", APIServer::handleBorrowers);
        server.createContext("/api/transactions", APIServer::handleTransactions);
        server.createContext("/api/overdue", APIServer::handleOverdue);
        server.createContext("/api/payments", APIServer::handlePayments);
        server.createContext("/", APIServer::handleRoot);
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
        server.start();
        
        log("✅ OPAC API SERVER STARTED on http://localhost:5000");
        log("📚 /api/books");
        log("👥 /api/borrowers");
        log("📋 /api/transactions");
        log("⚠️  /api/overdue");
        log("💳 /api/payments");
        log("Server is running... Press Ctrl+C to stop");
    }
    
    static void log(String msg) {
        System.out.println(msg);
        if (logFile != null) {
            logFile.println(msg);
            logFile.flush();
        }
    }
    
    static void handleRoot(HttpExchange ex) throws IOException {
        addCORSHeaders(ex);
        String body = "OPAC API Server is running!\n\n" +
                     "Available endpoints:\n" +
                     "- GET /api/books\n" +
                     "- GET /api/books/:id\n" +
                     "- POST /api/books\n" +
                     "- PUT /api/books/:id\n" +
                     "- DELETE /api/books/:id\n" +
                     "- GET /api/borrowers\n" +
                     "- POST /api/borrowers\n" +
                     "- GET /api/transactions\n" +
                     "- POST /api/transactions/issue\n" +
                     "- PUT /api/transactions/:id/return\n" +
                     "- GET /api/overdue\n";
        sendResponse(ex, 200, body);
    }
    
    static void handleBooks(HttpExchange ex) throws IOException {
        addCORSHeaders(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) {
            ex.sendResponseHeaders(200, 0);
            ex.close();
            return;
        }
        
        try {
            String path = ex.getRequestURI().getPath();
            String[] parts = path.split("/");
            String method = ex.getRequestMethod();
            
            if (method.equals("GET")) {
                if (parts.length >= 4 && isNumeric(parts[3])) {
                    // GET /api/books/:id
                    int bookId = Integer.parseInt(parts[3]);
                    Book book = bookDAO.getBookById(bookId);
                    if (book != null) {
                        String json = String.format("{\"bookId\":%d,\"title\":\"%s\",\"author\":\"%s\",\"category\":\"%s\",\"deweyDecimal\":\"%s\",\"isAvailable\":%s}",
                            book.getBookId(), escapeJson(book.getTitle()), escapeJson(book.getAuthor()),
                            escapeJson(book.getCategory()), escapeJson(book.getDeweyDecimal()), book.isAvailable());
                        sendResponse(ex, 200, json);
                    } else {
                        sendResponse(ex, 404, "{\"error\":\"Book not found\"}");
                    }
                } else {
                    // GET /api/books
                    List<Book> books = bookDAO.getAllBooks();
                    String json = "[\n";
                    for (int i = 0; i < books.size(); i++) {
                        Book b = books.get(i);
                        if (i > 0) json += ",";
                        json += String.format("  {\"bookId\":%d,\"title\":\"%s\",\"author\":\"%s\",\"category\":\"%s\",\"deweyDecimal\":\"%s\",\"isAvailable\":%s}\n",
                            b.getBookId(), escapeJson(b.getTitle()), escapeJson(b.getAuthor()),
                            escapeJson(b.getCategory()), escapeJson(b.getDeweyDecimal()), b.isAvailable());
                    }
                    json += "]";
                    sendResponse(ex, 200, json);
                }
            }
            else if (method.equals("POST")) {
                String body = readBody(ex);
                String title = extract(body, "title");
                String author = extract(body, "author");
                String category = extract(body, "category");
                
                if (title != null && author != null && category != null) {
                    bookDAO.addBook(title, author, category);
                    sendResponse(ex, 201, "{\"status\":\"created\"}");
                } else {
                    sendResponse(ex, 400, "{\"error\":\"Missing fields\"}");
                }
            }
            else if (method.equals("PUT")) {
                // PUT /api/books/:id
                if (parts.length >= 4 && isNumeric(parts[3])) {
                    int bookId = Integer.parseInt(parts[3]);
                    String body = readBody(ex);
                    String title = extract(body, "title");
                    String author = extract(body, "author");
                    String category = extract(body, "category");
                    
                    if (title != null && author != null && category != null) {
                        bookDAO.updateBook(bookId, title, author, category);
                        sendResponse(ex, 200, "{\"status\":\"updated\"}");
                    } else {
                        sendResponse(ex, 400, "{\"error\":\"Missing fields\"}");
                    }
                } else {
                    sendResponse(ex, 400, "{\"error\":\"Invalid request\"}");
                }
            }
            else if (method.equals("DELETE")) {
                // DELETE /api/books/:id
                if (parts.length >= 4 && isNumeric(parts[3])) {
                    int bookId = Integer.parseInt(parts[3]);
                    bookDAO.deleteBook(bookId);
                    sendResponse(ex, 200, "{\"status\":\"deleted\"}");
                } else {
                    sendResponse(ex, 400, "{\"error\":\"Invalid request\"}");
                }
            }
            else {
                sendResponse(ex, 405, "{\"error\":\"Method not allowed\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }
    
    static void handleBorrowers(HttpExchange ex) throws IOException {
        addCORSHeaders(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) {
            ex.sendResponseHeaders(200, 0);
            ex.close();
            return;
        }
        
        try {
            String path = ex.getRequestURI().getPath();
            String[] parts = path.split("/");
            String method = ex.getRequestMethod();
            
            if (method.equals("GET")) {
                if (parts.length >= 5 && isNumeric(parts[3]) && "profile".equalsIgnoreCase(parts[4])) {
                    int borrowerId = Integer.parseInt(parts[3]);
                    String profileJson = buildBorrowerProfileJson(borrowerId);
                    if (profileJson != null) {
                        sendResponse(ex, 200, profileJson);
                    } else {
                        sendResponse(ex, 404, "{\"error\":\"Borrower not found\"}");
                    }
                }
                else if (parts.length >= 4 && isNumeric(parts[3])) {
                    // GET /api/borrowers/:id
                    int borrowerId = Integer.parseInt(parts[3]);
                    Borrower borrower = borrowerDAO.getBorrowerById(borrowerId);
                    if (borrower != null) {
                        String json = String.format("{\"borrowerId\":%d,\"name\":\"%s\"}", 
                            borrower.getBorrowerId(), escapeJson(borrower.getName()));
                        sendResponse(ex, 200, json);
                    } else {
                        sendResponse(ex, 404, "{\"error\":\"Borrower not found\"}");
                    }
                } else {
                    // GET /api/borrowers
                    List<Borrower> borrowers = borrowerDAO.getAllBorrowers();
                    String json = "[\n";
                    for (int i = 0; i < borrowers.size(); i++) {
                        Borrower b = borrowers.get(i);
                        if (i > 0) json += ",";
                        json += String.format("  {\"borrowerId\":%d,\"name\":\"%s\"}\n", 
                            b.getBorrowerId(), escapeJson(b.getName()));
                    }
                    json += "]";
                    sendResponse(ex, 200, json);
                }
            }
            else if (method.equals("POST")) {
                String body = readBody(ex);
                String name = extract(body, "name");
                if (name != null) {
                    borrowerDAO.addBorrower(name);
                    sendResponse(ex, 201, "{\"status\":\"created\"}");
                } else {
                    sendResponse(ex, 400, "{\"error\":\"Missing name\"}");
                }
            }
            else if (method.equals("PUT")) {
                // PUT /api/borrowers/:id
                if (parts.length >= 4 && isNumeric(parts[3])) {
                    int borrowerId = Integer.parseInt(parts[3]);
                    String body = readBody(ex);
                    String name = extract(body, "name");
                    if (name != null) {
                        borrowerDAO.updateBorrower(borrowerId, name);
                        sendResponse(ex, 200, "{\"status\":\"updated\"}");
                    } else {
                        sendResponse(ex, 400, "{\"error\":\"Missing name\"}");
                    }
                } else {
                    sendResponse(ex, 400, "{\"error\":\"Invalid request\"}");
                }
            }
            else if (method.equals("DELETE")) {
                // DELETE /api/borrowers/:id
                if (parts.length >= 4 && isNumeric(parts[3])) {
                    int borrowerId = Integer.parseInt(parts[3]);
                    borrowerDAO.deleteBorrower(borrowerId);
                    sendResponse(ex, 200, "{\"status\":\"deleted\"}");
                } else {
                    sendResponse(ex, 400, "{\"error\":\"Invalid request\"}");
                }
            }
            else {
                sendResponse(ex, 405, "{\"error\":\"Method not allowed\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }
    
    static void handleTransactions(HttpExchange ex) throws IOException {
        addCORSHeaders(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) {
            ex.sendResponseHeaders(200, 0);
            ex.close();
            return;
        }
        
        try {
            String path = ex.getRequestURI().getPath();
            String[] parts = path.split("/");
            String method = ex.getRequestMethod();
            
            if (method.equals("GET")) {
                List<Transaction> transactions = transactionDAO.getAllTransactions();
                String json = "[\n";
                for (int i = 0; i < transactions.size(); i++) {
                    Transaction t = transactions.get(i);
                    if (i > 0) json += ",";
                        String returnDateValue = (t.getReturnDate() != null)
                            ? "\"" + t.getReturnDate() + "\""
                            : "null";

                        json += String.format("  {\"transactionId\":%d,\"bookId\":%d,\"borrowerId\":%d,\"issueDate\":\"%s\",\"dueDate\":\"%s\",\"returnDate\":%s,\"overdueFee\":%.2f}\n",
                            t.getTransactionId(), t.getBookId(), t.getBorrowerId(), t.getBorrowDate(), t.getDueDate(),
                            returnDateValue, t.getOverdueFee());
                }
                json += "]";
                sendResponse(ex, 200, json);
            }
            else if (method.equals("POST")) {
                if (path.contains("issue")) {
                    // POST /api/transactions/issue
                    String body = readBody(ex);
                    log("📝 Issue POST body: " + body);
                    String bookIdStr = extract(body, "bookId");
                    String borrowerIdStr = extract(body, "borrowerId");
                    String loanDaysStr = extract(body, "loanDays");
                    log("  - bookId: " + bookIdStr + ", borrowerId: " + borrowerIdStr + ", loanDays: " + loanDaysStr);
                    if (bookIdStr != null && borrowerIdStr != null) {
                        try {
                            int bookId = Integer.parseInt(bookIdStr);
                            int borrowerId = Integer.parseInt(borrowerIdStr);
                            int loanDays = 14; // default
                            if (loanDaysStr != null && !loanDaysStr.isEmpty()) {
                                try {
                                    loanDays = Integer.parseInt(loanDaysStr);
                                } catch (NumberFormatException e) {
                                    loanDays = 14;
                                }
                            }
                            boolean success = transactionDAO.borrowBook(bookId, borrowerId, loanDays);
                            if (success) {
                                log("✅ Book borrowed successfully: bookId=" + bookId + ", borrowerId=" + borrowerId + ", days=" + loanDays);
                                sendResponse(ex, 201, "{\"status\":\"borrowed\",\"loanDays\":" + loanDays + "}");
                            } else {
                                log("❌ borrowBook returned false");
                                sendResponse(ex, 400, "{\"error\":\"Failed to borrow book\"}");
                            }
                        } catch (Exception e) {
                            log("❌ Exception in borrowBook: " + e.getMessage());
                            e.printStackTrace();
                            sendResponse(ex, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                        }
                    } else {
                        log("❌ Missing bookId or borrowerId");
                        sendResponse(ex, 400, "{\"error\":\"Missing bookId or borrowerId\"}");
                    }
                } else {
                    sendResponse(ex, 404, "{\"error\":\"Not found\"}");
                }
            }
            else if (method.equals("PUT")) {
                if (path.contains("return") && parts.length >= 4 && isNumeric(parts[3])) {
                    // PUT /api/transactions/:id/return
                    int transactionId = Integer.parseInt(parts[3]);
                    
                    // Read request body for optional return date
                    String body = readBody(ex);
                    String returnDateStr = extract(body, "returnDate");
                    
                    double fee;
                    if (returnDateStr != null && !returnDateStr.isEmpty()) {
                        try {
                            java.time.LocalDate customReturnDate = java.time.LocalDate.parse(returnDateStr);
                            fee = transactionDAO.returnBookWithDate(transactionId, customReturnDate);
                        } catch (Exception e) {
                            // If date parsing fails, use default
                            fee = transactionDAO.returnBook(transactionId);
                        }
                    } else {
                        fee = transactionDAO.returnBook(transactionId);
                    }
                    
                    if (fee >= 0) {
                        sendResponse(ex, 200, "{\"status\":\"returned\",\"fee\":" + String.format("%.2f", fee) + "}");
                    } else {
                        sendResponse(ex, 400, "{\"error\":\"Failed to return book\"}");
                    }
                } else {
                    sendResponse(ex, 400, "{\"error\":\"Unknown transaction action\"}");
                }
            }
            else {
                sendResponse(ex, 405, "{\"error\":\"Method not allowed\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    static void handlePayments(HttpExchange ex) throws IOException {
        addCORSHeaders(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) {
            ex.sendResponseHeaders(200, 0);
            ex.close();
            return;
        }

        if (paymentDAO == null) {
            sendResponse(ex, 500, "{\"error\":\"Payments unavailable\"}");
            return;
        }

        try {
            String method = ex.getRequestMethod();
            if (method.equals("GET")) {
                Map<String, String> params = parseQuery(ex.getRequestURI().getQuery());
                boolean summaryOnly = "true".equalsIgnoreCase(params.getOrDefault("summary", "false"));
                Integer borrowerId = null;
                if (params.containsKey("borrowerId") && isNumeric(params.get("borrowerId"))) {
                    borrowerId = Integer.parseInt(params.get("borrowerId"));
                }

                if (summaryOnly) {
                    double totalPaid = borrowerId != null
                            ? paymentDAO.getTotalPaymentsForBorrower(borrowerId)
                            : paymentDAO.getTotalPayments();
                    String body = String.format(Locale.US,
                            "{\"borrowerId\":%s,\"totalPaid\":%.2f}",
                            borrowerId != null ? borrowerId.toString() : "null",
                            totalPaid);
                    sendResponse(ex, 200, body);
                } else {
                    List<Payment> payments = borrowerId != null
                            ? paymentDAO.getPaymentsForBorrower(borrowerId)
                            : paymentDAO.getAllPayments();
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < payments.size(); i++) {
                        if (i > 0) json.append(',');
                        json.append(paymentToJson(payments.get(i)));
                    }
                    json.append(']');
                    sendResponse(ex, 200, json.toString());
                }
            }
            else if (method.equals("POST")) {
                String body = readBody(ex);
                String borrowerIdStr = extract(body, "borrowerId");
                String amountStr = extract(body, "amount");
                if (borrowerIdStr == null || amountStr == null) {
                    sendResponse(ex, 400, "{\"error\":\"Missing borrowerId or amount\"}");
                    return;
                }

                Integer transactionId = null;
                String txIdStr = extract(body, "transactionId");
                if (txIdStr != null && !txIdStr.isEmpty()) {
                    transactionId = Integer.parseInt(txIdStr);
                }
                String remarks = extract(body, "remarks");
                if (remarks == null) remarks = "";

                int borrowerId = Integer.parseInt(borrowerIdStr);
                double amount = Double.parseDouble(amountStr);

                System.out.println("[DEBUG] POST /api/payments - borrowerId=" + borrowerId + ", amount=" + amount + ", transactionId=" + transactionId);
                boolean success = paymentDAO.recordPayment(borrowerId, transactionId, amount, remarks);
                if (success) {
                    System.out.println("[DEBUG] Payment recorded successfully");
                    String resp = String.format(Locale.US,
                            "{\"status\":\"recorded\",\"borrowerId\":%d,\"amount\":%.2f}",
                            borrowerId, amount);
                    sendResponse(ex, 201, resp);
                } else {
                    System.out.println("[DEBUG] Payment recording FAILED for borrowerId=" + borrowerId);
                    sendResponse(ex, 400, "{\"error\":\"Failed to record payment\"}");
                }
            }
            else {
                sendResponse(ex, 405, "{\"error\":\"Method not allowed\"}");
            }
        } catch (Exception e) {
            sendResponse(ex, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }
    
    static void addCORSHeaders(HttpExchange ex) {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        ex.getResponseHeaders().add("Content-Type", "application/json");
    }
    
    static void sendResponse(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes("UTF-8");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }
    
    static String readBody(HttpExchange ex) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(ex.getRequestBody()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        return sb.toString();
    }
    
    static String extract(String json, String key) {
        // Try to extract string value first: "key":"value"
        String searchStr = "\"" + key + "\":\"";
        int idx = json.indexOf(searchStr);
        if (idx != -1) {
            int start = idx + searchStr.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        }
        
        // Try to extract numeric value: "key":123
        String searchNum = "\"" + key + "\":";
        idx = json.indexOf(searchNum);
        if (idx != -1) {
            int start = idx + searchNum.length();
            int end = start;
            while (end < json.length() && Character.isDigit(json.charAt(end))) {
                end++;
            }
            return json.substring(start, end);
        }
        
        return null;
    }
    
    static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
    
    static boolean isNumeric(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    static void handleOverdue(HttpExchange ex) throws IOException {
        addCORSHeaders(ex);
        if (ex.getRequestMethod().equals("OPTIONS")) {
            ex.sendResponseHeaders(200, 0);
            ex.close();
            return;
        }
        
        try {
            String method = ex.getRequestMethod();
            
            if (method.equals("GET")) {
                log("📋 GET /api/overdue - fetching overdue transactions");
                // GET /api/overdue - returns all overdue transactions (unpaid)
                if (transactionDAO == null) {
                    log("❌ transactionDAO is null!");
                    sendResponse(ex, 500, "{\"error\":\"Database not initialized\"}");
                    return;
                }
                
                java.util.List<Transaction> overdueList = transactionDAO.getOverdueTransactions();
                log("📊 Found " + overdueList.size() + " overdue transactions");
                Map<Integer, Double> borrowerPayments = (paymentDAO != null)
                        ? paymentDAO.getPaymentsByBorrower()
                        : java.util.Collections.emptyMap();
                Map<Integer, Double> remainingPayments = new HashMap<>(borrowerPayments);
                Map<Integer, Double> borrowerOutstanding = new LinkedHashMap<>();
                Map<Integer, Double> borrowerGross = new LinkedHashMap<>();

                StringBuilder itemsJson = new StringBuilder("[");
                double grossTotal = 0;
                double outstandingTotal = 0;
                double paymentsAppliedTotal = 0;

                for (int i = 0; i < overdueList.size(); i++) {
                    Transaction t = overdueList.get(i);
                    java.time.LocalDate comparisonDate =
                        (t.getReturnDate() != null) ? t.getReturnDate() : java.time.LocalDate.now();
                    long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(t.getDueDate(), comparisonDate);
                    if (daysOverdue < 0) daysOverdue = 0;
                    double fee = daysOverdue * OverdueCalculator.getFeePerDay();
                    double availablePayment = remainingPayments.getOrDefault(t.getBorrowerId(), 0.0);
                    if (availablePayment < 0) availablePayment = 0;
                    double applied = Math.min(availablePayment, fee);
                    double outstanding = fee - applied;
                    remainingPayments.put(t.getBorrowerId(), availablePayment - applied);

                    grossTotal += fee;
                    outstandingTotal += outstanding;
                    paymentsAppliedTotal += applied;

                    borrowerOutstanding.merge(t.getBorrowerId(), outstanding, Double::sum);
                    borrowerGross.merge(t.getBorrowerId(), fee, Double::sum);

                    String returnDateValue = (t.getReturnDate() != null)
                        ? "\"" + t.getReturnDate() + "\""
                        : "null";

                    if (i > 0) itemsJson.append(',');
                    itemsJson.append(String.format(Locale.US,
                        "{\"transactionId\":%d,\"bookId\":%d,\"borrowerId\":%d,\"borrowDate\":\"%s\",\"dueDate\":\"%s\",\"returnDate\":%s,\"daysOverdue\":%d,\"overdueFee\":%.2f,\"appliedPayment\":%.2f,\"outstanding\":%.2f,\"borrowerOutstanding\":%.2f}",
                        t.getTransactionId(), t.getBookId(), t.getBorrowerId(),
                        t.getBorrowDate(), t.getDueDate(),
                        returnDateValue,
                        daysOverdue, fee, applied, outstanding,
                        borrowerOutstanding.getOrDefault(t.getBorrowerId(), outstanding)));
                }
                itemsJson.append(']');

                double grossFees = Math.max(grossTotal, 0);
                double outstandingFees = Math.max(outstandingTotal, 0);
                double paymentsApplied = Math.max(paymentsAppliedTotal, 0);

                StringBuilder borrowerJson = new StringBuilder("[");
                int idx = 0;
                for (Map.Entry<Integer, Double> entry : borrowerGross.entrySet()) {
                    if (idx++ > 0) borrowerJson.append(',');
                    int borrowerId = entry.getKey();
                    Borrower borrower = borrowerDAO != null ? borrowerDAO.getBorrowerById(borrowerId) : null;
                    double gross = entry.getValue();
                    double outstanding = borrowerOutstanding.getOrDefault(borrowerId, gross);
                    double paid = Math.min(borrowerPayments.getOrDefault(borrowerId, 0.0), gross);
                    borrowerJson.append(String.format(Locale.US,
                        "{\"borrowerId\":%d,\"name\":\"%s\",\"grossFees\":%.2f,\"payments\":%.2f,\"outstanding\":%.2f}",
                        borrowerId,
                        borrower != null ? escapeJson(borrower.getName()) : "",
                        gross,
                        paid,
                        outstanding));
                }
                borrowerJson.append(']');

                String response = String.format(Locale.US,
                        "{\"items\":%s,\"summary\":{\"grossFees\":%.2f,\"paymentsApplied\":%.2f,\"outstanding\":%.2f},\"borrowers\":%s}",
                        itemsJson,
                        grossFees,
                        paymentsApplied,
                        outstandingFees,
                        borrowerJson);

                log("✅ Sending overdue data with summary: " + response.length() + " bytes");
                sendResponse(ex, 200, response);
            }
            else {
                sendResponse(ex, 405, "{\"error\":\"Method not allowed\"}");
            }
        } catch (Exception e) {
            log("❌ Error in handleOverdue: " + e.getMessage());
            e.printStackTrace();
            sendResponse(ex, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    static String buildBorrowerProfileJson(int borrowerId) {
        if (borrowerDAO == null || transactionDAO == null) {
            return null;
        }
        Borrower borrower = borrowerDAO.getBorrowerById(borrowerId);
        if (borrower == null) {
            return null;
        }

        List<Transaction> txList = transactionDAO.getTransactionsByBorrower(borrowerId);
        int activeLoans = 0;
        for (Transaction t : txList) {
            if (t.getReturnDate() == null) {
                activeLoans++;
            }
        }
        int returned = txList.size() - activeLoans;

        double totalOverdue = 0;
        for (Transaction t : txList) {
            totalOverdue += calculateAccruedFee(t);
        }
        double totalPayments = paymentDAO != null ? paymentDAO.getTotalPaymentsForBorrower(borrowerId) : 0;
        double outstanding = Math.max(totalOverdue - totalPayments, 0);

        StringBuilder txJson = new StringBuilder("[");
        for (int i = 0; i < txList.size(); i++) {
            Transaction t = txList.get(i);
            if (i > 0) txJson.append(',');
            String returnDateValue = t.getReturnDate() != null ? "\"" + t.getReturnDate() + "\"" : "null";
            txJson.append(String.format(Locale.US,
                    "{\"transactionId\":%d,\"bookId\":%d,\"borrowDate\":\"%s\",\"dueDate\":\"%s\",\"returnDate\":%s,\"accruedFee\":%.2f}",
                    t.getTransactionId(),
                    t.getBookId(),
                    t.getBorrowDate(),
                    t.getDueDate(),
                    returnDateValue,
                    calculateAccruedFee(t)));
        }
        txJson.append(']');

        return String.format(Locale.US,
                "{\"borrower\":{\"borrowerId\":%d,\"name\":\"%s\"},\"stats\":{\"activeLoans\":%d,\"returned\":%d,\"totalOverdue\":%.2f,\"totalPayments\":%.2f,\"outstanding\":%.2f},\"transactions\":%s}",
                borrower.getBorrowerId(),
                escapeJson(borrower.getName()),
                activeLoans,
                returned,
                totalOverdue,
                totalPayments,
                outstanding,
                txJson.toString());
    }

    static double calculateAccruedFee(Transaction t) {
        if (t == null || t.getDueDate() == null) return 0;
        LocalDate comparison = t.getReturnDate() != null ? t.getReturnDate() : LocalDate.now();
        long days = ChronoUnit.DAYS.between(t.getDueDate(), comparison);
        if (days <= 0) return 0;
        return days * OverdueCalculator.getFeePerDay();
    }

    static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                params.put(kv[0], kv[1]);
            }
        }
        return params;
    }

    static String paymentToJson(Payment payment) {
        String txValue = payment.getTransactionId() != null ? payment.getTransactionId().toString() : "null";
        String remarks = payment.getRemarks() != null ? escapeJson(payment.getRemarks()) : "";
        String paidAt = payment.getPaidAt() != null ? "\"" + payment.getPaidAt() + "\"" : "null";
        return String.format(Locale.US,
                "{\"paymentId\":%d,\"borrowerId\":%d,\"transactionId\":%s,\"amount\":%.2f,\"remarks\":\"%s\",\"paidAt\":%s}",
                payment.getPaymentId(),
                payment.getBorrowerId(),
                txValue,
                payment.getAmount(),
                remarks,
                paidAt);
    }
}
