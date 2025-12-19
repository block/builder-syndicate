# Builder Syndicate PRD (v1)

## 1. Intro / Context
Builder Syndicate is an OSS-first, enterprise-focused link and knowledge aggregation platform inspired by Reddit/Hacker News/Lobsters. It prioritizes high-signal discovery and discussion for internal engineering/product communities. v1 ships as a single Misk-based monolith (JVM) with a Misk-UI-conformant frontend, Aurora MySQL persistence via jOOQ + Flyway, enterprise SSO/OIDC (all actions require login), and opinionated features for canonical-URL dedup, LLM-assisted summaries/tag suggestions (async), threaded comments with pre-post checks, and ranking tuned for freshness and evergreen content. The system is designed for easy OSS adoption and corporate deployment: shaded JAR on VMs via systemd, staging environment, Orc-based local dev, and pluggable modules (LLM provider, image moderation, job queue).

## 2. Personas & Pain Points
- Reader (Engineer/PM/Designer)
  - Pain: Hard to find relevant internal/external articles without hype; poor signal-to-noise ratio.
  - Pain: Feeds aren’t personalized; tagging is inconsistent.
  - Pain: Discussions derail; tone and low-effort comments waste time.
- Curator/Author
  - Pain: Sharing links leads to duplicates; engagement fragments across variants.
  - Pain: Writing summaries and tagging is tedious; needs assistive tooling.
- Moderator/Admin
  - Pain: Needs lightweight, transparent moderation tooling (lock/soft delete/pin) and auditability.
  - Pain: Wants insights (shares vs upvotes vs comments) without building analytics infra.
- Platform/Infra Engineer
  - Pain: Local setup and deployment friction; prefers simple JVM deploys, minimal CI/CD in v1, and clear runbooks.

## 3. Success Metrics (Quantitative)
- Adoption & Engagement
  - ≥ 60% of invited users log in at least once during first 30 days post-launch.
  - ≥ 35% weekly active users (WAU/MAU) by end of month 2.
  - Median time-to-first-upvote on new posts ≤ 12 hours within month 2.
- Content Quality & Discovery
  - ≥ 70% of posts have at least one tag within 24 hours.
  - ≥ 80% of posts have an LLM summary generated within 10 minutes of creation/ingestion.
  - Duplicate submission rate ≤ 5% of total link attempts (canonical-URL dedup effective).
- Reliability & Performance
  - P95 feed render (server time) ≤ 300 ms for Hot/New/Following queries at 10k posts, no server cache.
  - P95 post detail (server time) ≤ 400 ms at 100 comments/thread depth 3.
  - LLM job success rate ≥ 99% after retries; fail-open visible warning ≤ 1% of total posts.

## 4. Functional Requirements
| ID | Description | Priority |
|----|-------------|----------|
| F-01 | Enterprise SSO/OIDC login required for all reads and writes; provider-pluggable | Must |
| F-02 | Post types: link posts (URL) and self-hosted Markdown posts | Must |
| F-03 | Canonical-URL resolution and dedup on submission/ingestion; redirect dupes to active post | Must |
| F-04 | Store Markdown as source; pre-render sanitized HTML on write/edit | Must |
| F-05 | RSS/Atom polling via external/manual trigger; sources managed in DB with admin UI | Must |
| F-06 | LLM-generated summaries and tag suggestions run asynchronously; provider pluggable | Must |
| F-07 | Threaded comments; authors can edit (diff history) and soft delete; admin soft delete/restore | Must |
| F-08 | Pre-post checks for comments (answer-detection, tone); default-on, per-user disable with admin override; fail-open on LLM errors | Must |
| F-09 | Upvotes-only; prevent duplicate upvotes per user per target | Must |
| F-10 | Shares: trackable links; count first landing per user per post; store referrer and user agent | Must |
| F-11 | Ranking: Hot/Trending with HN-style time decay, limited to last 30 days | Must |
| F-12 | “All-time Golden” feed: algorithmic (LLM evergreen flag + age ≥ 3 months or interactions ≥ 100); admins can demote | Should |
| F-13 | Personalization: follow tags and authors; Following feed | Must |
| F-14 | Tags: LLM-prefilled, optional; normalized tags + post_tags; open creation; admin merge/rename with aliases | Must |
| F-15 | Search: MySQL FULLTEXT over titles, summaries, hidden_keywords; filters for tags/authors | Must |
| F-16 | Hidden search keywords editable by authors/admins; excluded from display | Must |
| F-17 | Reading progress (self-hosted posts only): client instrumentation; store DOM marker + max_scroll_pct; mark “read” at ≥80%; auto-resume | Should |
| F-18 | Gamification: award points for actions and for received upvotes; points_ledger + points_total; show totals on profiles | Should |
| F-19 | Profiles: show Slack handle, local hub link, GitHub by default with opt-out; show interests (followed tags) and followed authors | Should |
| F-20 | Admin moderation: lock posts (disable new comments), pin posts, immutable audit_log | Must |
| F-21 | Images in posts/comments: S3 presigned uploads; CDN front; EXIF stripping; responsive variants + srcset; pluggable moderation (noop base) | Must |
| F-22 | Manual-trigger GC job to delete unreferenced images via secured admin action | Should |
| F-23 | Admins and authors can trigger on-demand re-generation of LLM summaries/tags | Should |
| F-24 | Misk UI–conformant frontend | Must |
| F-25 | Admin UI to manage RSS sources and triggers (poll now, image GC) | Should |

## 5. Non‑Functional Requirements
| ID | Description | Metric / Threshold |
|----|-------------|--------------------|
| N-01 | Availability (app runtime) | ≥ 99.5% monthly (excluding planned maintenance) |
| N-02 | Performance: feed queries (no server-side cache) | P95 ≤ 300 ms at 10k posts, keyset pagination |
| N-03 | Performance: post detail (no server-side cache) | P95 ≤ 400 ms at 100 comments, depth ≤ 3 |
| N-04 | Security: logging/privacy | No request/response bodies in logs; scrub emails/tokens by default |
| N-05 | Security: authZ | All admin endpoints gated by Misk RBAC |
| N-06 | Data integrity: votes uniqueness | Unique (user_id, target_type, target_id) enforced |
| N-07 | LLM pipeline reliability | ≥ 99% success after retries; fail-open visible notice |
| N-08 | Search correctness | FULLTEXT matches on titles/summaries/hidden_keywords; body excluded |
| N-09 | Images processing | EXIF stripped; responsive variants generated asynchronously |
| N-10 | Portability/Deploy | Shaded JAR; systemd unit; staging environment available |
| N-11 | Observability | Prometheus metrics/health enabled; tracing if available in Misk |
| N-12 | Persistence migrations | Flyway-managed; reproducible local dev via Orc + Justfile |

## 6. Out‑of‑Scope (v1)
- Public/anonymous access (all actions require SSO/OIDC login)
- Email ingestion (jack@) and Slack notifications (candidate for v2)
- Server-side in-memory caching (e.g., Caffeine)
- GitHub Actions/automated CI/CD; deploy scripts (manual ops not included in repo)
- Kubernetes/Docker packaging (no Dockerfile in v1)
- User flag/report workflows
- External search service (use MySQL FULLTEXT only)
- Automatic backups/PITR guides (ops drills deferred)
- Emoji reactions (upvotes only)
- Body-content indexing in search
