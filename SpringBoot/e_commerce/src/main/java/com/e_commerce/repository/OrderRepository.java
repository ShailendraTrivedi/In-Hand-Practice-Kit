package com.e_commerce.repository;

import com.e_commerce.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
public class OrderRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper for Order (JDBC concept)
    private final RowMapper<Order> orderRowMapper = new RowMapper<Order>() {
        @Override
        public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
            Order order = new Order();
            order.setId(rs.getLong("id"));
            order.setProductId(rs.getLong("product_id"));
            order.setQuantity(rs.getInt("quantity"));
            order.setTotalAmount(rs.getDouble("total_amount"));
            order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
            String idempotencyKey = rs.getString("idempotency_key");
            if (!rs.wasNull()) {
                order.setIdempotencyKey(idempotencyKey);
            }
            return order;
        }
    };

    // Save order using JDBC
    public Order save(Order order) {
        String sql = "INSERT INTO orders (product_id, quantity, total_amount, status, idempotency_key) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, order.getProductId());
            ps.setInt(2, order.getQuantity());
            ps.setDouble(3, order.getTotalAmount());
            ps.setString(4, order.getStatus().name());
            ps.setString(5, order.getIdempotencyKey());
            return ps;
        }, keyHolder);

        Long generatedId = keyHolder.getKey().longValue();
        order.setId(generatedId);
        return order;
    }

    // Find order by idempotency key
    public Order findByIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT * FROM orders WHERE idempotency_key = ?";
        List<Order> orders = jdbcTemplate.query(sql, orderRowMapper, idempotencyKey);
        return orders.isEmpty() ? null : orders.get(0);
    }

    // Find order by ID using JDBC
    public Order findById(Long id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        List<Order> orders = jdbcTemplate.query(sql, orderRowMapper, id);
        return orders.isEmpty() ? null : orders.get(0);
    }

    // Find all orders using JDBC with pagination
    public List<Order> findAll(int page, int size, String sortBy, String direction) {
        // Validate and sanitize sortBy to prevent SQL injection
        String validSortBy = validateSortColumn(sortBy);
        String validDirection = "DESC".equalsIgnoreCase(direction) ? "DESC" : "ASC";

        int offset = page * size;
        String sql = String.format("SELECT * FROM orders ORDER BY %s %s LIMIT ? OFFSET ?",
                validSortBy, validDirection);
        return jdbcTemplate.query(sql, orderRowMapper, size, offset);
    }

    // Count total orders
    public long count() {
        String sql = "SELECT COUNT(*) FROM orders";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    // Validate sort column to prevent SQL injection
    private String validateSortColumn(String sortBy) {
        // Whitelist of allowed columns
        String[] allowedColumns = { "id", "product_id", "quantity", "total_amount", "status", "created_at",
                "updated_at" };
        for (String column : allowedColumns) {
            if (column.equalsIgnoreCase(sortBy)) {
                return column;
            }
        }
        return "created_at"; // Default to created_at if invalid
    }
}