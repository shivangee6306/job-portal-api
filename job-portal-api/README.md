# Job Portal API

Role-based Job Portal REST API built with **Spring Boot 3, Spring Security (JWT), Spring Data JPA, Swagger/OpenAPI, JUnit 5 and Mockito**.

Three roles:

| Role | What they can do |
|------|------------------|
| **CANDIDATE** | Browse jobs, apply (once per job), track own applications |
| **RECRUITER** | Post / edit / close own jobs, view applications for own jobs, shortlist or reject |
| **ADMIN** | Close any job |

## Features
- JWT authentication (stateless) + BCrypt password hashing
- Role-based authorization with `@PreAuthorize`
- Search by keyword and location, pagination and sorting
- Bean validation + global exception handler (clean JSON errors: 400 / 401 / 403 / 404 / 409)
- Soft delete for jobs (applications are never lost)
- Duplicate-application protection (service check + DB unique constraint)
- Swagger UI with a Bearer-token "Authorize" button
- Unit tests (Mockito) and integration tests (MockMvc)
- Dockerfile

## Tech stack
Java 17, Spring Boot 3.3, Spring Security 6, JJWT, Hibernate/JPA, H2 (default) or MS SQL Server, Maven.

## Run it

Requirements: **JDK 17+** and **Maven** (or open the folder in IntelliJ / VS Code / Eclipse and run `JobPortalApplication`).

```bash
mvn spring-boot:run
```

Open the API or Swagger UI:
- API base URL: http://localhost:9696
- Swagger UI: http://localhost:9696/swagger-ui.html (or open http://localhost:9696 to be redirected)

### Demo accounts (auto-created on startup)

| Role | Email | Password |
|------|-------|----------|
| ADMIN | admin@jobportal.com | Admin@123 |
| RECRUITER | recruiter@jobportal.com | Recruiter@123 |
| CANDIDATE | candidate@jobportal.com | Candidate@123 |

### Try it in Swagger
1. `POST /api/auth/login` with a demo account -> copy the `token`.
2. Click **Authorize** (top right) -> paste the token -> Authorize.
3. Now call the protected APIs.

### Use MS SQL Server instead of H2
1. In SSMS run: `CREATE DATABASE jobportal;`
2. Put your password in `src/main/resources/application-sqlserver.properties`
3. Run: `mvn spring-boot:run -Dspring-boot.run.profiles=sqlserver`

Tables are created automatically by Hibernate.

## API overview

| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/auth/register` | Public (role: RECRUITER or CANDIDATE) |
| POST | `/api/auth/login` | Public |
| GET | `/api/jobs?keyword=&location=&page=&size=&sortBy=&direction=` | Public |
| GET | `/api/jobs/{id}` | Public |
| GET | `/api/jobs/my` | RECRUITER |
| POST | `/api/jobs` | RECRUITER |
| PUT | `/api/jobs/{id}` | RECRUITER (owner) |
| DELETE | `/api/jobs/{id}` | RECRUITER (owner) or ADMIN |
| POST | `/api/jobs/{jobId}/apply` | CANDIDATE |
| GET | `/api/applications/my` | CANDIDATE |
| GET | `/api/jobs/{jobId}/applications` | RECRUITER (owner) |
| PATCH | `/api/applications/{id}/status` | RECRUITER (owner) |

`sortBy` can be `createdAt`, `salary`, `title`, `company`. `direction` is `asc` or `desc`.

## Tests

```bash
mvn test
```

## Docker

```bash
docker build -t job-portal-api .
docker run -p 9696:9696 -e JWT_SECRET=put-a-long-random-secret-of-32-plus-chars job-portal-api
```

## Deploy (free)
Push this repo to GitHub, then create a **Web Service** on Render or Railway from the Dockerfile. Set the environment variable `JWT_SECRET`. Add the live Swagger link to your CV and README.

## Project structure
```
controller/   REST endpoints
service/      business rules (ownership checks, duplicate checks)
repository/   Spring Data JPA
entity/       User, Job, JobApplication
dto/          request / response records
security/     JWT service, filter, UserDetailsService
config/       SecurityConfig, OpenAPI, demo data
exception/    custom exceptions + global handler
```
