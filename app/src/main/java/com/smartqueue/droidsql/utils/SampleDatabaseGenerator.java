package com.smartqueue.droidsql.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates SQL commands to create a comprehensive Sakila-style E-commerce database.
 * Includes explicit schemas, constraints, and 160+ sample data records.
 */
public class SampleDatabaseGenerator {

    public static List<String> getEcommerceSQL() {
        List<String> sql = new ArrayList<>();

        // 1. Clean up
        sql.add("DROP TABLE IF EXISTS reviews;");
        sql.add("DROP TABLE IF EXISTS payments;");
        sql.add("DROP TABLE IF EXISTS order_items;");
        sql.add("DROP TABLE IF EXISTS inventory;");
        sql.add("DROP TABLE IF EXISTS orders;");
        sql.add("DROP TABLE IF EXISTS products;");
        sql.add("DROP TABLE IF EXISTS categories;");
        sql.add("DROP TABLE IF EXISTS user_addresses;");
        sql.add("DROP TABLE IF EXISTS users;");

        // 2. Create Tables
        
        // Users Table
        sql.add("CREATE TABLE users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "first_name TEXT NOT NULL, " +
                "last_name TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, " +
                "phone TEXT, " +
                "password_hash TEXT, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "status TEXT CHECK(status IN ('Active', 'Suspended', 'Deleted')) DEFAULT 'Active'" +
                ");");

        // Addresses Table
        sql.add("CREATE TABLE user_addresses (" +
                "address_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "address_line1 TEXT NOT NULL, " +
                "city TEXT NOT NULL, " +
                "state TEXT, " +
                "postal_code TEXT, " +
                "country TEXT DEFAULT 'USA', " +
                "is_default BOOLEAN DEFAULT 0, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                ");");

        // Categories Table
        sql.add("CREATE TABLE categories (" +
                "category_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE, " +
                "description TEXT, " +
                "parent_id INTEGER NULL, " +
                "FOREIGN KEY (parent_id) REFERENCES categories(category_id)" +
                ");");

        // Products Table
        sql.add("CREATE TABLE products (" +
                "product_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category_id INTEGER, " +
                "sku TEXT UNIQUE, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "base_price DECIMAL(12,2) NOT NULL, " +
                "discount_price DECIMAL(12,2), " +
                "weight_kg DECIMAL(6,2), " +
                "is_active BOOLEAN DEFAULT 1, " +
                "FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE SET NULL" +
                ");");

        // Inventory Table
        sql.add("CREATE TABLE inventory (" +
                "inventory_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "product_id INTEGER, " +
                "stock_quantity INTEGER DEFAULT 0, " +
                "warehouse_location TEXT, " +
                "last_restock_date DATETIME, " +
                "FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE" +
                ");");

        // Orders Table
        sql.add("CREATE TABLE orders (" +
                "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "order_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "status TEXT CHECK(status IN ('Pending', 'Processing', 'Shipped', 'Delivered', 'Cancelled', 'Returned')) DEFAULT 'Pending', " +
                "shipping_address_id INTEGER, " +
                "total_tax DECIMAL(12,2) DEFAULT 0.00, " +
                "total_shipping DECIMAL(12,2) DEFAULT 0.00, " +
                "total_amount DECIMAL(12,2) NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id), " +
                "FOREIGN KEY (shipping_address_id) REFERENCES user_addresses(address_id)" +
                ");");

        // Order Items Table
        sql.add("CREATE TABLE order_items (" +
                "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_id INTEGER, " +
                "product_id INTEGER, " +
                "quantity INTEGER NOT NULL CHECK(quantity > 0), " +
                "unit_price DECIMAL(12,2) NOT NULL, " +
                "line_total DECIMAL(12,2) NOT NULL, " +
                "FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (product_id) REFERENCES products(product_id)" +
                ");");

        // Reviews Table
        sql.add("CREATE TABLE reviews (" +
                "review_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "product_id INTEGER, " +
                "user_id INTEGER, " +
                "rating INTEGER CHECK(rating BETWEEN 1 AND 5), " +
                "comment TEXT, " +
                "review_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL" +
                ");");

        // Payments Table
        sql.add("CREATE TABLE payments (" +
                "payment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_id INTEGER, " +
                "payment_method TEXT CHECK(payment_method IN ('Credit Card', 'PayPal', 'Bank Transfer', 'Crypto')), " +
                "transaction_id TEXT UNIQUE, " +
                "amount DECIMAL(12,2) NOT NULL, " +
                "payment_status TEXT DEFAULT 'Completed', " +
                "payment_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE" +
                ");");

        // 3. Insert Data
        
        // Categories
        sql.add("INSERT INTO categories (name, description) VALUES ('Electronics', 'Gadgets, Computers, and Mobile Devices');");
        sql.add("INSERT INTO categories (name, description) VALUES ('Home & Kitchen', 'Appliances and Home Decor');");
        sql.add("INSERT INTO categories (name, description) VALUES ('Clothing', 'Apparel and Fashion Accessories');");
        sql.add("INSERT INTO categories (name, description) VALUES ('Books', 'Physical and Digital books');");
        sql.add("INSERT INTO categories (name, description) VALUES ('Health & Beauty', 'Personal care and beauty products');");
        sql.add("INSERT INTO categories (name, description) VALUES ('Sports', 'Outdoor and Indoor sports equipment');");
        sql.add("INSERT INTO categories (name, description) VALUES ('Toys', 'Games and children toys');");

        // Users (20)
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('John', 'Doe', 'john@example.com', '555-0101');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('Jane', 'Smith', 'jane@test.org', '555-0102');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('Alice', 'Johnson', 'alice@gmail.com', '555-0103');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('Bob', 'Williams', 'bob@yahoo.com', '555-0104');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('Charlie', 'Brown', 'charlie@outlook.com', '555-0105');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('David', 'Miller', 'david@mail.com', '555-0106');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('Eve', 'Davis', 'eve@provider.com', '555-0107');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('Frank', 'Wilson', 'frank@web.com', '555-0108');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('Grace', 'Moore', 'grace@service.com', '555-0109');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('Heidi', 'Taylor', 'heidi@site.com', '555-0110');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('Ivan', 'Anderson', 'ivan@host.com', '555-0111');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('Judy', 'Thomas', 'judy@domain.com', '555-0112');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('Kevin', 'Jackson', 'kevin@email.com', '555-0113');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('Laura', 'White', 'laura@net.com', '555-0114');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('Mike', 'Harris', 'mike@cloud.com', '555-0115');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('Nina', 'Martin', 'nina@logic.com', '555-0116');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('Oscar', 'Thompson', 'oscar@alpha.com', '555-0117');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('Paul', 'Garcia', 'paul@beta.com', '555-0118');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('Quinn', 'Martinez', 'quinn@gamma.com', '555-0119');");
        sql.add("INSERT INTO users (first_name, last_name, email, phone) VALUES ('Rose', 'Robinson', 'rose@delta.com', '555-0120');");

        // Addresses (20)
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (1, '123 Maple St', 'Springfield', 'IL', '62704');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (2, '456 Oak Ave', 'New York', 'NY', '10001');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (3, '789 Pine Rd', 'Los Angeles', 'CA', '90001');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (4, '101 Cedar Ln', 'Austin', 'TX', '73301');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (5, '202 Birch Blvd', 'Seattle', 'WA', '98101');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (6, '303 Elm Dr', 'Miami', 'FL', '33101');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (7, '404 Walnut Ct', 'Chicago', 'IL', '60601');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (8, '505 Cherry Way', 'Denver', 'CO', '80201');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (9, '606 Ash St', 'Phoenix', 'AZ', '85001');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (10, '707 Poplar Ave', 'Boston', 'MA', '02101');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (11, '808 Spruce St', 'Portland', 'OR', '97201');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (12, '909 Willow Rd', 'San Jose', 'CA', '95101');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (13, '111 Aspen Ln', 'Detroit', 'MI', '48201');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (14, '222 Sycamore Dr', 'Nashville', 'TN', '37201');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (15, '333 Magnolia Ct', 'Atlanta', 'GA', '30301');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (16, '444 Hickory Way', 'Dallas', 'TX', '75201');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (17, '555 Redwood St', 'Houston', 'TX', '77001');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (18, '666 Juniper Ave', 'Charlotte', 'NC', '28201');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (19, '777 Cypress Rd', 'San Diego', 'CA', '92101');");
        sql.add("INSERT INTO user_addresses (user_id, address_line1, city, state, postal_code) VALUES (20, '888 Fir Blvd', 'Las Vegas', 'NV', '89101');");

        // Products (20)
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (1, 'ELEC-001', 'UltraPhone 15', 999.99, 0.2);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (1, 'ELEC-002', 'Laptop Air M2', 1199.50, 1.3);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (1, 'ELEC-003', 'Noise-Cancelling Headphones', 299.00, 0.3);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (1, 'ELEC-004', 'Smart Watch Gen 5', 199.99, 0.05);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (2, 'HOME-001', 'Espresso Machine', 450.00, 4.5);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (2, 'HOME-002', 'Air Fryer XL', 129.99, 5.0);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (2, 'HOME-003', 'Robot Vacuum', 249.00, 3.2);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (3, 'CLOT-001', 'Cotton Polo Shirt', 35.00, 0.2);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (3, 'CLOT-002', 'Slim Fit Jeans', 65.00, 0.5);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (3, 'CLOT-003', 'Winter Parka', 150.00, 1.5);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (4, 'BOOK-001', 'Mastering SQL', 49.99, 0.8);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (4, 'BOOK-002', 'Data Science from Scratch', 39.50, 0.7);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (4, 'BOOK-003', 'Introduction to Algorithms', 85.00, 1.2);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (5, 'HEAL-001', 'Electric Toothbrush', 89.00, 0.3);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (5, 'HEAL-002', 'Skincare Serum Bundle', 45.00, 0.1);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (6, 'SPOR-001', 'Yoga Mat Premium', 30.00, 1.0);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (6, 'SPOR-002', 'Adjustable Dumbbells', 180.00, 24.0);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (7, 'TOYS-001', 'Building Blocks Set', 55.00, 2.0);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (7, 'TOYS-002', 'Remote Control Car', 45.00, 0.9);");
        sql.add("INSERT INTO products (category_id, sku, name, base_price, weight_kg) VALUES (7, 'TOYS-003', 'Chess Set Wooden', 35.00, 1.1);");

        // Inventory (Generated based on product count)
        sql.add("INSERT INTO inventory (product_id, stock_quantity, warehouse_location) " +
                "SELECT product_id, (product_id * 10), 'Section-' || product_id FROM products;");

        // Orders (20)
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (1, 'Delivered', 1298.99, 1);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (2, 'Shipped', 65.00, 2);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (3, 'Delivered', 450.00, 3);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (4, 'Pending', 199.99, 4);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (5, 'Processing', 85.00, 5);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (6, 'Delivered', 35.00, 6);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (7, 'Shipped', 1199.50, 7);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (8, 'Delivered', 249.00, 8);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (9, 'Cancelled', 0.00, 9);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (10, 'Delivered', 150.00, 10);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (11, 'Delivered', 134.00, 11);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (12, 'Pending', 299.00, 12);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (13, 'Processing', 49.99, 13);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (14, 'Delivered', 180.00, 14);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (15, 'Shipped', 100.00, 15);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (16, 'Delivered', 45.00, 16);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (17, 'Delivered', 89.00, 17);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (18, 'Pending', 30.00, 18);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (19, 'Delivered', 999.99, 19);");
        sql.add("INSERT INTO orders (user_id, status, total_amount, shipping_address_id) VALUES (20, 'Shipped', 299.00, 20);");

        // Order Items (20)
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (1, 1, 1, 999.99, 999.99);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (1, 3, 1, 299.00, 299.00);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (2, 9, 1, 65.00, 65.00);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (3, 5, 1, 450.00, 450.00);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (4, 4, 1, 199.99, 199.99);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (5, 13, 1, 85.00, 85.00);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (6, 8, 1, 35.00, 35.00);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (7, 2, 1, 1199.50, 1199.50);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (8, 7, 1, 249.00, 249.00);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (10, 10, 1, 150.00, 150.00);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (11, 14, 1, 89.00, 89.00);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (11, 15, 1, 45.00, 45.00);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (12, 3, 1, 299.00, 299.00);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (13, 11, 1, 49.99, 49.99);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (14, 17, 1, 180.00, 180.00);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (15, 18, 2, 50.00, 100.00);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (16, 19, 1, 45.00, 45.00);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (17, 14, 1, 89.00, 89.00);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (18, 16, 1, 30.00, 30.00);");
        sql.add("INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (19, 1, 1, 999.99, 999.99);");

        // Reviews (20)
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (1, 1, 5, 'Best phone I have ever owned!');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (2, 7, 4, 'Very fast laptop, but pricey.');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (3, 1, 5, 'Silent as a tomb. Love them.');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (5, 3, 5, 'Perfect crema every time.');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (7, 8, 3, 'Misses a few spots in corners.');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (10, 10, 5, 'Keeps me very warm in blizzard.');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (13, 5, 5, 'A must-read for computer scientists.');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (17, 14, 4, 'Solid build, great grip.');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (1, 2, 5, 'Expensive but worth every penny.');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (8, 6, 4, 'Good quality cotton.');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (4, 4, 3, 'Battery life could be better.');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (11, 13, 5, 'Great technical explanations.');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (14, 17, 4, 'Cleans much better than manual.');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (19, 16, 5, 'Kid loves it! Very durable.');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (2, 20, 2, 'Screen has a dead pixel.');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (6, 2, 5, 'Healthy fries, what more do you want?');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (9, 3, 4, 'Fits true to size.');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (15, 11, 5, 'My skin feels much smoother.');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (12, 12, 4, 'Excellent entry level book.');");
        sql.add("INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (18, 15, 5, 'Great for yoga at home.');");

        // Payments (20)
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (1, 'Credit Card', 'TXN-001', 1298.99);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (2, 'PayPal', 'TXN-002', 65.00);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (3, 'Credit Card', 'TXN-003', 450.00);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (4, 'Bank Transfer', 'TXN-004', 199.99);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (5, 'PayPal', 'TXN-005', 85.00);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (6, 'Credit Card', 'TXN-006', 35.00);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (7, 'Credit Card', 'TXN-007', 1199.50);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (8, 'Crypto', 'TXN-008', 249.00);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (10, 'PayPal', 'TXN-009', 150.00);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (11, 'Credit Card', 'TXN-010', 134.00);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (12, 'Credit Card', 'TXN-011', 299.00);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (13, 'PayPal', 'TXN-012', 49.99);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (14, 'Bank Transfer', 'TXN-013', 180.00);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (15, 'Credit Card', 'TXN-014', 100.00);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (16, 'PayPal', 'TXN-015', 45.00);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (17, 'Credit Card', 'TXN-016', 89.00);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (18, 'Crypto', 'TXN-017', 30.00);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (19, 'Credit Card', 'TXN-018', 999.99);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (20, 'PayPal', 'TXN-019', 299.00);");
        sql.add("INSERT INTO payments (order_id, payment_method, transaction_id, amount) VALUES (1, 'Credit Card', 'TXN-020', 10.00);");

        return sql;
    }

    public static List<String> getWorldSQL() {
        List<String> sql = new ArrayList<>();

        sql.add("DROP TABLE IF EXISTS country_languages;");
        sql.add("DROP TABLE IF EXISTS cities;");
        sql.add("DROP TABLE IF EXISTS countries;");

        // Countries Table (Aligned with MySQL World Schema)
        sql.add("CREATE TABLE countries (" +
                "code TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "continent TEXT NOT NULL DEFAULT 'Asia' CHECK(continent IN ('Asia', 'Europe', 'North America', 'Africa', 'Oceania', 'Antarctica', 'South America')), " +
                "region TEXT NOT NULL DEFAULT '', " +
                "surface_area REAL NOT NULL DEFAULT 0.00, " +
                "indep_year INTEGER, " +
                "population INTEGER NOT NULL DEFAULT 0, " +
                "life_expectancy REAL, " +
                "gnp REAL, " +
                "gnp_old REAL, " +
                "local_name TEXT NOT NULL DEFAULT '', " +
                "government_form TEXT NOT NULL DEFAULT '', " +
                "head_of_state TEXT, " +
                "capital_id INTEGER, " +
                "code2 TEXT NOT NULL DEFAULT ''" +
                ");");

        // Cities Table
        sql.add("CREATE TABLE cities (" +
                "city_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "country_code TEXT, " +
                "district TEXT, " +
                "population INTEGER, " +
                "FOREIGN KEY (country_code) REFERENCES countries(code) ON DELETE CASCADE" +
                ");");

        // Country Languages Table
        sql.add("CREATE TABLE country_languages (" +
                "country_code TEXT, " +
                "language TEXT NOT NULL, " +
                "is_official BOOLEAN DEFAULT 0, " +
                "percentage REAL, " +
                "PRIMARY KEY (country_code, language), " +
                "FOREIGN KEY (country_code) REFERENCES countries(code) ON DELETE CASCADE" +
                ");");

        // 3. Insert Data - Comprehensive List (Expanding towards the 239 count)
        sql.add("INSERT INTO countries VALUES ('ABW','Aruba','North America','Caribbean',193.00,NULL,103000,78.4,828.00,793.00,'Aruba','Nonmetropolitan Territory of The Netherlands','Beatrix',129,'AW');");
        sql.add("INSERT INTO countries VALUES ('AFG','Afghanistan','Asia','Southern and Central Asia',652090.00,1919,22720000,45.9,5976.00,NULL,'Afganistan/Afqanestan','Islamic Emirate','Mohammad Omar',1,'AF');");
        sql.add("INSERT INTO countries VALUES ('AGO','Angola','Africa','Central Africa',1246700.00,1975,12878000,38.3,6648.00,7984.00,'Angola','Republic','José Eduardo dos Santos',56,'AO');");
        sql.add("INSERT INTO countries VALUES ('AIA','Anguilla','North America','Caribbean',96.00,NULL,8000,76.1,63.20,NULL,'Anguilla','Dependent Territory','Elisabeth II',62,'AI');");
        sql.add("INSERT INTO countries VALUES ('ALB','Albania','Europe','Southern Europe',28748.00,1912,3401200,71.6,3205.00,2500.00,'Shqipëria','Republic','Rexhep Mejdani',34,'AL');");
        sql.add("INSERT INTO countries VALUES ('AND','Andorra','Europe','Southern Europe',468.00,1278,78000,83.5,1630.00,NULL,'Andorra','Parliamentary Principality','',55,'AD');");
        sql.add("INSERT INTO countries VALUES ('ANT','Netherlands Antilles','North America','Caribbean',800.00,NULL,217000,74.7,1941.00,NULL,'Nederlandse Antillen','Nonmetropolitan Territory of The Netherlands','Beatrix',33,'AN');");
        sql.add("INSERT INTO countries VALUES ('ARE','United Arab Emirates','Asia','Middle East',83600.00,1971,2441000,74.1,37966.00,36846.00,'Al-Imarat al-’Arabiya al-Muttahida','Emirate','Zayid bin Sultan al-Nahayan',65,'AE');");
        sql.add("INSERT INTO countries VALUES ('ARG','Argentina','South America','South America',2780400.00,1816,37032000,75.1,340238.00,323310.00,'Argentina','Federal Republic','Fernando de la Rúa',69,'AR');");
        sql.add("INSERT INTO countries VALUES ('ARM','Armenia','Asia','Middle East',29800.00,1991,3520000,66.4,1813.00,1627.00,'Hajastan','Republic','Robert Kotšarjan',126,'AM');");
        sql.add("INSERT INTO countries VALUES ('ASM','American Samoa','Oceania','Polynesia',199.00,NULL,68000,75.1,334.00,NULL,'Amerikas Samoa','US Territory','George W. Bush',54,'AS');");
        sql.add("INSERT INTO countries VALUES ('ATA','Antarctica','Antarctica','Antarctica',13120000.00,NULL,0,NULL,0.00,NULL,'','Co-administration','',NULL,'AQ');");
        sql.add("INSERT INTO countries VALUES ('ATG','Antigua and Barbuda','North America','Caribbean',442.00,1981,68000,70.5,612.00,584.00,'Antigua and Barbuda','Constitutional Monarchy','Elisabeth II',63,'AG');");
        sql.add("INSERT INTO countries VALUES ('AUS','Australia','Oceania','Australia and New Zealand',7741220.00,1901,18886000,79.8,351182.00,392911.00,'Australia','Constitutional Monarchy','Elisabeth II',135,'AU');");
        sql.add("INSERT INTO countries VALUES ('AUT','Austria','Europe','Western Europe',83859.00,1918,8096000,77.7,211860.00,206025.00,'Österreich','Federal Republic','Thomas Klestil',152,'AT');");
        sql.add("INSERT INTO countries VALUES ('AZE','Azerbaijan','Asia','Middle East',86600.00,1991,7734000,62.9,4127.00,4100.00,'Azärbaycan','Federal Republic','Heydär Äliyev',144,'AZ');");
        sql.add("INSERT INTO countries VALUES ('BDI','Burundi','Africa','Eastern Africa',27834.00,1962,6695000,46.2,903.00,982.00,'Burundi/Uburundi','Republic','Pierre Buyoya',552,'BI');");
        sql.add("INSERT INTO countries VALUES ('BEL','Belgium','Europe','Western Europe',30518.00,1830,10239000,77.8,249704.00,243948.00,'België/Belgique','Constitutional Monarchy','Albert II',179,'BE');");
        sql.add("INSERT INTO countries VALUES ('BEN','Benin','Africa','Western Africa',112622.00,1960,6097000,50.2,2357.00,2141.00,'Bénin','Republic','Mathieu Kérékou',187,'BJ');");
        sql.add("INSERT INTO countries VALUES ('BFA','Burkina Faso','Africa','Western Africa',274000.00,1960,11937000,46.7,2425.00,2201.00,'Burkina Faso','Republic','Blaise Compaoré',549,'BF');");
        sql.add("INSERT INTO countries VALUES ('BGD','Bangladesh','Asia','Southern and Central Asia',143998.00,1971,129155000,60.2,32852.00,31966.00,'Bangladesh','Republic','Shahabuddin Ahmed',150,'BD');");
        sql.add("INSERT INTO countries VALUES ('BGR','Bulgaria','Europe','Eastern Europe',110912.00,1908,8190900,70.9,12178.00,10169.00,'Bălgarija','Republic','Petăr Stojanov',539,'BG');");
        sql.add("INSERT INTO countries VALUES ('BHR','Bahrain','Asia','Middle East',694.00,1971,617000,73.0,6366.00,6097.00,'Al-Bahrayn','Monarchy (Emirate)','Hamad ibn Isa al-Khalifa',149,'BH');");
        sql.add("INSERT INTO countries VALUES ('BHS','Bahamas','North America','Caribbean',13878.00,1973,307000,71.1,3527.00,3347.00,'The Bahamas','Constitutional Monarchy','Elisabeth II',148,'BS');");
        sql.add("INSERT INTO countries VALUES ('BIH','Bosnia and Herzegovina','Europe','Southern Europe',51197.00,1992,3972000,71.5,2841.00,NULL,'Bosna i Hercegovina','Federal Republic','Ante Jelavic',201,'BA');");
        sql.add("INSERT INTO countries VALUES ('BLR','Belarus','Europe','Eastern Europe',207600.00,1991,10236000,68.0,13714.00,NULL,'Belarus','Republic','Aljaksandr Lukašenka',3520,'BY');");
        sql.add("INSERT INTO countries VALUES ('BLZ','Belize','North America','Central America',22966.00,1981,241000,70.9,630.00,616.00,'Belize','Constitutional Monarchy','Elisabeth II',185,'BZ');");
        sql.add("INSERT INTO countries VALUES ('BMU','Bermuda','North America','North America',53.00,NULL,65000,76.9,2328.00,2190.00,'Bermuda','Dependent Territory','Elisabeth II',191,'BM');");
        sql.add("INSERT INTO countries VALUES ('BOL','Bolivia','South America','South America',1098581.00,1825,8329000,63.7,8571.00,7967.00,'Bolivia','Republic','Hugo Banzer Suárez',194,'BO');");
        sql.add("INSERT INTO countries VALUES ('BRA','Brazil','South America','South America',8547403.00,1822,170115000,62.9,776739.00,804108.00,'Brasil','Federal Republic','Fernando Henrique Cardoso',211,'BR');");
        sql.add("INSERT INTO countries VALUES ('BRB','Barbados','North America','Caribbean',430.00,1966,270000,73.0,2223.00,2186.00,'Barbados','Constitutional Monarchy','Elisabeth II',174,'BB');");
        sql.add("INSERT INTO countries VALUES ('BRN','Brunei','Asia','Southeast Asia',5765.00,1984,328000,73.6,11705.00,12460.00,'Brunei Darussalam','Monarchy (Sultanate)','Haji Hassan al-Bolkiah',538,'BN');");
        sql.add("INSERT INTO countries VALUES ('BTN','Bhutan','Asia','Southern and Central Asia',47000.00,1910,2124000,52.4,372.00,383.00,'Druk-Yul','Monarchy','Jigme Singye Wangchuk',192,'BT');");
        sql.add("INSERT INTO countries VALUES ('BWA','Botswana','Africa','Southern Africa',581730.00,1966,1622000,39.3,4834.00,4935.00,'Botswana','Republic','Festus G. Mogae',204,'BW');");
        sql.add("INSERT INTO countries VALUES ('CAF','Central African Republic','Africa','Central Africa',622984.00,1960,3615000,44.0,1054.00,993.00,'Centrafrique','Republic','Ange-Félix Patassé',597,'CF');");
        sql.add("INSERT INTO countries VALUES ('CAN','Canada','North America','North America',9970610.00,1867,31147000,79.4,598862.00,602869.00,'Canada','Constitutional Monarchy','Elisabeth II',1822,'CA');");
        sql.add("INSERT INTO countries VALUES ('CHE','Switzerland','Europe','Western Europe',41284.00,1291,7160400,79.6,264478.00,256092.00,'Schweiz/Suisse/Svizzera/Svizra','Federation','Adolf Ogi',3248,'CH');");
        sql.add("INSERT INTO countries VALUES ('CHL','Chile','South America','South America',756626.00,1810,15211000,75.7,75649.00,77473.00,'Chile','Republic','Ricardo Lagos Escobar',570,'CL');");
        sql.add("INSERT INTO countries VALUES ('CHN','China','Asia','Eastern Asia',9572900.00,-1523,1277558000,71.4,982268.00,917719.00,'Zhongguo','People''s Republic','Jiang Zemin',1891,'CN');");
        sql.add("INSERT INTO countries VALUES ('CIV','Côte d’Ivoire','Africa','Western Africa',322463.00,1960,14786000,45.2,11345.00,10285.00,'Côte d’Ivoire','Republic','Laurent Gbagbo',2814,'CI');");
        sql.add("INSERT INTO countries VALUES ('CMR','Cameroon','Africa','Central Africa',475442.00,1960,15085000,54.8,9174.00,8596.00,'Cameroun/Cameroon','Republic','Paul Biya',1804,'CM');");
        sql.add("INSERT INTO countries VALUES ('COD','Congo, The Democratic Republic of the','Africa','Central Africa',2344858.00,1960,51654000,48.8,6964.00,2474.00,'République Démocratique du Congo','Republic','Joseph Kabila',2298,'CD');");
        sql.add("INSERT INTO countries VALUES ('COG','Congo','Africa','Central Africa',342000.00,1960,2943000,47.4,2108.00,2287.00,'Congo','Republic','Denis Sassou-Nguesso',2296,'CG');");
        sql.add("INSERT INTO countries VALUES ('COK','Cook Islands','Oceania','Polynesia',236.00,NULL,20000,71.1,100.00,NULL,'The Cook Islands','Nonmetropolitan Territory of New Zealand','Elisabeth II',583,'CK');");
        sql.add("INSERT INTO countries VALUES ('COL','Colombia','South America','South America',1138914.00,1810,42321000,70.3,102896.00,105116.00,'Colombia','Republic','Andrés Pastrana Arango',2257,'CO');");
        sql.add("INSERT INTO countries VALUES ('COM','Comoros','Africa','Eastern Africa',1862.00,1975,578000,60.0,440.00,439.00,'Komori/Comores','Republic','Azali Assoumani',2295,'KM');");
        sql.add("INSERT INTO countries VALUES ('CPV','Cape Verde','Africa','Western Africa',4033.00,1975,428000,68.9,441.00,406.00,'Cabo Verde','Republic','António Mascarenhas Monteiro',1859,'CV');");
        sql.add("INSERT INTO countries VALUES ('CRI','Costa Rica','North America','Central America',51100.00,1821,4023000,75.8,10226.00,9757.00,'Costa Rica','Republic','Miguel Ángel Rodríguez Echeverría',584,'CR');");
        sql.add("INSERT INTO countries VALUES ('CUB','Cuba','North America','Caribbean',110861.00,1902,11201000,76.2,17843.00,18862.00,'Cuba','Socialist Republic','Fidel Castro Ruz',585,'CU');");
        sql.add("INSERT INTO countries VALUES ('CYP','Cyprus','Asia','Middle East',9251.00,1960,754700,76.7,9333.00,8246.00,'Kýpros/Kıbrıs','Republic','Glafkos Klerides',2430,'CY');");
        sql.add("INSERT INTO countries VALUES ('CZE','Czech Republic','Europe','Eastern Europe',78866.00,1993,10278100,74.5,55017.00,52037.00,'Česko','Republic','Václav Havel',3339,'CZ');");
        sql.add("INSERT INTO countries VALUES ('DEU','Germany','Europe','Western Europe',357022.00,1955,82164700,77.4,2133367.00,2102826.00,'Deutschland','Federal Republic','Johannes Rau',3068,'DE');");
        sql.add("INSERT INTO countries VALUES ('DJI','Djibouti','Africa','Eastern Africa',23200.00,1977,638000,50.8,527.00,525.00,'Djibouti/Jibuti','Republic','Ismail Omar Guelleh',585,'DJ');");
        sql.add("INSERT INTO countries VALUES ('DMA','Dominica','North America','Caribbean',751.00,1978,71000,73.4,256.00,243.00,'Dominica','Republic','Vernon Shaw',586,'DM');");
        sql.add("INSERT INTO countries VALUES ('DNK','Denmark','Europe','Nordic Countries',43094.00,800,5330000,76.5,174099.00,169264.00,'Danmark','Constitutional Monarchy','Margrethe II',3315,'DK');");
        sql.add("INSERT INTO countries VALUES ('DOM','Dominican Republic','North America','Caribbean',48511.00,1844,8495000,73.2,15846.00,15076.00,'República Dominicana','Republic','Hipólito Mejía Domínguez',587,'DO');");
        sql.add("INSERT INTO countries VALUES ('DZA','Algeria','Africa','Northern Africa',2381741.00,1962,31471000,69.7,49982.00,46966.00,'Al-Jaza’ir/Algérie','Republic','Abdelaziz Bouteflika',35,'DZ');");
        sql.add("INSERT INTO countries VALUES ('ECU','Ecuador','South America','South America',283561.00,1822,12646000,71.1,19770.00,19769.00,'Ecuador','Republic','Gustavo Noboa Bejarano',594,'EC');");
        sql.add("INSERT INTO countries VALUES ('EGY','Egypt','Africa','Northern Africa',1001449.00,1922,68470000,63.3,82710.00,75617.00,'Misr','Republic','Hosni Mubarak',608,'EG');");
        sql.add("INSERT INTO countries VALUES ('ERI','Eritrea','Africa','Eastern Africa',117600.00,1993,3850000,55.8,650.00,755.00,'Ertra','Republic','Isayas Afewerki [Isaias Afwerki]',652,'ER');");
        sql.add("INSERT INTO countries VALUES ('ESH','Western Sahara','Africa','Northern Africa',266000.00,NULL,293000,NULL,60.00,NULL,'As-Sahrawiya','Occupied by Marocco','Mohamed Abdelaziz',NULL,'EH');");
        sql.add("INSERT INTO countries VALUES ('ESP','Spain','Europe','Southern Europe',505992.00,1492,39433900,78.8,553233.00,532031.00,'España','Constitutional Monarchy','Juan Carlos I',653,'ES');");
        sql.add("INSERT INTO countries VALUES ('EST','Estonia','Europe','Nordic Countries',45227.00,1991,1439200,69.5,5328.00,3371.00,'Eesti','Republic','Lennart Meri',3791,'EE');");
        sql.add("INSERT INTO countries VALUES ('ETH','Ethiopia','Africa','Eastern Africa',1104300.00,1000,62565000,45.2,6353.00,6180.00,'YeItyop''ya','Republic','Negasso Gidada',756,'ET');");
        sql.add("INSERT INTO countries VALUES ('FIN','Finland','Europe','Nordic Countries',338145.00,1917,5171300,77.4,121914.00,119833.00,'Suomi','Republic','Tarja Halonen',3236,'FI');");
        sql.add("INSERT INTO countries VALUES ('FJI','Fiji Islands','Oceania','Melanesia',18274.00,1970,817000,67.9,1536.00,2149.00,'Fiji Islands','Republic','Josefa Iloilo',764,'FJ');");
        sql.add("INSERT INTO countries VALUES ('FRA','France','Europe','Western Europe',551500.00,843,59225700,78.8,1424285.00,1392448.00,'France','Republic','Jacques Chirac',2974,'FR');");
        sql.add("INSERT INTO countries VALUES ('GBR','United Kingdom','Europe','British Islands',242900.00,1066,59623400,77.7,1378330.00,1296830.00,'United Kingdom','Constitutional Monarchy','Elisabeth II',456,'GB');");
        sql.add("INSERT INTO countries VALUES ('IND','India','Asia','Southern Asia',3287263.00,1947,1013662000,62.5,447114.00,430572.00,'Bharat/India','Federal Republic','Kocheril Raman Narayanan',1109,'IN');");
        sql.add("INSERT INTO countries VALUES ('ITA','Italy','Europe','Southern Europe',301316.00,1861,57683100,79.0,1161755.00,1145351.00,'Italia','Republic','Carlo Azeglio Ciampi',1464,'IT');");
        sql.add("INSERT INTO countries VALUES ('JPN','Japan','Asia','Eastern Asia',377829.00,-660,126714000,80.7,3787306.00,4150953.00,'Nihon/Nippon','Constitutional Monarchy','Akihito',1532,'JP');");
        sql.add("INSERT INTO countries VALUES ('MEX','Mexico','North America','Central America',1958201.00,1821,98881000,71.5,414972.00,401461.00,'México','Federal Republic','Vicente Fox Quesada',2515,'MX');");
        sql.add("INSERT INTO countries VALUES ('RUS','Russian Federation','Europe','Eastern Europe',17075400.00,1991,146934000,67.2,276608.00,442989.00,'Rossija','Federal Republic','Vladimir Putin',3580,'RU');");
        sql.add("INSERT INTO countries VALUES ('USA','United States','North America','North America',9363520.00,1776,278357000,77.1,8510700.00,8110900.00,'United States','Federal Republic','George W. Bush',3813,'US');");

        addExactCities(sql);
        
        sql.add("INSERT INTO country_languages VALUES ('USA', 'English', 1, 86.2);");
        sql.add("INSERT INTO country_languages VALUES ('GBR', 'English', 1, 95.3);");
        sql.add("INSERT INTO country_languages VALUES ('JPN', 'Japanese', 1, 99.1);");
        sql.add("INSERT INTO country_languages VALUES ('CHN', 'Chinese', 1, 92.0);");
        sql.add("INSERT INTO country_languages VALUES ('IND', 'Hindi', 1, 41.0);");

        return sql;
    }

    private static void addExactCities(List<String> sql) {
        // AFGHANISTAN
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Kabul','AFG','Kabol',1780000);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Qandahar','AFG','Qandahar',339200);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Herat','AFG','Herat',186800);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Mazar-e-Sharif','AFG','Balkh',127800);");

        // NETHERLANDS
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Amsterdam','NLD','Noord-Holland',731200);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Rotterdam','NLD','Zuid-Holland',593321);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Haag','NLD','Zuid-Holland',440900);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Utrecht','NLD','Utrecht',234323);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Eindhoven','NLD','Noord-Brabant',201843);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Tilburg','NLD','Noord-Brabant',193238);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Groningen','NLD','Groningen',173139);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Breda','NLD','Noord-Brabant',160398);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Apeldoorn','NLD','Gelderland',153491);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Nijmegen','NLD','Gelderland',152463);");

        // ARGENTINA
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Buenos Aires','ARG','Distrito Federal',2982146);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('La Matanza','ARG','Buenos Aires',1255288);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Córdoba','ARG','Córdoba',1197926);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Rosario','ARG','Santa Fé',910224);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Lomas de Zamora','ARG','Buenos Aires',622013);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Quilmes','ARG','Buenos Aires',553809);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Almirante Brown','ARG','Buenos Aires',538495);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('La Plata','ARG','Buenos Aires',521936);");

        // AUSTRALIA
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Sydney','AUS','New South Wales',3276203);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Melbourne','AUS','Victoria',2865329);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Brisbane','AUS','Queensland',1291117);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Perth','AUS','Western Australia',1096829);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Adelaide','AUS','South Australia',978100);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Canberra','AUS','Capital Territory',310100);");

        // BRAZIL (Large sample of exact cities)
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('São Paulo','BRA','São Paulo',9968485);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Rio de Janeiro','BRA','Rio de Janeiro',5598953);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Salvador','BRA','Bahia',2302832);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Belo Horizonte','BRA','Minas Gerais',2095448);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Fortaleza','BRA','Ceará',2097757);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Brasília','BRA','Distrito Federal',1969868);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Curitiba','BRA','Paraná',1587315);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Recife','BRA','Pernambuco',1421947);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Porto Alegre','BRA','Rio Grande do Sul',1314032);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Manaus','BRA','Amazonas',1157357);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Belém','BRA','Pará',1181814);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Goiânia','BRA','Goiás',1056330);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Guarulhos','BRA','São Paulo',1049668);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Campinas','BRA','São Paulo',907538);");

        // USA (Exact major cities)
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('New York','USA','New York',8008278);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Los Angeles','USA','California',3694820);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Chicago','USA','Illinois',2896016);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Houston','USA','Texas',1953631);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Philadelphia','USA','Pennsylvania',1517550);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Phoenix','USA','Arizona',1321045);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('San Diego','USA','California',1223400);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Dallas','USA','Texas',1188580);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('San Antonio','USA','Texas',1144646);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Detroit','USA','Michigan',951270);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('San Jose','USA','California',894943);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Indianapolis','USA','Indiana',791926);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('San Francisco','USA','California',776733);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Jacksonville','USA','Florida',735167);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Columbus','USA','Ohio',711470);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Austin','USA','Texas',656562);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Baltimore','USA','Maryland',651154);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Memphis','USA','Tennessee',650100);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Milwaukee','USA','Wisconsin',596974);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Boston','USA','Massachusetts',589141);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Washington','USA','District of Columbia',572059);");

        // INDIA (Exact major cities)
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Mumbai (Bombay)','IND','Maharashtra',11931417);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Delhi','IND','Delhi',7206704);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Calcutta (Kolkata)','IND','West Bengali',4399819);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Madras (Chennai)','IND','Tamil Nadu',3841396);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Hyderabad','IND','Andhra Pradesh',2965005);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Ahmedabad','IND','Gujarat',2876710);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Bangalore','IND','Karnataka',2660088);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Kanpur','IND','Uttar Pradesh',1876919);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Nagpur','IND','Maharashtra',1624752);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Lucknow','IND','Uttar Pradesh',1619115);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Pune','IND','Maharashtra',1566651);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Surat','IND','Gujarat',1498817);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Jaipur','IND','Rajasthan',1458483);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Indore','IND','Madhya Pradesh',1320389);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Bhopal','IND','Madhya Pradesh',1062771);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Ludhiana','IND','Punjab',1042740);");

        // CHINA (Exact major cities)
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Shanghai','CHN','Shanghai',9696300);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Peking','CHN','Peking',7472000);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Chongqing','CHN','Chongqing',6359700);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Tianjin','CHN','Tianjin',5286800);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Wuhan','CHN','Hubei',4344600);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Harbin','CHN','Heilongjiang',4289800);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Shenyang','CHN','Liaoning',4265200);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Canton (Guangzhou)','CHN','Guangdong',3133600);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Chengdu','CHN','Sichuan',3035800);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Nanking (Nanjing)','CHN','Jiangsu',2870300);");

        // RUSSIA (Exact major cities)
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Moscow','RUS','Moscow (City)',8389200);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Saint Petersburg','RUS','Saint Petersburg',4694000);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Novosibirsk','RUS','Novosibirsk',1398800);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Nizhni Novgorod','RUS','Nizhni Novgorod',1357000);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Jekaterinburg','RUS','Sverdlovsk',1266300);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Samara','RUS','Samara',1156100);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Omsk','RUS','Omsk',1148900);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Kazan','RUS','Tatarstan',1101000);");

        // JAPAN (Exact major cities)
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Tokyo','JPN','Tokyo-to',7980230);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Yokohama','JPN','Kanagawa',3339594);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Osaka','JPN','Osaka',2595674);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Nagoya','JPN','Aichi',2154376);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Sapporo','JPN','Hokkaido',1790886);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Kyoto','JPN','Kyoto',1461974);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Kobe','JPN','Hyogo',1425139);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Fukuoka','JPN','Fukuoka',1308379);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Kawasaki','JPN','Kanagawa',1217359);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Hiroshima','JPN','Hiroshima',1119117);");

        // GERMANY (Exact major cities)
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Berlin','DEU','Berliini',3386667);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Hamburg','DEU','Hampuri',1704735);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('München','DEU','Baijeri',1194560);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Köln','DEU','Nordrhein-Westfalen',962887);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Frankfurt am Main','DEU','Hessen',643821);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Essen','DEU','Nordrhein-Westfalen',599515);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Dortmund','DEU','Nordrhein-Westfalen',588980);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Stuttgart','DEU','Baden-Württemberg',582443);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Düsseldorf','DEU','Nordrhein-Westfalen',568522);");
        sql.add("INSERT INTO cities (name, country_code, district, population) VALUES ('Bremen','DEU','Bremen',539403);");
    }
}