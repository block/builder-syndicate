# Builder Syndicate Engineering Specification (v1.1)

Status: Draft (developer-ready) — gRPC-first revision with gap analysis updates

## 1. Overview
Builder Syndicate is an OSS-first, enterprise-focused link and knowledge aggregation platform for internal communities. This project serves as an exemplary Misk reference implementation. v1 ships as a single Misk-based JVM monolith with:
- gRPC-first domain services (misk-grpc + Wire-generated stubs)
- A Misk-UI–conformant browser frontend
- Aurora MySQL via jOOQ + Flyway
- Enterprise SSO/OIDC (all actions require login)
- Hexagonal-lite architecture: pure Kotlin core layer, Misk as adapter

Transport model (v1):
- Internals: gRPC services across domains (Posts, Comments, Feeds, Search, Tags, Shares, Images, IngestionAdmin, LlmAdmin, Admin)
- Browser UI: Thin JSON WebActions adapter for the UI that delegates to gRPC services
  - Rationale: keeps v1 infra simple (no Envoy/gRPC‑Web). v2+ can move to gRPC‑Web behind Envoy if desired without changing core services.

Primary objectives:
- High-signal discovery/discussion with upvotes-only and time-decay ranking
- Low-friction ingest (RSS/Atom), canonical-URL dedup, quality LLM summaries
- Enterprise-ready auth/RBAC, observability, predictable deploys (shaded JAR + systemd)
- Exemplary Misk patterns for community reference

## 2. Scope (summarized)
- In-scope (v1):
  - Misk monolith; Misk UI; gRPC-first domain services; UI adapter via WebActions
  - Aurora MySQL; jOOQ + Flyway; denormalized counters + periodic repair
  - SSO/OIDC required for all actions; RBAC via Misk; logout (current session)
  - Link posts + self-hosted Markdown posts (pre-render sanitized HTML)
  - Canonical-URL resolution & dedup; duplicate submission returns existing post with `was_duplicate: true` (200 response, not error)
  - LLM: async summaries + tag suggestions; provider-pluggable; prompts versioned in-repo; cached with metadata; retries/backoff; fail-open; admin can override evergreen flag
  - Threaded comments; edit history & soft-delete; collapsible with per-user state persistence
  - Pre-post checks (answer/tone) default-on with per-user toggle + admin override; fail-open
  - Upvotes-only; undo upvote supported; points deducted on undo; floor at zero
  - Ranking: HN-style time decay; Hot limited to last 30 days; All-time Golden = evergreen + (age ≥ 3 months OR interactions ≥ 100); admin demotion
  - Personalization: follow tags/authors; Following feed
  - Search: MySQL FULLTEXT over titles/summaries/hidden_keywords (no body); exact phrases; boolean operators; malformed queries fall back to simple text search; expired posts toggle (exclude expired from feeds; search/profile only)
  - Votes: upvotes-only; uniqueness enforced; single polymorphic table (no FKs); voting history visible to self only
  - Shares: share links with tokens; click tracking (first landing per user per post); capture referrer + user agent pre-auth (no IP)
  - Images: S3 presigned uploads; CDN front; EXIF stripping; responsive variants via background job; pluggable moderation module (noop base); manual-trigger GC via admin action with grace period; images on soft-deleted content protected until hard delete; comment images included (lower priority)
  - Reading progress (self-hosted posts only): DOM last_position_marker + max_scroll_pct; mark read at ≥ 80%; auto-resume
  - Profiles: show Slack/hub/GitHub; followed tags/authors; public points; single "private profile" toggle (not per-field); voting history (self-only); comment history
  - Best-before/expiration: configurable per post; visible on profile and search (with toggle); authors can extend
  - Admin: lock/pin posts; soft delete/restore; merge duplicate posts; impersonation (logged); deleted content visible inline to mods
  - Observability: Prometheus metrics/health; tracing if available; metadata-only logs with PII scrubbing
  - Environments: local (Orc), staging, prod (shaded JAR + systemd); SemVer releases + annotated tags; CHANGELOG from Conventional Commits
  - Feeds: Hot, New, Following, Top (24h/7d/30d), Golden; infinite scroll; scroll position persisted across sessions; expired posts excluded from feeds
- Fast-follow (post-launch): Drafts with auto-save and sharing; scheduled publishing; dark mode (system auto-detect + manual toggle); admin dashboard with stats and export; post preview before publish with AI-generated summary/tags (author can edit before submit); RSS/Atom ingest with sources in DB + admin UI; "External" badge on imported posts
- Out-of-scope (v1): Email ingestion; Slack notifications; server-side caches; CI/CD automation; Docker/Kubernetes; user flag/report; external search engine; backup/PITR runbooks; emoji reactions; indexing post body content; account deletion; user blocking; data export; in-app notifications; mobile-responsive; rate limits

## 3. System Architecture
- Monolith (Misk)
  - Modules: gRPC Domain Services, UI WebActions adapter, Database (Hikari + jOOQ + Flyway), Auth/RBAC, JobQueue, LLM Client, RSS Ingestion, Image Pipeline, Search, Admin UI
  - Serves static frontend (Misk UI–conformant)
- Data stores: Aurora MySQL (InnoDB); S3-compatible object storage for images (CDN fronted)
- Jobs: Misk JobQueue abstraction (SQS prod; InMemory/LocalStack dev)
- External: Third-party LLM APIs; RSS/Atom feeds per DB-configured sources
- Environments: Local via Orc; staging/prod via shaded JAR + systemd; config via Misk Secrets (AWS Secrets Manager in staging/prod)

## 4. Package Structure (Hexagonal-lite)

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

## 4.1 Modular Composition Architecture

Builder Syndicate supports two deployment modes:
1. **Standalone deployment** — Use the default module bundle as-is
2. **Enterprise deployment** — Import as a library and compose custom module lists

### Design Principles

**Composition over configuration.** Internal teams import the OSS project as a library and compose their own module list without modifying OSS source code.

### Entry Point Requirements

The `main()` function MUST be thin:
```kotlin
fun main(args: Array<String>) {
    val modules = listOf(
        DefaultBuilderSyndicateModule(),  // or custom module list
        // ... additional modules
    )
    MiskApplication.start(modules)
}
```

- All setup logic MUST be encapsulated in Guice modules, NOT in `main()`
- `main()` only instantiates modules and passes to `MiskApplication.start()`

### Visibility Requirements

All Guice modules, service classes, actions, and core business logic MUST have **public visibility**:
- All Guice `KAbstractModule` subclasses: `public`
- Service classes: `public`
- WebActions and gRPC service implementations: `public`
- Repository interfaces (ports) and implementations: `public`
- Entity/domain classes: `public`

Avoid `internal` or `private` visibility on anything needed for module composition.

### Default Module Bundle

The OSS repo ships with `DefaultBuilderSyndicateModule` that wires all default implementations:
```kotlin
class DefaultBuilderSyndicateModule : KAbstractModule() {
    override fun configure() {
        install(CoreModule())
        install(DatabaseModule())        // jOOQ + Aurora MySQL
        install(AuthModule())            // SSO/OIDC
        install(LlmModule())             // OpenAI/Anthropic
        install(StorageModule())         // S3
        install(JobQueueModule())        // Misk JobQueue
        install(WebModule())             // WebActions + gRPC
        install(AdminModule())
    }
}
```

### Swappable Subsystems

Enterprise deployments may override any of these Guice modules:
| Module | Purpose | Override Example |
|--------|---------|------------------|
| `AuthModule` | SSO/OIDC provider | Corporate SAML, custom IdP |
| `DatabaseModule` | Repository bindings | Different DB, custom sharding |
| `LlmModule` | LLM provider bindings | Internal LLM, different vendor |
| `StorageModule` | Image/file storage | Internal object store, GCS |
| `JobQueueModule` | Async job execution | Custom queue, Kafka |

### Enterprise Composition Example

```kotlin
fun main(args: Array<String>) {
    val modules = listOf(
        // Use OSS core + web, but swap infra
        CoreModule(),
        WebModule(),
        AdminModule(),
        
        // Enterprise overrides
        CorporateSamlAuthModule(),
        InternalLlmModule(),
        CustomStorageModule(),
        KafkaJobQueueModule(),
        
        // Enterprise additions
        SlackIntegrationModule(),
        InternalAnalyticsModule(),
    )
    MiskApplication.start(modules)
}
```

### Module Design Guidelines

1. **Single responsibility** — Each module handles one subsystem
2. **Interface-first bindings** — Modules bind interfaces to implementations; consumers depend on interfaces
3. **No hidden state** — All configuration via Misk Config; no static singletons
4. **Testable in isolation** — Modules can be installed independently in tests

## 5. Tech Stack & Dependencies
- Kotlin on JVM; Misk; misk-grpc; Wire (Protobufs)
- Build: Gradle Kotlin DSL
- DB: Aurora MySQL; HikariCP; jOOQ codegen; Flyway migrations
- Jobs: Misk JobQueue (SQS prod; InMemory dev)
- LLM: pluggable HTTP client; prompts versioned in-repo
- UI: Misk UI guidance and components; WebActions adapter calls gRPC services
- Storage/CDN: S3 + CloudFront (or equivalent)
- Observability: Prometheus metrics; tracing (OpenTelemetry if available in Misk)

## 6. Data Model (tables, key fields)
Polymorphic relationships avoid explicit FKs for scalability; integrity enforced in application.

### Core Tables
- **users**: id, idp_sub (unique), email, name, avatar_url, slack_handle, hub_link, github_handle, profile_opt_outs, points_total, created_at, updated_at
- **posts**: id, type(link|markdown), source_type(internal|rss|external), title, url?, canonical_url(unique for links), markdown_body?, html_rendered?, summary_html?, best_before_at?, hidden_keywords(FULLTEXT), author_user_id, is_locked, is_deleted, is_pinned, is_evergreen, counters(upvote_count, comment_count, share_clicks_count), created_at, updated_at
- **post_edits**: id, post_id, editor_user_id, prev_html, new_html, diff?, edited_at
- **comments**: id, post_id, author_user_id, parent_comment_id?, markdown_body, html_rendered, is_deleted, upvote_count, created_at, updated_at
- **comment_edits**: id, comment_id, editor_user_id, prev_html, new_html, diff?, edited_at
- **tags**: id, name(unique), alias_to_tag_id?, created_at
- **post_tags**: post_id, tag_id, created_at (PK over (post_id, tag_id))
- **votes**: id, user_id, target_type(post|comment), target_id, created_at; unique(user_id, target_type, target_id)
- **points_ledger**: id, user_id, action_type, amount, target_type?, target_id?, created_at
- **share_links**: id, post_id, token(unique), creator_user_id?, created_at
- **share_clicks**: id, share_link_id, post_id, user_id?, referrer?, user_agent?, first_for_user(bool), created_at
- **reading_progress**: id, user_id, post_id, dom_marker, max_scroll_pct, updated_at
- **images**: id, uploader_user_id, s3_key, content_type, size_bytes?, exif_stripped_at?, variants_json, created_at
- **post_images**: post_id, image_id, position, created_at (PK over (post_id, image_id))
- **comment_images**: comment_id, image_id, position, created_at (PK over (comment_id, image_id))
- **rss_sources**: id, name, feed_url(unique), enabled, last_polled_at?, created_at, updated_at
- **audit_log**: id, actor_user_id, action_type, target_type, target_id, details_json, created_at (immutable)
- **user_follows**: id, user_id, follow_type(tag|user), follow_target_id, created_at
- **user_preferences**: user_id (PK), comment_check_enabled, collapsed_comments_json, feed_scroll_positions_json, updated_at
- **llm_cache**: id, provider, model, prompt_template_version, input_content_hash, response_json, created_at

### Fast-Follow Tables
- **drafts**: id, author_user_id, type(link|markdown), title, url?, markdown_body?, tags_json, hidden_keywords, scheduled_at?, share_token(unique), created_at, updated_at

### Indexes (non-exhaustive)
- posts(created_at DESC, id DESC), posts(canonical_url), posts(is_pinned DESC, created_at DESC), posts(source_type)
- comments(post_id, created_at DESC, id DESC), comments(parent_comment_id, created_at DESC)
- post_tags(tag_id, post_id)
- votes(target_type, target_id), votes(user_id, target_type, target_id)
- share_clicks(share_link_id, created_at DESC), share_clicks(post_id, user_id)
- rss_sources(enabled, updated_at)
- user_follows(user_id, follow_type), user_follows(follow_target_id, follow_type)
- llm_cache(input_content_hash, provider, model, prompt_template_version)
- FULLTEXT: posts(title, summary_html, hidden_keywords)

## 7. RPC Surface (gRPC)
Note: Browser UI uses WebActions to call these services, mapping JSON ↔︎ protobufs server-side.

### Core Services

```proto
syntax = "proto3";
package buildersyndicate.v1;

import "google/protobuf/timestamp.proto";

service PostsService {
  rpc CreateMarkdownPost(CreateMarkdownPostRequest) returns (Post);
  rpc CreateLinkPost(CreateLinkPostRequest) returns (CreateLinkPostResponse); // may return existing on duplicate
  rpc UpdatePost(UpdatePostRequest) returns (Post);
  rpc GetPost(GetPostRequest) returns (Post);
  rpc ListPosts(ListPostsRequest) returns (ListPostsResponse);
  rpc PreviewPost(PreviewPostRequest) returns (PreviewPostResponse); // AI summary + tags preview
  rpc PinPost(PinPostRequest) returns (Post);
  rpc LockPost(LockPostRequest) returns (Post);
  rpc SoftDeletePost(SoftDeletePostRequest) returns (Post);
  rpc RestorePost(RestorePostRequest) returns (Post);
  rpc MergePosts(MergePostsRequest) returns (Post); // admin: combine duplicates
  rpc ExtendBestBefore(ExtendBestBeforeRequest) returns (Post);
}

service CommentsService {
  rpc CreateComment(CreateCommentRequest) returns (CreateCommentResponse); // includes pre-check warnings
  rpc UpdateComment(UpdateCommentRequest) returns (Comment);
  rpc SoftDeleteComment(SoftDeleteCommentRequest) returns (Comment);
  rpc RestoreComment(RestoreCommentRequest) returns (Comment);
  rpc ListComments(ListCommentsRequest) returns (ListCommentsResponse);
}

service VotesService {
  rpc Upvote(UpvoteRequest) returns (UpvoteResponse);
  rpc RemoveUpvote(RemoveUpvoteRequest) returns (RemoveUpvoteResponse);
  rpc ListUserVotes(ListUserVotesRequest) returns (ListUserVotesResponse); // voting history
}

service TagsService {
  rpc CreateTag(CreateTagRequest) returns (Tag);
  rpc ListTags(ListTagsRequest) returns (ListTagsResponse);
  rpc ApplyTags(ApplyTagsRequest) returns (ApplyTagsResponse);
  rpc MergeTag(MergeTagRequest) returns (Tag);
  rpc RenameTag(RenameTagRequest) returns (Tag);
}

service SearchService {
  rpc SearchPosts(SearchPostsRequest) returns (SearchPostsResponse);
}

service SharesService {
  rpc CreateShareLink(CreateShareLinkRequest) returns (CreateShareLinkResponse);
  rpc RecordShareClick(RecordShareClickRequest) returns (RecordShareClickResponse);
}

service ImagesService {
  rpc CreatePresignedUpload(CreatePresignedUploadRequest) returns (CreatePresignedUploadResponse);
  rpc FinalizeUpload(FinalizeUploadRequest) returns (Image);
  rpc TriggerImagesGc(TriggerImagesGcRequest) returns (TriggerImagesGcResponse);
}

service IngestionAdminService {
  rpc ListRssSources(ListRssSourcesRequest) returns (ListRssSourcesResponse);
  rpc UpsertRssSource(UpsertRssSourceRequest) returns (RssSource);
  rpc ToggleRssSource(ToggleRssSourceRequest) returns (RssSource);
  rpc TriggerPollFeeds(TriggerPollFeedsRequest) returns (TriggerPollFeedsResponse);
}

service LlmAdminService {
  rpc TriggerRegenerateSummary(TriggerRegenerateRequest) returns (Post);
  rpc TriggerRegenerateTags(TriggerRegenerateRequest) returns (Post);
}

service ReadingProgressService {
  rpc UpdateProgress(UpdateProgressRequest) returns (UpdateProgressResponse);
  rpc GetProgress(GetProgressRequest) returns (GetProgressResponse);
}

service FollowService {
  rpc FollowTag(FollowTagRequest) returns (FollowResponse);
  rpc UnfollowTag(UnfollowTagRequest) returns (FollowResponse);
  rpc FollowUser(FollowUserRequest) returns (FollowResponse);
  rpc UnfollowUser(UnfollowUserRequest) returns (FollowResponse);
  rpc ListFollowing(ListFollowingRequest) returns (ListFollowingResponse);
}

service ProfileService {
  rpc GetProfile(GetProfileRequest) returns (Profile);
  rpc UpdateProfile(UpdateProfileRequest) returns (Profile);
  rpc ListUserPosts(ListUserPostsRequest) returns (ListPostsResponse);
  rpc ListUserComments(ListUserCommentsRequest) returns (ListCommentsResponse);
}

service PreferencesService {
  rpc GetPreferences(GetPreferencesRequest) returns (UserPreferences);
  rpc UpdatePreferences(UpdatePreferencesRequest) returns (UserPreferences);
  rpc UpdateCollapseState(UpdateCollapseStateRequest) returns (UpdateCollapseStateResponse);
  rpc UpdateScrollPosition(UpdateScrollPositionRequest) returns (UpdateScrollPositionResponse);
}

service AuthService {
  rpc Logout(LogoutRequest) returns (LogoutResponse);
}

service AdminService {
  rpc ImpersonateUser(ImpersonateUserRequest) returns (ImpersonateUserResponse);
  rpc EndImpersonation(EndImpersonationRequest) returns (EndImpersonationResponse);
  rpc ListAuditLog(ListAuditLogRequest) returns (ListAuditLogResponse);
}
```

### Fast-Follow Services

```proto
service DraftsService {
  rpc CreateDraft(CreateDraftRequest) returns (Draft);
  rpc UpdateDraft(UpdateDraftRequest) returns (Draft);
  rpc DeleteDraft(DeleteDraftRequest) returns (DeleteDraftResponse);
  rpc ListDrafts(ListDraftsRequest) returns (ListDraftsResponse);
  rpc GetDraftByShareToken(GetDraftByShareTokenRequest) returns (Draft);
  rpc PublishDraft(PublishDraftRequest) returns (Post);
  rpc ScheduleDraft(ScheduleDraftRequest) returns (Draft);
}

service DashboardService {
  rpc GetStats(GetStatsRequest) returns (DashboardStats);
  rpc ExportStats(ExportStatsRequest) returns (ExportStatsResponse);
}
```

Error semantics map to canonical gRPC status codes (see §11).

## 8. Ingestion & Dedup
- Triggered externally (manual) → enqueues PollFeeds job
- Worker: fetch feed; for each item → normalize URL; resolve canonical (rel=canonical → og:url → final URL after redirects)
- If canonical exists in DB, skip insert and optionally update freshness; else insert new link post with source_type=rss and enqueue LLM summary/tags
- Imported posts display "External" badge in UI
- Imported posts editable by admins
- Robustness: timeouts, retries with backoff; per-source circuit breaker

## 9. LLM Pipeline
- Asynchronous jobs via JobQueue; provider pluggable (OpenAI/Anthropic/local)
- Prompts stored and versioned in-repo (resources/llm/prompts)
- Cache outputs with: provider, model, prompt_template_version, input_content_hash; recompute only on content change or manual re-gen
- Post preview: synchronous call to generate summary + tags before publish; author can edit results
- Resilience: retries + circuit breaker; pre-post checks fail-open with visible warnings
- Privacy: no redaction in v1 (explicit decision)

## 10. Feeds & Ranking
- Hot/Trending = HN-style time decay over last 30 days; compute on read
- New = reverse chronological; Following = authors + tags
- Top = by window (24h/7d/30d)
- All-time Golden = is_evergreen AND (age ≥ 3 months OR interactions ≥ 100); admins can demote
- Pagination: keyset on (created_at DESC, id DESC); infinite scroll in UI
- Scroll position: persisted per user per feed in user_preferences; restored on return

## 11. Error Handling & gRPC Status Codes
- INVALID_ARGUMENT: validation errors (missing title, bad URL)
- NOT_FOUND: post/comment/tag not found
- ALREADY_EXISTS: duplicate tag name, duplicate vote (NOTE: duplicate URLs return 200 with was_duplicate, not an error)
- UNAUTHENTICATED: user not logged in / missing token
- PERMISSION_DENIED: RBAC failures; non-author editing own content; admin-only endpoints
- FAILED_PRECONDITION: posting comment on locked post; image finalize before presign
- ABORTED: concurrency conflicts (optimistic control if applied)
- UNAVAILABLE: LLM provider temporarily unavailable; RSS fetch circuit open
- DEADLINE_EXCEEDED: long-running operations exceeding deadline
- INTERNAL: unexpected exceptions

Error details: include google.rpc.ErrorInfo or domain-specific details in metadata where useful (e.g., existing_post_id on ALREADY_EXISTS for duplicate URL).

## 12. Search
- FULLTEXT over posts(title, summary_html, hidden_keywords); boolean mode
- Exact phrase support: quoted strings
- Boolean operators: AND, OR, NOT
- Malformed queries (unbalanced quotes, invalid operators) fall back to simple text search
- Filters: tags, authors; optional time window; expired posts toggle (default excludes expired)
- Hidden keywords: stored (not displayed); editable by authors/admins

## 13. Comments & Moderation
- Threaded (adjacency list), no FKs; edit history in comment_edits with minimal public diffs
- Collapsible threads; collapse state persisted per user in user_preferences
- Soft delete; authors can soft delete own; admins can soft delete/restore
- Deleted comments hidden entirely from non-moderators (no placeholder); visible to moderators inline with visual indicator
- Pre-post checks (answer/tone): default-on; per-user toggle in preferences; admin override; fail-open on LLM errors
- No per-user comment rate limits in v1

## 14. Personalization & Profiles
- Follow tags and authors; Following feed computed via union
- Profiles: Slack/hub/GitHub by default; single "private profile" toggle hides these from others
- Voting history: users can view their own upvotes only (not visible to others)
- Comment history: users can view their own comments
- Public points totals

## 15. Voting & Gamification
- votes table polymorphic; unique(user_id, target_type, target_id)
- Upvote and RemoveUpvote operations supported
- Denormalized counters maintained with transactional updates; periodic repair job reconciles drift
- Points: points_ledger for every event; users.points_total denormalized for fast reads
- Award points for actions and for received upvotes; deduct on undo
- Points floor at zero (cannot go negative)
- Points visible to all users on profiles

## 16. Shares Tracking
- share_links tokens per post; UI exposes shareable /p/{id}?share={token}
- share_clicks: capture referrer + user agent pre-auth (before login redirect); record first landing per user per post after auth; do not attribute later actions to sharer

## 17. Images Pipeline
- Presigned uploads to S3; finalize upload records image and relation (post_images/comment_images) then enqueues processing
- Comment images included in v1 (lower priority than post images)
- Processing job: strip EXIF; generate responsive variants (thumbnail/medium/large); update images.variants_json
- Served via CDN; srcset markup in HTML
- Moderation: pluggable module (noop base) to allow downstream upgrades
- Manual-trigger GC job via secured admin action:
  - Grace period for orphaned uploads (e.g., 24h) before deletion
  - Images attached to soft-deleted content protected until hard delete

## 18. Authentication, Authorization, Config
- Auth: SSO/OIDC for all actions; logout invalidates current session only
- RBAC: Misk authorization for admin endpoints/actions
- Admin impersonation: for debugging; start/end logged in audit_log
- Secrets/Config: Misk Config + Secret<T>; AWS Secrets Manager backing in staging/prod; simpler env/files locally

## 19. Observability & Logging
- /health and /metrics (Prometheus)
- Tracing: enable if available (OpenTelemetry)
- Logging: structured, metadata-only; scrub PII; no request/response bodies
- Audit: immutable audit_log for admin actions, tag merges/renames, impersonation

## 20. Performance & Indexing
- No server-side cache in v1; rely on keyset pagination and composite indexes
- Hot paths and indexes:
  - posts(created_at, id), comments(post_id, created_at, id)
  - post_tags(tag_id, post_id), votes(target_type, target_id)
  - share_clicks(share_link_id, created_at), share_clicks(post_id, user_id)
  - user_follows(user_id, follow_type)
  - FULLTEXT: posts(title, summary_html, hidden_keywords)
- Denormalized counters avoid heavy COUNT(*) on read paths

## 21. Best-Before / Expiration
- Posts can have optional best_before_at timestamp
- Expired posts (best_before_at < now):
  - Excluded from Hot/New/Following/Top/Golden feeds
  - Visible in search (with toggle to include/exclude, default exclude)
  - Visible on author's profile
  - Display visual "expired" indicator on detail page
- Authors can extend best_before_at
- No automatic archival or deletion

## 22. Testing Strategy
- Unit tests
  - URL normalization & canonical resolution heuristics
  - Ranking formula & best-before decay; Golden eligibility
  - Votes idempotency & counter updates; undo logic; points floor
  - Tag merging/aliases; hidden keywords logic
  - LLM prompt selection & caching key (provider/model/template/hash)
  - Search query parsing (exact phrases, boolean operators)
- gRPC integration tests (local via Orc)
  - Flyway migrations; jOOQ codegen; all services compile/invoke
  - RSS ingestion path: parse feeds; dedup by canonical; create posts with source_type
  - LLM pipeline: fake provider; retries; circuit breaker; fail-open pre-post checks; preview; re-gen triggers
  - Comments: threaded retrieval; edits & diffs; soft delete placeholders; collapse state
  - Search: FULLTEXT queries with tag/author filters; exact phrases; boolean; expired toggle
  - Images: presigned upload; finalize; EXIF strip + variants job (mockable); CDN URL assembly
  - Reading progress: record DOM marker + pct; mark read; auto-resume
  - Admin actions: lock/pin; soft delete/restore; merge posts; impersonation; audit_log entries; RBAC gates
  - Voting: upvote, undo, points ledger, points floor
- WebActions adapter tests
  - JSON ↔︎ protobuf mapping; validation; error mapping to HTTP from gRPC status codes
  - Duplicate URL returns existing post with 200 (not error)
- Manual checklist
  - End-to-end share flow with lifetime dedup per user per post
  - Following feed correctness; Hot/New/Top tabs; Golden eligibility behaviors
  - Profiles reflect public points and opted-in fields only
  - Voting history and comment history tabs
  - Scroll position persistence across sessions
  - Post preview with AI summary/tag editing

## 23. Security & Privacy Notes
- All actions require SSO/OIDC; no anonymous access
- RBAC gates admin endpoints
- Logs: metadata only; scrub PII
- LLM: third-party usage allowed; no redaction in v1
- Retention: audit_log, edit histories, share_clicks, and metadata logs retained indefinitely in v1
- Impersonation: logged; visible banner in UI

## 24. Open Questions / Future (v2+)
- Switch UI to gRPC‑Web (Envoy) to remove WebActions adapter
- Slack notifications; email ingestion; server caches; CI/CD; containerization
- Backups/PITR playbooks; image size/type enforcement; CDN cache strategies; automatic image GC
- Enhanced ranking signals; user flag/report; stricter LLM privacy
- Account deletion; data export; user blocking
- Mobile-responsive design
- In-app notifications

---

## Appendix A: Representative Protobuf Messages

```proto
message ListPostsRequest {
  enum FeedKind { HOT = 0; NEW = 1; FOLLOWING = 2; TOP_24H = 3; TOP_7D = 4; TOP_30D = 5; GOLDEN = 6; }
  FeedKind kind = 1;
  string cursor = 2;
  int32 limit = 3;
  bool include_expired = 4;
}

message ListPostsResponse {
  repeated Post posts = 1;
  string next_cursor = 2;
}

message CreateLinkPostRequest {
  string title = 1;
  string url = 2;
  repeated string tags = 3;
  repeated string hidden_keywords = 4;
  string summary_override = 5; // author-edited AI summary
  google.protobuf.Timestamp best_before_at = 6;
}

message CreateLinkPostResponse {
  Post post = 1;
  bool was_duplicate = 2; // true if returning existing post
}

message PreviewPostRequest {
  string title = 1;
  string url = 2;
  string markdown_body = 3;
}

message PreviewPostResponse {
  string suggested_summary = 1;
  repeated string suggested_tags = 2;
}

message UpvoteRequest {
  string target_type = 1;
  string target_id = 2;
}

message RemoveUpvoteRequest {
  string target_type = 1;
  string target_id = 2;
}

message SearchPostsRequest {
  string query = 1;
  repeated string tag_names = 2;
  repeated string author_ids = 3;
  bool include_expired = 4;
  string cursor = 5;
  int32 limit = 6;
}

message CreateCommentRequest {
  string post_id = 1;
  string parent_comment_id = 2;
  string markdown_body = 3;
  bool skip_pre_check = 4; // if user acknowledged warning
}

message CreateCommentResponse {
  Comment comment = 1;
  repeated PreCheckWarning warnings = 2; // empty if passed or skipped
}

message PreCheckWarning {
  string type = 1; // "answer_detection", "tone"
  string message = 2;
}

message UserPreferences {
  bool comment_check_enabled = 1;
  map<string, bool> collapsed_comments = 2; // comment_id -> collapsed
  map<string, string> feed_scroll_positions = 3; // feed_kind -> cursor
}
```

## Appendix B: UI Components Inventory

### Launch
1. Login page (SSO redirect)
2. Logout action
3. Feed tabs (Hot, New, Following, Top 24h/7d/30d, Golden)
4. Post list with infinite scroll
5. Post detail page
6. Create post form (link/markdown toggle)
7. Post preview panel (AI summary + tags, editable)
8. Threaded comments (collapsible)
9. Comment form with pre-check warnings
10. Upvote button (toggle for undo)
11. Share button with copy link
12. Tag picker (autocomplete + create)
13. Search page with filters + expired toggle
14. User profile (posts, comments, voting history, points, follows)
15. Settings (pre-check toggle, profile opt-outs)
16. Admin: RSS sources panel
17. Admin: Moderation controls (lock, pin, delete, restore, merge)
18. Admin: Audit log viewer
19. Admin: Image GC trigger
20. Admin: Impersonation (start/end + banner)
21. "External" badge for RSS posts
22. "Expired" badge for past-best-before posts
23. Deleted content indicator (mods only)
24. Best-before picker + extend button

### Fast-Follow
25. Draft editor with auto-save indicator
26. Draft sharing modal
27. Schedule picker
28. Dark mode (auto system detect)
29. Admin dashboard with charts
30. Dashboard export button
