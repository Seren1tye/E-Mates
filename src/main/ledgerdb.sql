CREATE DATABASE ledgerdb;
USE ledgerdb;

CREATE TABLE users(
	user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR (255),
    email VARCHAR (255),
    password VARCHAR (255)
    );
    
INSERT INTO users
	VALUES (1, "Firman", "firman@gmail.com", "fir");

CREATE TABLE bankdetails(
	bank_id INT PRIMARY KEY AUTO_INCREMENT,
    bank_name VARCHAR (255),
    interest_rate DECIMAL (5,4)
    );
    
INSERT INTO bankDetails  
	VALUES (1, "RHB", 0.026), 
		   (2, "Maybank", 0.025),  
           (3, "Hong Leong", 0.023),  
           (4, "Alliance", 0.0285),   
           (5, "AmBank", 0.0255),   
           (6, "Standard Chartered", 0.0265);
           
CREATE TABLE interest(  
	user_id INT,  
    deposit DECIMAL (10,2),  
    interest DECIMAL (10,2),  
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE 
    );
    
DELIMITER $$

CREATE TRIGGER after_insert_users
AFTER INSERT ON users
FOR EACH ROW
BEGIN
    INSERT INTO interest (user_id, deposit, interest)
    VALUES (NEW.user_id, 0.00, 0.00);
END$$

DELIMITER ;
