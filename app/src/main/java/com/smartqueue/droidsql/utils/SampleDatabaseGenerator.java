package com.smartqueue.droidsql.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates SQL commands to create a sample E-commerce database.
 * Includes explicit schemas, constraints, and sample data.
 */
public class SampleDatabaseGenerator {

    public static List<String> getEcommerceSQL() {
        List<String> sql = new ArrayList<>();

        // 1. Clean up
        sql.add("DROP TABLE IF EXISTS order_items;");
        sql.add("DROP TABLE IF EXISTS orders;");
        sql.add("DROP TABLE IF EXISTS products;");
        sql.add("DROP TABLE IF EXISTS categories;");
        sql.add("DROP TABLE IF EXISTS users;");

        // 2. Create Tables
        
        // Users Table
        sql.add("CREATE TABLE users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "email TEXT NOT NULL, " +
                "join_date DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");");

        // Categories Table
        sql.add("CREATE TABLE categories (" +
                "category_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "description TEXT" +
                ");");

        // Products Table (FK -> Categories)
        sql.add("CREATE TABLE products (" +
                "product_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category_id INTEGER, " +
                "name TEXT NOT NULL, " +
                "price DECIMAL(10,2) NOT NULL, " +
                "stock_quantity INTEGER DEFAULT 0, " +
                "FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE SET NULL" +
                ");");

        // Orders Table (FK -> Users)
        sql.add("CREATE TABLE orders (" +
                "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "order_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "status TEXT CHECK(status IN ('Pending', 'Shipped', 'Delivered', 'Cancelled')) DEFAULT 'Pending', " +
                "total_amount DECIMAL(10,2), " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ");");

        // Order Items Table (FK -> Orders, Products)
        sql.add("CREATE TABLE order_items (" +
                "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_id INTEGER, " +
                "product_id INTEGER, " +
                "quantity INTEGER NOT NULL, " +
                "unit_price DECIMAL(10,2) NOT NULL, " +
                "FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (product_id) REFERENCES products(product_id)" +
                ");");

        // 3. Insert Data
        
        // Users
        sql.add("INSERT INTO users (username, email) VALUES ('john_doe', 'john@example.com');");
        sql.add("INSERT INTO users (username, email) VALUES ('jane_smith', 'jane@test.org');");
        sql.add("INSERT INTO users (username, email) VALUES ('alice_wonder', 'alice@wonderland.net');");
        sql.add("INSERT INTO users (username, email) VALUES ('bob_builder', 'bob@construction.com');");
        sql.add("INSERT INTO users (username, email) VALUES ('charlie_brown', 'charlie@peanuts.io');");

        // Categories
        sql.add("INSERT INTO categories (name, description) VALUES ('Electronics', 'Gadgets and devices');");
        sql.add("INSERT INTO categories (name, description) VALUES ('Books', 'Paperback and Hardcover books');");
        sql.add("INSERT INTO categories (name, description) VALUES ('Clothing', 'Men and Women fashion');");
        sql.add("INSERT INTO categories (name, description) VALUES ('Home', 'Furniture and Decor');");

        // Products
        // Electronics
        sql.add("INSERT INTO products (category_id, name, price, stock_quantity) VALUES (1, 'Smartphone X', 699.99, 50);");
        sql.add("INSERT INTO products (category_id, name, price, stock_quantity) VALUES (1, 'Laptop Pro', 1299.50, 30);");
        sql.add("INSERT INTO products (category_id, name, price, stock_quantity) VALUES (1, 'Wireless Earbuds', 149.00, 100);");
        // Books
        sql.add("INSERT INTO products (category_id, name, price, stock_quantity) VALUES (2, 'The Great Algorithm', 45.00, 20);");
        sql.add("INSERT INTO products (category_id, name, price, stock_quantity) VALUES (2, 'Database Design 101', 39.99, 15);");
        // Clothing
        sql.add("INSERT INTO products (category_id, name, price, stock_quantity) VALUES (3, 'Cotton T-Shirt', 19.99, 200);");
        sql.add("INSERT INTO products (category_id, name, price, stock_quantity) VALUES (3, 'Denim Jeans', 59.95, 80);");
        // Home
        sql.add("INSERT INTO products (category_id, name, price, stock_quantity) VALUES (4, 'Coffee Table', 120.00, 10);");

        // Orders
        sql.add("INSERT INTO orders (user_id, status, total_amount) VALUES (1, 'Delivered', 1449.49);"); // John bought Laptop + Earbuds
        sql.add("INSERT INTO orders (user_id, status, total_amount) VALUES (2, 'Pending', 59.95);"); // Jane bought Jeans
        sql.add("INSERT INTO orders (user_id, status, total_amount) VALUES (3, 'Shipped', 84.99);"); // Alice bought books

        // Order Items
        // Order 1 (John)
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (1, 2, 1, 1299.50);"); // Laptop
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (1, 3, 1, 149.99);"); // Earbuds
        // Order 2 (Jane)
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (2, 7, 1, 59.95);"); // Jeans
        // Order 3 (Alice)
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (3, 4, 1, 45.00);"); // Book 1
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (3, 5, 1, 39.99);"); // Book 2

        return sql;
    }
}
