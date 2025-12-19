# Builder Syndicate PRD (v1.1)

## 1. Intro / Context
Builder Syndicate is an OSS-first, enterprise-focused link and knowledge aggregation platform inspired by Reddit/Hacker News/Lobsters. It prioritizes high-signal discovery and discussion for internal engineering/product communities. v1 ships as a single Misk-based monolith (JVM) with a Misk-UI-conformant frontend, Aurora MySQL persistence via jOOQ + Flyway, enterprise SSO/OIDC (all actions require login), and opinionated features for canonical-URL dedup, LLM-assisted summaries/tag suggestions (async), threaded comments with pre-post checks, and ranking tuned for freshness and evergreen content. The system is designed for easy OSS adoption and corporate deployment: shaded JAR on VMs via systemd, staging environment, Orc-based local dev, and pluggable modules (LLM provider, image moderation, job queue).

This project also serves as an exemplary Misk reference implementation for the community.

## 2. Personas & Pain Points
- Reader (Engineer/PM/Designer)
  - Pain: Hard to find relevant internal/external articles without hype; poor signal-to-noise ratio.
  - Pain: Feeds aren't personalized; tagging is inconsistent.
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

### Core Features (Launch)
| ID | Description | Priority |
|----|-------------|----------|
| F-01 | Enterprise SSO/OIDC login required for all reads and writes; provider-pluggable | Must |
| F-02 | Logout functionality (current session only) | Must |
| F-03 | Post types: link posts (URL) and self-hosted Markdown posts | Must |
| F-04 | Canonical-URL resolution and dedup on submission/ingestion; return existing post with `was_duplicate: true` (200 response, not error) | Must |
| F-05 | Store Markdown as source; pre-render sanitized HTML on write/edit | Must |
| F-06 | ~~Post preview before publish showing AI-generated summary and tags~~ | ~~Must~~ Deferred to fast-follow |
| F-07 | ~~Author can edit AI-generated summary and tags before publish~~ | ~~Must~~ Deferred to fast-follow |
| F-08 | ~~RSS/Atom polling via external/manual trigger; sources managed in DB with admin UI~~ | ~~Must~~ Deferred to post-v1 |
| F-09 | ~~RSS-imported posts display "External" indicator badge~~ | ~~Must~~ Deferred to post-v1 |
| F-10 | ~~RSS-imported posts editable by authors/admins after import~~ | ~~Must~~ Deferred to post-v1 |
| F-11 | LLM-generated summaries and tag suggestions run asynchronously; provider pluggable | Must |
| F-12 | Threaded comments; authors can edit (diff history) and soft delete; admin soft delete/restore | Must |
| F-13 | Comments collapsible/expandable; collapse state persisted per user | Should |
| F-14 | Pre-post checks for comments (answer-detection, tone); default-on, per-user disable with admin override; fail-open on LLM errors | Must |
| F-15 | Upvotes-only; prevent duplicate upvotes per user per target | Must |
| F-16 | Undo/remove upvote; deducts points from author; points floor at zero | Must |
| F-17 | Shares: trackable links; capture referrer/UA pre-auth; count first landing per user per post after auth | Must |
| F-18 | Ranking: Hot/Trending with HN-style time decay, limited to last 30 days | Must |
| F-19 | "All-time Golden" feed: algorithmic (LLM evergreen flag + age ≥ 3 months or interactions ≥ 100); admins can demote | Should |
| F-20 | Personalization: follow tags and authors; Following feed | Must |
| F-21 | Tags: LLM-prefilled, optional; normalized tags + post_tags; open creation by users; admin merge/rename with aliases | Must |
| F-22 | Search: MySQL FULLTEXT over titles, summaries, hidden_keywords; filters for tags/authors | Must |
| F-23 | Search: support quoted exact phrases and boolean operators (AND/OR/NOT) | Should |
| F-24 | Search: toggle to include/exclude expired posts | Should |
| F-25 | Hidden search keywords editable by authors/admins; excluded from display | Must |
| F-26 | Reading progress (self-hosted posts only): client instrumentation; store DOM marker + max_scroll_pct; mark "read" at ≥80%; auto-resume | Should |
| F-27 | Gamification: award points for actions and for received upvotes; points_ledger + points_total; show totals on profiles; points visible to all users | Should |
| F-28 | Profiles: show Slack handle, local hub link, GitHub by default; single "private profile" toggle (not per-field) | Should |
| F-29 | Profiles: voting history tab (self-only, not visible to others) | Should |
| F-30 | Profiles: comment history tab | Should |
| F-31 | Admin moderation: lock posts (disable new comments), pin posts, immutable audit_log | Must |
| F-32 | Admin moderation: deleted content visible inline to moderators with visual indicator | Must |
| F-33 | Admin moderation: merge duplicate posts (combines comments and upvotes) | Should |
| F-34 | Admin impersonation for debugging; logged in audit_log | Should |
| F-35 | Images in posts/comments: S3 presigned uploads; CDN front; EXIF stripping; responsive variants + srcset; pluggable moderation (noop base); comment images lower priority | Must |
| F-36 | Manual-trigger GC job with grace period; soft-deleted content images protected until hard delete | Should |
| F-37 | Admins and authors can trigger on-demand re-generation of LLM summaries/tags | Should |
| F-38 | Misk UI–conformant frontend | Must |
| F-39 | Admin UI to manage RSS sources and triggers (poll now, image GC) | Should |
| F-40 | Best-before/expiration: posts can have expiration date; expired posts excluded from feeds, visible on profile and in search (with toggle); authors can extend | Should |
| F-43 | Feeds: Hot, New, Following, Top (24h/7d/30d), Golden tabs | Must |
| F-44 | Deleted comments hidden entirely from non-moderators (no placeholder) | Must |
| F-45 | Search: malformed queries fall back to simple text search | Should |
| F-46 | LLM evergreen flag: admin can manually override | Should |
| F-41 | Feeds: infinite scroll with keyset pagination | Should |
| F-42 | Feeds: remember scroll position across sessions | Should |

### Fast-Follow Features (Post-Launch)
| ID | Description | Priority |
|----|-------------|----------|
| FF-01 | Drafts system: separate from scheduled; auto-save enabled | Should |
| FF-02 | Draft sharing: shareable via link; requires login to view; no comments on drafts | Should |
| FF-03 | Scheduled publishing: posts can be scheduled; editable before publish time | Should |
| FF-04 | Dark mode: auto-detect system preference + manual toggle override | Should |
| FF-08 | Post preview before publish showing AI-generated summary and tags (moved from F-06) | Should |
| FF-09 | Author can edit AI-generated summary and tags before publish (moved from F-07) | Should |
| FF-10 | RSS/Atom polling via external/manual trigger; sources managed in DB with admin UI | Should |
| FF-11 | RSS-imported posts display "External" indicator badge | Should |
| FF-12 | RSS-imported posts editable by authors/admins after import | Should |
| FF-05 | Admin dashboard: stats (posts/day, active users); cached data; CSV/JSON export | Should |
| FF-06 | LLM opt-out per post: special permission; author can disable LLM summary for specific post | Could |
| FF-07 | User onboarding flow after first login | Could |

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
| N-08 | Search correctness | FULLTEXT matches on titles/summaries/hidden_keywords; body excluded; exact phrases and boolean operators supported |
| N-09 | Images processing | EXIF stripped; responsive variants generated asynchronously |
| N-10 | Portability/Deploy | Shaded JAR; systemd unit; staging environment available |
| N-11 | Observability | Prometheus metrics/health enabled; tracing if available in Misk |
| N-12 | Persistence migrations | Flyway-managed; reproducible local dev via Orc + Justfile |
| N-13 | Architecture | Hexagonal-lite: core logic in pure Kotlin packages; Misk as adapter layer |
| N-14 | Build | Gradle Kotlin DSL |
| N-15 | Modular composition | Thin main(); all setup in Guice modules; public visibility on modules/services/actions; DefaultBuilderSyndicateModule for standalone; swappable subsystems (auth, db, LLM, storage, jobs) |

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
- Account deletion
- User blocking
- Data export (GDPR-style)
- In-app notifications
- Content warnings
- Mobile-responsive design
- Rate limits on posts/comments
- Shadow-banning
- Unread indicators for comments
- Comments on drafts
- Max limits (tags per post, image size, image count, comment length, comment depth enforcement)

## 7. Clarifications & Decisions
| Item | Decision |
|------|----------|
| Link post body | AI-generated summary serves as body; no manual body field |
| Comment depth | Not enforced; performance note only (depth ≤ 3 for P95 targets) |
| Points visibility | Public on all profiles |
| Score display | No downvotes; scores only increase from 0 |
| Duplicate URL handling | Return 200 with existing post + `was_duplicate: true` |
| Voting history | Self-only; not visible to other users |
| Deleted comments | Hidden entirely from non-mods (no placeholder) |
| Profile privacy | Single toggle (not per-field opt-outs) |
| Expired posts | Excluded from feeds; visible in search (with toggle) and profile |
| Dark mode | Auto-detect + manual toggle override |
| Comment images | In v1 but lower priority than post images |
| Image GC | Grace period for orphaned uploads; soft-deleted content protected |
| Search errors | Malformed queries fall back to simple text search |
| LLM summary edits | LLM can overwrite on regeneration |
| Share tracking | Capture referrer/UA pre-auth; user attribution post-login |

See [DECISIONS.md](DECISIONS.md) for full rationale on each decision.
