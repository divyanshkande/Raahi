# Raahi

Raahi is an AI-powered travel itinerary planner. Users can register, log in, enter a destination, trip duration, and interests, and receive a detailed day-by-day itinerary with map links. The application also supports modifying itineraries through a conversational assistant and maintains a search history per user.

## Features

- User registration and login with JWT authentication
- AI-powered itinerary generation via OpenRouter / Spring AI
- Itinerary modification through a travel assistant chat
- Search history with pagination and detail view
- Clickable Google Maps links for each location
- Caching of identical itinerary requests (Caffeine, 6-hour TTL)
- Rate limiting: 10 AI requests/minute/user; 5 auth attempts/15 minutes
- Responsive React frontend with Tailwind CSS

## Architecture

```text
React Frontend
      |
      | REST API (JWT)
      v
Spring Boot Backend
      |
      +---- MySQL (Flyway migrations)
      |
      +---- OpenRouter / Spring AI
```

Docker Compose orchestrates MySQL, backend, and frontend containers.

## Tech Stack

- **Frontend**: React 18, Tailwind CSS, Axios, React Leaflet (OpenStreetMap)
- **Backend**: Spring Boot 3.5, Java 21, Spring Security, Spring Data JPA, Spring AI (OpenAI client pointed at OpenRouter)
- **Database**: MySQL 8.0, Flyway migrations
- **Caching**: Caffeine
- **Build**: Maven, npm, Docker Compose

## Environment Variables

### Backend (`backend/.env`)

| Variable | Description |
|----------|-------------|
| `DB_URL` | JDBC URL for MySQL (e.g., `jdbc:mysql://localhost:3306/raahi`) |
| `DB_USERNAME` | MySQL username |
| `DB_PASSWORD` | MySQL password |
| `OPENROUTER_API_KEY` | OpenRouter API key for AI itinerary generation |
| `OPENROUTER_MODEL_NAME` | Model name (e.g., `openai/gpt-4o-mini`) |
| `OPENROUTER_PROMPT` | Prompt template for itinerary generation |
| `JWT_SECRET` | Secret for signing JWTs (32+ chars, generate with `openssl rand -base64 48`) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins (e.g., `http://localhost:3000,https://your-frontend.vercel.app`) |
| `JWT_EXPIRATION_MS` | Token lifetime in milliseconds (optional, default: 86400000) |
| `PORT` | Server port (optional, default: 8080) |

### Frontend (`frontend/.env`)

| Variable | Description |
|----------|-------------|
| `REACT_APP_API_URL` | Backend API base URL (e.g., `http://localhost:8080` or `https://your-backend.onrender.com`) |

**Never commit real `.env` files.** Use `.env.example` as a template and supply secrets via your hosting platform's environment configuration.

## Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/divyanshkande/Raahi.git
   cd raahi
   ```

2. **Configure environment variables**
   - Copy `backend/.env.example` to `backend/.env` and fill in real values
   - Copy `frontend/.env.example` to `frontend/.env` and set `REACT_APP_API_URL`

3. **Start MySQL**
   - Ensure MySQL 8.0 is running and accessible at the `DB_URL`
   - Create the database if it doesn't exist: `CREATE DATABASE raahi;`

4. **Start the backend**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
   Backend runs on `http://localhost:8080`

5. **Start the frontend**
   ```bash
   cd frontend
   npm install
   npm start
   ```
   Frontend runs on `http://localhost:3000`

6. **Run tests**
   ```bash
   # Backend tests
   cd backend && ./mvnw test

   # Frontend production build (validates compile)
   cd frontend && npm run build
   ```

## Docker

```powershell
# Validate compose configuration
docker compose config

# Build images (requires Docker daemon)
docker compose build

# Start all services
docker compose up
```

**Note:** `docker compose config` validates successfully. `docker compose build` was not verified during the latest audit because the Docker daemon was unavailable. The configuration is correct and ready for deployment environments where Docker is running.

## Database & Flyway

### Fresh database

```text
Empty MySQL database
        ↓
Application starts
        ↓
Flyway executes V1__initial_schema.sql
        ↓
Hibernate validates schema (ddl-auto=validate)
```

### Existing database

If deploying against a database that already has the schema (created manually or by a previous version), you must baseline it before Flyway can manage it:

```bash
# Using Flyway CLI or Maven plugin
flyway baseline -baselineVersion=1 -baselineDescription="Initial schema"
```

This marks the existing schema as version 1 so Flyway will apply future migrations (V2, V3, ...) on top of it. Do not run `V1__initial_schema.sql` against an already-baselined database.

### ⚠️ V2 Migration — Data Loss Warning

**Migration `V2__drop_orphaned_trips_table.sql` drops the `trips` table if it exists.**

```sql
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `trips`;
SET FOREIGN_KEY_CHECKS = 1;
```

This migration **permanently deletes all data** in the `trips` table. The table is described as "orphaned" and "not mapped to any current JPA entity," but **you must verify the target database before applying this migration**. If the `trips` table contains valuable data in your environment, back it up or migrate it before running Flyway migrations. Do not assume the table is empty unless you have confirmed it.

## API Overview

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/auth/register` | Register a new user | No |
| POST | `/auth/login` | Log in, receive JWT | No |
| POST | `/api/tour/plan` | Generate a new itinerary | Yes (JWT) |
| POST | `/api/tour/modify` | Modify an existing itinerary via assistant | Yes (JWT) |
| POST | `/api/tour/assistant` | Chat with travel assistant | Yes (JWT) |
| GET | `/api/history` | List paginated search history | Yes (JWT) |
| GET | `/api/history/{id}` | Get full itinerary detail | Yes (JWT) |
| DELETE | `/api/history/{id}` | Delete a history entry | Yes (JWT) |

## Security

- **JWT authentication**: Stateless, HS256, configurable expiration
- **BCrypt password hashing**: `BCryptPasswordEncoder` with default strength
- **Environment-based secrets**: All credentials via `.env` / platform env vars
- **CORS**: Configured via `CORS_ALLOWED_ORIGINS`, credentials allowed
- **Content Security Policy**: Restricts scripts, styles, fonts, images, connections to trusted sources
- **HSTS**: Enabled with 1-year max-age and subdomain inclusion
- **X-Frame-Options**: `SAMEORIGIN`
- **X-Content-Type-Options**: `nosniff`
- **Request/body limits**: 10 MB max multipart and POST size; 8 KB max header size
- **Rate limiting**: 10 AI requests/minute per user; 5 login/register attempts per 15 minutes per IP
- **Hibernate schema validation**: `spring.jpa.hibernate.ddl-auto=validate` ensures runtime schema matches entities

## Testing

```text
Backend tests: 115/115 PASS
Frontend production build: PASS
Docker Compose config: PASS
Docker build: NOT VERIFIED — Docker daemon unavailable during latest verification
```

## Deployment

The application is prepared for deployment, but a public deployment URL has not yet been configured.

Once deployed, update this section with the live frontend URL.

## Screenshots

| Screenshot | Description |
|------------|-------------|
| `screenshots/home.png` | Homepage with navigation |
| `screenshots/about.png` | About page describing features |
| `screenshots/login.png` | Login / registration page |
| `screenshots/dashboard.png` | Authenticated dashboard with input form |
| `screenshots/itinerary_loaded.png` | Generated itinerary with map |
| `screenshots/day_time_itinerary.png` | Day/time breakdown with Google Maps links |

## Suggested resume/LinkedIn description

- Spring Boot REST API with JWT authentication and BCrypt password hashing
- React frontend with Tailwind CSS, Axios, and OpenStreetMap integration
- MySQL database with Flyway migrations and Hibernate schema validation (`ddl-auto=validate`)
- OpenRouter AI integration via Spring AI for itinerary generation with Caffeine caching
- Rate limiting: 10 AI requests/minute/user + auth rate limits (5 attempts/15 min)
- Security headers: CSP, HSTS, X-Frame-Options, CORS via environment config
- Docker Compose for multi-service deployment (MySQL, backend, frontend)
- 115/115 backend tests passing, frontend production build successful