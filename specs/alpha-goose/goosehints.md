# Goose Hints for Builder Syndicate (v1)

Quick context
- Purpose: Enterprise knowledge aggregator to help teams and individuals find what matters and learn from each other faster.
- Architecture: Single Misk JVM monolith, gRPC‑first domain services (misk‑grpc + Wire), with a thin JSON WebActions adapter for the browser UI in v1 (can move to gRPC‑Web later).
- Frontend: Conforms to Misk UI guidance (no custom stack assumptions like Vite for v1).
- Deploys: Shaded JAR on VMs via systemd; staging env exists; Orc + Justfile for local dev; CI/CD intentionally deferred in v1.

Key decisions to remember
- Auth: SSO/OIDC required for all actions (read & write). RBAC via Misk; no anonymous access.
- DB: Aurora MySQL + jOOQ + Flyway. Prefer keyset pagination and composite indexes; no ORM/JPA.
- Protos: Package `buildersyndicate.v1`; services split by domain (Posts, Comments, Feeds, Search, Tags, Shares, Images, IngestionAdmin, LlmAdmin, Admin, ReadingProgress).
- Transport: UI calls WebActions that delegate to gRPC; gRPC status codes map to appropriate HTTP for UI adapter.
- Content types: Link posts + self‑hosted Markdown posts. Markdown stored as source; sanitized HTML pre‑rendered on write/edit.
- Canonical dedup: Server‑side fetch/parse rel=canonical (og:url fallback). Duplicate submissions redirect to the active post.
- LLM: Async summaries + tag suggestions. Provider pluggable; prompts versioned in‑repo; cached with (provider, model, template_version, input_hash). Retries + circuit breaker; fail‑open on errors. No redaction in v1.
- Comments: Threaded (adjacency list). Edit with diff history; soft delete placeholder. Pre‑post checks (answer/tone) default‑on, per‑user disable; admin override. No comment rate limits in v1.
- Voting: Upvotes‑only; single polymorphic votes table; unique(user_id, target_type, target_id). No explicit FKs.
- Counters: Denormalized upvote/comment/share_clicks counts updated on write; periodic repair jobs reconcile drift.
- Shares: Share links with tokens; clicks tracked as first landing per user per post (lifetime). Store referrer + user agent; do NOT store IP; do NOT attribute actions to sharer.
- Ranking: HN‑style time‑decay for Hot over last 30 days. All‑time Golden: requires evergreen flag (LLM) and (age ≥ 3 months OR interactions ≥ 100). Compute scores on read.
- Search: MySQL FULLTEXT over titles, summaries, hidden_keywords; filters for tags/authors; DO NOT index body content. Hidden keywords are author/admin editable and not displayed.
- Personalization: Follow tags/authors; Following feed. Tags are open creation; admins can merge/rename and maintain aliases.
- Reading progress: Only for self‑hosted posts. Store DOM last_position_marker + max_scroll_pct; consider “read” at ≥ 80%; auto‑resume to marker.
- Images: S3 presigned uploads; CDN front. Strip EXIF; generate responsive variants via background job; moderation module is pluggable (noop base). Manual‑trigger GC to remove unreferenced images via secured admin action. No strict size/type limits in v1.
- Logging/observability: Prometheus /metrics and health endpoints. Tracing if available in Misk. Logs are metadata‑only; scrub PII; never log request/response bodies.
- Docs present: PRD and Engineering Spec in repo root; align updates across both.

Do NOT (v1 scope constraints)
- Do not add Docker/Kubernetes or a Dockerfile.
- Do not add CI/CD automation (documented Justfile workflows only).
- Do not enable anonymous reads or writes.
- Do not index post body content in search.
- Do not cache server‑side feeds (caching may come in v2+).
- Do not implement user flag/report workflows.
- Do not attribute votes/comments to sharers (shares are clicks analytics only).

Testing & reliability hints
- Unit‑test URL normalization/canonical extraction, ranking formula, best‑before decay, votes idempotency, LLM cache key, tag merges/aliases.
- Integration‑test gRPC services with Orc (local MySQL + InMemory/LocalStack SQS). Include WebActions ↔︎ gRPC mapping and error translation.
- Error semantics: map domain validations to INVALID_ARGUMENT/409; RBAC failures to PERMISSION_DENIED; locked‑post comment attempts to FAILED_PRECONDITION; transient upstream to UNAVAILABLE with retries/circuit breaker.

Performance hints
- Use keyset pagination over (created_at DESC, id DESC). Provide opaque cursor encoding both values.
- Ensure composite indexes for feeds and lookups: posts(created_at,id), comments(post_id,created_at,id), post_tags(tag_id,post_id), votes(target_type,target_id), share_clicks(share_link_id,created_at), share_clicks(post_id,user_id).
- Keep feed queries tight: limit Hot to last 30 days.

Admin & operations
- Admin actions: lock/pin posts; soft delete/restore posts/comments; tag merge/rename with aliases; trigger RSS poll; trigger image GC; LLM re‑gen for summaries/tags.
- Audit every admin action in immutable audit_log.
- Env config via Misk Secrets (AWS Secrets Manager in staging/prod; env/files locally).

File/structure tips to nudge future agents
- Protos under `proto/buildersyndicate/v1/*.proto` (service‑per‑domain; Wire config checked in).
- WebActions adapter in a dedicated module that only translates JSON↔︎protobuf and invokes gRPC services.
- SQL migrations in Flyway `db/migration` with clear forward‑only scripts; jOOQ codegen updated after migration.
- Prompts in `resources/llm/prompts/` with explicit template versioning.
- Justfile contains: `just dev`, `just db:migrate`, `just codegen`, `just run`, `just test` (documented in CONTRIBUTING.md).

Common pitfalls
- Forgetting to store LLM cache metadata (provider/model/template/input_hash) → unnecessary recomputes.
- Not stripping EXIF before publishing → privacy leak.
- Treating duplicate submissions as “shares” → incorrect analytics; only share_clicks count as shares.
- Adding body content to FULLTEXT by habit → breaks v1 requirement.
- Introducing explicit FKs on polymorphic tables → conflicts with scalability choice.

Parking lot (v2+ candidates)
- Switch UI to gRPC‑Web behind Envoy.
- Slack notifications; email ingestion; server‑side caches.
- CI/CD automation and container packaging.
- Backups/PITR runbooks; image size/type enforcement; CDN caching strategies; automatic image GC.
- User flag/report workflows; expanded ranking signals; stricter LLM privacy.
