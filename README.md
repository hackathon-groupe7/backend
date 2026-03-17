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
  - list supported heating types (`/heating-types`)
- Carbon KPIs:
  - total CO2e
  - CO2e per m2
  - CO2e per employee
  - construction vs operation split
- Impact CO2 integration:
  - fetches official ADEME-based heating emissions from `https://impactco2.fr/api/v1/chauffage`
  - derives operation factor in `kgCO2e/MWh` with local fallback if API is unavailable

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
  "heatingType": "ELECTRIC",
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

- Material emission factors are persisted in database and used during calculation.
- Operation emission factor is fetched from Impact CO2 API (`/chauffage`) and cached in-memory.
- `heatingType` is required when creating a site and maps to official Impact CO2 heating slugs.
- If Impact CO2 cannot be reached, calculation falls back to local default operation factor.
- Each site creation stores an emission snapshot for historization.
- Optional site seeding is available: fixed rows from `src/main/resources/seed/sites_seed.csv` + reproducible random demo sites.


# ENVIROMENT
- DB_USER
- DB_PASSWORD
- DB_URL
- JWT_ISSUER
- JWT_EXPIRATION_MINUTES=120
- IMPACT_CO2_BASE_URL=https://impactco2.fr/api/v1
- IMPACT_CO2_API_KEY=
- IMPACT_CO2_REFERENCE_SURFACE_M2=100
- IMPACT_CO2_REFERENCE_ANNUAL_CONSUMPTION_MWH=10.0
- SEED_SITES_ENABLED=false
- SEED_SITES_FORCE_RELOAD=false
- SEED_SITES_USER_EMAIL=demo@capgemini.com
- SEED_SITES_USER_PASSWORD=DemoPass123!
- SEED_SITES_CSV_PATH=seed/sites_seed.csv
- SEED_SITES_RANDOM_COUNT=8

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
app.impact-co2.base-url=
app.impact-co2.api-key=
app.impact-co2.reference-surface-m2=
app.impact-co2.reference-annual-consumption-mwh=
app.seed.sites.enabled=
app.seed.sites.force-reload=
app.seed.sites.seed-user-email=
app.seed.sites.seed-user-password=
app.seed.sites.csv-path=
app.seed.sites.random-count=
logging.level.org.apache.coyote.http11.Http11Processor=
```