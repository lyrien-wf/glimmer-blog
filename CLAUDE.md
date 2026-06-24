# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Personal blog website with a Spring Boot 3 backend and Vue 3 frontend. Apple-style minimalist design. Supports Markdown article creation via file upload or online editor.

## Tech Stack

- **Backend**: Spring Boot 3.2, Spring Data JPA, MySQL 8, JWT (jjwt 0.12), flexmark-java (MD→HTML), Lombok
- **Frontend**: Vue 3 + Vite 5, Vue Router 4, Axios, Vditor (MD editor), highlight.js
- **Java**: 17 (required by Spring Boot 3)

## Build & Run Commands

### Backend
```bash
cd blog-backend
mvn clean package -DskipTests    # build JAR
mvn spring-boot:run              # run locally (port 8080)
```
Config: `src/main/resources/application.yml` — update MySQL credentials and `blog.jwt-secret` before running.

### Frontend
```bash
cd blog-frontend
npm install
npm run dev      # dev server on port 5173, proxies /api to localhost:8080
npm run build    # production build to dist/
```

### Database
```bash
mysql -u root -p < docs/blog-init.sql
```
Creates `blog` database with tables: user, category, article, tag, article_tag. Seeds default admin (admin / admin123).

## Architecture

### Backend (`blog-backend/src/main/java/com/blog/`)

- `BlogApplication.java` — entry point
- `config/` — JwtUtil (token gen/validate), JwtFilter (OncePerRequestFilter, protects `/api/admin/**`), MarkdownUtil (flexmark), WebConfig (CORS), WebMvcConfig (upload dir mapping), BlogConfig (custom properties), GlobalExceptionHandler
- `model/` — JPA entities: Article (has `htmlCache` field for pre-rendered HTML, `@Transient` categoryObj/tags), User, Category, Tag, ArticleTag (composite key via ArticleTagId)
- `repository/` — Spring Data JPA interfaces
- `service/` — ArticleService (core logic: CRUD, MD render, tag/category association), UserService (login + password change), CategoryService, TagService
- `controller/` — AuthController (`/api/auth`), ArticleController (`/api/articles`, `/api/categories`, `/api/tags`), AdminController (`/api/admin/**`)

**Key patterns:**
- API responses wrapped in `ApiResponse<T>` with `{code, message, data}` structure
- Article MD content stored as-is in `content`, rendered HTML cached in `html_cache` on save
- JWT stored client-side in localStorage, sent via `Authorization: Bearer` header
- JwtFilter skips public paths (`/api/auth/**`, `/api/articles/**`, `/api/categories`, `/api/tags`) and validates all `/api/admin/**` requests

### Frontend (`blog-frontend/src/`)

- `api/index.js` — all API calls via axios instance with JWT interceptor
- `router/index.js` — routes + guard for `/admin/**` (checks localStorage token)
- `views/` — Home (article grid + search), Article (MD render + highlight.js + TOC), Categories, AdminLogin, AdminArticles (table), AdminEdit (Vditor + sidebar properties panel)
- `components/` — NavBar, Footer, Pagination, ArticleCard, TocNav (floating right-side TOC), ImageUploader
- `assets/styles/main.css` — Apple design system: CSS variables for colors/radii/shadows, `.md-content` for rendered article styles, responsive breakpoints

**Design tokens** (CSS variables in `main.css`):
- `--color-accent: #0071e3` (Apple blue)
- `--color-bg: #ffffff`, `--color-bg-alt: #f5f5f7`
- `--radius-md: 12px`, `--shadow-card: 0 2px 20px rgba(0,0,0,0.07)`

## Deployment (1Panel)

Backend: upload JAR → Supervisor process. Frontend: `npm run build` → upload `dist/` → 1Panel static site + Nginx reverse proxy `/api` to `:8080`. SSL via Let's Encrypt.
