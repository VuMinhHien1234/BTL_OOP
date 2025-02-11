package com.example.shop_manager.GUI;

import com.example.shop_manager.Interface.IReport;
import com.example.shop_manager.Response.ReportResponse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

public class ReportGUI extends JPanel implements IReport {
    private JTextField txtCustomerID;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblTotalAmount;
    private ReportResponse reportResponse;

    public ReportGUI() {
        reportResponse = new ReportResponse();
        setLayout(new BorderLayout());

        String[] columnNames = {"Invoice ID", "Customer ID", "Customer Name", "Product ID", "Price", "Quantity", "Total"};
        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new GridLayout(2, 1));
        JPanel panelRow1 = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnPrintInvoices = new JButton("Print All Invoices");
        btnPrintInvoices.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printAllInvoices();
            }
        });
        panelRow1.add(btnPrintInvoices);

        JPanel panelRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelRow2.add(new JLabel("Enter Customer ID:"));
        txtCustomerID = new JTextField(10);
        panelRow2.add(txtCustomerID);
        JButton btnSearchInvoices = new JButton("Search by Customer ID");
        btnSearchInvoices.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchInvoicesByCustomerID();
            }
        });
        panelRow2.add(btnSearchInvoices);

        lblTotalAmount = new JLabel("Total Amount: 0.0");
        panelRow2.add(lblTotalAmount);

        controlPanel.add(panelRow1);
        controlPanel.add(panelRow2);
        add(controlPanel, BorderLayout.SOUTH);
    }
    @Override
    public void printAllInvoices() {
        try {
            List<Object[]> invoices = reportResponse.fetchAllInvoices();
            updateTable(invoices);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching all invoices: " + ex.getMessage());
        }
    }
    @Override
    public void searchInvoicesByCustomerID() {
        String customerID = txtCustomerID.getText().trim();
        if (customerID.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Customer ID.");
            return;
        }
        try {
            List<Object[]> invoices = reportResponse.fetchInvoicesByCustomerID(customerID);
            updateTable(invoices);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching invoices by Customer ID: " + ex.getMessage());
        }
    }
    @Override
    public void updateTable(List<Object[]> invoices) {
        model.setRowCount(0);
        double totalAmount = 0.0;
        for (Object[] row : invoices) {
            model.addRow(row);
            totalAmount += (double) row[6];
        }
       lblTotalAmount.setText("Total Amount: " + Math.round(totalAmount));
    }
}
