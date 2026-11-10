# GitSummarizer Backend

Spring Boot backend that fetches GitHub repository information and generates an AI summary.

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Maven
- OpenAI Java SDK

## Prerequisites

- JDK 21
- PostgreSQL
- IntelliJ IDEA or another Java IDE
- OpenAI API key for AI summaries

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
2. Open **Run → Edit Configurations…** and select `BackendApplication`.
3. Under **Environment variables**, set the variables your local setup needs:

   ```text
   DB_PASSWORD=your-local-postgres-password
   OPENAI_API_KEY=your-openai-api-key
   SERVER_PORT=8081