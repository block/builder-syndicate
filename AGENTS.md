# Builder Syndicate - Agent Guide

## Project Goals

- **OSS-first enterprise link aggregator** (Reddit/HN/Lobsters style)
- **Exemplary Misk reference implementation** — showcase idiomatic patterns, no escape hatches
- **LLM-built** — multiple AI builders (Claude, Amp, ChatGPT, Gemini)

## Architecture

### Hexagonal-lite Package Structure

```
src/main/kotlin/xyz/block/buildersyndicate/
├── core/           # Pure Kotlin — NO framework imports
│   ├── users/      # User entity, UserRepository interface
│   ├── posts/      # Post entity, PostRepository, PostService, MarkdownRenderer
│   ├── comments/   # (future)
│   └── ...
├── adapters/       # Framework integrations
│   ├── db/         # jOOQ repository implementations, DatabaseModule
│   ├── misk/       # WebActions, gRPC services, AuthModule
│   └── ...
└── app/            # Wiring only
    ├── BuilderSyndicateService.kt  # Thin main()
    └── BuilderSyndicateModule.kt   # Root Guice module
```

### Core Rules (Invariants)

1. **`core/` has NO framework imports** — only stdlib + `java.time`
2. **`main()` is thin** — only instantiate modules, call `MiskApplication.start()`
3. **All Guice modules, services, actions, repositories are `public`** (not `internal`)
4. **Interfaces in `core/`, implementations in `adapters/`**

### Enterprise Composition

OSS ships `DefaultBuilderSyndicateModule` bundling defaults. Enterprises swap modules:
- `AuthModule` — SSO integration
- `DatabaseModule` — connection config
- `StorageModule` — S3/CDN

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin, Java 21 |
| Framework | Misk |
| Build | Gradle Kotlin DSL |
| API | gRPC (Wire) + WebActions (JSON) |
| Database | MySQL 8.0 (Aurora in prod) |
| Migrations | Flyway |
| Query | jOOQ (generated Kotlin) |
| Frontend | React + TypeScript |

## Commands

```bash
just dev          # Start MySQL via docker-compose
just down         # Stop containers
just db-migrate   # Run Flyway migrations
just codegen      # Generate jOOQ + proto code
just build        # Build project
just test         # Run tests
just run          # Start the app
```

## Conventions

### Database

- Migrations: `V001__description.sql`, `V002__description.sql`
- Primary keys: `id BIGINT AUTO_INCREMENT`
- Timestamps: `created_at DATETIME`, `updated_at DATETIME`
- Charset: `utf8mb4_unicode_ci`
- No polymorphic FKs — application-level integrity

### gRPC / WebActions

- One gRPC service per domain: `PostsService`, `UsersService`
- Proto3 syntax, request/response naming: `CreatePostRequest`, `CreatePostResponse`
- WebActions path: `/api/v1/{resource}/{action}`
- JSON ↔ protobuf mapping handled server-side

### Testing

- **Unit tests**: Core logic (pure Kotlin, no DB)
- **Integration tests**: Repositories against docker MySQL
- **Service tests**: gRPC services with test fixtures

### Commits

- Conventional commits: `feat(scope):`, `fix(scope):`, `chore:`
- Scopes: `core`, `db`, `adapters`, `misk`, `ui`
- One logical change per commit

## Authentication (Dev Mode)

For local development, the OSS version ships a fake auth provider:

1. `GET /login` shows a simple user picker (no external OAuth needed)
2. Selecting a user sets session cookie
3. User is created in DB on first selection if not exists

`AuthModule` is swappable — enterprises replace with real SSO.

## Frontend (Misk UI)

- React + TypeScript in `web/` directory
- Built assets served by Misk at `/`
- Build integration: `./gradlew buildWeb`
- Use standard React patterns (hooks, functional components)
- API calls via generated proto clients or fetch to WebActions

## Ticket Guidance

When creating or working tickets:

1. **Behavioral AC only** — "user can X", not "file exists"
2. **Human-comprehensible size** — a reviewer should understand the full changeset without scrolling endlessly
3. **No standalone scaffolding** — fold entity+repo+migration+impl into one vertical slice
4. **Split by capability** — read vs write, not by code layer
5. **Facets are vertical slices** — tickets are labeled by facet (e.g., `auth`, `posts`, `comments`) not by code layer

## Code Style

- **No sloppy overcommenting** — code should be self-explanatory; comments are for "why", not "what"
- **Don't narrate the obvious** — `// Create a new user` above `createUser()` is noise
- **Match existing patterns** — look at neighboring code before inventing new conventions

## File Paths

| Path | Purpose |
|------|---------|
| `src/main/resources/db/migration/` | Flyway SQL migrations |
| `src/main/proto/` | Proto definitions |
| `web/src/` | React frontend |

## Common Pitfalls

- **Don't import Misk in `core/`** — keep domain pure
- **Don't put logic in `main()`** — use Guice modules
- **Don't use `internal`** — modules must be composable
- **Repository methods return `null` for not-found** — not exceptions

## Agent Execution Tips

### Environment Pre-flight (CRITICAL)

- **Check for port conflicts before Docker**: Run `lsof -i :3306` — local MySQL will shadow Docker container
- **If port 3306 is in use**: Change docker-compose to use port 3307 and update all JDBC URLs
- **Diagnose connection issues at the source** — auth errors often mean you're hitting the wrong MySQL instance

### Dependency Strategy

- **Always use latest stable versions** — web search Maven Central / Gradle Plugin Portal before adding any dependency
- **Never trust pinned versions** — versions in tips files, old branches, or existing code are snapshots, not guidance
- **Misk BOM is source of truth** for Misk-related dependency versions — don't pin Misk modules separately
- **Flyway on Gradle 9+**: Use `net.ltgt.flyway` plugin (not `org.flywaydb.flyway` which uses deprecated APIs)
- **Solve compatibility at root cause** — don't downgrade to work around issues, fix forward

### Problem-Solving Approach

- **Don't cargo-cult from old branches** — understand WHY a config exists before copying it
- **Separate problems** — port conflicts, auth plugins, and plugin compatibility are distinct issues
- **Fail fast on environment issues** — check ports/connectivity before debugging code

### Context Management

- **Use `Task` subagents for multi-file work** — creating 5+ files or modifying across layers should be one Task call
- **Delegate research upfront** — use `librarian` or `oracle` for framework questions before writing code
- **Batch verification** — write a verify script, don't run 5 separate curl commands
- **Read tickets before implementing** — they're in `tasks/`, drift is expensive

### Misk-Specific Gotchas

- **YAML config files cannot be empty** — use `{}` as minimum content for environment overrides
- **Prometheus metrics on separate port** — default 9102, not `/_admin/metrics` on main port
- **Add logback.xml early** — Jetty DEBUG logs consume huge token counts; set level to WARN before testing
- **Use `jakarta.inject`** not `javax.inject` for annotations

### Build & Test

- **DB tasks need `-PwithDb` flag** — Flyway/jOOQ tasks are skipped during normal build to allow cold builds
- **Prefer `gradle compileKotlin` over `gradle run`** — verify compilation without port conflicts
- **Hermit manages tooling** — use `gradle` not `./gradlew`, activate with `source bin/activate-hermit`

### Prior Work

- **Check git log first** — `git log --oneline -10` shows what's already done
- **Learnings in `tips/`** — files document non-obvious discoveries (environment-specific, not version pins)
- **Specs in `specs/specs/`** — PRD, engineering spec, implementation plan are authoritative
- **Old branches are not reference implementations** — they may contain workarounds, not solutions

## Reference Docs

- [Misk GitHub](https://github.com/cashapp/misk)
- [Wire (protobufs)](https://github.com/square/wire)
- [jOOQ](https://www.jooq.org/doc/latest/manual/)
- [Flyway](https://flywaydb.org/documentation/)
