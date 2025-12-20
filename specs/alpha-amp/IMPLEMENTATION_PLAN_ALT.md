# Builder Syndicate - Implementation Plan

> **Note:** See [DECISIONS.md](DECISIONS.md) for authoritative decisions on scope and behavior.

## Philosophy

1. **Vertical slices**: Each task delivers testable functionality, not scaffolding
2. **LLM-friendly sizing**: Tasks fit in a single agent session (~10-15 files max)
3. **Hexagonal-lite**: Core logic in pure Kotlin; Misk as adapter
4. **Exemplary Misk app**: Showcase patterns, not escape hatches
5. **Micro steel thread first**: Auth → Markdown Posts → Done (10 tasks = working blog)

---

## Milestones

| Milestone | Tasks | What You Get |
|-----------|-------|--------------|
| **Micro Steel Thread** | 10 | Login, markdown posts, feed (working blog) |
| **Link Posts** | 2 | URL posts with canonical dedup |
| **Votes** | 3 | Upvote/undo on posts |
| **Points** | 2 | Author karma system |
| **Comments** | 4 | Threaded discussion |
| **Full Steel Thread** | — | Complete social features |

---

## Task Sizing Guidelines

Each task should:
- Touch ≤15 files (ideal: 5-10)
- Be completable by a sub-agent in one session
- Have clear, testable acceptance criteria
- Follow the vertical slice pattern (entity → repo → migration → adapter → service → API)

If a task grows beyond this, split it.

---

## Package Structure

```
xyz.block.buildersyndicate/
├── core/                      # Pure Kotlin, NO framework imports
│   ├── users/                 # User entity, UserRepository interface
│   ├── posts/                 # Post entity, PostRepository, PostService
│   ├── comments/              # (future)
│   ├── votes/                 # (future)
│   └── ...
├── adapters/                  # Framework integrations
│   ├── db/                    # jOOQ repository implementations
│   └── misk/                  # WebActions, gRPC services, AuthModule
└── app/                       # Wiring only
    ├── BuilderSyndicateService.kt
    └── BuilderSyndicateModule.kt
```

---

# 🎯 MICRO STEEL THREAD (10 tasks)

*Goal: Working blog - login, create markdown posts, view feed*

---

## Phase 0: Bootstrap (3 tasks)

| ID | Title | Description | Status |
|----|-------|-------------|--------|
| B1 | Bootstrap Gradle project | Gradle KTS, Misk deps, Flyway/jOOQ, docker-compose MySQL, Justfile | ✅ GH #4 |
| B2 | Misk app skeleton | Main class, Guice modules, health/metrics endpoints | ✅ GH #5 |
| B3 | React frontend shell | Misk-UI integration, build pipeline, app chrome | |

**B1 includes:** Gradle wrapper, settings, build.gradle.kts, Misk core dependencies, Flyway plugin, jOOQ codegen config, docker-compose.yml, Justfile commands (dev, down, db-migrate, codegen, build, test, run).

**B2 includes:** BuilderSyndicateService (thin main), BuilderSyndicateModule, DatabaseModule wiring, `/_status` and `/metrics` endpoints, placeholder root response.

**B3 includes:** web/ directory with React/TypeScript, Gradle task to build frontend, Misk static asset serving, navigation chrome component.

> **Size hotspots:** B1, B3, A2, and MR1 touch more config/UI than typical tasks. Keep minimal; if they expand beyond ~15 files, split backend vs. frontend.

---

## Phase 1: Auth & Users (2 tasks)

| ID | Title | Description | Blocked By |
|----|-------|-------------|------------|
| A1 | User entity with persistence | User entity, UserRepository, V001 migration, JooqUserRepository | B1 |
| A2 | Dev auth module | Login/logout flow, session management, user picker UI | A1, B3 |

**A1 includes:** `core/users/User.kt` (entity), `core/users/UserRepository.kt` (interface), `V001__create_users_table.sql`, `adapters/db/JooqUserRepository.kt`. Integration tests for CRUD.

**A2 includes:** `adapters/misk/AuthModule.kt` (swappable by enterprise deployers), login WebAction with simple user picker UI, session cookie handling, logout endpoint. Creates user on first login if not exists.

---

## Phase 2: Markdown Posts (3 tasks)

| ID | Title | Description | Blocked By |
|----|-------|-------------|------------|
| MP1 | Post entity with persistence | Post entity, PostRepository, V002 migration, JooqPostRepository | A1 |
| MP2 | PostService with business logic | Create/update/delete logic, MarkdownRenderer, HTML sanitization | MP1 |
| MP3 | Posts gRPC + WebActions | Proto definitions, gRPC service, REST endpoints, auth enforcement | MP2, A2 |

**MP1 includes:** `core/posts/Post.kt`, `core/posts/PostRepository.kt`, `V002__create_posts_table.sql`, `adapters/db/JooqPostRepository.kt`. Integration tests.

**MP2 includes:** `core/posts/PostService.kt`, `core/posts/MarkdownRenderer.kt` (CommonMark → sanitized HTML). Unit tests for rendering and author validation.

**MP3 includes:** `posts.proto`, `adapters/misk/PostsGrpcService.kt`, WebActions at `/api/v1/posts/*`. Tests for auth (401/403) and CRUD operations.

---

## Phase 3: Feed & Create UI (2 tasks)

| ID | Title | Description | Blocked By |
|----|-------|-------------|------------|
| MR1 | Feed and post detail UI | Homepage feed, post list, post detail page | MP3, B3 |
| MR2 | Create post UI | New post form, submit flow, auth guards | MR1 |

**MR1 includes:** Feed component (posts newest-first), post card component, detail page with rendered HTML, React Router setup. Logged-out users can browse and view posts (read-only).

**MR2 includes:** Create post form (title + markdown body), submit to API, redirect on success, "New Post" button (auth-gated).

---

# ✅ MICRO STEEL THREAD COMPLETE

*You now have a working blog. 10 tasks total.*

---

# 🔧 LAYER 1: Link Posts (2 tasks)

| ID | Title | Description | Blocked By |
|----|-------|-------------|------------|
| LP1 | Extend Post for links | Add url/canonical_url fields, CanonicalUrlResolver, V003 migration | MP2 |
| LP2 | Link post UI | Link/markdown toggle in create form, link display in feed | LP1, MR2 |

**LP1 includes:** Extend Post entity, add CanonicalUrlResolver to normalize URLs, migration to add columns, update PostService for link handling (requires PostService from MP2).

**LP2 includes:** Toggle component in create form, conditional rendering in feed (link icon, external link behavior).

---

# 🔧 LAYER 2: Votes (3 tasks)

| ID | Title | Description | Blocked By |
|----|-------|-------------|------------|
| V1 | Vote entity with persistence | Vote entity, VoteRepository, V004 migration, JooqVoteRepository | MP1 |
| V2 | VoteService and API | Upvote/undo logic, VotesService gRPC, WebActions | V1, A2 |
| V3 | Upvote button UI | Toggle component, optimistic updates, vote count display | V2, MR1 |

**V1 includes:** `core/votes/Vote.kt` (polymorphic target: post or comment), `VoteRepository`, migration with unique constraint per user+target.

**V2 includes:** `VoteService` (upvote, undo, check), gRPC service, `/api/v1/votes/*` endpoints.

**V3 includes:** Upvote button component, state management, count display on posts.

---

# 🔧 LAYER 3: Points (2 tasks)

| ID | Title | Description | Blocked By |
|----|-------|-------------|------------|
| PT1 | Points ledger and service | PointsLedger entity, V005 migration, PointsService | V1 |
| PT2 | Integrate points with votes | Award on upvote, deduct on undo, floor at 0, display on profile | PT1, V2 |

**PT1 includes:** `core/points/PointsLedger.kt`, `PointsService` (award, deduct, get total), migration adding `points_total` to users table.

**PT2 includes:** Hook VoteService to PointsService, update user karma transactionally, add points display to any existing profile/user UI.

---

# 🔧 LAYER 4: Comments (4 tasks)

| ID | Title | Description | Blocked By |
|----|-------|-------------|------------|
| C1 | Comment entity with persistence | Comment entity, CommentRepository, V006 migration | MP1 |
| C2 | CommentService and API | Create/edit/delete logic, threading, gRPC + WebActions | C1, A2 |
| C3 | Comment form UI | Reply to post, reply to comment, submit flow | C2, MR1 |
| C4 | Threaded comment display | Recursive rendering, collapse/expand, edit history | C3 |

**C1 includes:** `core/comments/Comment.kt` (with parent_id for threading), `CommentRepository`, migration with indexes.

**C2 includes:** `CommentService` (create, update, soft delete), gRPC service, WebActions, author validation.

**C3 includes:** Comment form component, reply button, submit to API.

**C4 includes:** Threaded display component, collapse state, edit indicator. Optionally: V007 comment_edits table for history.

---

# ✅ FULL STEEL THREAD COMPLETE

*You now have a complete social platform: posts, link posts, votes, comments, points.*
*~21 tasks total.*

---

# 🚀 LAUNCH FEATURES

The following features complete v1. Each is sized as 2-4 tasks following the same vertical slice pattern.

## Tags (3 tasks)
- T1: Tag entity, TagRepository, migration
- T2: TagService, apply/remove/merge, gRPC + WebActions  
- T3: Tag picker UI, filter by tag in feed

## Feeds (2 tasks)
- F1: FeedService with Hot/New/Top algorithms, HotRanking
- F2: Feed tabs UI, infinite scroll, keyset pagination

## Search (2 tasks)
- S1: FULLTEXT index migration, SearchService, gRPC
- S2: Search page UI with filters

## Profiles (3 tasks)
- PR1: Profile fields migration, ProfileService
- PR2: Profile page UI (posts, points, follows)
- PR3: Voting/comment history tabs

## LLM Infrastructure (3 tasks)
- L1: LlmProvider port, cache entity, migration
- L2: OpenAI/Anthropic adapter implementations
- L3: Prompt template system, resources/llm/prompts/

## LLM Features (3 tasks)
- LS1: Summary job for link posts
- LS2: Tag suggestion job
- LS3: Comment tone check (opt-in)

## Images (4 tasks)
- I1: Image entity, S3StorageAdapter
- I2: Upload flow, presigned URLs
- I3: EXIF stripping, variant generation jobs
- I4: Image upload UI, drag & drop

## Admin & Moderation (4 tasks)
- AM1: Lock/pin/soft-delete in PostService + CommentService
- AM2: Admin gRPC endpoints, role checks
- AM3: Audit log entity, migration, AuditService
- AM4: Admin UI (moderation controls, audit viewer)

---

# ⏳ FAST-FOLLOW (Post-Launch)

## Drafts & Scheduled Publishing (3 tasks)
- D1: Draft entity, auto-save, share token
- D2: AI preview (summary + tags before publish)
- D3: Scheduled publishing job

## Dark Mode (1 task)
- DM1: CSS variables, system preference detection, manual toggle

## Dashboard (2 tasks)
- AD1: DashboardStatsService, materialized stats
- AD2: Dashboard UI with charts, export

## RSS Ingestion (3 tasks)
- R1: RssSource entity, RssPollJob
- R2: Admin panel for sources
- R3: External badge on RSS-originated posts

---

## Summary

| Milestone | Tasks | What You Get |
|-----------|-------|--------------|
| 🎯 Micro Steel Thread | 10 | Working blog (login, markdown posts, feed) |
| 🔧 Full Steel Thread | ~21 | + Link posts, votes, comments, points |
| 🚀 Launch | ~45 | All v1 features |
| ⏳ Fast-Follow | ~54 | + Drafts, dark mode, dashboard, RSS |

---

## Key Differences from Original Fine-Grained Plan

| Aspect | Original | Current |
|--------|----------|---------|
| Task granularity | Single commits (144+) | Vertical slices (~54) |
| Task sizing | Very small, scaffolding-heavy | LLM-session-sized (~10 files) |
| Testability | Some tasks untestable alone | Every task has test criteria |
| Dependencies | Complex web | Clear linear + parallel paths |
