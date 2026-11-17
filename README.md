# GitSummarizer backend

Java 21 · Spring Boot 4 · Spring MVC REST API · Spring Data JPA · H2 or PostgreSQL.

## Run locally

Install JDK 21 and Maven 3.9+, then run:

```bash
mvn spring-boot:run
```

The API starts at <http://localhost:8081>. H2 is used by default and stores data in `./data/gitsummarizer`; no database service is needed for a local run. Hibernate creates the tables on startup. To use PostgreSQL, set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` before starting the server.

## Free API configuration

- GitHub REST API works without a key for public repositories. Set `GITHUB_TOKEN` to raise the standard rate limit for your own use.
- Repository summaries use the Gemini Developer API when `GEMINI_API_KEY` is set. Google AI Studio offers free-tier model access with usage limits; choose a model available in your account and set `GEMINI_MODEL` if needed.
- If no Gemini key is set, or the provider is unavailable, the server returns a clearly labeled local summary based on the README and recent commit messages.

Set keys in your shell or deployment environment. Do not commit real keys.

## Optional PostgreSQL

Create a database called `gitsummarizer`, then set:

```text
DB_URL=jdbc:postgresql://localhost:5432/gitsummarizer
DB_USERNAME=postgres
DB_PASSWORD=your-local-password
```

The PostgreSQL driver is included. `DB_URL` is the signal to use PostgreSQL instead of the default H2 file database.

## Authentication and password reset

Passwords are hashed with PBKDF2-HMAC-SHA256. Random bearer tokens are stored as SHA-256 hashes and expire after seven days. A password reset invalidates existing sessions.

Configure SMTP with `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, and `MAIL_FROM` to deliver reset emails. For local development only, set `AUTH_RESET_EXPOSE_LINK=true`; the reset endpoint then returns `X-Dev-Reset-Link`. Keep this disabled in production. Reset links expire after 30 minutes.

## REST endpoints

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| `GET` | `/api/health` | Public | Health check |
| `POST` | `/api/auth/register` | Public | Create an account |
| `POST` | `/api/auth/login` | Public | Sign in |
| `GET` | `/api/auth/me` | Bearer token | Read the current account |
| `POST` | `/api/auth/logout` | Bearer token | Revoke the current session |
| `POST` | `/api/auth/forgot-password` | Public | Request a reset email |
| `POST` | `/api/auth/reset-password` | Public | Change a password with a reset token |
| `POST` | `/api/ai/summarize` | Bearer token | Fetch, summarize, and save a repository |
| `GET` | `/api/history` | Bearer token | List the current user's searches |
| `PATCH` | `/api/history/{id}` | Bearer token | Save or unsave a search (`{"saved":true}`) |
| `DELETE` | `/api/history/{id}` | Bearer token | Remove one search |
| `DELETE` | `/api/history?keepSaved=true` | Bearer token | Clear history and keep saved searches |

API errors use `{ "code": "...", "message": "...", "timestamp": "..." }`. Repository input accepts a GitHub owner/repository URL; private repositories are not available through the unauthenticated GitHub API.
