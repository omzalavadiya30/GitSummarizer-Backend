# GitSummarizer Backend

Spring Boot backend for GitSummarizer, an application that will summarize GitHub repositories and save summary history.

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Maven

## Prerequisites

- JDK 21
- PostgreSQL
- IntelliJ IDEA or another Java IDE

## Local Database Setup

Create a PostgreSQL database named `gitsummarizer` with owner `postgres`.

In pgAdmin:

1. Expand **Servers → PostgreSQL → Databases**.
2. Right-click **Databases** and select **Create → Database…**.
3. Enter `gitsummarizer` as the database name and keep `postgres` as the owner.

On startup, Hibernate creates or updates the development schema. The current models use these tables:

- `app_users`
- `saved_repository_summaries`

## Configure and Run in IntelliJ IDEA

1. Open this project in IntelliJ IDEA.
2. Open **Run → Edit Configurations…** and select `GitSummarizerBackend`.
3. Under **Environment variables**, set:

   ```text
   DB_PASSWORD=your-local-postgres-password
   ```

   Use your local PostgreSQL password in IntelliJ. Do not commit the real password.

4. If port `8080` is already in use, also set:

   ```text
   SERVER_PORT=8081
   ```

5. Save the configuration and run `BackendApplication`.

The database URL defaults to `jdbc:postgresql://localhost:5432/gitsummarizer`, and the username defaults to `postgres`. You can override them with `DB_URL` and `DB_USERNAME` environment variables.

## Current Status

The initial backend foundation includes the PostgreSQL connection, JPA models, and repositories for users and saved repository summaries. GitHub repository fetching and summary-generation API endpoints are planned for later development.