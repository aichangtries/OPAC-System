# LQRY Backend API Endpoints

## Configuration
**Base URL:** `http://localhost:8080/api` (update in HTML: line 732)

---

## 📚 Books Endpoints

### GET /api/books
**Description:** Retrieve all books from database  
**Method:** GET  
**Response:**
```json
[
  {
    "bookId": 1,
    "title": "The Great Gatsby",
    "author": "F. Scott Fitzgerald",
    "category": "Literature",
    "deweyDecimal": "810 - American Literature",
    "isAvailable": true
  }
]
```
**Status Codes:** 200 OK | 500 Server Error

---

### POST /api/books
**Description:** Create a new book  
**Method:** POST  
**Headers:** `Content-Type: application/json`  
**Request Body:**
```json
{
  "title": "Clean Code",
  "author": "Robert Martin",
  "category": "Programming",
  "deweyDecimal": "000 - General Works",
  "isAvailable": true
}
```
**Response:**
```json
{
  "bookId": 5,
  "title": "Clean Code",
  "author": "Robert Martin",
  "category": "Programming",
  "deweyDecimal": "000 - General Works",
  "isAvailable": true
}
```
**Status Codes:** 201 Created | 400 Bad Request | 500 Server Error

---

### PUT /api/books/{bookId}
**Description:** Update an existing book  
**Method:** PUT  
**Path Parameter:** `bookId` (integer)  
**Headers:** `Content-Type: application/json`  
**Request Body:**
```json
{
  "title": "Clean Code (Updated)",
  "author": "Robert Martin",
  "category": "Programming",
  "deweyDecimal": "000 - General Works"
}
```
**Response:**
```json
{
  "bookId": 5,
  "title": "Clean Code (Updated)",
  "author": "Robert Martin",
  "category": "Programming",
  "deweyDecimal": "000 - General Works",
  "isAvailable": true
}
```
**Status Codes:** 200 OK | 404 Not Found | 400 Bad Request | 500 Server Error

---

### DELETE /api/books/{bookId}
**Description:** Delete a book by ID  
**Method:** DELETE  
**Path Parameter:** `bookId` (integer)  
**Response:** None (empty body)  
**Status Codes:** 204 No Content | 404 Not Found | 500 Server Error

---

## 👥 Borrowers Endpoints

### GET /api/borrowers
**Description:** Retrieve all borrowers  
**Method:** GET  
**Response:**
```json
[
  {
    "borrowerId": 1,
    "name": "John Doe"
  },
  {
    "borrowerId": 2,
    "name": "Jane Smith"
  }
]
```
**Status Codes:** 200 OK | 500 Server Error

---

### POST /api/borrowers
**Description:** Create a new borrower  
**Method:** POST  
**Headers:** `Content-Type: application/json`  
**Request Body:**
```json
{
  "name": "Michael Johnson"
}
```
**Response:**
```json
{
  "borrowerId": 4,
  "name": "Michael Johnson"
}
```
**Status Codes:** 201 Created | 400 Bad Request | 500 Server Error

---

### PUT /api/borrowers/{borrowerId}
**Description:** Update an existing borrower  
**Method:** PUT  
**Path Parameter:** `borrowerId` (integer)  
**Headers:** `Content-Type: application/json`  
**Request Body:**
```json
{
  "name": "Michael J. Johnson"
}
```
**Response:**
```json
{
  "borrowerId": 4,
  "name": "Michael J. Johnson"
}
```
**Status Codes:** 200 OK | 404 Not Found | 400 Bad Request | 500 Server Error

---

### DELETE /api/borrowers/{borrowerId}
**Description:** Delete a borrower by ID  
**Method:** DELETE  
**Path Parameter:** `borrowerId` (integer)  
**Response:** None (empty body)  
**Status Codes:** 204 No Content | 404 Not Found | 500 Server Error

---

## 📋 Transactions Endpoints

### GET /api/transactions
**Description:** Retrieve all transactions (both active and completed)  
**Method:** GET  
**Response:**
```json
[
  {
    "transactionId": 1,
    "bookId": 4,
    "borrowerId": 1,
    "issueDate": "2026-03-15",
    "returnDate": null,
    "status": "Active"
  },
  {
    "transactionId": 2,
    "bookId": 2,
    "borrowerId": 2,
    "issueDate": "2026-03-10",
    "returnDate": "2026-03-20",
    "status": "Completed"
  }
]
```
**Status Codes:** 200 OK | 500 Server Error

---

### POST /api/transactions/issue
**Description:** Issue a book (create a new transaction)  
**Method:** POST  
**Headers:** `Content-Type: application/json`  
**Request Body:**
```json
{
  "bookId": 2,
  "borrowerId": 3,
  "issueDate": "2026-03-23"
}
```
**Response:**
```json
{
  "transactionId": 3,
  "bookId": 2,
  "borrowerId": 3,
  "issueDate": "2026-03-23",
  "returnDate": null,
  "status": "Active"
}
```
**Business Rules:**
- Book must exist and be available (`isAvailable: true`)
- Borrower must exist
- Set book's `isAvailable` to `false` after issuing

**Status Codes:** 201 Created | 400 Bad Request (book not available) | 404 Not Found | 500 Server Error

---

### PUT /api/transactions/{transactionId}/return
**Description:** Return a book (complete a transaction)  
**Method:** PUT  
**Path Parameter:** `transactionId` (integer)  
**Headers:** `Content-Type: application/json`  
**Request Body:**
```json
{
  "returnDate": "2026-03-23"
}
```
**Response:**
```json
{
  "transactionId": 1,
  "bookId": 4,
  "borrowerId": 1,
  "issueDate": "2026-03-15",
  "returnDate": "2026-03-23",
  "status": "Completed"
}
```
**Business Rules:**
- Transaction must exist and not already have a return date
- Set book's `isAvailable` to `true` after returning
- Change transaction status to `"Completed"`

**Status Codes:** 200 OK | 404 Not Found (transaction doesn't exist) | 400 Bad Request (already returned) | 500 Server Error

---

## Error Response Format (Standard)

All errors should return a consistent format:

```json
{
  "error": "Error message here",
  "timestamp": "2026-03-23T12:34:56Z",
  "status": 400
}
```

---

## Frontend Auto-Sync Configuration

**Refresh Interval:** 3 seconds (can be adjusted in HTML)  
**Timeout:** None specified (adjust per your needs)  
**Fallback:** Sample data loads if API is unavailable

---

## Testing the Endpoints

### Using cURL:

```bash
# Get all books
curl -X GET http://localhost:8080/api/books

# Create a new book
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"New Book","author":"Author Name","category":"Tech","deweyDecimal":"000","isAvailable":true}'

# Update a book
curl -X PUT http://localhost:8080/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Updated Title","author":"Author","category":"Tech","deweyDecimal":"000"}'

# Delete a book
curl -X DELETE http://localhost:8080/api/books/1

# Issue a book
curl -X POST http://localhost:8080/api/transactions/issue \
  -H "Content-Type: application/json" \
  -d '{"bookId":1,"borrowerId":1,"issueDate":"2026-03-23"}'

# Return a book
curl -X PUT http://localhost:8080/api/transactions/1/return \
  -H "Content-Type: application/json" \
  -d '{"returnDate":"2026-03-23"}'
```

---

## Implementation Notes

✓ All endpoints use JSON format  
✓ All date fields use ISO 8601 format: `YYYY-MM-DD`  
✓ Implement CORS headers if frontend is on different domain  
✓ Add input validation for all fields  
✓ Always update related entities (e.g., book availability when issuing/returning)  
✓ Database should cascade delete or handle foreign key constraints  

---

## Next Steps

1. Implement these endpoints in your Java backend
2. Update `API_BASE_URL` in the HTML file (line 732)
3. Test each endpoint with cURL
4. Start the backend server and open the HTML file
5. Data changes via CLI will auto-reflect on the frontend within 3 seconds
