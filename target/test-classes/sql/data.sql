
INSERT INTO product (product_id, sku, name, brand, category, price, currency, status, updated_at)
VALUES (RANDOM_UUID(), 'SKU-1001', 'Widget', 'Acme', 'Gadgets', 19.99, 'USD', 'ACTIVE', CURRENT_TIMESTAMP() - 1),
       (RANDOM_UUID(), 'SKU-1002', 'Widget Plus', 'Acme', 'Gadgets', 10.00, 'USD', 'ACTIVE', CURRENT_TIMESTAMP() - 1),
       (RANDOM_UUID(), 'SKU-2001', 'Thing', 'Acme', 'Accessories', 5.00, 'EUR', 'INACTIVE', CURRENT_TIMESTAMP() - 1);
MERGE INTO inventory KEY(product_id, location_id) VALUES
 ((SELECT product_id FROM product WHERE sku='SKU-1001'), 'SEA-01', 20, 5, 15, 10, CURRENT_TIMESTAMP() - 1),
 ((SELECT product_id FROM product WHERE sku='SKU-2001'), 'SEA-01', 0, 0, 0, 5, CURRENT_TIMESTAMP() - 1);
INSERT INTO inventory_txn (product_id, location_id, delta, reason, created_at)
SELECT product_id, 'SEA-01', -5, 'SALE', CURRENT_TIMESTAMP() - 1 FROM product WHERE sku='SKU-1001';
