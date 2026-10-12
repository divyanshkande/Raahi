# Deploying Raahi

## What changed (read this first)

- **Passwords are now hashed with BCrypt** (`UserService` / `SecurityConfig`). Existing plaintext passwords in your DB will no longer work — wipe the `users` table or have people re-register.
- **Login/Register now return a JWT** (`AuthResponse: { token, email, username, message }`) instead of a plain string. The frontend stores the token in `localStorage` (`utils/auth.js`) and sends it as `Authorization: Bearer <token>` on every request (`services/apiClient.js`).
- **`/api/tour/**` now requires a valid token.** `/auth/**` stays public. Backed by `JwtAuthFilter` + `SecurityConfig`.
- **CORS is centralized** in `SecurityConfig` via `CORS_ALLOWED_ORIGINS` (comma-separated env var) — the old hardcoded `@CrossOrigin(origins = "http://localhost:3000")` annotations are gone.
- **The `/auth` route actually exists now** and `/plan` is wrapped in `ProtectedRoute`, redirecting to `/auth` if you're not logged in. The Navbar's "Sign In" link points at a real route instead of a dead anchor.
- Fixed a pre-existing typo (`OPENROTER_MODEL_NAME` → `OPENROUTER_MODEL_NAME`) in `application.properties`.
- `server.port` now reads `$PORT` — required by most PaaS hosts (Render, Railway, Fly.io all inject this).
- **Flyway migration `V2__drop_orphaned_trips_table.sql` drops the `trips` table if it exists.** This migration **permanently deletes all data** in that table. Before deploying against an existing database, verify whether the `trips` table contains valuable data and back it up if necessary. Do not assume the table is empty unless confirmed.

## 1. Generate a JWT secret

```bash
openssl rand -base64 48
```

Keep this out of source control. Put it in `backend/.env` (local) or your host's env var / secrets manager (production).

## 2. Test the whole stack locally with Docker

```bash
cp backend/.env.example backend/.env      # fill in real values, including JWT_SECRET
export $(grep -v '^#' backend/.env | xargs)   # or use `set -a; source backend/.env; set +a`
docker compose up --build
```

- Backend: http://localhost:8080
- Frontend: http://localhost:3000
- MySQL: localhost:3306 (user `root`, password from `DB_PASSWORD`)

## 3. Deploying the backend (any host works — Render, Railway, Fly.io, a VPS, etc.)

The backend is a standard Spring Boot app with a `Dockerfile`, so any container host works. Steps are roughly the same everywhere:

1. Push this repo to GitHub (or your host's git).
2. Create a new Web Service pointing at `/backend`, using the `Dockerfile`.
3. Set these environment variables on the host:
   - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (pointing at your hosted MySQL — see below)
   - `OPENROUTER_API_KEY`, `OPENROUTER_MODEL_NAME`, `OPENROUTER_PROMPT`
   - `JWT_SECRET` (from step 1)
   - `CORS_ALLOWED_ORIGINS` — set this to your deployed **frontend** URL once you have it (e.g. `https://raahi.vercel.app`). You can list more than one, comma-separated, no spaces.
   - Most hosts set `PORT` automatically; you don't need to set it yourself.

**Database**: you need a hosted MySQL instance. Free/cheap options: Railway MySQL, Aiven, PlanetScale (MySQL-compatible), or your host's managed MySQL add-on. Point `DB_URL` at it, e.g.:
`jdbc:mysql://<host>:3306/<dbname>?useSSL=true&serverTimezone=UTC`

## 4. Deploying the frontend

Two options:

**A. Static hosting (simplest — Vercel, Netlify, Cloudflare Pages):**
- Build command: `npm run build`, output directory: `build`
- Set the env var `REACT_APP_API_URL` to your deployed backend URL **at build time** (CRA bakes env vars in at build, not runtime — so you must rebuild if this changes).
- These hosts handle SPA routing fallback automatically; if asked, configure a rewrite of `/*` → `/index.html`.

**B. Docker + nginx (if you want everything containerized):**
```bash
docker build --build-arg REACT_APP_API_URL=https://your-backend-url.com -t raahi-frontend ./frontend
docker run -p 80:80 raahi-frontend
```
The included `nginx.conf` already handles client-side routing fallback.

## 5. After both are deployed

Go back to the backend's `CORS_ALLOWED_ORIGINS` and set it to your real frontend URL, then redeploy the backend (or just restart it if your host supports env var hot-reload). Until you do this, the browser will block requests with a CORS error even though the API itself works fine.

## Checklist

- [ ] `JWT_SECRET` set and kept secret (32+ random chars)
- [ ] MySQL reachable from the backend host, `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` set
- [ ] `OPENROUTER_API_KEY` set (get one at openrouter.ai)
- [ ] Backend deployed, `CORS_ALLOWED_ORIGINS` includes your frontend's real URL
- [ ] Frontend deployed with `REACT_APP_API_URL` pointing at your real backend URL
- [ ] Tried registering + logging in on the deployed site, confirmed `/plan` loads and redirects to `/auth` when logged out
