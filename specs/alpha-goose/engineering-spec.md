# Builder Syndicate Engineering Specification (v1)

Status: Draft (developer-ready) — gRPC-first revision

## 1. Overview
Builder Syndicate is an OSS-first, enterprise-focused link and knowledge aggregation platform for internal communities. v1 ships as a single Misk-based JVM monolith with:
- gRPC-first domain services (misk-grpc + Wire-generated stubs)
- A Misk-UI–conformant browser frontend
- Aurora MySQL via jOOQ + Flyway
- Enterprise SSO/OIDC (all actions require login)

Transport model (v1):
- Internals: gRPC services across domains (Posts, Comments, Feeds, Search, Tags, Shares, Images, IngestionAdmin, LlmAdmin, Admin)
- Browser UI: Thin JSON WebActions adapter for the UI that delegates to gRPC services
  - Rationale: keeps v1 infra simple (no Envoy/gRPC‑Web). v2+ can move to gRPC‑Web behind Envoy if desired without changing core services.

Primary objectives:
- High-signal discovery/discussion with upvotes-only and time-decay ranking
- Low-friction ingest (RSS/Atom), canonical-URL dedup, quality LLM summaries
- Enterprise-ready auth/RBAC, observability, predictable deploys (shaded JAR + systemd)

## 2. Scope (summarized)
- In-scope (v1):
  - Misk monolith; Misk UI; gRPC-first domain services; UI adapter via WebActions
  - Aurora MySQL; jOOQ + Flyway; denormalized counters + periodic repair
  - SSO/OIDC required for all actions; RBAC via Misk
  - Link posts + self-hosted Markdown posts (pre-render sanitized HTML)
  - Canonical-URL resolution & dedup; duplicate submission redirects to active post
  - RSS/Atom ingest with sources in DB + admin UI; manual trigger; JobQueue worker (SQS prod/InMemory dev)
  - LLM: async summaries + tag suggestions; provider-pluggable; prompts versioned in-repo; cached with metadata; retries/backoff; fail-open
  - Threaded comments; edit history & soft-delete; pre-post checks (answer/tone) default-on with per-user toggle + admin override
  - Ranking: HN-style time decay; Hot limited to last 30 days; All-time Golden = evergreen + (age ≥ 3 months or interactions ≥ 100); admin demotion
  - Personalization: follow tags/authors; Following feed
  - Search: MySQL FULLTEXT over titles/summaries/hidden_keywords (no body)
  - Votes: upvotes-only; uniqueness enforced; single polymorphic table (no FKs)
  - Shares: share links with tokens; click tracking (first landing per user per post); store referrer + user agent (no IP)
  - Images: S3 presigned uploads; CDN front; EXIF stripping; responsive variants via background job; pluggable moderation module (noop base); manual-trigger GC via admin action
  - Reading progress (self-hosted posts only): DOM last_position_marker + max_scroll_pct; mark read at ≥ 80%; auto-resume
  - Profiles: show Slack/hub/GitHub; followed tags/authors; public points; opt-outs
  - Observability: Prometheus metrics/health; tracing if available; metadata-only logs with PII scrubbing
  - Environments: local (Orc), staging, prod (shaded JAR + systemd); SemVer releases + annotated tags; CHANGELOG from Conventional Commits
- Out-of-scope (v1): Email ingestion; Slack notifications; server-side caches; CI/CD automation; Docker/Kubernetes; user flag/report; external search engine; backup/PITR runbooks; emoji reactions; indexing post body content

## 3. System Architecture
- Monolith (Misk)
  - Modules: gRPC Domain Services, UI WebActions adapter, Database (Hikari + jOOQ + Flyway), Auth/RBAC, JobQueue, LLM Client, RSS Ingestion, Image Pipeline, Search, Admin UI
  - Serves static frontend (Misk UI–conformant)
- Data stores: Aurora MySQL (InnoDB); S3-compatible object storage for images (CDN fronted)
- Jobs: Misk JobQueue abstraction (SQS prod; InMemory/LocalStack dev)
- External: Third-party LLM APIs; RSS/Atom feeds per DB-configured sources
- Environments: Local via Orc; staging/prod via shaded JAR + systemd; config via Misk Secrets (AWS Secrets Manager in staging/prod)

## 4. Tech Stack & Dependencies
- Kotlin (or Java) on JVM; Misk; misk-grpc; Wire (Protobufs)
- DB: Aurora MySQL; HikariCP; jOOQ codegen; Flyway migrations
- Jobs: Misk JobQueue (SQS prod; InMemory dev)
- LLM: pluggable HTTP client; prompts versioned in-repo
- UI: Misk UI guidance and components; WebActions adapter calls gRPC services
- Storage/CDN: S3 + CloudFront (or equivalent)
- Observability: Prometheus metrics; tracing (OpenTelemetry if available in Misk)

## 5. Data Model (tables, key fields)
Polymorphic relationships avoid explicit FKs for scalability; integrity enforced in application.
- users: id, idp_sub (unique), email, name, avatar_url, slack_handle, hub_link, github_handle, profile_opt_outs, created_at, updated_at
- posts: id, type(link|markdown), title, url?, canonical_url(unique for links), markdown_body?, html_rendered?, summary_html?, best_before_at?, hidden_keywords(FULLTEXT), author_user_id, is_locked, is_deleted, is_pinned, is_evergreen, counters(upvote_count, comment_count, share_clicks_count), created_at, updated_at
- post_edits: id, post_id, editor_user_id, prev_html, new_html, diff?, edited_at
- comments: id, post_id, author_user_id, parent_comment_id?, markdown_body, html_rendered, is_deleted, upvote_count, created_at, updated_at
- comment_edits: id, comment_id, editor_user_id, prev_html, new_html, diff?, edited_at
- tags: id, name(unique), alias_to_tag_id?, created_at
- post_tags: post_id, tag_id, created_at (PK over (post_id, tag_id))
- votes: id, user_id, target_type(post|comment), target_id, created_at; unique(user_id, target_type, target_id)
- share_links: id, post_id, token(unique), creator_user_id?, created_at
- share_clicks: id, share_link_id, post_id, user_id?, referrer?, user_agent?, first_for_user(bool), created_at
- reading_progress: id, user_id, post_id, dom_marker, max_scroll_pct, updated_at
- images: id, uploader_user_id, s3_key, content_type, size_bytes?, exif_stripped_at?, variants_json, created_at
- post_images: post_id, image_id, position, created_at (PK over (post_id, image_id))
- comment_images: comment_id, image_id, position, created_at (PK over (comment_id, image_id))
- rss_sources: id, name, feed_url(unique), enabled, last_polled_at?, created_at, updated_at
- audit_log: id, actor_user_id, action_type, target_type, target_id, details_json, created_at (immutable)

Indexes (non-exhaustive):
- posts(created_at DESC, id DESC), posts(canonical_url), posts(is_pinned DESC, created_at DESC)
- comments(post_id, created_at DESC, id DESC), comments(parent_comment_id, created_at DESC)
- post_tags(tag_id, post_id)
- votes(target_type, target_id), votes(user_id, target_type, target_id)
- share_clicks(share_link_id, created_at DESC), share_clicks(post_id, user_id)
- rss_sources(enabled, updated_at)
- FULLTEXT: posts(title, summary_html, hidden_keywords)

## 6. RPC Surface (gRPC)
Note: Browser UI uses WebActions to call these services, mapping JSON ↔︎ protobufs server-side. Later, gRPC‑Web can replace the adapter unchanged.

Core services (representative):

```proto
syntax = "proto3";
package buildersyndicate.v1;

import "google/protobuf/timestamp.proto";

service PostsService {
  rpc CreateMarkdownPost(CreateMarkdownPostRequest) returns (Post);
  rpc CreateLinkPost(CreateLinkPostRequest) returns (Post);
  rpc UpdatePost(UpdatePostRequest) returns (Post);
  rpc GetPost(GetPostRequest) returns (Post);
  rpc ListPosts(ListPostsRequest) returns (ListPostsResponse); // Hot/New/Following/Top
  rpc PinPost(PinPostRequest) returns (Post);        // admin
  rpc LockPost(LockPostRequest) returns (Post);      // admin
  rpc SoftDeletePost(SoftDeletePostRequest) returns (Post); // author|admin
  rpc RestorePost(RestorePostRequest) returns (Post);       // admin
}

service CommentsService {
  rpc CreateComment(CreateCommentRequest) returns (Comment);
  rpc UpdateComment(UpdateCommentRequest) returns (Comment);
  rpc SoftDeleteComment(SoftDeleteCommentRequest) returns (Comment);
  rpc RestoreComment(RestoreCommentRequest) returns (Comment); // admin
  rpc ListComments(ListCommentsRequest) returns (ListCommentsResponse); // threaded
}

service VotesService {
  rpc Upvote(UpvoteRequest) returns (UpvoteResponse); // idempotent
}

service TagsService {
  rpc CreateTag(CreateTagRequest) returns (Tag);
  rpc ListTags(ListTagsRequest) returns (ListTagsResponse);
  rpc ApplyTags(ApplyTagsRequest) returns (ApplyTagsResponse);
  rpc MergeTag(MergeTagRequest) returns (Tag); // admin
  rpc RenameTag(RenameTagRequest) returns (Tag); // admin
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
  rpc TriggerImagesGc(TriggerImagesGcRequest) returns (TriggerImagesGcResponse); // admin
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
}

// Message types (abbreviated)
message Post { /* id, title, type, urls, counters, flags, timestamps, etc. */ }
message Comment { /* id, post_id, parent_id, body/html, flags, counters */ }
message Tag { /* id, name */ }
```

Error semantics map to canonical gRPC status codes (see §10).

## 7. Ingestion & Dedup
- Triggered externally (manual) → enqueues PollFeeds job
- Worker: fetch feed; for each item → normalize URL; resolve canonical (rel=canonical → og:url → final URL after redirects)
- If canonical exists in DB, skip insert and optionally update freshness; else insert new link post and enqueue LLM summary/tags
- Robustness: timeouts, retries with backoff; per-source circuit breaker

## 8. LLM Pipeline
- Asynchronous jobs via JobQueue; provider pluggable (OpenAI/Anthropic/local)
- Prompts stored and versioned in-repo (resources/llm/prompts)
- Cache outputs with: provider, model, prompt_template_version, input_content_hash; recompute only on content change or manual re-gen
- Resilience: retries + circuit breaker; pre-post checks fail-open with visible warnings
- Privacy: no redaction in v1 (explicit decision)

## 9. Feeds & Ranking
- Hot/Trending = HN-style time decay over last 30 days; compute on read
- New = reverse chronological; Following = authors + tags
- Top = by window (24h/7d/30d)
- All-time Golden = is_evergreen AND (age ≥ 3 months OR interactions ≥ 100); admins can demote
- Pagination: keyset on (created_at DESC, id DESC)

## 10. Error Handling & gRPC Status Codes
- INVALID_ARGUMENT: validation errors (missing title, bad URL)
- NOT_FOUND: post/comment/tag not found
- ALREADY_EXISTS: duplicate canonical URL, duplicate tag name, duplicate vote
- UNAUTHENTICATED: user not logged in / missing token
- PERMISSION_DENIED: RBAC failures; non-author editing own content; admin-only endpoints
- FAILED_PRECONDITION: posting comment on locked post; image finalize before presign
- ABORTED: concurrency conflicts (optimistic control if applied)
- UNAVAILABLE: LLM provider temporarily unavailable; RSS fetch circuit open
- DEADLINE_EXCEEDED: long-running operations exceeding deadline
- INTERNAL: unexpected exceptions

Error details: include google.rpc.ErrorInfo or domain-specific details in metadata where useful (e.g., canonical_url on ALREADY_EXISTS).

## 11. Search
- FULLTEXT over posts(title, summary_html, hidden_keywords); boolean mode
- Filters: tags, authors; optional time window
- Hidden keywords: stored (not displayed); editable by authors/admins

## 12. Comments & Moderation
- Threaded (adjacency list), no FKs; edit history in comment_edits with minimal public diffs
- Soft delete with placeholder; authors can soft delete own; admins can soft delete/restore
- Pre-post checks (answer/tone): default-on; per-user toggle; admin override; fail-open on LLM errors
- No per-user comment rate limits in v1

## 13. Personalization & Profiles
- Follow tags and authors; Following feed computed via union
- Profiles: Slack/hub/GitHub by default with opt-outs; interests (followed tags) and followed authors visible (with opt-out)
- Public points totals

## 14. Voting & Gamification
- votes table polymorphic; unique(user_id, target_type, target_id)
- Denormalized counters maintained with transactional updates; periodic repair job reconciles drift
- Points: points_ledger for every event; users.points_total denormalized for fast reads
- Award points for actions and for received upvotes (no emoji reactions in v1)

## 15. Shares Tracking
- share_links tokens per post; UI exposes shareable /p/{id}?share={token}
- share_clicks recorded on landing; count only first landing per user per post; store referrer + user agent; do not attribute later actions to sharer

## 16. Images Pipeline
- Presigned uploads to S3; finalize upload records image and relation (post_images/comment_images) then enqueues processing
- Processing job: strip EXIF; generate responsive variants (thumbnail/medium/large); update images.variants_json
- Served via CDN; srcset markup in HTML
- Moderation: pluggable module (noop base) to allow downstream upgrades
- Manual-trigger GC job via secured admin action

## 17. Authentication, Authorization, Config
- Auth: SSO/OIDC for all actions
- RBAC: Misk authorization for admin endpoints/actions
- Secrets/Config: Misk Config + Secret<T>; AWS Secrets Manager backing in staging/prod; simpler env/files locally

## 18. Observability & Logging
- /health and /metrics (Prometheus)
- Tracing: enable if available (OpenTelemetry)
- Logging: structured, metadata-only; scrub PII; no request/response bodies
- Audit: immutable audit_log for admin actions and tag merges/renames

## 19. Performance & Indexing
- No server-side cache in v1; rely on keyset pagination and composite indexes
- Hot paths and indexes:
  - posts(created_at, id), comments(post_id, created_at, id)
  - post_tags(tag_id, post_id), votes(target_type, target_id)
  - share_clicks(share_link_id, created_at), share_clicks(post_id, user_id)
  - FULLTEXT: posts(title, summary_html, hidden_keywords)
- Denormalized counters avoid heavy COUNT(*) on read paths

## 20. Testing Strategy
- Unit tests
  - URL normalization & canonical resolution heuristics
  - Ranking formula & best-before decay; Golden eligibility
  - Votes idempotency & counter updates
  - Tag merging/aliases; hidden keywords logic
  - LLM prompt selection & caching key (provider/model/template/hash)
- gRPC integration tests (local via Orc)
  - Flyway migrations; jOOQ codegen; all services compile/invoke
  - RSS ingestion path: parse feeds; dedup by canonical; create posts
  - LLM pipeline: fake provider; retries; circuit breaker; fail-open pre-post checks; re-gen triggers
  - Comments: threaded retrieval; edits & diffs; soft delete placeholders
  - Search: FULLTEXT queries with tag/author filters
  - Images: presigned upload; finalize; EXIF strip + variants job (mockable); CDN URL assembly
  - Reading progress: record DOM marker + pct; mark read; auto-resume
  - Admin actions: lock/pin; soft delete/restore; audit_log entries; RBAC gates
- WebActions adapter tests
  - JSON ↔︎ protobuf mapping; validation; error mapping to HTTP (e.g., 400/401/403/409/412/503) from gRPC status codes
- Manual checklist
  - End-to-end share flow with lifetime dedup per user per post
  - Following feed correctness; Hot/New/Top tabs; Golden eligibility behaviors
  - Profiles reflect public points and opted-in fields only

## 21. Implementation Plan (milestones)
1) Repo bootstrap: Misk app; Wire/Protobuf setup; base modules; Misk UI shell; Flyway/jOOQ
2) Auth & Users; RBAC; Profiles; WebActions adapter scaffold
3) Posts/Comments gRPC services + edit histories; Markdown render + sanitize
4) Votes + counters; points_ledger + totals
5) Tags model (open creation) + post_tags; merges/aliases (admin)
6) Search (FULLTEXT) + filters; hidden_keywords
7) RSS ingestion: DB sources + admin UI; manual trigger + JobQueue worker; canonical dedup
8) LLM pipeline: async summaries/tags; caching; fail-open pre-post checks; re-gen triggers
9) Ranking and feeds (Hot/New/Following/Top) + Golden; keyset pagination
10) Shares: tokens + click events (referrer/UA); lifetime de-dup
11) Images: presigned upload; finalize; EXIF strip + variants; CDN; post/comment linking; admin GC trigger
12) Reading progress: DOM marker + pct; mark read; auto-resume
13) Observability & logging; audit_log; admin tools (lock/pin/delete/restore)
14) Staging environment configs; systemd; docs (README/ARCHITECTURE/RUNBOOK/PRD); Justfile

## 22. Security & Privacy Notes
- All actions require SSO/OIDC; no anonymous access
- RBAC gates admin endpoints
- Logs: metadata only; scrub PII
- LLM: third-party usage allowed; no redaction in v1
- Retention: audit_log, edit histories, share_clicks, and metadata logs retained indefinitely in v1

## 23. Open Questions / Future (v2+)
- Switch UI to gRPC‑Web (Envoy) to remove WebActions adapter
- Slack notifications; email ingestion; server caches; CI/CD; containerization
- Backups/PITR playbooks; image size/type enforcement; CDN cache strategies; automatic image GC
- Enhanced ranking signals; user flag/report; stricter LLM privacy

---

Appendix A: Representative Protobuf Messages (abbreviated)

```proto
message ListPostsRequest {
  enum FeedKind { HOT = 0; NEW = 1; FOLLOWING = 2; TOP_24H = 3; TOP_7D = 4; TOP_30D = 5; GOLDEN = 6; }
  FeedKind kind = 1;
  string cursor = 2; // opaque (created_at|id encoded)
  int32 limit = 3;   // default 25, max 100
}

message ListPostsResponse {
  repeated Post posts = 1;
  string next_cursor = 2; // empty if end
}

message CreateLinkPostRequest { string title = 1; string url = 2; repeated string tags = 3; repeated string hidden_keywords = 4; }
message CreateMarkdownPostRequest { string title = 1; string markdown_body = 2; repeated string tags = 3; repeated string hidden_keywords = 4; }

message UpvoteRequest { string target_type = 1; string target_id = 2; }

message SearchPostsRequest { string query = 1; repeated string tag_names = 2; repeated string author_ids = 3; }
```
