package com.example.shop_manager.Entity;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Inventory {
	private int id;
	private String name;
	private String category;
	private double price;
	private int quantity;

	// Constructor
	public Inventory(int id, String name, String category, double price, int quantity) {
		this.id = id;
		this.name = name;
		this.category = category;
		this.price = price;
		this.quantity = quantity;
	}

	public Inventory() {
	}

	// Getter and Setter methods
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	// Utility method for creating Inventory from ResultSet
	public static Inventory fromResultSet(ResultSet resultSet) throws SQLException {
		return new Inventory(
				resultSet.getInt("id"),
				resultSet.getString("name"),
				resultSet.getString("category"),
				resultSet.getDouble("price"),
				resultSet.getInt("quantity")
		);
	}
}
