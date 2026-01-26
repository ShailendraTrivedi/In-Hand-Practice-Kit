package com.e_commerce.repository;

import com.e_commerce.model.DigitalProduct;
import com.e_commerce.model.PhysicalProduct;
import com.e_commerce.model.Product;
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
import java.util.Map;

@Repository
public class ProductRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // RowMapper for Product (JDBC concept)
    private final RowMapper<Product> productRowMapper = new RowMapper<Product>() {
        @Override
        public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
            String productType = rs.getString("product_type");
            Product product;

            if ("Physical".equals(productType)) {
                PhysicalProduct physicalProduct = new PhysicalProduct();
                physicalProduct.setWeight(rs.getDouble("weight"));
                physicalProduct.setShippingAddress(rs.getString("shipping_address"));
                product = physicalProduct;
            } else {
                DigitalProduct digitalProduct = new DigitalProduct();
                digitalProduct.setDownloadLink(rs.getString("download_link"));
                Long fileSize = rs.getLong("file_size_mb");
                if (!rs.wasNull()) {
                    digitalProduct.setFileSizeInMB(fileSize);
                }
                product = digitalProduct;
            }

            product.setId(rs.getLong("id"));
            product.setName(rs.getString("name"));
            product.setPrice(rs.getDouble("price"));

            return product;
        }
    };

    // Create product using JDBC
    public Product save(Product product) {
        String sql;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        if (product instanceof PhysicalProduct) {
            PhysicalProduct physicalProduct = (PhysicalProduct) product;
            sql = "INSERT INTO products (name, price, product_type, weight, shipping_address) VALUES (?, ?, ?, ?, ?)";

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[] { "ID" });
                ps.setString(1, physicalProduct.getName());
                ps.setDouble(2, physicalProduct.getPrice());
                ps.setString(3, "Physical");
                ps.setDouble(4, physicalProduct.getWeight());
                ps.setString(5, physicalProduct.getShippingAddress());
                return ps;
            }, keyHolder);
        } else if (product instanceof DigitalProduct) {
            DigitalProduct digitalProduct = (DigitalProduct) product;
            sql = "INSERT INTO products (name, price, product_type, download_link, file_size_mb) VALUES (?, ?, ?, ?, ?)";

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[] { "ID" });
                ps.setString(1, digitalProduct.getName());
                ps.setDouble(2, digitalProduct.getPrice());
                ps.setString(3, "Digital");
                ps.setString(4, digitalProduct.getDownloadLink());
                ps.setObject(5, digitalProduct.getFileSizeInMB());
                return ps;
            }, keyHolder);
        } else {
            throw new IllegalArgumentException("Unknown product type");
        }

        // Get the ID from the key holder (handle multiple keys)
        Map<String, Object> keys = keyHolder.getKeys();
        Long generatedId = ((Number) keys.get("ID")).longValue();
        product.setId(generatedId);
        return product;
    }

    // Find all products using JDBC with pagination
    public List<Product> findAll(int page, int size, String sortBy, String direction) {
        // Validate and sanitize sortBy to prevent SQL injection
        String validSortBy = validateSortColumn(sortBy);
        String validDirection = "DESC".equalsIgnoreCase(direction) ? "DESC" : "ASC";

        int offset = page * size;
        String sql = String.format("SELECT * FROM products ORDER BY %s %s LIMIT ? OFFSET ?",
                validSortBy, validDirection);
        return jdbcTemplate.query(sql, productRowMapper, size, offset);
    }

    // Count total products
    public long count() {
        String sql = "SELECT COUNT(*) FROM products";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    // Validate sort column to prevent SQL injection
    private String validateSortColumn(String sortBy) {
        // Whitelist of allowed columns
        String[] allowedColumns = { "id", "name", "price", "product_type", "created_at" };
        for (String column : allowedColumns) {
            if (column.equalsIgnoreCase(sortBy)) {
                return column;
            }
        }
        return "id"; // Default to id if invalid
    }

    // Find product by ID using JDBC
    public Product findById(Long id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        List<Product> products = jdbcTemplate.query(sql, productRowMapper, id);
        return products.isEmpty() ? null : products.get(0);
    }

    // Delete product using JDBC
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }

    // Check if product exists (for duplicate checking)
    public boolean existsByNameAndPrice(String name, Double price) {
        String sql = "SELECT COUNT(*) FROM products WHERE name = ? AND price = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name, price);
        return count != null && count > 0;
    }
}