package com.example.shop_manager.Main;

import com.example.shop_manager.GUI.*;

import javax.swing.*;


public class ShopApp extends javax.swing.JFrame{
    private JFrame frame;
    private JTabbedPane tabbedPane;

    public ShopApp() {
        frame = new JFrame("SHOP MANANGEMENT");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Customer", new CustomerGUI());
        tabbedPane.addTab("Product", new ProductGUI());
        tabbedPane.addTab("Order", new OrderGUI());
        tabbedPane.addTab("Report", new ReportGUI());
        tabbedPane.addTab("Inventory", new InventoryGUI());

        frame.add(tabbedPane);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(ShopApp::new);
    }
}

