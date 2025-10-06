-- Clear existing data
DELETE FROM transactions;
DELETE FROM accounts;
DELETE FROM users;

-- Insert sample users with realistic BCrypt passwords (password: 'password123')
INSERT INTO users (id, first_name, last_name, email, password, phone, created_at) VALUES 
(1, 'John', 'Doe', 'john.doe@email.com', '$2a$10$xyz123abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQR', '+1-555-0101', '2024-01-15 10:00:00'),
(2, 'Jane', 'Smith', 'jane.smith@email.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUV', '+1-555-0102', '2024-01-16 11:30:00'),
(3, 'Bob', 'Johnson', 'bob.johnson@email.com', '$2a$10$ghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZab', '+1-555-0103', '2024-01-17 09:15:00'),
(4, 'Alice', 'Brown', 'alice.brown@email.com', '$2a$10$mnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefg', '+1-555-0104', '2024-01-18 14:20:00'),
(5, 'Charlie', 'Wilson', 'charlie.wilson@email.com', '$2a$10$qrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijk', '+1-555-0105', '2024-01-19 16:45:00');

-- Insert accounts with realistic demo money balances
INSERT INTO accounts (id, account_number, balance, account_type, user_id, created_at) VALUES 
(1, 'ACC0000000001', 12500.75, 'SAVINGS', 1, '2024-01-15 10:05:00'),
(2, 'ACC0000000002', 3500.25, 'CHECKING', 1, '2024-01-15 10:10:00'),
(3, 'ACC0000000003', 18750.50, 'SAVINGS', 2, '2024-01-16 11:35:00'),
(4, 'ACC0000000004', 4200.80, 'CHECKING', 2, '2024-01-16 11:40:00'),
(5, 'ACC0000000005', 9300.00, 'SAVINGS', 3, '2024-01-17 09:20:00'),
(6, 'ACC0000000006', 2750.30, 'CHECKING', 3, '2024-01-17 09:25:00'),
(7, 'ACC0000000007', 15200.00, 'SAVINGS', 4, '2024-01-18 14:25:00'),
(8, 'ACC0000000008', 3800.90, 'CHECKING', 4, '2024-01-18 14:30:00'),
(9, 'ACC0000000009', 11000.25, 'SAVINGS', 5, '2024-01-19 16:50:00'),
(10, 'ACC0000000010', 2950.60, 'CHECKING', 5, '2024-01-19 16:55:00');

-- Insert sample transactions
INSERT INTO transactions (id, from_account_id, to_account_id, amount, transaction_type, description, transaction_date, status) VALUES 
(1, 1, 3, 500.00, 'TRANSFER', 'Rent payment', '2024-01-20 09:00:00', 'COMPLETED'),
(2, 3, 2, 200.00, 'TRANSFER', 'Dinner payment', '2024-01-21 19:30:00', 'COMPLETED'),
(3, 2, 5, 100.00, 'TRANSFER', 'Birthday gift', '2024-01-22 14:15:00', 'COMPLETED'),
(4, 5, 1, 300.00, 'TRANSFER', 'Loan repayment', '2024-01-23 11:45:00', 'COMPLETED'),
(5, 1, 4, 150.00, 'TRANSFER', 'Shopping', '2024-01-24 16:20:00', 'COMPLETED'),
(6, 4, 6, 75.50, 'TRANSFER', 'Utility bill', '2024-01-25 10:30:00', 'COMPLETED'),
(7, 6, 7, 200.00, 'TRANSFER', 'Investment', '2024-01-26 13:15:00', 'COMPLETED'),
(8, 7, 8, 50.00, 'TRANSFER', 'Coffee shop', '2024-01-27 08:45:00', 'COMPLETED'),
(9, 8, 9, 100.00, 'TRANSFER', 'Book purchase', '2024-01-28 15:20:00', 'COMPLETED'),
(10, 9, 10, 25.00, 'TRANSFER', 'Lunch', '2024-01-29 12:30:00', 'COMPLETED');
-- Clear existing data
DELETE FROM transactions;
DELETE FROM accounts;
DELETE FROM users;

-- Insert sample users with BCrypt passwords (password: 'password123')
INSERT INTO users (id, first_name, last_name, email, password, phone, created_at) VALUES 
(1, 'John', 'Doe', 'john.doe@email.com', '$2a$10$xyz123abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQR', '+1-555-0101', CURRENT_TIMESTAMP),
(2, 'Jane', 'Smith', 'jane.smith@email.com', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUV', '+1-555-0102', CURRENT_TIMESTAMP),
(3, 'Bob', 'Johnson', 'bob.johnson@email.com', '$2a$10$ghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZab', '+1-555-0103', CURRENT_TIMESTAMP),
(4, 'Alice', 'Brown', 'alice.brown@email.com', '$2a$10$mnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefg', '+1-555-0104', CURRENT_TIMESTAMP),
(5, 'Charlie', 'Wilson', 'charlie.wilson@email.com', '$2a$10$qrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijk', '+1-555-0105', CURRENT_TIMESTAMP);

-- Insert sample accounts with REAL demo money balances
INSERT INTO accounts (id, account_number, balance, account_type, user_id, created_at) VALUES 
(1, 'ACC0000000001', 12500.75, 'SAVINGS', 1, CURRENT_TIMESTAMP),
(2, 'ACC0000000002', 3500.25, 'CHECKING', 1, CURRENT_TIMESTAMP),
(3, 'ACC0000000003', 18750.50, 'SAVINGS', 2, CURRENT_TIMESTAMP),
(4, 'ACC0000000004', 4200.80, 'CHECKING', 2, CURRENT_TIMESTAMP),
(5, 'ACC0000000005', 9300.00, 'SAVINGS', 3, CURRENT_TIMESTAMP),
(6, 'ACC0000000006', 2750.30, 'CHECKING', 3, CURRENT_TIMESTAMP),
(7, 'ACC0000000007', 15200.00, 'SAVINGS', 4, CURRENT_TIMESTAMP),
(8, 'ACC0000000008', 3800.90, 'CHECKING', 4, CURRENT_TIMESTAMP),
(9, 'ACC0000000009', 11000.25, 'SAVINGS', 5, CURRENT_TIMESTAMP),
(10, 'ACC0000000010', 2950.60, 'CHECKING', 5, CURRENT_TIMESTAMP);

-- Insert initial deposit transactions
INSERT INTO transactions (from_account_id, to_account_id, amount, transaction_type, description, transaction_date, status) VALUES 
(NULL, 1, 12500.75, 'DEPOSIT', 'Account opening bonus', DATEADD('DAY', -30, CURRENT_TIMESTAMP), 'COMPLETED'),
(NULL, 2, 3500.25, 'DEPOSIT', 'Initial deposit', DATEADD('DAY', -25, CURRENT_TIMESTAMP), 'COMPLETED'),
(NULL, 3, 18750.50, 'DEPOSIT', 'Welcome bonus', DATEADD('DAY', -20, CURRENT_TIMESTAMP), 'COMPLETED'),
(NULL, 4, 4200.80, 'DEPOSIT', 'Account funding', DATEADD('DAY', -15, CURRENT_TIMESTAMP), 'COMPLETED'),
(NULL, 5, 9300.00, 'DEPOSIT', 'Initial balance', DATEADD('DAY', -10, CURRENT_TIMESTAMP), 'COMPLETED');

-- Insert sample transfer transactions
INSERT INTO transactions (from_account_id, to_account_id, amount, transaction_type, description, transaction_date, status) VALUES 
(1, 3, 500.00, 'TRANSFER', 'Rent payment', DATEADD('DAY', -5, CURRENT_TIMESTAMP), 'COMPLETED'),
(3, 2, 200.00, 'TRANSFER', 'Dinner payment', DATEADD('DAY', -3, CURRENT_TIMESTAMP), 'COMPLETED'),
(2, 5, 100.00, 'TRANSFER', 'Birthday gift', DATEADD('DAY', -1, CURRENT_TIMESTAMP), 'COMPLETED');

-- Reset sequences
ALTER TABLE users ALTER COLUMN id RESTART WITH 6;
ALTER TABLE accounts ALTER COLUMN id RESTART WITH 11;
ALTER TABLE transactions ALTER COLUMN id RESTART WITH 9;