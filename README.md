# Task Manager API

Advanced Spring Boot Task Manager backend with:
- JWT authentication
- Role-based access (Admin/User)
- Task CRUD + assignment
- Pagination and filtering
- Email notifications
- Redis caching
- Docker deployment

## Tech Stack
- Java 17
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA (MySQL)
- Spring Cache + Redis
- Spring Mail
- Docker / Docker Compose

## Role-Based Access
- `ROLE_USER`
  - Create/update/read tasks
- `ROLE_ADMIN`
  - All user capabilities
  - Delete tasks
  - Assign tasks via dedicated endpoint
  - Manage user roles

## Authentication APIs
Base URL: `/api/auth`

- `POST /api/auth/register` (new users default to `ROLE_USER`)
- `POST /api/auth/login`

Example response:
```json
{
  "token": "<jwt>",
  "tokenType": "Bearer"
}
```

## Task APIs
Base URL: `/api/tasks` (requires `Authorization: Bearer <token>`)

- `POST /api/tasks`
- `GET /api/tasks`
- `GET /api/tasks/{id}`
- `PUT /api/tasks/{id}`
- `PATCH /api/tasks/{taskId}/assign/{userId}` (ADMIN only)
- `DELETE /api/tasks/{id}` (ADMIN only)

### Pagination and filtering
`GET /api/tasks?page=0&size=10&sortBy=createdAt&direction=desc&status=TODO&assigneeId=1&keyword=login`

## Admin APIs
Base URL: `/api/admin/users` (ADMIN only)

- `GET /api/admin/users`
- `PATCH /api/admin/users/{id}/role`

## Notifications (Email)
Task assignment and status changes trigger email notifications to the assignee email.
Default local settings point to MailHog.

## Redis Caching
- Caches single task lookups (`tasks` cache)
- Caches paginated task queries (`taskPages` cache)
- Evicts relevant caches on create/update/assign/delete

## Local Run (without Docker)
1. Start MySQL, Redis, and MailHog (or equivalent SMTP).
2. Update `src/main/resources/application.properties` as needed.
3. Run:
```bash
mvn spring-boot:run
```

## Docker Deployment
```bash
docker compose up --build
```

Services:
- API: `http://localhost:8080`
- MySQL: `localhost:3306`
- Redis: `localhost:6379`
- MailHog UI: `http://localhost:8025`
