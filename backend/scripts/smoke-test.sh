#!/bin/bash
# ============================================
# 小兔鲜儿Smoke Test脚本
# 测试所有服务端点是否正常响应
# 需要先启动所有服务和基础设施
# ============================================

set -e

BASE_URL="http://localhost"
TOKEN=""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color
PASS=0
FAIL=0
TOTAL=0

# 辅助函数
check_response() {
    local test_name="$1"
    local status_code="$2"
    TOTAL=$((TOTAL+1))
    if [[ "$status_code" =~ ^2[0-9][0-9]$ ]] || [[ "$status_code" =~ ^3[0-9][0-9]$ ]]; then
        echo -e "  ${GREEN}[PASS]${NC} $test_name (HTTP $status_code)"
        PASS=$((PASS+1))
    else
        echo -e "  ${RED}[FAIL]${NC} $test_name (HTTP $status_code)"
        FAIL=$((FAIL+1))
    fi
}

do_curl() {
    local method="$1"
    local url="$2"
    local data="$3"
    local extra_args="$4"

    if [[ -n "$TOKEN" ]]; then
        extra_args="$extra_args -H 'Authorization: Bearer $TOKEN'"
    fi

    if [[ -n "$data" ]]; then
        curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" \
            -H "Content-Type: application/json" \
            $extra_args \
            -d "$data" 2>/dev/null || echo "000"
    else
        curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" \
            -H "Content-Type: application/json" \
            $extra_args 2>/dev/null || echo "000"
    fi
}

echo "============================================"
echo "  小兔鲜儿微服务 Smoke Test"
echo "  测试日期: $(date)"
echo "============================================"
echo ""

# ============================================
# Step 1: 登录获取Token
# ============================================
echo "--------------------------------------------"
echo "Step 1: 用户认证"
echo "--------------------------------------------"

echo "  正在登录获取Token..."
# 假设登录端点
# LOGIN_RESP=$(curl -s -X POST "${BASE_URL}:8101/api/auth/login" \
#     -H "Content-Type: application/json" \
#     -d '{"account":"xiaotuxian001","password":"123456"}')
# TOKEN=$(echo $LOGIN_RESP | jq -r '.data.token' 2>/dev/null || echo "")
# 如果没有auth服务，使用测试Token
TOKEN="test-smoke-token-2025"
echo -e "  ${YELLOW}[INFO]${NC} 使用测试Token (请替换为实际登录Token)"
echo ""

# ============================================
# Step 2: 测试认证服务 (8101)
# ============================================
echo "--------------------------------------------"
echo "Step 2: 认证服务 (端口8101)"
echo "--------------------------------------------"

echo "  测试 GET  /api/auth/user/info"
check_response "GET /api/auth/user/info" "$(do_curl "GET" "${BASE_URL}:8101/api/auth/user/info")"

echo ""

# ============================================
# Step 3: 测试会员服务 (8102)
# ============================================
echo "--------------------------------------------"
echo "Step 3: 会员服务 (端口8102)"
echo "--------------------------------------------"

echo "  测试 GET  /member/address"
check_response "GET /member/address" "$(do_curl "GET" "${BASE_URL}:8102/member/address")"

echo "  测试 POST /member/address"
check_response "POST /member/address" "$(do_curl "POST" "${BASE_URL}:8102/member/address" '{"receiverName":"李四","receiverPhone":"13800000002","province":"广东省","city":"广州市","county":"天河区","addressDetail":"测试地址100号"}')"

echo "  测试 GET  /member/collect"
check_response "GET /member/collect" "$(do_curl "GET" "${BASE_URL}:8102/member/collect?page=1&pageSize=10")"

echo ""

# ============================================
# Step 4: 测试分类服务 (8105)
# ============================================
echo "--------------------------------------------"
echo "Step 4: 分类服务 (端口8105)"
echo "--------------------------------------------"

echo "  测试 GET  /category/list"
check_response "GET /category/list" "$(do_curl "GET" "${BASE_URL}:8105/category/list")"

echo "  测试 GET  /category/1/sub"
check_response "GET /category/1/sub" "$(do_curl "GET" "${BASE_URL}:8105/category/1/sub")"

echo ""

# ============================================
# Step 5: 测试商品服务 (8106)
# ============================================
echo "--------------------------------------------"
echo "Step 5: 商品服务 (端口8106)"
echo "--------------------------------------------"

echo "  测试 GET  /goods/1"
check_response "GET /goods/1" "$(do_curl "GET" "${BASE_URL}:8106/goods/1")"

echo "  测试 GET  /goods/list?categoryId=1"
check_response "GET /goods/list" "$(do_curl "GET" "${BASE_URL}:8106/goods/list?categoryId=1")"

echo "  测试 GET  /goods/hot"
check_response "GET /goods/hot" "$(do_curl "GET" "${BASE_URL}:8106/goods/hot")"

echo "  测试 GET  /goods/new"
check_response "GET /goods/new" "$(do_curl "GET" "${BASE_URL}:8106/goods/new")"

echo "  测试 GET  /goods/1/skus"
check_response "GET /goods/1/skus" "$(do_curl "GET" "${BASE_URL}:8106/goods/1/skus")"

echo ""

# ============================================
# Step 6: 测试购物车服务 (8107)
# ============================================
echo "--------------------------------------------"
echo "Step 6: 购物车服务 (端口8107)"
echo "--------------------------------------------"

echo "  测试 GET  /member/cart"
check_response "GET /member/cart" "$(do_curl "GET" "${BASE_URL}:8107/member/cart")"

echo "  测试 POST /member/cart"
check_response "POST /member/cart" "$(do_curl "POST" "${BASE_URL}:8107/member/cart" '{"skuId":1,"count":2}')"

echo "  测试 PUT  /member/cart/1"
check_response "PUT /member/cart/1" "$(do_curl "PUT" "${BASE_URL}:8107/member/cart/1" '{"count":3}')"

echo "  测试 PUT  /member/cart/selected"
check_response "PUT /member/cart/selected" "$(do_curl "PUT" "${BASE_URL}:8107/member/cart/selected" '{"selected":true,"ids":[1]}')"

echo "  测试 DELETE /member/cart"
check_response "DELETE /member/cart" "$(do_curl "DELETE" "${BASE_URL}:8107/member/cart" '{"ids":[1]}')"

echo ""

# ============================================
# Step 7: 测试订单服务 (8108)
# ============================================
echo "--------------------------------------------"
echo "Step 7: 订单服务 (端口8108)"
echo "--------------------------------------------"

echo "  测试 GET  /member/order/pre"
check_response "GET /member/order/pre" "$(do_curl "GET" "${BASE_URL}:8108/member/order/pre")"

echo "  测试 GET  /member/order"
check_response "GET /member/order" "$(do_curl "GET" "${BASE_URL}:8108/member/order?page=1&pageSize=10")"

echo ""

# ============================================
# Step 8: 测试支付服务 (8109)
# ============================================
echo "--------------------------------------------"
echo "Step 8: 支付服务 (端口8109)"
echo "--------------------------------------------"

echo "  测试 POST /pay/mock"
check_response "POST /pay/mock" "$(do_curl "POST" "${BASE_URL}:8109/pay/mock" '{"orderId":1}')"

echo ""

# ============================================
# Step 9: 测试物流服务 (8110)
# ============================================
echo "--------------------------------------------"
echo "Step 9: 物流服务 (端口8110)"
echo "--------------------------------------------"

echo "  测试 GET  /member/order/1/logistics"
check_response "GET /member/order/1/logistics" "$(do_curl "GET" "${BASE_URL}:8110/member/order/1/logistics")"

echo ""

# ============================================
# Step 10: 测试评价服务 (8111)
# ============================================
echo "--------------------------------------------"
echo "Step 10: 评价服务 (端口8111)"
echo "--------------------------------------------"

echo "  测试 GET  /goods/1/evaluate"
check_response "GET /goods/1/evaluate" "$(do_curl "GET" "${BASE_URL}:8111/goods/1/evaluate")"

echo "  测试 GET  /goods/1/evaluate/page?page=1&pageSize=10"
check_response "GET /goods/1/evaluate/page" "$(do_curl "GET" "${BASE_URL}:8111/goods/1/evaluate/page?page=1&pageSize=10")"

echo ""

# ============================================
# Summary
# ============================================
echo "============================================"
echo -e "  测试摘要"
echo "============================================"
echo -e "  ${GREEN}通过: $PASS${NC}"
echo -e "  ${RED}失败: $FAIL${NC}"
echo -e "  ${YELLOW}总计: $TOTAL${NC}"
echo "============================================"

if [ "$FAIL" -eq 0 ]; then
    echo -e "  ${GREEN}所有Smoke Test通过!${NC}"
else
    echo -e "  ${RED}部分测试失败，请检查服务状态.${NC}"
fi
echo "============================================"
