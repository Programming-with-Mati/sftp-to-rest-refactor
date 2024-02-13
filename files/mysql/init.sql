CREATE SCHEMA IF NOT EXISTS shop;
USE shop;

CREATE TABLE customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255)
);

CREATE TABLE customer_points (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    points INT,
    date_created DATE,
    customer_id BIGINT
);

CREATE TABLE reward (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      rate INT,
                      date_created DATE,
                      date_valid DATE,
                      redeemed BIT,
                      customer_id BIGINT
);

INSERT INTO customer (email) VALUES ('customer1@example.com');
INSERT INTO customer (email) VALUES ('customer2@example.com');
INSERT INTO customer (email) VALUES ('customer3@example.com');

-- INSERT INTO customer_points (points, date_created, customer_id) VALUES (100, '2024-02-12', 1);
-- INSERT INTO customer_points (points, date_created, customer_id) VALUES (50, '2024-02-11', 2);
-- INSERT INTO customer_points (points, date_created, customer_id) VALUES (25, '2024-02-10', 3);
