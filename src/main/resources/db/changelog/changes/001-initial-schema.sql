--liquibase formatted sql

--changeset doguscan:create-database
--validCheckSum: 9:4d73db962551de73f0f7518cca4ebc7a
--GRANT ALL PRIVILEGES ON my_database.* TO 'username'@'localhost' IDENTIFIED BY 'password';

-- UPDATE DATABASECHANGELOG
-- SET MD5SUM = NULL
-- WHERE ID = '001' AND AUTHOR = 'doguscan' AND FILENAME = 'db/changelog/changes/001-initial-schema.sql';

-- Clear the DATABASECHANGELOG table to reset Liquibase state (use carefully, for dev/test only)
DELETE FROM DATABASECHANGELOG;

--changeset doguscan:sql-1
--validCheckSum: 9:eb5e1d7df63834329149f2a52541f711
-- Create Tool Table
CREATE TABLE IF NOT EXISTS tool (
                      tool_code VARCHAR(4) PRIMARY KEY NOT NULL,
                      tool_type VARCHAR(50) NOT NULL,
                      brand VARCHAR(50) NOT NULL UNIQUE
);

--changeset doguscan:sql-2
--validCheckSum: 9:1140294ca26236c0c7746fcb94029770
-- Create the ToolCharge table
CREATE TABLE IF NOT EXISTS tool_charge (
                             tool_charge_id VARCHAR(4) PRIMARY KEY NOT NULL,
                             tool_code VARCHAR(4) NOT NULL,
                             daily_rental_charge DECIMAL(10, 2) NOT NULL,
                             weekday_charge BOOLEAN DEFAULT TRUE NOT NULL,
                             weekend_charge BOOLEAN DEFAULT FALSE NOT NULL,
                             holiday_charge BOOLEAN DEFAULT FALSE NOT NULL,
                                 CONSTRAINT fk_tool_charge_tool
                                 FOREIGN KEY (tool_code) REFERENCES tool(tool_code)
                                     ON DELETE CASCADE
);

--changeset doguscan:sql-3
--validCheckSum: 9:b437fbaa60623ab01944f3fb8b4bf066
-- Create Rental Table
CREATE TABLE IF NOT EXISTS rental (
                        rental_id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
                        tool_code VARCHAR(4) NOT NULL,
                        rental_days INT NOT NULL,
                        checkout_date DATE NOT NULL,
                        discount_percent DECIMAL(5,2) NOT NULL,
                        pre_discount_charge DECIMAL(10,2),
                        discount_amount DECIMAL(10,2),
                        final_charge DECIMAL(10,2),
                        due_date DATE,
                        CONSTRAINT fk_rental_tool FOREIGN KEY (tool_code) REFERENCES tool(tool_code)
);
--changeset doguscan:sql-4
--validCheckSum: 9:a7f7d3baaf890102fa090eedafe2cd14
-- Create Audit Table
CREATE TABLE IF NOT EXISTS audit (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
                       transaction_id VARCHAR(255) NOT NULL,
                       username VARCHAR(255) NOT NULL,
                       timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       success BOOLEAN NOT NULL,
                       message TEXT
);
--changeset doguscan:sql-5
-- Insert data into the Tool table first
INSERT INTO tool (tool_code, tool_type, brand) VALUES ('CHNS', 'Chainsaw', 'Stihl');
INSERT INTO tool (tool_code, tool_type, brand) VALUES ('LADW', 'Ladder', 'Werner');
INSERT INTO tool (tool_code, tool_type, brand) VALUES ('JAKD', 'Jackhammer', 'DeWalt');
INSERT INTO tool (tool_code, tool_type, brand) VALUES ('JAKR', 'Jackhammer', 'Ridgid');
--changeset doguscan:sql-6
-- Then insert data into the ToolCharge table, referencing tool_code values that already exist in the Tool table
INSERT INTO tool_charge (tool_charge_id, tool_code, daily_rental_charge, weekday_charge, weekend_charge, holiday_charge) VALUES ('C001','CHNS', 1.49, 1, 0, 1);
INSERT INTO tool_charge (tool_charge_id, tool_code, daily_rental_charge, weekday_charge, weekend_charge, holiday_charge) VALUES ('L001','LADW', 1.99, 1, 1, 0);
INSERT INTO tool_charge (tool_charge_id, tool_code, daily_rental_charge, weekday_charge, weekend_charge, holiday_charge) VALUES ('D001','JAKD', 2.99, 1, 0, 0);
INSERT INTO tool_charge (tool_charge_id, tool_code, daily_rental_charge, weekday_charge, weekend_charge, holiday_charge) VALUES ('R001','JAKR', 2.99, 1, 0, 0);

