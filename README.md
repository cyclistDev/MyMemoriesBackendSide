# MyMemories API

MyMemories is a Spring Boot backend for user accounts and personal memories. The current API implements registration and login. Memory-management endpoints and a frontend are not included yet.

## Requirements

- Java 21
- PostgreSQL
- The included Maven wrapper (`mvnw.cmd` on Windows)

## Configuration

The committed `src/main/resources/application.properties` reads database and JWT settings from environment variables. Do not commit real database credentials, JWT secrets, `.env` files, or local application configuration.

| Environment variable | Purpose |
| --- | --- |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL, for example `jdbc:postgresql://localhost:5432/mymemories` |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `JWT_SECRET` | Random secret used to sign JWTs; use a strong value of at least 32 bytes for HS256 |
| `JWT_EXPIRATION` | Token lifetime in milliseconds |

Set these values in your local shell for development and in your hosting provider's secret/environment settings for deployment. Generate a local JWT secret in PowerShell without storing it in the repository:

```powershell
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
$env:JWT_EXPIRATION = "86400000"
```

Set the three `SPRING_DATASOURCE_*` variables to match your local PostgreSQL instance before starting the application. This repository currently has no checked-in database migration or schema scripts, so ensure the database schema is available before exercising the API.

## Run Locally

After PostgreSQL is running and the environment variables are set:

```powershell
.\mvnw.cmd spring-boot:run
```

The API will use `http://localhost:8080` unless the hosting environment supplies a different port.

## API

### Register

`POST /api/auth/register`

Request body:

```json
{
  "username": "your-test-user",
  "email": "your-test-email@example.com",
  "password": "YOUR_DISPOSABLE_TEST_PASSWORD"
}
```

Successful registration returns `201 Created` with the user's id, username, email, and JWT. The current service requires a non-empty username and email, a password of at least six characters, and a unique email address.

### Login

`POST /api/auth/login`

Request body:

```json
{
  "email": "your-test-email@example.com",
  "password": "YOUR_DISPOSABLE_TEST_PASSWORD"
}
```

Successful login returns `200 OK` with the user's id, username, email, and JWT. Invalid credentials return `401 Unauthorized`.

Use a disposable local test password in these examples, not a password used for another account. Replace the placeholder values before sending requests.

## Test

Run the test suite from PowerShell:

```powershell
.\mvnw.cmd test
```

The tests use an in-memory H2 database and a randomly generated, test-only JWT key. They do not require PostgreSQL or real credentials. The suite checks Spring startup, registration and login responses, and malformed bearer-token handling.

To test the authentication endpoints manually, start the app, then send requests with PowerShell. Replace the example values with a disposable local account:

```powershell
$registerBody = @{
    username = "your-test-user"
    email = "your-test-email@example.com"
    password = "YOUR_DISPOSABLE_TEST_PASSWORD"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
    -Uri "http://localhost:8080/api/auth/register" `
    -ContentType "application/json" `
    -Body $registerBody
```

Then test login using the same email and disposable password:

```powershell
$loginBody = @{
    email = "your-test-email@example.com"
    password = "YOUR_DISPOSABLE_TEST_PASSWORD"
} | ConvertTo-Json

Invoke-RestMethod -Method Post `
    -Uri "http://localhost:8080/api/auth/login" `
    -ContentType "application/json" `
    -Body $loginBody
```

## GitHub and Deployment Checklist

- Store production database credentials and `JWT_SECRET` in the hosting provider's secret manager, not in source files.
- Keep local secrets and local configuration out of Git; inspect `git status` and the staged diff before pushing.
- Set all required environment variables in the hosting service before starting the application.
- Set `CORS_ALLOWED_ORIGINS` to the frontend origin (comma-separated if there is more than one); it is empty by default, so cross-origin browser requests are not allowed until configured.
- Keep the allowed-origin list restricted to frontend origins you control.
- Configure a database schema and backups before using the service with persistent user data.
- Treat registration and login as the only available API routes until memory endpoints are implemented.

## Current Project Notes

- Backend source is under `src/main/java/com/group5/MyMemories`.
- Java 21 and Spring Boot are configured in `pom.xml`.
- `application-local.properties` is ignored by `.gitignore`; the tracked `application.properties` contains environment-variable references, not credentials.
- The root `package-lock.json` is empty and untracked; there is no frontend project in this repository yet.