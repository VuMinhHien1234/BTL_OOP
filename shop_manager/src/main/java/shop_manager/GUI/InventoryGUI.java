package com.example.shop_manager.GUI;


import com.example.shop_manager.Interface.IInventory;
import com.example.shop_manager.Response.InventoryResponse;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class InventoryGUI extends JPanel implements IInventory {
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchNameField;
    private JTextField searchCategoryField;
    private InventoryResponse response;

    public InventoryGUI() {
        response = new InventoryResponse();
        setLayout(new BorderLayout());
        String[] columnNames = {"Product ID", "Product Name", "Category", "Price", "Quantity"};
        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        searchPanel.add(new JLabel("Search by Product Name:"));
        searchNameField = new JTextField();
        searchPanel.add(searchNameField);

        searchPanel.add(new JLabel("Search by Category:"));
        searchCategoryField = new JTextField();
        searchPanel.add(searchCategoryField);

        add(searchPanel, BorderLayout.NORTH);

        JPanel controlPanel = new JPanel();
        JButton btnLoad = new JButton("Load All Products");
        controlPanel.add(btnLoad);
        btnLoad.addActionListener(e -> loadProducts());

        add(controlPanel, BorderLayout.SOUTH);

        // Add listeners for live search
        searchNameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchProducts();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchProducts();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchProducts();
            }
        });

        searchCategoryField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchProducts();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchProducts();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchProducts();
            }
        });
    }
    @Override
    public void loadProducts() {
        try {
            response.loadProducts(model);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    @Override
    public void searchProducts() {
        try {
            String nameKeyword = searchNameField.getText().trim();
            String categoryKeyword = searchCategoryField.getText().trim();
            response.searchProducts(model, nameKeyword, categoryKeyword);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
