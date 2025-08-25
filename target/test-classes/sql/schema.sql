DROP TABLE IF EXISTS inventory_txn;
DROP TABLE IF EXISTS inventory;
DROP TABLE IF EXISTS product;
CREATE TABLE product(product_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY, sku VARCHAR(100) UNIQUE NOT NULL,
 name VARCHAR(255), brand VARCHAR(100), category VARCHAR(100), price DECIMAL(12,2) NOT NULL,
 currency VARCHAR(3) NOT NULL, status VARCHAR(10) NOT NULL, updated_at TIMESTAMP);
CREATE TABLE inventory(product_id UUID NOT NULL, location_id VARCHAR(20) NOT NULL, on_hand INT NOT NULL,
 reserved INT NOT NULL, available INT NOT NULL, safety_stock INT NOT NULL, updated_at TIMESTAMP,
 PRIMARY KEY(product_id, location_id), FOREIGN KEY(product_id) REFERENCES product(product_id));
CREATE TABLE inventory_txn(txn_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY, product_id UUID NOT NULL,
 location_id VARCHAR(20) NOT NULL, delta INT NOT NULL, reason VARCHAR(50), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
