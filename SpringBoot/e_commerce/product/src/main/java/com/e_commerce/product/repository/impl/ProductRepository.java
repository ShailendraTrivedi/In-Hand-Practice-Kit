package com.e_commerce.product.repository.impl;

import com.e_commerce.product.entity.DigitalProduct;
import com.e_commerce.product.entity.PhysicalProduct;
import com.e_commerce.product.entity.Product;
import com.e_commerce.product.repository.IProductRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ProductRepository implements IProductRepository {

    private final JdbcTemplate jdbcTemplate;

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

    @Override
    public Product save(Product product) {
        String sql;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        if (product instanceof PhysicalProduct) {
            PhysicalProduct physicalProduct = (PhysicalProduct) product;
            sql = "INSERT INTO products (name, price, product_type, weight, shipping_address) VALUES (?, ?, ?, ?, ?)";

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && !keys.isEmpty()) {
            // PostgreSQL returns keys in lowercase, try both cases
            Object idValue = keys.get("id");
            if (idValue == null) {
                idValue = keys.get("ID");
            }
            if (idValue != null) {
                Long generatedId = ((Number) idValue).longValue();
                product.setId(generatedId);
            }
        }
        return product;
    }

    @Override
    public List<Product> findAll(int page, int size, String sortBy, String direction) {
        String validSortBy = validateSortColumn(sortBy);
        String validDirection = "DESC".equalsIgnoreCase(direction) ? "DESC" : "ASC";

        int offset = page * size;
        String sql = String.format("SELECT * FROM products ORDER BY %s %s LIMIT ? OFFSET ?",
                validSortBy, validDirection);
        return jdbcTemplate.query(sql, productRowMapper, size, offset);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM products";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    private String validateSortColumn(String sortBy) {
        String[] allowedColumns = { "id", "name", "price", "product_type", "created_at" };
        for (String column : allowedColumns) {
            if (column.equalsIgnoreCase(sortBy)) {
                return column;
            }
        }
        return "id";
    }

    @Override
    public Product findById(Long id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        List<Product> products = jdbcTemplate.query(sql, productRowMapper, id);
        return products.isEmpty() ? null : products.get(0);
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }

    @Override
    public boolean existsByNameAndPrice(String name, Double price) {
        String sql = "SELECT COUNT(*) FROM products WHERE name = ? AND price = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name, price);
        return count != null && count > 0;
    }
}

