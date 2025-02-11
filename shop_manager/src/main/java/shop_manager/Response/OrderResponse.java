package com.example.shop_manager.Response;

import com.example.shop_manager.Main.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class OrderResponse {

    public static void addOrder(DefaultTableModel tableModel, String orderId, String customerId, String productId, String quantityText) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            int quantity = parseQuantity(quantityText);
            if (quantity <= 0) return;

            customerId = validateCustomerId(conn, customerId);
            if (customerId == null) return;

            productId = validateProductId(conn, productId);
            if (productId == null) return;

            if (!checkOrderIdExists(conn, orderId)) {
                insertOrder(conn, orderId);
            } else {
                JOptionPane.showMessageDialog(null, "Order ID is already in use.");
                return;
            }

            linkOrderToCustomer(conn, orderId, customerId);
            processProductOrder(conn, tableModel, orderId, productId, quantity);

            JOptionPane.showMessageDialog(null, "Order added successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadData(tableModel);
    }


    // Xóa đơn hàng
    public static void deleteOrder(String orderId, JTable orderTable, DefaultTableModel tableModel) {
        if (orderId.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Order ID cannot be empty.");
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            deleteOrderProduct(conn, orderId);
            deleteOrderCustomer(conn, orderId);
            deleteOrderReport(conn, orderId);
            deleteOrderMain(conn, orderId, orderTable, tableModel);
            conn.commit();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error deleting order: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        loadData(tableModel);
    }

    // Load lại đơn hàng
    public static void loadData(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        String sql = """
                SELECT
                    o.id AS order_id,
                    o.status,
                    c.id AS customer_id,
                    c.name AS customer_name,
                    p.id AS product_id,
                    p.price,
                    op.quantity,
                    (p.price * op.quantity) AS total
                FROM
                    `Order` o
                JOIN
                    Order_Customer oc ON o.id = oc.id_order
                JOIN
                    Customer c ON oc.id_customer = c.id
                JOIN
                    Order_Product op ON o.id = op.order_id
                JOIN
                    Product p ON op.product_id = p.id
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                double total = rs.getDouble("total");
                double formattedTotal = Math.round(total);
                Object[] rowData = {
                        rs.getString("order_id"),
                        rs.getString("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("product_id"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        formattedTotal,
                        rs.getString("status")
                };
                tableModel.addRow(rowData);
            }
        } catch (SQLException ex) {
        }
    }



    // Cập nhập đơn hàng
    public static void updateOrder(String orderId, String customerId, String productId, String quantityText, JTable orderTable, DefaultTableModel tableModel) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            Double price = getProductPrice(conn, productId);
            if (price == null) {
                JOptionPane.showMessageDialog(null, "Product not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int quantityInput = Integer.parseInt(quantityText.trim());
            if(quantityInput <0 ) {
                JOptionPane.showMessageDialog(null, "Quantity must greater than 0");
                return;
            }
            int stockQuantity = getProductStockQuantity(conn, productId);
            int orderQuantity = getOldOrderQuantity(conn, orderId, productId);

            // Tính tổng số lượng có thể sử dụng từ kho và đơn hàng hiện tại
            int totalAvailableQuantity = stockQuantity + orderQuantity;

            if (quantityInput > totalAvailableQuantity) {
                quantityInput = totalAvailableQuantity;
                JOptionPane.showMessageDialog(null, "Not enough stock. Order quantity has been adjusted to available stock.", "Stock Adjusted", JOptionPane.INFORMATION_MESSAGE);
            }

            // Cập nhật tổng giá trị đơn hàng
            double total = price * quantityInput;
            updateOrderPrice(conn, orderId, total);

            int oldQuantityOrder = getOldOrderQuantity(conn, orderId, productId);
            // Điều chỉnh số lượng trong kho và Order_Product
            adjustProductQuantity(conn, orderId,productId, oldQuantityOrder, quantityInput);

            conn.commit();
            JOptionPane.showMessageDialog(null, "Order and product quantity updated successfully!");
            loadData(tableModel);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Please enter valid numbers for quantity.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static int getOldOrderQuantity(Connection conn, String orderId, String productId) throws SQLException {
        String query = "SELECT quantity FROM Order_Product WHERE order_id = ? AND product_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, orderId);
            stmt.setString(2, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("quantity");
            }
        }
        throw new SQLException("Order product not found.");
    }

    private static void adjustProductQuantity(Connection conn, String orderId, String productId, int oldQuantityOrder, int newQuantityInput) throws SQLException {
        // Lấy số lượng hiện tại trong kho
        int currentStock = getProductStockQuantity(conn,productId);

        // Trường hợp tổng số hàng trong kho cộng với số trong đơn hàng nhỏ hơn hoặc bằng số hàng nhập vào
        if (currentStock + oldQuantityOrder <= newQuantityInput) {
            // Cập nhật số lượng trong kho thành 0
            String sqlUpdateStockToZero = "UPDATE Product SET quantity = 0 WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateStockToZero)) {
                stmt.setString(1, productId);
                stmt.executeUpdate();
            }

            // Cập nhật số lượng trong đơn hàng bằng tổng số lượng trong kho và đơn hàng
            String sqlUpdateOrderProductQuantity = "UPDATE Order_Product SET quantity = ? WHERE order_id = ? AND product_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateOrderProductQuantity)) {
                stmt.setInt(1, currentStock + oldQuantityOrder); // Số lượng mới trong đơn hàng
                stmt.setString(2, orderId);
                stmt.setString(3, productId);
                stmt.executeUpdate();
            }
        } else {
            // Trường hợp giảm số lượng trong đơn hàng và trả lại số vào kho
            if (newQuantityInput < oldQuantityOrder) {
                // Trả lại số lượng vào kho
                int quantityToReturn = oldQuantityOrder - newQuantityInput;
                String sqlUpdateStock = "UPDATE Product SET quantity = quantity + ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateStock)) {
                    stmt.setInt(1, quantityToReturn); // Thêm vào kho
                    stmt.setString(2, productId);
                    stmt.executeUpdate();
                }
            }

            // Trường hợp số lượng trong kho đủ, thực hiện trừ đi số lượng nhập vào từ kho
            if (newQuantityInput > oldQuantityOrder) {
                int quantityToReduce = newQuantityInput - oldQuantityOrder;// Trừ đi số lượng kho
                String sqlUpdateStock = "UPDATE Product SET quantity = quantity - ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateStock)) {
                    stmt.setInt(1, quantityToReduce);
                    stmt.setString(2, productId);
                    stmt.executeUpdate();
                }
            }

            // Cập nhật lại số lượng trong đơn hàng với số lượng mới
            String sqlUpdateOrderProduct = "UPDATE Order_Product SET quantity = ? WHERE order_id = ? AND product_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateOrderProduct)) {
                stmt.setInt(1, newQuantityInput);
                stmt.setString(2, orderId);
                stmt.setString(3, productId);
                stmt.executeUpdate();
            }
        }
    }

    private static Double getProductPrice(Connection conn, String productId) throws SQLException {
        String query = "SELECT price FROM Product WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price");
            }
        }
        return null;
    }

    private static void updateOrderPrice(Connection conn, String orderId, double total) throws SQLException {
        String sqlUpdateOrder = "UPDATE `Order` SET totalprice = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateOrder)) {
            stmt.setDouble(1, total);
            stmt.setString(2, orderId);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated <= 0) {
                throw new SQLException("Order ID not found.");
            }
        }
    }


    private static int getProductStockQuantity(Connection conn, String productId) throws SQLException {
        String sql = "SELECT quantity FROM Product WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("quantity");
            } else {
                return 0;
            }
        }
    }



    private static int parseQuantity(String quantityText) {
        try {
            int quantity = Integer.parseInt(quantityText.trim());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(null, "Quantity must be greater than 0.");
            }
            return quantity;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Invalid quantity. Please enter a valid number.");
            return -1;
        }
    }
    private static String validateProductId(Connection conn, String productId) throws SQLException {
        while (true) {
            String checkProduct = "SELECT id FROM Product WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkProduct)) {
                stmt.setString(1, productId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return productId;
                    } else {
                        JOptionPane.showMessageDialog(null, "Product ID not found. Please try again.");
                        productId = JOptionPane.showInputDialog(null, "Enter Product ID:");
                        if (productId == null || productId.trim().isEmpty()) {
                            JOptionPane.showMessageDialog(null, "Product ID cannot be empty.");
                            return null;
                        }
                    }
                }
            }
        }
    }
    private static String validateCustomerId(Connection conn, String customerId) throws SQLException {
        while (true) {
            String checkCustomer = "SELECT id FROM Customer WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkCustomer)) {
                stmt.setString(1, customerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return customerId;
                    } else {
                        JOptionPane.showMessageDialog(null, "Customer ID not found. Please try again.");
                        customerId = JOptionPane.showInputDialog(null, "Enter Customer ID:");
                        if (customerId == null || customerId.trim().isEmpty()) {
                            JOptionPane.showMessageDialog(null, "Customer ID cannot be empty.");
                            return null;
                        }
                    }
                }
            }
        }
    }

    private static boolean checkOrderIdExists(Connection conn, String orderId) throws SQLException {
        String checkOrder = "SELECT id FROM `Order` WHERE id = ?";
        try (PreparedStatement checkOrderStmt = conn.prepareStatement(checkOrder)) {
            checkOrderStmt.setString(1, orderId);
            try (ResultSet rs = checkOrderStmt.executeQuery()) {
                return rs.next();
            }
        }
    }private static void insertOrder(Connection conn, String orderId) throws SQLException {
        String insertOrder = "INSERT INTO `Order` (id, totalPrice, status) VALUES (?, 0, '1')";
        try (PreparedStatement stmt = conn.prepareStatement(insertOrder)) {
            stmt.setString(1, orderId);
            stmt.executeUpdate();
        }
    }
    private static void linkOrderToCustomer(Connection conn, String orderId, String customerId) throws SQLException {
        String insertOrderCustomer = "INSERT INTO Order_Customer (id_order, id_customer) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertOrderCustomer)) {
            stmt.setString(1, orderId);
            stmt.setString(2, customerId);
            stmt.executeUpdate();
        }
    }
    private static void processProductOrder(Connection conn, DefaultTableModel tableModel, String orderId, String productId, int quantity) throws SQLException {
        String checkProduct = "SELECT quantity, price FROM Product WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(checkProduct)) {
            stmt.setString(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int availableQuantity = rs.getInt("quantity");
                    double productPrice = rs.getDouble("price");

                    if (availableQuantity < quantity) {
                        JOptionPane.showMessageDialog(null, "Not enough product in stock.");
                        return;
                    }
                    insertOrderProduct(conn, orderId, productId, quantity);
                    updateProductQuantity(conn, productId, quantity);
                    updateOrderTotalPrice(conn, orderId, productPrice * quantity);
                } else {
                    JOptionPane.showMessageDialog(null, "Product ID not found.");
                }
            }
        }
    }

    private static void insertOrderProduct(Connection conn, String orderId, String productId, int quantity) throws SQLException {
        String insertOrderProduct = "INSERT INTO Order_Product (order_id, product_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertOrderProduct)) {
            stmt.setString(1, orderId);
            stmt.setString(2, productId);
            stmt.setInt(3, quantity);
            stmt.executeUpdate();
        }
    }

    private static void updateProductQuantity(Connection conn, String productId, int quantity) throws SQLException {
        String updateProductQuantity = "UPDATE Product SET quantity = quantity - ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateProductQuantity)) {
            stmt.setInt(1, quantity);
            stmt.setString(2, productId);
            stmt.executeUpdate();
        }
    }

    private static void updateOrderTotalPrice(Connection conn, String orderId, double totalPrice) throws SQLException {
        String updateOrderPrice = "UPDATE `Order` SET totalPrice = totalPrice + ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateOrderPrice)) {
            stmt.setDouble(1, totalPrice);
            stmt.setString(2, orderId);
            stmt.executeUpdate();
        }
    }  private static void deleteOrderProduct(Connection conn, String orderId) throws SQLException {
        String deleteOrderProduct = "DELETE FROM Order_Product WHERE order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteOrderProduct)) {
            stmt.setString(1, orderId);
            stmt.executeUpdate();
        }
    }

    private static void deleteOrderCustomer(Connection conn, String orderId) throws SQLException {
        String deleteOrderCustomer = "DELETE FROM Order_Customer WHERE id_order = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteOrderCustomer)) {
            stmt.setString(1, orderId);
            stmt.executeUpdate();
        }
    }

    private static void deleteOrderReport(Connection conn, String orderId) throws SQLException {
        String deleteOrderReport = "DELETE FROM Report_Order WHERE order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteOrderReport)) {
            stmt.setString(1, orderId);
            stmt.executeUpdate();
        }
    }

    private static void deleteOrderMain(Connection conn, String orderId, JTable orderTable, DefaultTableModel tableModel) throws SQLException {
        String deleteOrder = "DELETE FROM `Order` WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteOrder)) {
            stmt.setString(1, orderId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                int selectedRow = orderTable.getSelectedRow();
                tableModel.removeRow(selectedRow);
            }
        }
    }


}
