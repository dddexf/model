# Local Development

## Directory layout

- `frontend/`: Vue3 + Vite UI
- `backend/`: Spring Boot API and SSE stream
- `db/`: MySQL schema and seed scripts
- `docs/`: API and local docs

## 1. Prepare MySQL

1. Create database:

```sql
CREATE DATABASE model CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

2. Execute:
- `db/schema.sql`
- `db/seed_model.sql` (replace `api_key_cipher` first)

## 2. Start backend

```bash
cd backend
mvn spring-boot:run
```

Backend persistence layer uses MyBatis-Plus.

Optional environment variables:

- `MYSQL_URL`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `APP_MASTER_KEY`

Current default database in `application.yml`:

- host: `192.168.113.130`
- port: `3306`
- database: `model`
- username: `root`
- password: `123`

## 3. Start frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend dev server: `http://localhost:5173`  
Backend server: `http://localhost:8080`
