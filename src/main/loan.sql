CREATE DATABASE loan;
USE loan;

CREATE TABLE Users(
 UserId INT PRIMARY KEY AUTO_INCREMENT,
 Name VARCHAR(255),
 Email VARCHAR(255),
 Password VARCHAR(255)
);

INSERT INTO Users
VALUES (1,"Syakir","syakir@gamil.com","sykr")
;

CREATE TABLE Balance(
 UserID INT, 
 Balance DECIMAL(65,2),
 FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

INSERT INTO Balance
VALUES (1,500)
;

CREATE TABLE LoanDetails(
 UserID INT,
 LoanID INT PRIMARY KEY AUTO_INCREMENT,
 Principal DECIMAL(65,2),
 InterestRate INT,
 RepaymentPeriod INT,
 MonthlyRepay DECIMAL(65,2),
 Balance DECIMAL(65,2),
 Status VARCHAR(100),
 CreatedAt DATE
);

CREATE TABLE Repay(
 UserId INT,
 LoanID INT,
 RepayID INT,
 Repayment DECIMAL(65,2),
 Balance DECIMAL(65,2),
 PaymentDate DATE,
 DueDate DATE
);
