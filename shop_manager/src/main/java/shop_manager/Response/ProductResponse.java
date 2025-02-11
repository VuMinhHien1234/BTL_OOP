package com.example.shop_manager.Response;

import com.example.shop_manager.Entity.Product;
import com.example.shop_manager.Main.DatabaseConnection;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;

public class ProductResponse {

    public ArrayList<Product> loadAllProducts() throws SQLException {
        ArrayList<Product> productList = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM product";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String category = resultSet.getString("category");
                double price = resultSet.getDouble("price");
                int quantity = resultSet.getInt("quantity");

                productList.add(new Product(id, name, category, price, quantity));
            }
        }
        return productList;
    }

    public Product searchProductByName(String searchName) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM product WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, searchName);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String category = resultSet.getString("category");
                double price = resultSet.getDouble("price");
                int quantity = resultSet.getInt("quantity");
                return new Product(id, name, category, price, quantity);
            }
        }
        return null;
    }

    public boolean addProduct(Product product) throws SQLException {
        if (product.getQuantity() < 0) {
            JOptionPane.showMessageDialog(null, "Product quantity is less than 0");
            return false;
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO product(name, category, price, quantity) VALUES(?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, product.getName());
            statement.setString(2, product.getCategory());
            statement.setDouble(3, product.getPrice());
            statement.setInt(4, product.getQuantity());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateProduct(Product product) throws SQLException {
        if (product.getQuantity() < 0) {
            JOptionPane.showMessageDialog(null, "Product quantity is less than 0");
            return false;
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "UPDATE product SET name = ?, category = ?, price = ?, quantity = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, product.getName());
            statement.setString(2, product.getCategory());
            statement.setDouble(3, product.getPrice());
            statement.setInt(4, product.getQuantity());
            statement.setInt(5, product.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteProduct(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM product WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }

    public boolean isExistProduct(Product product) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Product WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, product.getName());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        }
        catch (SQLException e) {

        }
        return false;
    }
}