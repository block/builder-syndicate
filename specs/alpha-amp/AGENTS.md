# Builder Syndicate - Agent Hints

## Project Goals

- **OSS-first enterprise link aggregator** (Reddit/HN/Lobsters style)
- **Exemplary Misk reference implementation** - showcase patterns, not escape hatches
- **LLM-built** - commits sized for context windows; multiple AI builders (Claude, Amp, ChatGPT, Gemini)

## Architecture

### Hexagonal-lite (Package Boundaries)
```
com.buildersyndicate/
├── core/       # Pure Kotlin, NO framework imports
├── adapters/   # Adapters (Misk, jOOQ, S3, LLM providers)
└── app/        # Wiring, main, config
```

**Key principle:** Core logic decoupled from Misk. If Misk was a bad decision, swap the adapter layer without touching core.

### Modular Composition (Enterprise Deployment)

The OSS project supports two deployment modes: standalone and enterprise (custom modules).

**Rules for AI builders:**
- `main()` MUST be thin — only instantiate modules and call `MiskApplication.start()`
- All setup logic MUST live in Guice modules, NOT in `main()`
- All Guice modules, services, actions, repositories MUST be `public` (not `internal`)
- Ship `DefaultBuilderSyndicateModule` that bundles all default implementations
- Commonly swapped: `AuthModule`, `DatabaseModule`, `LlmModule`, `StorageModule`, `JobQueueModule`
- Any Guice module can be replaced or extended

**Design for composition:** Internal teams import OSS as a library and compose their own module list without modifying OSS source.

### Tech Stack
- Kotlin + Misk + misk-grpc + Wire (Protobufs)
- Gradle Kotlin DSL
- Aurora MySQL + jOOQ + Flyway
- Misk UI (React)
- S3 + CDN for images

## Conventions

### Commits
- **Small, single-purpose** - one logical change per commit
- **Conventional commits:** `feat(scope):`, `chore:`, `fix:`, etc.
- **Scopes:** `core`, `db`, `adapters`, `misk`, `ui`

### Database
- Flyway migrations: `V001__`, `V002__`, etc.
- Primary keys: `id BIGINT AUTO_INCREMENT`
- Timestamps: `created_at`, `updated_at` (DATETIME)
- Polymorphic relationships: no FKs, application-level integrity

### gRPC
- One service per domain
- Proto3 syntax
- Request/Response naming: `{RpcName}Request`, `{RpcName}Response`

### WebActions
- Path: `/api/v1/{resource}/{action}`
- JSON ↔ Protobuf mapping server-side

## Key Decisions

| Decision | Rationale |
|----------|-----------|
| No downvotes | Scores only go up from 0 |
| Points floor at zero | Cannot go negative |
| Undo upvote supported | Deducts points from author |
| No comment depth limit | Performance note only, not enforced |
| No rate limits | Trust enterprise SSO users |
| No max limits | Tags, images, comment length uncapped |
| AI summary as body | Link posts don't have manual body field |
| RSS posts editable | "External" badge, but content modifiable |
| Logout = current session | Not all devices |
| Dark mode = auto-detect | System preference, no manual toggle |

## Implementation Order

1. **Micro Steel Thread (19 commits)** - Login, markdown posts, feed
2. **Full Steel Thread (45 commits)** - + Link posts, votes, comments, points
3. **Launch (144 commits)** - All v1 features
4. **Fast-Follow (161 commits)** - Drafts, dark mode, dashboard

## What's NOT in v1

- Account deletion, user blocking, data export
- In-app notifications, email, Slack
- Mobile responsive, rate limits
- Report/flag workflow, content warnings
- CI/CD, Docker/K8s
- Server-side caching

## Commands

```bash
just dev        # Local dev (via Orc)
just db:migrate # Run migrations
just codegen    # Generate jOOQ + protos
just build      # Build
just test       # Test
just run        # Run the app
```

## File Paths

| Path | Purpose |
|------|---------|
| `syndicate-protos/src/main/proto/xyz/block/` | Proto definitions |
| `infra/migrations/` | SQL migrations |
| `resources/llm/prompts/{feature}/v{N}.txt` | LLM prompt templates |

## Patterns

### Pagination
- Keyset pagination over `(created_at DESC, id DESC)`
- Cursor encodes both values (opaque to client)

## Common Pitfalls

- LLM cache key must include: provider, model, template version, input content hash
- Strip EXIF from images before storing (privacy)

## Testing

- Unit tests: core logic (ranking, dedup, points)
- Integration tests: gRPC services with Orc DB
- WebActions tests: JSON ↔ proto mapping

## Files to Know

| File | Purpose |
|------|---------|
| `amp-spec/prd.md` | Product requirements v1.1 |
| `amp-spec/engineering-spec.md` | Technical spec v1.1 |
| `amp-spec/IMPLEMENTATION_PLAN_ALT.md` | 144-commit phased plan |
| `amp-spec/GAP_ANALYSIS.md` | Gaps identified in original docs |

## Tips for AI Builders

1. **Read the phase you're on** before starting work
2. **Core packages have no Misk imports** - keep it pure Kotlin
3. **One migration per schema change** - sequential numbering
4. **Check existing patterns** before creating new components
5. **Commits should fit in context window** - if it's too big, split it
6. **Blog posts accompany commits** - code doesn't need extensive comments
