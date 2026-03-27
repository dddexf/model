# Multi-Model Chat Monorepo

This repo includes:

- `frontend/`: Vue 3 + Vite chat UI
- `backend/`: Spring Boot API, SSE stream, MySQL persistence (MyBatis-Plus)
- `db/`: SQL schema and model seed scripts
- `docs/`: API contract and local setup notes

## Quick start

1. Prepare MySQL database and execute:
- `db/schema.sql`
- `db/seed_model.sql`

2. Start backend:

```bash
cd backend
mvn spring-boot:run
```

3. Start frontend:

```bash
cd frontend
npm install
npm run dev
```

## Environment variables

- `MYSQL_URL`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `APP_MASTER_KEY`
