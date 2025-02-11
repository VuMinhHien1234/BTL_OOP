package com.example.shop_manager.Response;

import com.example.shop_manager.Entity.Inventory;
import com.example.shop_manager.Main.DatabaseConnection;

import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryResponse  {

    public void loadProducts(DefaultTableModel model) {
        model.setRowCount(0);
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM product";
            ResultSet resultSet = connection.createStatement().executeQuery(sql);

            while (resultSet.next()) {
                Inventory inventory = Inventory.fromResultSet(resultSet);
                model.addRow(new Object[]{
                        inventory.getId(),
                        inventory.getName(),
                        inventory.getCategory(),
                        inventory.getPrice(),
                        inventory.getQuantity()
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading products: " + e.getMessage());
        }
    }

    public void searchProducts(DefaultTableModel model, String nameKeyword, String categoryKeyword) {
        model.setRowCount(0); // Clear table
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM product WHERE name LIKE ? AND category LIKE ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, nameKeyword + "%"); // Search by starting characters of name
            statement.setString(2, categoryKeyword + "%"); // Search by starting characters of category
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Inventory inventory = Inventory.fromResultSet(resultSet);
                model.addRow(new Object[]{
                        inventory.getId(),
                        inventory.getName(),
                        inventory.getCategory(),
                        inventory.getPrice(),
                        inventory.getQuantity()
                });
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching products: " + e.getMessage());
        }
    }
}
