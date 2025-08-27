INSERT INTO products (name, price, stock, category, is_active, created_at)
VALUES ('Laptop', 1200.00, 10, 'Electronics', true, CURRENT_TIMESTAMP);

INSERT INTO products (name, price, stock, category, is_active, created_at)
VALUES ('Phone', 800.00, 15, 'Electronics', true, CURRENT_TIMESTAMP);

INSERT INTO products (name, price, stock, category, is_active, created_at)
VALUES ('Shoes', 50.00, 30, 'Fashion', true, CURRENT_TIMESTAMP);

INSERT INTO products (name, price, stock, category, is_active, created_at)
VALUES ('Book', 20.00, 50, 'Education', true, CURRENT_TIMESTAMP);


INSERT INTO orders (customer_name, customer_email, order_date, status, total_amount)
VALUES ('John Doe', 'john@example.com', CURRENT_TIMESTAMP, 'PENDING', 2020.00);


INSERT INTO order_items (order_id, product_id, quantity, unit_price, total_price)
VALUES (1, 1, 1, 1200.00, 1200.00);

INSERT INTO order_items (order_id, product_id, quantity, unit_price, total_price)
VALUES (1, 2, 1, 800.00, 800.00);
