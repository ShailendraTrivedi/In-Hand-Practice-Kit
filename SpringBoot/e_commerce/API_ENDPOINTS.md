# E-Commerce API Endpoints

Base URL: `http://localhost:8080`

---

## 1. Health Check

### GET /health
Check if the application is running.

**cURL:**
```bash
curl -X GET http://localhost:8080/health
```

**Expected Response:**
```
Application is running
```

---

## 2. Product APIs

### POST /products/physical
Create a new physical product.

**cURL:**
```bash
curl -X POST http://localhost:8080/products/physical \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "price": 999.99,
    "weight": 2.5,
    "shippingAddress": "123 Main Street, New York, NY 10001"
  }'
```

**More Examples:**

```bash
# Gaming Laptop
curl -X POST http://localhost:8080/products/physical \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Laptop",
    "price": 1499.99,
    "weight": 3.2,
    "shippingAddress": "456 Oak Avenue, Los Angeles, CA 90001"
  }'

# Wireless Mouse
curl -X POST http://localhost:8080/products/physical \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wireless Mouse",
    "price": 29.99,
    "weight": 0.15,
    "shippingAddress": "789 Pine Road, Chicago, IL 60601"
  }'

# Mechanical Keyboard
curl -X POST http://localhost:8080/products/physical \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mechanical Keyboard",
    "price": 89.99,
    "weight": 1.2,
    "shippingAddress": "321 Elm Street, Houston, TX 77001"
  }'
```

---

### POST /products/digital
Create a new digital product.

**cURL:**
```bash
curl -X POST http://localhost:8080/products/digital \
  -H "Content-Type: application/json" \
  -d '{
    "name": "E-Book: Java Programming",
    "price": 19.99,
    "downloadLink": "https://example.com/downloads/java-book.pdf",
    "fileSizeInMB": 15
  }'
```

**More Examples:**

```bash
# Software License
curl -X POST http://localhost:8080/products/digital \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Premium Software License",
    "price": 49.99,
    "downloadLink": "https://example.com/licenses/premium-license",
    "fileSizeInMB": 0
  }'

# Online Course
curl -X POST http://localhost:8080/products/digital \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Spring Boot Masterclass",
    "price": 99.99,
    "downloadLink": "https://example.com/courses/spring-boot",
    "fileSizeInMB": 250
  }'

# Digital Music Album
curl -X POST http://localhost:8080/products/digital \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Best Hits 2024",
    "price": 9.99,
    "downloadLink": "https://example.com/music/best-hits-2024",
    "fileSizeInMB": 120
  }'
```

---

### GET /products
Get all products.

**cURL:**
```bash
curl -X GET http://localhost:8080/products
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "name": "Laptop",
    "price": 999.99,
    "productType": "Physical",
    "weight": 2.5,
    "shippingAddress": "123 Main Street, New York, NY 10001"
  },
  {
    "id": 2,
    "name": "E-Book: Java Programming",
    "price": 19.99,
    "productType": "Digital",
    "downloadLink": "https://example.com/downloads/java-book.pdf",
    "fileSizeInMB": 15
  }
]
```

---

### GET /products/{id}
Get a product by ID.

**cURL:**
```bash
curl -X GET http://localhost:8080/products/1
```

**Expected Response:**
```json
{
  "id": 1,
  "name": "Laptop",
  "price": 999.99,
  "productType": "Physical",
  "weight": 2.5,
  "shippingAddress": "123 Main Street, New York, NY 10001"
}
```

**Error Response (Product Not Found):**
```json
{
  "status": 404,
  "error": "Product Not Found",
  "message": "Product with id 999 not found"
}
```

---

### DELETE /products/{id}
Delete a product by ID.

**cURL:**
```bash
curl -X DELETE http://localhost:8080/products/1
```

**Expected Response:**
```
Product deleted successfully
```

---

## 3. Order APIs

### POST /orders
Create a new order.

**cURL:**
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'
```

**More Examples:**

```bash
# Order for Digital Product
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 2,
    "quantity": 1
  }'

# Bulk Order
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 10
  }'
```

**Expected Response:**
```json
{
  "id": 1,
  "productId": 1,
  "quantity": 2,
  "totalAmount": 1999.98,
  "status": "PENDING"
}
```

---

### GET /orders/status/{id}
Get order status by order ID.

**cURL:**
```bash
curl -X GET http://localhost:8080/orders/status/1
```

**Expected Response:**
```json
{
  "id": 1,
  "productId": 1,
  "quantity": 2,
  "totalAmount": 1999.98,
  "status": "PENDING"
}
```

**Possible Status Values:**
- `PENDING` - Order is created but not processed
- `PROCESSING` - Order is being processed
- `COMPLETED` - Order is completed
- `FAILED` - Order processing failed

---

## 4. Report APIs

### GET /reports/total-revenue
Get total revenue from all products.

**cURL:**
```bash
curl -X GET http://localhost:8080/reports/total-revenue
```

**Expected Response:**
```json
{
  "totalRevenue": 1019.98
}
```

---

### GET /reports/expensive-products
Get all products with price greater than 100.0, sorted by price (descending).

**cURL:**
```bash
curl -X GET http://localhost:8080/reports/expensive-products
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "name": "Laptop",
    "price": 999.99,
    "productType": "Physical",
    "weight": 2.5,
    "shippingAddress": "123 Main Street, New York, NY 10001"
  }
]
```

---

### GET /reports/parallel
Get parallel report using Fork/Join framework.

**cURL:**
```bash
curl -X GET http://localhost:8080/reports/parallel
```

**Expected Response:**
```json
{
  "totalRevenue": 1019.98,
  "expensiveProductCount": 1,
  "totalProducts": 2,
  "averagePrice": 509.99
}
```

---

## Testing Workflow

### Step 1: Check Health
```bash
curl -X GET http://localhost:8080/health
```

### Step 2: Create Products
```bash
# Create Physical Product
curl -X POST http://localhost:8080/products/physical \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "price": 999.99,
    "weight": 2.5,
    "shippingAddress": "123 Main St"
  }'

# Create Digital Product
curl -X POST http://localhost:8080/products/digital \
  -H "Content-Type: application/json" \
  -d '{
    "name": "E-Book",
    "price": 19.99,
    "downloadLink": "https://example.com/download",
    "fileSizeInMB": 5
  }'
```

### Step 3: Get All Products
```bash
curl -X GET http://localhost:8080/products
```

### Step 4: Create Order
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'
```

### Step 5: Check Order Status
```bash
curl -X GET http://localhost:8080/orders/status/1
```

### Step 6: Get Reports
```bash
# Total Revenue
curl -X GET http://localhost:8080/reports/total-revenue

# Expensive Products
curl -X GET http://localhost:8080/reports/expensive-products

# Parallel Report
curl -X GET http://localhost:8080/reports/parallel
```

---

## Postman Collection Import

You can import these endpoints into Postman by:

1. **Create a new Collection** in Postman named "E-Commerce API"
2. **Add each endpoint** as a new request
3. **Copy the cURL commands** above and use Postman's "Import" → "Raw text" feature
4. **Or manually create requests** using the details below:

### Request Templates:

**Health Check:**
- Method: GET
- URL: `http://localhost:8080/health`

**Create Physical Product:**
- Method: POST
- URL: `http://localhost:8080/products/physical`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "name": "Laptop",
  "price": 999.99,
  "weight": 2.5,
  "shippingAddress": "123 Main St"
}
```

**Create Digital Product:**
- Method: POST
- URL: `http://localhost:8080/products/digital`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "name": "E-Book",
  "price": 19.99,
  "downloadLink": "https://example.com/download",
  "fileSizeInMB": 5
}
```

**Get All Products:**
- Method: GET
- URL: `http://localhost:8080/products`

**Get Product by ID:**
- Method: GET
- URL: `http://localhost:8080/products/1`

**Delete Product:**
- Method: DELETE
- URL: `http://localhost:8080/products/1`

**Create Order:**
- Method: POST
- URL: `http://localhost:8080/orders`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "productId": 1,
  "quantity": 2
}
```

**Get Order Status:**
- Method: GET
- URL: `http://localhost:8080/orders/status/1`

**Get Total Revenue:**
- Method: GET
- URL: `http://localhost:8080/reports/total-revenue`

**Get Expensive Products:**
- Method: GET
- URL: `http://localhost:8080/reports/expensive-products`

**Get Parallel Report:**
- Method: GET
- URL: `http://localhost:8080/reports/parallel`

---

## Notes

- All endpoints return JSON responses except `/health` which returns plain text
- Product IDs and Order IDs are auto-generated by the database
- The `productId` in order creation must reference an existing product
- Products with duplicate name and price will return `null` (duplicate prevention)
- Error responses follow the format defined in `GlobalExceptionHandler`

