package com.example.shop_manager.GUI;

import com.example.shop_manager.Entity.Product;
import com.example.shop_manager.Interface.IProduct;
import com.example.shop_manager.Response.ProductResponse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class ProductGUI extends JPanel implements IProduct {

    private JTextField txtId, txtName, txtPrice, txtQuantity, txtSearch;
    private JComboBox<String> cmbCategory;
    private JTable table;
    private DefaultTableModel model;

    private ProductResponse productResponse;

    public ProductGUI() {
        productResponse = new ProductResponse();
        setLayout(new BorderLayout());

        // Tạo bảng hiển thị sản phẩm
        String[] columnNames = {"Product ID", "Product Name", "Category", "Price", "Quantity"};
        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panelForm = new JPanel(new GridLayout(6, 2));

//        panelForm.add(new JLabel("Product ID:"));
        txtId = new JTextField();
//        txtId.setEditable(false);
//        panelForm.add(txtId);

        panelForm.add(new JLabel("Product Name:"));
        txtName = new JTextField();
        panelForm.add(txtName);

        panelForm.add(new JLabel("Category:"));
        cmbCategory = new JComboBox<>(new String[]{"Electronics", "Home Appliances", "Food"});
        panelForm.add(cmbCategory);

        panelForm.add(new JLabel("Price:"));
        txtPrice = new JTextField();
        panelForm.add(txtPrice);

        panelForm.add(new JLabel("Quantity:"));
        txtQuantity = new JTextField();
        panelForm.add(txtQuantity);

        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(e -> addProduct());
        panelForm.add(btnAdd);

        JButton btnUpdate = new JButton("Update");
        btnUpdate.addActionListener(e -> updateProduct());
        panelForm.add(btnUpdate);

        JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(e -> deleteProduct());
        panelForm.add(btnDelete);

        JButton btnLoadData = new JButton("Load Data");
        btnLoadData.addActionListener(e -> loadData());
        panelForm.add(btnLoadData);

        add(panelForm, BorderLayout.EAST);

        // Thanh tìm kiếm
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        searchPanel.add(new JLabel("Search by Name:"));
        txtSearch = new JTextField(10); // Sử dụng biến thành viên
        searchPanel.add(txtSearch);

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> searchProduct());
        searchPanel.add(btnSearch);
        add(searchPanel, BorderLayout.NORTH);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    txtId.setText(model.getValueAt(selectedRow, 0).toString());
                    txtName.setText(model.getValueAt(selectedRow, 1).toString());
                    cmbCategory.setSelectedItem(model.getValueAt(selectedRow, 2).toString());
                    txtPrice.setText(model.getValueAt(selectedRow, 3).toString());
                    txtQuantity.setText(model.getValueAt(selectedRow, 4).toString());
                }
            }
        });

    }
    @Override
    public void loadData() {
        model.setRowCount(0);
        try {
            ArrayList<Product> products = productResponse.loadAllProducts();
            for (Product product : products) {
                model.addRow(new Object[]{product.getId(), product.getName(), product.getCategory(),
                        product.getPrice(), product.getQuantity()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    @Override
    public void addProduct() {
        try {
            String name = txtName.getText().trim();
            String category = (String) cmbCategory.getSelectedItem();
            double price = Double.parseDouble(txtPrice.getText().trim());
            int quantity = Integer.parseInt(txtQuantity.getText().trim());

            Product product = new Product(0, name, category, price, quantity);
            while (productResponse.isExistProduct(product)) {
                JOptionPane.showMessageDialog(table,"This product is already exist. Please add a new one.");
                product.setName(JOptionPane.showInputDialog(table, "Enter Product Name:"));
            }

            if (productResponse.addProduct(product)) {
                model.addRow(new Object[]{product.getId(), product.getName(), product.getCategory(),
                        product.getPrice(), product.getQuantity()});
                JOptionPane.showMessageDialog(this, "Product added successfully!");
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add product.");
            }
        } catch (Exception ex) {

        }
    }
    @Override
    public void updateProduct() {
        try {
            int id = Integer.parseInt(txtId.getText().trim());
            String name = txtName.getText().trim();
            String category = (String) cmbCategory.getSelectedItem();
            double price = Double.parseDouble(txtPrice.getText().trim());
            int quantity = Integer.parseInt(txtQuantity.getText().trim());

            Product product = new Product(id, name, category, price, quantity);
            if (productResponse.updateProduct(product)) {
                int selectedRow = table.getSelectedRow();
                model.setValueAt(name, selectedRow, 1);
                model.setValueAt(category, selectedRow, 2);
                model.setValueAt(price, selectedRow, 3);
                model.setValueAt(quantity, selectedRow, 4);
                JOptionPane.showMessageDialog(this, "Product updated successfully!");
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update product.");
            }
        } catch (Exception ex) {

        }
    }
    @Override
    public void deleteProduct() {
        try {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a product to delete.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(table, "Are you sure you want to delete this product?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            int id = (int) model.getValueAt(selectedRow, 0);
            if (productResponse.deleteProduct(id)) {
                model.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete product.");
            }
        } catch (Exception ex) {

        }
    }
    @Override
    public void searchProduct() {
        try {
            String searchName = txtSearch.getText().trim();
            Product product = productResponse.searchProductByName(searchName);

            if (product != null) {
                model.setRowCount(0);
                model.addRow(new Object[]{product.getId(), product.getName(), product.getCategory(),
                        product.getPrice(), product.getQuantity()});
                JOptionPane.showMessageDialog(this, "Product found!");
            } else {
                JOptionPane.showMessageDialog(this, "No product found with the given name.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    public void clearFields() {
        txtId.setText("");
        txtName.setText("");
        txtPrice.setText("");
        txtQuantity.setText("");
        cmbCategory.setSelectedIndex(0);
        txtSearch.setText("");
    }
}