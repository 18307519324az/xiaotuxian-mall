# API

This mock service provides demo-oriented APIs for the storefront.

## Core areas

- Auth: `/login`, `/register`, `/member/profile`
- Home and content: `/home/*`, `/category/*`, `/goods`, `/brand/*`, `/topic/*`
- Cart and checkout: `/member/cart`, `/member/order/pre`
- Order lifecycle: `/member/order`, `/member/order/repurchase`, `/member/order/{id}`
- Benefits: `/member/coupon`, `/member/coupon/exchange`, `/member/gift-card`, `/member/gift-card/bind`
- User flows: review, points, message, invite, after-sale, customer service

## Notes

- Responses use the storefront-compatible `code`, `msg`, `traceId`, and `result` shape.
- The service is intended for local demo and frontend compatibility testing, not production use.
