# Repository Guidelines

## Project Structure & Module Organization

This repository is currently design-led. Use `DESIGN.md` as the single source of truth for scope, architecture, phases, and technical decisions.

Planned structure:

- `server/`: Java 21 + Spring Boot backend
- `server/src/main/java/com/example/copilot/`: `controller/`, `service/`, `repository/`, `entity/`, `dto/`, `config/`, `security/`, `rag/`, `eval/`, `exception/`, `common/`
- `server/src/main/resources/`: `application.yml` and `db/migration/` for Flyway scripts
- `web/`: Next.js frontend
- `web/app/`, `web/components/`, `web/lib/`, `web/hooks/`, `web/types/`

Keep implementation aligned with the phased plan in `DESIGN.md`: MVP first, advanced retrieval and production scaling later.

## Build, Test, and Development Commands

Expected local commands after scaffolding:

- `cd server && ./mvnw spring-boot:run`: start the backend
- `cd server && ./mvnw test`: run backend tests
- `cd server && ./mvnw clean package`: build the backend artifact
- `cd web && npm install`: install frontend dependencies
- `cd web && npm run dev`: start the frontend
- `cd web && npm run build`: build the frontend

Update this section when actual scripts are added.

## Coding Style & Naming Conventions

Use 4 spaces for Java and 2 spaces for TypeScript, JSON, and YAML. Follow standard naming:

- Java classes: `PascalCase`, e.g. `KnowledgeBaseService`
- Methods and fields: `camelCase`
- React components: `PascalCase`, e.g. `CitationCard.tsx`
- Flyway files: `V1__create_users_table.sql`

Prefer clear module boundaries matching the design document. Keep API DTOs separate from persistence entities.

## Testing Guidelines

Use JUnit 5 for backend tests and add integration tests for the document pipeline and chat flow. Place tests under `server/src/test/java/` and name them `*Test.java`.

Prioritize coverage for:

- authentication and authorization
- document parsing, chunking, and indexing
- retrieval and cited-answer generation
- evaluation metrics and status transitions

## Commit & Pull Request Guidelines

No Git history is available in this workspace, so adopt Conventional Commits: `feat: add document upload endpoint`, `fix: handle failed indexing job`.

PRs should include:

- a short summary
- affected modules or APIs
- screenshots for UI changes
- notes on schema, config, or deployment changes

## Security & Configuration Tips

Never commit secrets. Store API keys, JWT secrets, and database credentials in environment variables. Validate uploaded file types, restrict access by authenticated user, and keep prompt rules grounded in retrieved context with citation output.
