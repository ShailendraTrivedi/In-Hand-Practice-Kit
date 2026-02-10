package com.e_commerce.order.repository.impl;

import com.e_commerce.order.entity.Order;
import com.e_commerce.order.repository.IOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class OrderRepository implements IOrderRepository {

    private final JdbcTemplate jdbcTemplate;

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

    @Override
    public Order save(Order order) {
        String sql = "INSERT INTO orders (product_id, quantity, total_amount, status, idempotency_key) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "ID" });
            ps.setLong(1, order.getProductId());
            ps.setInt(2, order.getQuantity());
            ps.setDouble(3, order.getTotalAmount());
            ps.setString(4, order.getStatus().name());
            ps.setString(5, order.getIdempotencyKey());
            return ps;
        }, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        Long generatedId = ((Number) keys.get("ID")).longValue();
        order.setId(generatedId);
        return order;
    }

    @Override
    public Order findByIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT * FROM orders WHERE idempotency_key = ?";
        List<Order> orders = jdbcTemplate.query(sql, orderRowMapper, idempotencyKey);
        return orders.isEmpty() ? null : orders.get(0);
    }

    @Override
    public Order findById(Long id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        List<Order> orders = jdbcTemplate.query(sql, orderRowMapper, id);
        return orders.isEmpty() ? null : orders.get(0);
    }

    @Override
    public List<Order> findAll(int page, int size, String sortBy, String direction) {
        String validSortBy = validateSortColumn(sortBy);
        String validDirection = "DESC".equalsIgnoreCase(direction) ? "DESC" : "ASC";

        int offset = page * size;
        String sql = String.format("SELECT * FROM orders ORDER BY %s %s LIMIT ? OFFSET ?",
                validSortBy, validDirection);
        return jdbcTemplate.query(sql, orderRowMapper, size, offset);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM orders";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    private String validateSortColumn(String sortBy) {
        String[] allowedColumns = { "id", "product_id", "quantity", "total_amount", "status", "created_at",
                "updated_at" };
        for (String column : allowedColumns) {
            if (column.equalsIgnoreCase(sortBy)) {
                return column;
            }
        }
        return "created_at";
    }
}

