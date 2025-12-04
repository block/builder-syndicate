# Builder Syndicate Implementation Plan
## Overview

This document provides a complete, incremental implementation plan for Builder Syndicate - an enterprise link aggregation platform. The plan is structured as 96 conventional commits, each representing a discrete, reviewable unit of work that includes:

- Database migrations (Flyway)
- gRPC service implementation (Wire + misk-grpc)
- WebActions JSON adapter
- Misk UI frontend components
- Tests (unit + integration)

## Plan Philosophy

1. **Vertical Slices**: Each commit delivers end-to-end functionality (DB → gRPC → WebActions → UI)
2. **Incremental**: Features build on each other; intermediate states may be incomplete
3. **Steel Thread First**: Core loop (auth → posts → feeds → votes → comments) prioritized
4. **Parallelizable**: Dependency graph enables concurrent work by multiple builders
5. **Conventional Commits**: Each item is a succinct, atomic commit

## Critical Decisions

### Auth Approach
- Use **$MISK_EXT libraries** for local authentication in v1
- Design with **pluggable interface** for future SSO/OIDC support
- No separate "user registration" - users created on first $MISK_EXT auth

### Observability
- Prometheus metrics, health checks, and logging **already provided by Misk**
- No additional observability commits needed

### Out of Scope (v1)
- Deployment automation (systemd/staging handled separately)
- Documentation (README/ARCHITECTURE handled separately)
- CI/CD pipeline

## Steel Thread Path (Critical Path)

**Minimum Viable Product** - 11 commits:
```
F1 → F2 → F3 → A1 → P1 → P7 → V1 → V2 → C1 → C2 → V5
```

This delivers:
- Auth + user management
- Link post creation
- Post feed (New)
- Upvoting posts
- Commenting on posts
- Upvoting comments

From here, all other features are enhancements.

## Full Implementation Plan

See the main plan document for all 96 commits organized in 25 phases:

### Foundation & Core (F, A, P, V, C)
- **F1-F6**: Misk setup, DB, gRPC, UI shell
- **A1**: $MISK_EXT auth + users
- **P1-P7**: Posts (link + markdown) + editing + New feed
- **V1-V8**: Voting (posts + comments) + points ledger
- **C1-C4**: Comments (threaded) + editing

### Discovery & Organization (T, S, FD)
- **T1-T5**: Tags (create, apply, filter, admin)
- **S1-S3**: Search (FULLTEXT, hidden keywords, filters)
- **FD1-FD3**: Ranking (Hot, Top) + pagination

### Content Quality (L, LS, LT, LC)
- **L1-L3**: LLM infrastructure (provider, JobQueue, caching)
- **LS1-LS4**: LLM summaries for posts
- **LT1-LT2**: LLM tag suggestions
- **LC1-LC4**: LLM pre-post checks for comments

### Personalization (PS)
- **PS1-PS5**: Follow tags/authors + Following feed + profile display

### Content Sources (R)
- **R1-R5**: RSS ingestion (sources, polling, dedup)

### Rich Content (I)
- **I1-I9**: Images (upload, EXIF, variants, CDN, moderation, GC)

### Advanced Engagement (SH, RP, G)
- **SH1-SH4**: Share tracking (links, clicks, dedup)
- **RP1-RP4**: Reading progress (DOM marker, scroll %, auto-resume)
- **G1-G3**: Golden feed (evergreen posts)

### Admin & Moderation (AP, AC, AU)
- **AP1-AP4**: Post moderation (lock, pin, delete, restore)
- **AC1-AC2**: Comment moderation (delete, restore)
- **AU1-AU3**: Audit log + UI

### Profile Enhancement (PR)
- **PR1-PR6**: Profile fields (Slack, hub, GitHub, opt-outs, points, best-before)

## Screen Inventory

Each commit specifies required screens/UI updates. Key screens:

1. **Login** - $MISK_EXT auth redirect
2. **Home/Feed** - Post list with tabs (Hot/New/Following/Top/Golden)
3. **Post Detail** - Individual post + threaded comments
4. **Create/Edit Post** - Form for link or markdown posts
5. **User Profile** - Posts, comments, points, followed tags/authors
6. **Search** - Results with tag/author filters
7. **Settings** - Pre-post check toggle, profile opt-outs
8. **Admin Panel** - Moderation tools, RSS sources, audit log, image GC

## Dependency Graph

See `DEPENDENCY_GRAPH.md` for full graph.

### Parallel Work Streams (Suggested Builder Assignment)

**Stream 1: Foundation & Auth** (Critical - 1 builder)
- F1-F6, A1

**Stream 2: Posts Core** (1 builder)
- P1-P7

**Stream 3: Voting & Points** (1 builder)
- V1-V4 (after P7)

**Stream 4: Comments** (1 builder)
- C1-C4 (after P3)
- V5-V8 (after C2 + V1)

**Stream 5: Tags** (1 builder)
- T1-T5

**Stream 6: Search** (1 builder)
- S1-S3 (after P7)

**Stream 7: Feeds** (1 builder)
- FD1-FD3 (after P7 + V2)

**Stream 8: LLM Infrastructure** (1 builder)
- L1-L3

**Stream 9: LLM Summaries** (1 builder)
- LS1-LS4 (after L2, L3, P1)

**Stream 10: LLM Tags** (can merge with Stream 9)
- LT1-LT2 (after L2, L3, T2)

**Stream 11: LLM Comment Checks** (can merge with Stream 9)
- LC1-LC4 (after L2, L3, C1)

**Stream 12: Personalization** (1 builder)
- PS1-PS5 (after T1, A1, P7)

**Stream 13: RSS** (1 builder)
- R1-R5

**Stream 14: Images** (1 builder)
- I1-I9

**Stream 15: Shares** (can merge with Stream 16)
- SH1-SH4 (after P3)

**Stream 16: Reading Progress** (can merge with Stream 15)
- RP1-RP4 (after P3)

**Stream 17: Golden** (can merge with Stream 7)
- G1-G3 (after P1, V2, FD1)

**Stream 18: Admin Posts** (can merge with Stream 2)
- AP1-AP4 (after P5)

**Stream 19: Admin Comments** (can merge with Stream 4)
- AC1-AC2 (after C4)

**Stream 20: Audit** (1 builder, late stage)
- AU1-AU3 (after AP4, AC2, T4, T5)

**Stream 21: Profile Enhancement** (can merge with Stream 1)
- PR1-PR6 (after A1)

### Team Size Recommendations

- **Minimum**: 3-4 builders (serial with some parallelization)
- **Optimal**: 6-8 builders (maximum efficiency)
- **Maximum useful**: ~12 builders (diminishing returns beyond this due to dependencies)

### Wave-Based Parallelization

**Wave 1** (After F1-F6, A1 complete):
- Posts (P1-P7)
- Tags foundation (T1-T2)
- LLM infrastructure (L1-L3)
- RSS foundation (R1)
- Images foundation (I1)

**Wave 2** (After P7 complete):
- Voting (V1-V8)
- Comments (C1-C4)
- Search (S1-S3)
- Feeds (FD1-FD3)
- Shares (SH1-SH4)
- Reading Progress (RP1-RP4)

**Wave 3** (After LLM + features complete):
- LLM Summaries (LS1-LS4)
- LLM Tags (LT1-LT2)
- LLM Comment Checks (LC1-LC4)
- Personalization (PS1-PS5)

**Wave 4** (Polish & Admin):
- Golden (G1-G3)
- Admin Posts (AP1-AP4)
- Admin Comments (AC1-AC2)
- Profile Enhancement (PR1-PR6)

**Wave 5** (Final):
- Audit (AU1-AU3)

## Conventions for Builders

### Commit Message Format
```
<type>: <description>

<optional body>

<optional footer>
```

**Types**: `feat`, `fix`, `chore`, `refactor`, `test`, `docs`

**Examples**:
- `feat: add link post creation with canonical URL resolution`
- `chore: setup database connection with HikariCP and jOOQ`
- `feat: add upvoting for posts with deduplication`

### Code Organization
```
src/
├── main/
│   ├── kotlin/
│   │   └── com/buildersyndicate/
│   │       ├── services/        # gRPC service implementations
│   │       ├── adapters/        # WebActions JSON adapters
│   │       ├── models/          # jOOQ generated + domain models
│   │       ├── jobs/            # JobQueue workers
│   │       ├── llm/             # LLM client + providers
│   │       └── admin/           # Admin UI WebActions
│   ├── resources/
│   │   ├── db/migration/        # Flyway migrations (V001__*, V002__*, etc.)
│   │   ├── llm/prompts/         # LLM prompt templates
│   │   └── web/                 # Misk UI static assets
│   └── proto/
│       └── buildersyndicate/v1/ # Wire protobuf definitions
└── test/
    └── kotlin/
        └── com/buildersyndicate/
            ├── services/        # Service tests
            └── integration/     # Integration tests
```

### Database Migrations
- Sequential numbering: `V001__init_users.sql`, `V002__init_posts.sql`, etc.
- Each commit with schema changes includes ONE migration file
- Use `created_at`, `updated_at` (DATETIME) consistently
- Use `id` (BIGINT AUTO_INCREMENT) for primary keys
- Polymorphic relationships: no FKs (application-level integrity)

### gRPC Services
- One service per domain: `PostsService`, `CommentsService`, `VotesService`, etc.
- Proto3 syntax
- Canonical error codes (see engineering spec §10)
- Request/Response message naming: `{RpcName}Request`, `{RpcName}Response`

### WebActions Adapters
- Path: `/api/v1/{resource}/{action}`
- JSON ↔ Protobuf mapping
- HTTP status codes from gRPC status:
  - `INVALID_ARGUMENT` → 400
  - `NOT_FOUND` → 404
  - `ALREADY_EXISTS` → 409
  - `UNAUTHENTICATED` → 401
  - `PERMISSION_DENIED` → 403
  - `FAILED_PRECONDITION` → 412
  - `UNAVAILABLE` → 503
  - `INTERNAL` → 500

### UI Components (Misk UI)
- React + TypeScript
- Misk UI component library conformance
- Client-side routing (React Router)
- API calls via fetch to WebActions endpoints
- Markdown rendering: use safe CommonMark library
- Accessibility: ARIA labels, keyboard nav

### Testing Requirements
- **Unit tests**: For business logic (ranking, dedup, LLM caching keys)
- **Integration tests**: gRPC service invocation with test DB (Orc)
- **Coverage target**: No strict %, but cover critical paths
- **Test naming**: `{MethodName}_when{Condition}_should{Outcome}`

### LLM Integration
- Prompts in `resources/llm/prompts/{feature}/v{N}.txt`
- Cache key: `{provider}|{model}|{template_version}|{content_hash}`
- Always fail-open with visible warnings
- Retry with exponential backoff (3 attempts)
- Circuit breaker per provider

## Mockup Details by Feature

Each commit in the main plan includes screen descriptions. For detailed mockups, refer to the main plan document's screen sections.

### Key UI Patterns

**Post List Item**:
```
[⬆ 42] Title of the Post                              [3 comments] [share]
       by username • 2 hours ago • tag1, tag2
       AI Summary: This is a brief summary...
```

**Threaded Comments**:
```
┌─ username • 1 hour ago • ⬆ 5
│  Comment text here...
│  [reply] [edit] [delete]
│
│  ┌─ another_user • 30 min ago • ⬆ 2
│  │  Reply text...
│  │  [reply]
```

**Create Post Form**:
```
┌─────────────────────────────────────┐
│ [Link] [Text]                       │  ← Toggle
│                                     │
│ URL: [____________________________] │  (Link mode)
│ Title: [__________________________] │  (auto-filled)
│ Tags: [tag1 ×] [tag2 ×] [+ Add...] │
│ Hidden Keywords: [________________] │
│                                     │
│              [Cancel] [Submit]      │
└─────────────────────────────────────┘
```

## Open Questions for Product Owner

Before starting implementation, clarify:

1. **$MISK_EXT Configuration**: What $MISK_EXT endpoints/config do we use?
2. **Admin Roles**: How are admin users identified in $MISK_EXT? (email domain? specific group?)
3. **LLM Provider**: Which LLM provider for v1? (OpenAI? Anthropic? Local?)
4. **S3 Bucket**: What S3 bucket/region for images?
5. **CDN**: CloudFront distribution or other CDN?
6. **Database**: Aurora MySQL cluster details for dev/staging/prod?
7. **JobQueue**: SQS queue names/ARNs for prod?
8. **Points Values**: How many points for each action? (post creation: 10? upvote received: 5? comment: 2?)
9. **Hot Feed Constants**: Confirm HN ranking constants (gravity = 1.8? time unit = hours?)
10. **Golden Eligibility**: Confirm thresholds (age ≥ 3 months? interactions ≥ 100?)

## Next Steps

1. **Review this plan** with stakeholders
2. **Clarify open questions** above
3. **Assign builders to streams** based on dependency graph
4. **Set up repo** with F1 (Misk init)
5. **Kick off Wave 1** builders in parallel
6. **Daily syncs** to manage handoffs between dependent streams

## Contact

For questions about this plan or implementation guidance:
- Technical questions: [Engineering lead]
- Product questions: [Product owner]
- Dependency conflicts: Refer to `DEPENDENCY_GRAPH.md`
