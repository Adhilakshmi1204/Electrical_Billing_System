CREATE DATABASE ElectricityBillingDB;

USE ElectricityBillingDB;

CREATE TABLE Customers (
    customer_id INT PRIMARY KEY,
    name VARCHAR(50),
    address VARCHAR(100),
    email VARCHAR(50)
);

CREATE TABLE BillingRecords (
    bill_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT,
    units_consumed DOUBLE,
    amount DOUBLE,
    bill_date DATE,
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
);
select * from Customers;
ALTER TABLE BillingRecords
ADD COLUMN status VARCHAR(20) DEFAULT 'Unpaid';

CREATE TABLE Payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    bill_id INT,
    payment_amount DOUBLE,
    payment_date DATE,
    payment_method VARCHAR(20),
    FOREIGN KEY (bill_id) REFERENCES BillingRecords(bill_id)
);

drop database ElectricityBillingDB;
-- Create the database
CREATE DATABASE ElectricityBillingDB;

-- Use the newly created database
USE ElectricityBillingDB;

-- Create the Customers table
CREATE TABLE Customers (
    customer_id INT PRIMARY KEY,
    name VARCHAR(50),
    address VARCHAR(100),
    email VARCHAR(50)
);

-- Create the BillingRecords table
CREATE TABLE BillingRecords (
    bill_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT,
    units_consumed DOUBLE,
    amount DOUBLE,
    bill_date DATE,
    status VARCHAR(20) DEFAULT 'Unpaid',  -- Default status set to 'Unpaid'
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
);

-- Create the Payments table
CREATE TABLE Payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    bill_id INT,
    payment_amount DOUBLE,
    payment_date DATE,
    payment_method VARCHAR(20),
    FOREIGN KEY (bill_id) REFERENCES BillingRecords(bill_id)
);

select * from Customers;
select * from BillingRecords;
select * from Payments;
show tables;
