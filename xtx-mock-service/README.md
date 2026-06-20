# XiaoTuXian Mock Service

This package contains the standalone mock aggregation service used by the Vue 3 storefront in mock mode.

## Scope

- Mock storefront APIs for login, home, category, goods, cart, checkout, order, coupon, gift card, review, message, points, invite, and after-sale flows
- Runtime JSON persistence for demo-only user data under `data/`
- Inventory-aware cart and checkout validation aligned with the frontend demo

## Tech stack

- Java 17
- Spring Boot 3
- Maven
- Hutool JSON

## Run

```bash
mvn clean package -DskipTests
java -jar target/xtx-mock-service-1.0.0.jar
```

Default port: `8099`

## Docs

- `docs/STARTUP.md`
- `docs/API.md`
- `docs/DATA.md`
