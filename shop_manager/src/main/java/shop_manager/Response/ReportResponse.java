package com.example.shop_manager.Response;


import com.example.shop_manager.Main.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportResponse {

    public List<Object[]> fetchAllInvoices() throws SQLException {
        List<Object[]> invoices = new ArrayList<>();
        String sql = """
             SELECT o.id AS invoice_id,
                           c.id AS customer_id,
                           c.name AS customer_name,
                           p.id AS product_id,
                           p.price,
                           op.quantity,
                           ROUND(p.price * op.quantity, 2) AS total
                    FROM `Order` o
                    LEFT JOIN Order_Customer oc ON o.id = oc.id_order
                    LEFT JOIN Customer c ON oc.id_customer = c.id
                    LEFT JOIN Order_Product op ON o.id = op.order_id
                    LEFT JOIN Product p ON op.product_id = p.id
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Object[] row = {
                        resultSet.getString("invoice_id"),
                        resultSet.getString("customer_id"),
                        resultSet.getString("customer_name"),
                        resultSet.getString("product_id"),
                        resultSet.getDouble("price"),
                        resultSet.getInt("quantity"),
                        resultSet.getDouble("total")
                };
                invoices.add(row);
            }
        }
        return invoices;
    }

    public List<Object[]> fetchInvoicesByCustomerID(String customerID) throws SQLException {
        List<Object[]> invoices = new ArrayList<>();
        String sql = """
            SELECT o.id AS invoice_id, 
                   c.id AS customer_id, 
                   c.name AS customer_name, 
                   p.id AS product_id, 
                   p.price, 
                   op.quantity, 
                  ROUND(p.price * op.quantity, 2) AS total
            FROM `Order` o
            LEFT JOIN Order_Customer oc ON o.id = oc.id_order
            LEFT JOIN Customer c ON oc.id_customer = c.id
            LEFT JOIN Order_Product op ON o.id = op.order_id
            LEFT JOIN Product p ON op.product_id = p.id
            WHERE c.id = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, customerID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Object[] row = {
                            resultSet.getString("invoice_id"),
                            resultSet.getString("customer_id"),
                            resultSet.getString("customer_name"),
                            resultSet.getString("product_id"),
                            resultSet.getDouble("price"),
                            resultSet.getInt("quantity"),
                            resultSet.getDouble("total")
                    };
                    invoices.add(row);
                }
            }
        }
        return invoices;
    }

}