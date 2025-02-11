package com.example.shop_manager.Interface;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public interface IReport {
	void printAllInvoices();
	void searchInvoicesByCustomerID();
	void updateTable(List<Object[]> invoices);
}
