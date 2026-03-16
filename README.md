# Capgemini Hackathon Backend

Spring Boot backend for carbon-footprint management of physical sites (construction + operation), with PostgreSQL persistence and secure JWT authentication.

## What is implemented

- JWT authentication endpoints (`/api/auth/register`, `/api/auth/login`)
- Nested JWT security:
  - token is first signed (JWS HS256)
  - then encrypted (JWE A256GCM)
  - signing and encryption use two separate keys
- Site APIs (`/api/sites`) for:
  - create site with materials
  - list/get own sites
  - historized emissions (`/history`)
  - compare multiple sites (`/compare?ids=...`)
- Carbon KPIs:
  - total CO2e
  - CO2e per m2
  - CO2e per employee
  - construction vs operation split

## Run locally

1. Create a PostgreSQL database:

```sql
CREATE DATABASE capgemini;
```

2. Export environment variables:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/capgemini"
export DB_USER="postgres"
export DB_PASSWORD="postgres"

# 32 random bytes, base64 encoded (separate keys)
export JWT_SIGNING_KEY_BASE64="$(openssl rand -base64 32)"
export JWT_ENCRYPTION_KEY_BASE64="$(openssl rand -base64 32)"
export JWT_ISSUER="capgemini-backend"
```

3. Start the app:

```bash
./mvnw spring-boot:run
```

## API quick start

### Register

`POST /api/auth/register`

```json
{
  "email": "user@example.com",
  "password": "StrongPass123!"
}
```

### Login

`POST /api/auth/login`

```json
{
  "email": "user@example.com",
  "password": "StrongPass123!"
}
```

Use returned token as `Authorization: Bearer <token>`.

### Create site

`POST /api/sites`

```json
{
  "name": "Rennes Campus",
  "city": "Rennes",
  "surfaceM2": 11771,
  "parkingSpots": 300,
  "annualEnergyMwh": 1840,
  "employeeCount": 1800,
  "workstationCount": 1037,
  "materials": [
    { "materialType": "CONCRETE", "quantityTonnes": 4000 },
    { "materialType": "STEEL", "quantityTonnes": 900 },
    { "materialType": "GLASS", "quantityTonnes": 300 },
    { "materialType": "WOOD", "quantityTonnes": 120 }
  ]
}
```

## Notes

- Emission factors are seeded with default values and can be externalized later (ADEME/OpenData integration ready via service boundary).
- Each site creation stores an emission snapshot for historization.


# ENVIROMENT
- DB_USER
- DB_PASSWORD
- DB_URL
- JWT_ISSUER
- JWT_EXPIRATION_MINUTES=120

# PROPERTIES
```
spring.application.name=capgemini-backend

spring.datasource.url=
spring.datasource.username=
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=
spring.jpa.open-in-view=
spring.jpa.properties.hibernate.format_sql=

spring.jackson.time-zone=UTC

server.error.include-message=

app.jwt.issuer=
app.jwt.expiration-minutes=
app.jwt.signing-key-base64=
app.jwt.encryption-key-base64=
logging.level.org.apache.coyote.http11.Http11Processor=
```