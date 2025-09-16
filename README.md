# Calendar Monorepo

Monorepo containing a React frontend and Spring Boot backend for an Outlook-like interview calendar. The project ships as a
three-directory layout (`frontend`, `backend`, `infra`) so that the infrastructure, API, and client can evolve together.

## Prerequisites
- Node.js & npm
- Java 21
- Docker

## Running Infrastructure
```
cd infra
docker-compose up -d
```

## Running the Backend
```
cd backend
./mvnw spring-boot:run
```
The backend connects to the PostgreSQL and MailHog services started via Docker.

> **Note**: the container image used for evaluation does not have internet access, so the first Maven run will fail while
> attempting to download Spring Boot dependencies. Re-run the command once network access is available or after populating the
> local Maven cache.

## Running the Frontend
```
cd frontend
npm install
npm run dev
```
The frontend expects the backend at `http://localhost:8080/api` (configure via `frontend/.env`).

### GitHub Pages Deployment
```
cd frontend
npm run build
npm run preview # optional local smoke-test
```
Deploy the contents of `frontend/dist` to GitHub Pages and ensure `VITE_API_BASE_URL` points to a reachable backend origin. For
static hosting you can bake the value into an `.env.production` file.

## Example API calls
```
# List public availability
curl http://localhost:8080/api/public/demo-token/availability?from=2024-01-01T00:00:00Z&to=2024-01-07T00:00:00Z

# List event types visible to the public token
curl http://localhost:8080/api/public/demo-token/event-types

# Developer bookings (requires X-Dev-Id header)
curl -H "X-Dev-Id: 11111111-1111-1111-1111-111111111111" \
  "http://localhost:8080/api/dev/11111111-1111-1111-1111-111111111111/bookings?from=2024-01-01T00:00:00Z&to=2024-01-07T00:00:00Z"

# Approve a booking
curl -X POST -H "X-Dev-Id: 11111111-1111-1111-1111-111111111111" \
  http://localhost:8080/api/dev/11111111-1111-1111-1111-111111111111/bookings/<booking-id>/approve
```

## Notifications
- **Email**: The backend uses Spring Mail and is preconfigured to target the MailHog instance defined in `infra/docker-compose.yml`.
- **Telegram**: Set the `TELEGRAM_BOT_TOKEN` environment variable before running the backend. Developers can store their
  `telegram_chat_id` to receive reminders; messages are logged when a token or chat id is unavailable.
