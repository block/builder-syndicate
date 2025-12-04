# Builder Syndicate - Alternate Implementation Plan

> **Note:** See [DECISIONS.md](DECISIONS.md) for authoritative decisions on scope and behavior.

## Philosophy

1. **LLM-friendly commits**: Small, single-purpose, fit in context window
2. **Hexagonal-lite**: Core logic in pure Kotlin packages; Misk as adapter
3. **Exemplary Misk app**: Showcase patterns, not escape hatches
4. **Micro steel thread first**: Auth → Markdown Posts → Done (19 commits = working blog)
5. **Layer incrementally**: Link posts → Votes → Comments → Tags → etc.
6. **Fast-follows separate**: Drafts, preview, dark mode, dashboard, RSS are post-launch

---

## Milestones

| Milestone | Commits | Cumulative | What You Get |
|-----------|---------|------------|--------------|
| **Micro Steel Thread** | 19 | 19 | Login, markdown posts, feed (blog) |
| **Link Posts** | 4 | 23 | URL posts with canonical dedup |
| **Votes** | 7 | 30 | Upvote/undo posts |
| **Points** | 4 | 34 | Gamification |
| **Comments** | 11 | 45 | Threaded discussion |
| **Full Steel Thread** | — | 45 | Complete social features |
| **Launch** | 100 | 145 | All v1 features |
| **Fast-Follow** | 17 | 162 | Drafts, dark mode, dashboard |

---

## Package Structure

```
com.buildersyndicate/
├── core/                      # Pure Kotlin, no framework imports
│   ├── posts/
│   │   ├── Post.kt           # Entity
│   │   ├── PostRepository.kt # Interface (port)
│   │   └── PostService.kt    # Business logic
│   ├── comments/
│   ├── votes/
│   ├── tags/
│   ├── users/
│   ├── feeds/
│   ├── search/
│   ├── shares/
│   ├── images/
│   ├── rss/
│   └── llm/
├── adapters/                  # Adapters (Misk, MySQL, S3, LLM providers)
│   ├── db/                   # jOOQ repositories
│   ├── misk/                 # Misk modules, WebActions, gRPC
│   ├── storage/              # S3 adapter
│   └── llm/                  # OpenAI/Anthropic adapters
└── app/                       # Wiring, main, config
```

---

## Phases

---

# 🎯 MICRO STEEL THREAD (19 commits)

*Goal: Working blog - login, create markdown posts, view feed*

---

### Phase 0: Bootstrap (6 commits)

| ID | Commit | Description |
|----|--------|-------------|
| B1 | `chore: init gradle kts project` | Gradle wrapper, settings, root build.gradle.kts |
| B2 | `chore: add misk dependencies` | Misk core, misk-hibernate, misk-grpc, Wire |
| B3 | `chore: add flyway and jooq` | DB tooling, codegen config |
| B4 | `chore: add orc local dev config` | Orc + Justfile for local MySQL |
| B5 | `feat: add misk app skeleton` | Main class, health endpoint, metrics |
| B6 | `feat: add misk-ui shell` | Empty React app, build integration |

---

### Phase 1: Auth & Users (5 commits)

| ID | Commit | Description |
|----|--------|-------------|
| A1 | `feat(core): add User entity and UserRepository port` | Pure Kotlin |
| A2 | `feat(db): V001 users table migration` | Flyway + jOOQ codegen |
| A3 | `feat(adapters): add JooqUserRepository` | Implements port |
| A4 | `feat(misk): add $MISK_EXT auth module` | Login, callback, session |
| A5 | `feat(misk): add logout endpoint` | Current session only |

---

### Phase 2: Markdown Posts (6 commits)

| ID | Commit | Description |
|----|--------|-------------|
| MP1 | `feat(core): add Post entity (markdown only)` | Pure Kotlin, no link type yet |
| MP2 | `feat(core): add PostRepository port` | CRUD interface |
| MP3 | `feat(db): V002 posts table migration` | Schema + indexes |
| MP4 | `feat(adapters): add JooqPostRepository` | Implements port |
| MP5 | `feat(core): add PostService` | Create, update, delete logic |
| MP6 | `feat(misk): add PostsService gRPC + WebActions` | Proto + JSON endpoints |

---

### Phase 3: Markdown Rendering + UI (2 commits)

| ID | Commit | Description |
|----|--------|-------------|
| MR1 | `feat(core): add MarkdownRenderer + HtmlSanitizer` | CommonMark + safe HTML |
| MR2 | `feat(ui): add create post form + detail + feed` | Full markdown post UI |

---

# ✅ MICRO STEEL THREAD COMPLETE
*You now have a working blog. Document the build process here!*

---

# 🔧 LAYER 1: Link Posts (4 commits)

### Phase 4: Link Posts (4 commits)

| ID | Commit | Description |
|----|--------|-------------|
| LP1 | `feat(core): extend Post entity with link type` | Add url, canonical_url |
| LP2 | `feat(core): add CanonicalUrlResolver` | URL normalization logic |
| LP3 | `feat(db): V003 add link columns to posts` | Schema migration |
| LP4 | `feat(ui): add link post toggle to create form` | Link/markdown switch |

---

# 🔧 LAYER 2: Votes (7 commits)

### Phase 5: Votes (7 commits)

| ID | Commit | Description |
|----|--------|-------------|
| V1 | `feat(core): add Vote entity` | Polymorphic target |
| V2 | `feat(core): add VoteRepository port` | Upsert, remove, exists |
| V3 | `feat(db): V004 votes table migration` | Unique constraint |
| V4 | `feat(adapters): add JooqVoteRepository` | With counter update |
| V5 | `feat(core): add VoteService` | Upvote + undo logic |
| V6 | `feat(misk): add VotesService gRPC` | Upvote, RemoveUpvote |
| V7 | `feat(ui): add upvote button component` | Toggle state |

---

# 🔧 LAYER 3: Points (4 commits)

### Phase 6: Points (4 commits)

| ID | Commit | Description |
|----|--------|-------------|
| PT1 | `feat(core): add PointsLedger and PointsService` | Award/deduct, floor at 0 |
| PT2 | `feat(db): V005 points_ledger + users.points_total` | Schema |
| PT3 | `feat(adapters): add JooqPointsRepository` | Transactional updates |
| PT4 | `feat(core): integrate points with VoteService` | Award on upvote, deduct on undo |

---

# 🔧 LAYER 4: Comments (11 commits)

### Phase 7: Comments (8 commits)

| ID | Commit | Description |
|----|--------|-------------|
| C1 | `feat(core): add Comment entity` | Threaded, parent_id |
| C2 | `feat(core): add CommentRepository port` | CRUD, list by post |
| C3 | `feat(db): V006 comments table migration` | Indexes for threading |
| C4 | `feat(adapters): add JooqCommentRepository` | Threaded fetch |
| C5 | `feat(core): add CommentService` | Create, edit, soft delete |
| C6 | `feat(misk): add CommentsService gRPC` | CRUD + threading |
| C7 | `feat(ui): add comment form` | Reply to post or comment |
| C8 | `feat(ui): add threaded comment display` | Collapsible threads |

---

### Phase 8: Comment Edits (3 commits)

| ID | Commit | Description |
|----|--------|-------------|
| CE1 | `feat(db): V007 comment_edits table` | History tracking |
| CE2 | `feat(core): add edit history to CommentService` | Store diffs |
| CE3 | `feat(ui): add edit history viewer` | Show previous versions |

---

# ✅ FULL STEEL THREAD COMPLETE (45 commits)
*You now have a complete social platform: posts, votes, comments, points*

---

# 🚀 LAUNCH FEATURES

### Phase 9: Post Edits (3 commits)

| ID | Commit | Description |
|----|--------|-------------|
| PE1 | `feat(db): V008 post_edits table` | History tracking |
| PE2 | `feat(core): add edit history to PostService` | Store diffs |
| PE3 | `feat(ui): add post edit history viewer` | Show previous versions |

---

### Phase 10: Tags (7 commits)

| ID | Commit | Description |
|----|--------|-------------|
| T1 | `feat(core): add Tag entity and TagRepository port` | Open creation |
| T2 | `feat(db): V008 tags + post_tags tables` | Schema |
| T3 | `feat(adapters): add JooqTagRepository` | CRUD + aliases |
| T4 | `feat(core): add TagService` | Create, apply, merge, rename |
| T5 | `feat(misk): add TagsService gRPC` | All tag operations |
| T6 | `feat(ui): add tag picker component` | Autocomplete + create |
| T7 | `feat(ui): add tag filter to feeds` | Filter posts by tag |

---

### Phase 10: Feeds (6 commits)

| ID | Commit | Description |
|----|--------|-------------|
| F1 | `feat(core): add FeedService` | Hot, New, Top logic |
| F2 | `feat(core): add HotRanking algorithm` | HN-style decay |
| F3 | `feat(misk): add feeds to PostsService.ListPosts` | FeedKind enum |
| F4 | `feat(ui): add feed tabs` | Hot, New, Top (24h/7d/30d) |
| F5 | `feat(ui): add infinite scroll` | Keyset pagination |
| F6 | `feat(ui): persist scroll position` | LocalStorage + restore |

---

### Phase 11: Search (5 commits)

| ID | Commit | Description |
|----|--------|-------------|
| S1 | `feat(db): V009 FULLTEXT index on posts` | title, summary, hidden_keywords |
| S2 | `feat(core): add SearchService` | Boolean mode, exact phrases |
| S3 | `feat(adapters): add JooqSearchRepository` | FULLTEXT queries |
| S4 | `feat(misk): add SearchService gRPC` | With filters |
| S5 | `feat(ui): add search page` | Query + tag/author filters + expired toggle |

---

### Phase 12: Personalization (6 commits)

| ID | Commit | Description |
|----|--------|-------------|
| PS1 | `feat(db): V010 user_follows table` | Tags + authors |
| PS2 | `feat(core): add FollowService` | Follow/unfollow |
| PS3 | `feat(core): add Following feed to FeedService` | Union of followed |
| PS4 | `feat(misk): add follow endpoints` | gRPC + WebActions |
| PS5 | `feat(ui): add follow buttons` | On tags and profiles |
| PS6 | `feat(ui): add Following feed tab` | Personal feed |

---

### Phase 13: Profiles (6 commits)

| ID | Commit | Description |
|----|--------|-------------|
| PR1 | `feat(db): V011 profile fields on users` | slack, hub, github, opt_outs |
| PR2 | `feat(core): add ProfileService` | Get, update, opt-outs |
| PR3 | `feat(misk): add profile endpoints` | gRPC + WebActions |
| PR4 | `feat(ui): add profile page` | Posts, points, follows |
| PR5 | `feat(ui): add voting history tab` | User's upvotes |
| PR6 | `feat(ui): add comment history tab` | User's comments |

---

### Phase 14: LLM Infrastructure (5 commits)

| ID | Commit | Description |
|----|--------|-------------|
| L1 | `feat(core): add LlmProvider port` | Complete, summarize, suggest |
| L2 | `feat(adapters): add OpenAiLlmProvider` | HTTP client |
| L3 | `feat(core): add LlmCache` | Provider+model+template+hash key |
| L4 | `feat(db): V012 llm_cache table` | Cached responses |
| L5 | `feat(adapters): add prompt templates` | resources/llm/prompts/ |

---

### Phase 15: LLM Summaries (4 commits)

| ID | Commit | Description |
|----|--------|-------------|
| LS1 | `feat(core): add SummaryJob` | Async generation |
| LS2 | `feat(misk): add SummaryJobHandler` | JobQueue integration |
| LS3 | `feat(core): enqueue summary on post create` | Trigger job |
| LS4 | `feat(ui): display summary on post` | With loading state |

---

### Phase 16: LLM Tag Suggestions (3 commits)

| ID | Commit | Description |
|----|--------|-------------|
| LT1 | `feat(core): add TagSuggestionJob` | Async generation |
| LT2 | `feat(misk): add TagSuggestionJobHandler` | JobQueue integration |
| LT3 | `feat(core): enqueue tag suggestions on post create` | Trigger job |

---

### Phase 17: LLM Comment Checks (5 commits)

| ID | Commit | Description |
|----|--------|-------------|
| LC1 | `feat(core): add CommentCheckService` | Answer detection, tone |
| LC2 | `feat(db): V013 user_preferences table` | check_enabled flag |
| LC3 | `feat(core): integrate checks in CommentService` | Fail-open |
| LC4 | `feat(ui): add pre-post check feedback` | Warnings before submit |
| LC5 | `feat(ui): add check toggle in settings` | Per-user opt-out |

---

### ~~Phase 18: RSS Ingestion~~ — DEFERRED TO POST-V1

> **Decision:** RSS ingestion is deferred to post-v1. See [DECISIONS.md](DECISIONS.md#9-rss-posts--defer-to-post-v1).

~~| ID | Commit | Description |~~
~~|----|--------|-------------|~~
~~| R1 | `feat(db): V014 rss_sources table` | Feed URLs, enabled, last_polled |~~
~~| R2 | `feat(core): add RssSource and RssService` | Parse, dedup |~~
~~| R3 | `feat(adapters): add JooqRssSourceRepository` | CRUD |~~
~~| R4 | `feat(core): add RssPollJob` | Fetch + ingest |~~
~~| R5 | `feat(misk): add IngestionAdminService gRPC` | Source CRUD, trigger |~~
~~| R6 | `feat(ui): add RSS admin panel` | Sources + poll now button |~~

---

### ~~Phase 19: External Post Indicator~~ — DEFERRED TO POST-V1

> **Decision:** Depends on RSS; deferred. See [DECISIONS.md](DECISIONS.md#9-rss-posts--defer-to-post-v1).

~~| ID | Commit | Description |~~
~~|----|--------|-------------|~~
~~| EX1 | `feat(db): V015 add source_type to posts` | internal, rss, external |~~
~~| EX2 | `feat(ui): add "External" badge` | Visual indicator |~~

---

### Phase 20: Shares (5 commits)

| ID | Commit | Description |
|----|--------|-------------|
| SH1 | `feat(db): V016 share_links + share_clicks` | Schema |
| SH2 | `feat(core): add ShareService` | Create link, record click |
| SH3 | `feat(adapters): add JooqShareRepository` | First-click dedup |
| SH4 | `feat(misk): add SharesService gRPC` | Endpoints |
| SH5 | `feat(ui): add share button with copy link` | UI component |

---

### Phase 21: Images (11 commits)

> **Decisions:** Comment images in v1 (lower priority). GC has grace period; soft-deleted content images protected. See [DECISIONS.md](DECISIONS.md).

| ID | Commit | Description |
|----|--------|-------------|
| I1 | `feat(core): add Image entity and ImageRepository port` | Metadata |
| I2 | `feat(db): V017 images + post_images + comment_images tables` | Schema (comment_images lower priority) |
| I3 | `feat(adapters): add S3StorageAdapter` | Presigned URLs |
| I4 | `feat(core): add ImageService` | Upload flow |
| I5 | `feat(core): add ExifStripJob` | Remove metadata |
| I6 | `feat(core): add VariantGenerationJob` | Responsive sizes |
| I7 | `feat(misk): add ImagesService gRPC` | Upload, finalize |
| I8 | `feat(ui): add image upload to post form` | Drag & drop |
| I9 | `feat(misk): add image GC admin trigger` | Manual cleanup with grace period |
| I10 | `feat(core): add GC grace period logic` | Skip orphans < 24h old |
| I11 | `feat(core): protect soft-deleted content images` | GC respects soft-delete |

---

### Phase 22: Reading Progress (4 commits)

| ID | Commit | Description |
|----|--------|-------------|
| RP1 | `feat(db): V018 reading_progress table` | DOM marker, scroll % |
| RP2 | `feat(core): add ReadingProgressService` | Update, mark read |
| RP3 | `feat(misk): add ReadingProgressService gRPC` | Endpoints |
| RP4 | `feat(ui): add scroll tracking + resume` | Client instrumentation |

---

### Phase 23: Golden Feed (4 commits)

| ID | Commit | Description |
|----|--------|-------------|
| G1 | `feat(core): add evergreen eligibility logic` | Age + interactions |
| G2 | `feat(core): add Golden feed to FeedService` | Filtered query |
| G3 | `feat(misk): add admin demote endpoint` | Remove from golden |
| G4 | `feat(ui): add Golden feed tab` | Evergreen content |

---

### Phase 24: Best-Before / Expiration (4 commits)

| ID | Commit | Description |
|----|--------|-------------|
| BB1 | `feat(core): add best_before logic to PostService` | Expiration check |
| BB2 | `feat(ui): add best-before picker to post form` | Date input |
| BB3 | `feat(ui): add expired indicator on posts` | Visual badge |
| BB4 | `feat(ui): add extend best-before for authors` | Edit date |

---

### Phase 25: Admin Moderation (8 commits)

| ID | Commit | Description |
|----|--------|-------------|
| AM1 | `feat(core): add lock/pin to PostService` | Moderation flags |
| AM2 | `feat(misk): add LockPost, PinPost gRPC` | Admin endpoints |
| AM3 | `feat(core): add soft delete/restore` | Posts + comments |
| AM4 | `feat(misk): add delete/restore gRPC` | Admin endpoints |
| AM5 | `feat(ui): add moderation controls` | Lock, pin, delete buttons |
| AM6 | `feat(ui): show deleted content to mods` | Inline indicator |
| AM7 | `feat(core): add PostMergeService` | Combine duplicates |
| AM8 | `feat(ui): add merge posts dialog` | Admin tool |

---

### Phase 26: Audit Log (4 commits)

| ID | Commit | Description |
|----|--------|-------------|
| AU1 | `feat(db): V019 audit_log table` | Immutable log |
| AU2 | `feat(core): add AuditService` | Log admin actions |
| AU3 | `feat(adapters): integrate audit with moderation` | Auto-log |
| AU4 | `feat(ui): add audit log viewer` | Admin panel |

---

### Phase 27: Admin Impersonation (3 commits)

| ID | Commit | Description |
|----|--------|-------------|
| AI1 | `feat(misk): add impersonation session handling` | Switch user context |
| AI2 | `feat(core): audit impersonation start/end` | Logged |
| AI3 | `feat(ui): add impersonation banner` | Visual indicator |

---

## Fast-Follow Features (Post-Launch)

### FF1: Drafts & Preview (8 commits)

> **Decision:** Drafts and AI preview are fast-follow, not v1 launch. See [DECISIONS.md](DECISIONS.md).

| ID | Commit | Description |
|----|--------|-------------|
| D1 | `feat(db): V020 drafts table` | With share_token |
| D2 | `feat(core): add Draft entity and DraftService` | CRUD, auto-save |
| D3 | `feat(misk): add DraftsService gRPC` | Endpoints |
| D4 | `feat(ui): add draft editor with auto-save` | Indicator |
| D5 | `feat(core): add AI preview to DraftService` | Fetch summary + tags |
| D6 | `feat(ui): add preview panel` | Show AI output, allow edits |
| D7 | `feat(core): add draft sharing` | Token-based access |
| D8 | `feat(ui): add share draft modal` | Copy link |

### FF2: Scheduled Publishing (3 commits)

| ID | Commit | Description |
|----|--------|-------------|
| SC1 | `feat(core): add scheduled_at to drafts` | Publish timing |
| SC2 | `feat(core): add ScheduledPublishJob` | Cron job |
| SC3 | `feat(ui): add schedule picker` | Date/time input |

### FF3: Dark Mode (3 commits)

> **Decision:** Auto-detect + manual toggle override. See [DECISIONS.md](DECISIONS.md).

| ID | Commit | Description |
|----|--------|-------------|
| DM1 | `feat(ui): add dark mode CSS variables` | Theme tokens |
| DM2 | `feat(ui): add system preference detection` | Auto-switch |
| DM3 | `feat(ui): add manual dark mode toggle` | User override + persistence |

### FF4: Admin Dashboard (4 commits)

| ID | Commit | Description |
|----|--------|-------------|
| AD1 | `feat(core): add DashboardStatsService` | Aggregate queries |
| AD2 | `feat(db): add materialized stats table` | Cached daily |
| AD3 | `feat(ui): add dashboard with charts` | Posts/day, users, etc. |
| AD4 | `feat(ui): add export button` | CSV/JSON download |

### FF5: RSS Ingestion (8 commits) — Moved from Launch

> **Decision:** RSS deferred to post-v1. See [DECISIONS.md](DECISIONS.md).

| ID | Commit | Description |
|----|--------|-------------|
| R1 | `feat(db): V021 rss_sources table` | Feed URLs, enabled, last_polled |
| R2 | `feat(core): add RssSource and RssService` | Parse, dedup |
| R3 | `feat(adapters): add JooqRssSourceRepository` | CRUD |
| R4 | `feat(core): add RssPollJob` | Fetch + ingest |
| R5 | `feat(misk): add IngestionAdminService gRPC` | Source CRUD, trigger |
| R6 | `feat(ui): add RSS admin panel` | Sources + poll now button |
| R7 | `feat(db): V022 add source_type to posts` | internal, rss, external |
| R8 | `feat(ui): add "External" badge` | Visual indicator |

---

## Commit Count Summary

### 🎯 Micro Steel Thread (19 commits)
| Phase | Commits | Cumulative |
|-------|---------|------------|
| Bootstrap | 6 | 6 |
| Auth & Users | 5 | 11 |
| Markdown Posts | 6 | 17 |
| Markdown Rendering + UI | 2 | 19 |
| **MICRO STEEL THREAD** | — | **19** |

### 🔧 Layers to Full Steel Thread (+26 commits)
| Phase | Commits | Cumulative |
|-------|---------|------------|
| Link Posts | 4 | 23 |
| Votes | 7 | 30 |
| Points | 4 | 34 |
| Comments | 8 | 42 |
| Comment Edits | 3 | 45 |
| **FULL STEEL THREAD** | — | **45** |

### 🚀 Launch Features (+91 commits)
| Phase | Commits | Cumulative |
|-------|---------|------------|
| Post Edits | 3 | 48 |
| Tags | 7 | 55 |
| Feeds | 6 | 61 |
| Search | 5 | 66 |
| Personalization | 6 | 72 |
| Profiles | 6 | 78 |
| LLM Infra | 5 | 83 |
| LLM Summaries | 4 | 87 |
| LLM Tags | 3 | 90 |
| LLM Comment Checks | 5 | 95 |
| ~~RSS~~ | ~~6~~ | ~~101~~ | *Deferred* |
| ~~External Indicator~~ | ~~2~~ | ~~103~~ | *Deferred* |
| Shares | 5 | 100 |
| Images | 11 | 111 |
| Reading Progress | 4 | 115 |
| Golden | 4 | 119 |
| Best-Before | 4 | 123 |
| Admin Moderation | 8 | 131 |
| Audit | 4 | 135 |
| Impersonation | 3 | 138 |
| **LAUNCH** | — | **~138** |

### ⏳ Fast-Follow (+26 commits)
| Phase | Commits | Cumulative |
|-------|---------|------------|
| FF1: Drafts & Preview | 8 | 146 |
| FF2: Scheduled | 3 | 149 |
| FF3: Dark Mode | 3 | 152 |
| FF4: Dashboard | 4 | 156 |
| FF5: RSS | 8 | 164 |
| **FULL** | — | **~164** |

---

## Summary

| Milestone | Commits | What You Get |
|-----------|---------|--------------|
| 🎯 Micro Steel Thread | 19 | Working blog (login, markdown posts, feed) |
| 🔧 Full Steel Thread | 45 | Social platform (+ link posts, votes, comments, points) |
| 🚀 Launch | ~138 | All v1 features (RSS deferred) |
| ⏳ Fast-Follow | ~164 | + Drafts, preview, dark mode, dashboard, RSS |

---

## Key Differences from Original Plan

| Aspect | Original | Alternate |
|--------|----------|-----------|
| Commit count | 96 | 144 (launch) / 161 (full) |
| Commit size | Larger, vertical slices | Smaller, single-purpose |
| Steel thread | 35 commits | 19 commits (micro) / 45 (full) |
| Architecture | Implicit in Misk | Explicit hexagonal packages |
| Domain purity | Mixed with framework | Pure Kotlin domain layer |
| Gaps coverage | Missing items | Includes all identified gaps |
| Fast-follows | Mixed in | Separated post-launch |
| LLM-friendliness | Standard | Optimized for context windows |
