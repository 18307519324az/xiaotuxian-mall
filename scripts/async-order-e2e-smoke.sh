#!/bin/bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8108}"
INVENTORY_URL="${INVENTORY_URL:-http://localhost:8106}"
USER_ID="${USER_ID:-1}"
SKU_ID="${SKU_ID:-1027026}"
ADDRESS_ID="${ADDRESS_ID:-1}"

echo "[async-order-e2e] warmup stock"
curl -s -X POST "$INVENTORY_URL/inventory/stocks/warmup" \
  -H "Content-Type: application/json" \
  -d "{\"skuIds\":[$SKU_ID],\"forceRefresh\":true}" > /dev/null

echo "[async-order-e2e] fetch token"
TOKEN=$(curl -s "$BASE_URL/member/order/token" \
  -H "X-User-Id: $USER_ID" \
  | python3 -c "import sys, json; print(json.load(sys.stdin)['result']['token'])")

echo "[async-order-e2e] submit async order"
RESPONSE=$(curl -s -X POST "$BASE_URL/member/order/async" \
  -H "X-User-Id: $USER_ID" \
  -H "Content-Type: application/json" \
  -d "{\"addressId\":$ADDRESS_ID,\"goods\":[{\"skuId\":$SKU_ID,\"count\":1}],\"token\":\"$TOKEN\",\"payChannel\":1}")

echo "$RESPONSE"

ORDER_NO=$(printf '%s' "$RESPONSE" | python3 -c "import sys, json; print((json.load(sys.stdin).get('result') or {}).get('orderNo', ''))")

if [ -z "$ORDER_NO" ]; then
  echo "[async-order-e2e] orderNo missing"
  exit 1
fi

echo "[async-order-e2e] poll process status"
for _ in $(seq 1 20); do
  STATUS_JSON=$(curl -s "$BASE_URL/member/order/process/$ORDER_NO")
  STATUS=$(printf '%s' "$STATUS_JSON" | python3 -c "import sys, json; print((json.load(sys.stdin).get('result') or {}).get('status', ''))")
  echo "$STATUS_JSON"
  if [ "$STATUS" != "PROCESSING" ]; then
    break
  fi
  sleep 1
done
