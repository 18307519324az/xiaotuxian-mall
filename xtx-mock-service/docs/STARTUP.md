# Startup

## Requirements

- JDK 17
- Maven 3.8+

## Build

```bash
mvn clean package -DskipTests
```

## Run

```bash
java -jar target/xtx-mock-service-1.0.0.jar
```

The service listens on `http://localhost:8099`.

## Frontend proxy

The Vue storefront can proxy `/api` to `http://localhost:8099`.
