#!/bin/bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8108}"
INVENTORY_URL="${INVENTORY_URL:-http://localhost:8106}"
MYSQL_BIN="${MYSQL_BIN:-mysql}"
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-<your-db-password>}"
USER_ID="${USER_ID:-1}"
SKU_ID="${SKU_ID:-1027026}"
ADDRESS_ID="${ADDRESS_ID:-1}"
CONCURRENCY="${CONCURRENCY:-20}"
TMP_DIR="$(mktemp -d)"

cleanup() {
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

mysql_exec() {
  "$MYSQL_BIN" -N -B -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" "-p$MYSQL_PASSWORD" "$@"
}

read_stock() {
  mysql_exec -D xtx_inventory -e "SELECT total_stock,available_stock,locked_stock,sold_stock,version FROM stock_sku WHERE sku_id=$SKU_ID;"
}

ORIGINAL_STOCK="$(read_stock)"
ORIGINAL_TOTAL="$(printf '%s' "$ORIGINAL_STOCK" | awk '{print $1}')"
ORIGINAL_AVAILABLE="$(printf '%s' "$ORIGINAL_STOCK" | awk '{print $2}')"
ORIGINAL_LOCKED="$(printf '%s' "$ORIGINAL_STOCK" | awk '{print $3}')"
ORIGINAL_SOLD="$(printf '%s' "$ORIGINAL_STOCK" | awk '{print $4}')"
ORIGINAL_VERSION="$(printf '%s' "$ORIGINAL_STOCK" | awk '{print $5}')"

echo "[concurrency] set controlled baseline"
mysql_exec -D xtx_inventory -e "UPDATE stock_sku SET total_stock=20, available_stock=10, locked_stock=1, sold_stock=9 WHERE sku_id=$SKU_ID;"
curl -s -X POST "$INVENTORY_URL/inventory/stocks/warmup" \
  -H "Content-Type: application/json" \
  -d "{\"skuIds\":[$SKU_ID],\"forceRefresh\":true}" > /dev/null

for i in $(seq 1 "$CONCURRENCY"); do
  TOKEN=$(curl -s "$BASE_URL/member/order/token" \
    -H "X-User-Id: $USER_ID" \
    | python3 -c "import sys, json; print(json.load(sys.stdin)['result']['token'])")
  printf '%s\n' "$TOKEN" > "$TMP_DIR/token-$i.txt"
done

for i in $(seq 1 "$CONCURRENCY"); do
  TOKEN=$(cat "$TMP_DIR/token-$i.txt")
  (
    curl -s -X POST "$BASE_URL/member/order/async" \
      -H "X-User-Id: $USER_ID" \
      -H "Content-Type: application/json" \
      -d "{\"addressId\":$ADDRESS_ID,\"goods\":[{\"skuId\":$SKU_ID,\"count\":1}],\"token\":\"$TOKEN\",\"payChannel\":1}" \
      > "$TMP_DIR/resp-$i.json"
  ) &
done
wait

python3 - <<'PY' "$TMP_DIR" "$CONCURRENCY"
import json, pathlib, sys
tmp = pathlib.Path(sys.argv[1])
count = int(sys.argv[2])
success = []
failed = 0
for i in range(1, count + 1):
    data = json.loads((tmp / f"resp-{i}.json").read_text(encoding="utf-8"))
    result = data.get("result") or {}
    if result.get("orderNo"):
        success.append(result["orderNo"])
    else:
        failed += 1
print(f"submit_success={len(success)}")
print(f"submit_fail={failed}")
for order_no in success:
    print(f"order_no={order_no}")
PY

SUCCESS_ORDERS=$(python3 - <<'PY' "$TMP_DIR" "$CONCURRENCY"
import json, pathlib, sys
tmp = pathlib.Path(sys.argv[1])
count = int(sys.argv[2])
orders = []
for i in range(1, count + 1):
    data = json.loads((tmp / f"resp-{i}.json").read_text(encoding="utf-8"))
    result = data.get("result") or {}
    if result.get("orderNo"):
        orders.append(result["orderNo"])
print("\n".join(orders))
PY
)

if [ -n "$SUCCESS_ORDERS" ]; then
  while IFS= read -r ORDER_NO; do
    [ -z "$ORDER_NO" ] && continue
    curl -s -X PUT "$BASE_URL/member/order/$ORDER_NO/cancel" \
      -H "X-User-Id: $USER_ID" \
      -H "Content-Type: application/json" \
      -d '{"cancelReason":"controlled concurrency cleanup"}' > /dev/null || true
  done <<EOF
$SUCCESS_ORDERS
EOF
fi

echo "[concurrency] restore original stock snapshot"
mysql_exec -D xtx_inventory -e "UPDATE stock_sku SET total_stock=$ORIGINAL_TOTAL, available_stock=$ORIGINAL_AVAILABLE, locked_stock=$ORIGINAL_LOCKED, sold_stock=$ORIGINAL_SOLD, version=$ORIGINAL_VERSION WHERE sku_id=$SKU_ID;"
curl -s -X POST "$INVENTORY_URL/inventory/stocks/warmup" \
  -H "Content-Type: application/json" \
  -d "{\"skuIds\":[$SKU_ID],\"forceRefresh\":true}" > /dev/null
