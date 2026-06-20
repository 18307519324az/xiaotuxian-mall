#!/bin/bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8108}"
USER_ID="${USER_ID:-1}"
ADDRESS_ID="${ADDRESS_ID:-1}"
COUPON_SKU_ID="${COUPON_SKU_ID:-1027026}"
GIFT_SKU_ID="${GIFT_SKU_ID:-1027026}"
COUPON_ID="${COUPON_ID:-coupon_1}"
GIFT_CARD_CODE="${GIFT_CARD_CODE:-GIFT-2026-003}"

get_token() {
  curl -s "$BASE_URL/member/order/token" \
    -H "X-User-Id: $USER_ID" \
    | python3 -c "import sys, json; print(json.load(sys.stdin)['result']['token'])"
}

echo "[benefit-smoke] sync coupon order"
COUPON_TOKEN="$(get_token)"
curl -s -X POST "$BASE_URL/member/order" \
  -H "X-User-Id: $USER_ID" \
  -H "Content-Type: application/json" \
  -d "{\"addressId\":$ADDRESS_ID,\"goods\":[{\"skuId\":$COUPON_SKU_ID,\"count\":1}],\"token\":\"$COUPON_TOKEN\",\"payChannel\":1,\"couponId\":\"$COUPON_ID\"}"

echo
echo "[benefit-smoke] async gift-card order"
GIFT_TOKEN="$(get_token)"
curl -s -X POST "$BASE_URL/member/order/async" \
  -H "X-User-Id: $USER_ID" \
  -H "Content-Type: application/json" \
  -d "{\"addressId\":$ADDRESS_ID,\"goods\":[{\"skuId\":$GIFT_SKU_ID,\"count\":1}],\"token\":\"$GIFT_TOKEN\",\"payChannel\":1,\"giftCardCode\":\"$GIFT_CARD_CODE\"}"
