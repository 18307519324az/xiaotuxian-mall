-- Runtime stock reconciliation helper
-- Use this when stock_sku drifts from reservation facts in the local demo environment.

-- Validate the reservation aggregates first.
SELECT
  sku_id,
  SUM(CASE WHEN status = 1 THEN count ELSE 0 END) AS locked_qty,
  SUM(CASE WHEN status = 2 THEN count ELSE 0 END) AS sold_qty,
  SUM(CASE WHEN status = 3 THEN count ELSE 0 END) AS released_qty
FROM xtx_inventory.stock_reservation
WHERE sku_id IN (1027026, 1027027)
GROUP BY sku_id;

-- Reconcile one SKU from reservation facts.
-- On 2026-06-20 this was used to repair sku_id=1027027 in the local runtime:
-- total=1000, locked=0, sold=1, available=999.
UPDATE xtx_inventory.stock_sku s
LEFT JOIN (
  SELECT
    sku_id,
    SUM(CASE WHEN status = 1 THEN count ELSE 0 END) AS locked_qty,
    SUM(CASE WHEN status = 2 THEN count ELSE 0 END) AS sold_qty
  FROM xtx_inventory.stock_reservation
  WHERE sku_id = 1027027
  GROUP BY sku_id
) r ON r.sku_id = s.sku_id
SET
  s.available_stock = s.total_stock - IFNULL(r.locked_qty, 0) - IFNULL(r.sold_qty, 0),
  s.locked_stock = IFNULL(r.locked_qty, 0),
  s.sold_stock = IFNULL(r.sold_qty, 0),
  s.version = s.version + 1
WHERE s.sku_id = 1027027;

-- Post-check.
SELECT sku_id, total_stock, available_stock, locked_stock, sold_stock, version
FROM xtx_inventory.stock_sku
WHERE sku_id IN (1027026, 1027027);
