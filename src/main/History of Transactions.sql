-- Create a unified Transactions table in a new database
CREATE DATABASE IF NOT EXISTS UnifiedBankLedger;
USE UnifiedBankLedger;

CREATE TABLE Transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,         -- Unique ID for each transaction
    user_id INT,                                          -- ID of the user performing the transaction (can be NULL if not applicable)
    amount DECIMAL(10, 2) NOT NULL,                        -- Transaction amount (positive for debit, negative for credit)
    description VARCHAR(255) NOT NULL,                     -- Description of the transaction
    transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP,   -- Date and time of the transaction (current timestamp by default)
    transaction_type ENUM('debit', 'credit') NOT NULL,     -- Type of transaction ('debit' or 'credit')
    balance DECIMAL(10, 2)                                 -- Balance after the transaction (if applicable)
);

-- Insert data from BankDB into the unified table
INSERT INTO Transactions (amount, description, transaction_date, transaction_type, balance)
VALUES
(1300.50, 'Living Expense', '2024-10-08', 'debit', 1300.50),
(240.57, 'Rental Fee', '2024-10-25', 'debit', 1059.93);

-- Insert data from Ledger into the unified table (assuming user_id is available or NULL)
INSERT INTO Transactions (user_id, amount, description, transaction_date, transaction_type)
VALUES
(NULL, 1300.50, 'Living Expense', '2024-10-08', 'debit'),
(NULL, 240.57, 'Rental Fee', '2024-10-25', 'debit');
