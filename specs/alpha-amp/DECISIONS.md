# Builder Syndicate - Resolved Ambiguities

This document records decisions made to resolve ambiguities between the PRD, engineering spec, gap analysis, and implementation plan. These decisions are authoritative and supersede any conflicting statements in other documents.

## Decision Summary

| # | Topic | Decision |
|---|-------|----------|
| 1 | Comment images | **In v1** (lower priority) |
| 2 | Drafts | **Fast-follow** (post-launch) |
| 3 | AI Preview before publish | **Fast-follow** (post-launch) |
| 4 | Duplicate URL API response | **Return 200** with `was_duplicate: true` |
| 5 | Dark mode toggle | **Auto-detect + manual override** |
| 6 | Voting history visibility | **Self-only** (private) |
| 7 | Expired posts in feeds | **Exclude** (search/profile only) |
| 8 | "External" badge scope | **All non-internal** source types |
| 9 | RSS posts | **Defer to post-v1** |
| 10 | Admin evergreen override | **Yes** (admin can set/unset) |
| 11 | Top feeds (24h/7d/30d) | **Include in v1** |
| 12 | Points values | **Implementation-defined** |
| 13 | Ranking formula | **Implementation-defined** (HN-style decay required) |
| 14 | Profile privacy model | **Single "private profile" toggle** |
| 15 | Deleted comments (non-mods) | **Hide entirely** (no placeholder) |
| 16 | Share click tracking | **Capture pre-auth metadata** before login redirect |
| 17 | Image GC grace period | **Yes** (grace period for orphaned uploads) |
| 18 | Soft-deleted content images | **Protected** (keep until hard delete) |
| 19 | Malformed search queries | **Graceful fallback** to simple text search |
| 20 | Author summary edits | **LLM can overwrite** on regeneration |

---

## Detailed Decisions

### 1. Comment Images — In v1 (Lower Priority)

**Decision:** Comment images are included in v1 scope, but at lower priority than post images.

**Implications:**
- Keep `comment_images` table in schema
- Implement after post images are working
- Include in Phase 21 (Images) of implementation plan

---

### 2. Drafts — Fast-Follow

**Decision:** Drafts system (auto-save, sharing) is post-launch, not v1.

**Implications:**
- PRD FF-01/FF-02 remain as fast-follow
- GAP_ANALYSIS "Must" priority should be read as "Must for completeness" not "Must for launch"
- Implementation plan FF1 placement is correct

---

### 3. AI Preview Before Publish — Fast-Follow

**Decision:** Pre-publish AI preview (showing summary/tags for author to edit) is post-launch.

**Implications:**
- F-06/F-07 in PRD are deferred to fast-follow
- For v1 launch: AI summaries/tags generated async after publish; authors can edit after
- Preview panel moves to FF1 in implementation plan
- Engineering spec scope should note this deferral

---

### 4. Duplicate URL API Response — Return 200

**Decision:** When submitting a duplicate URL, both gRPC and WebActions return success (200/OK) with the existing post and `was_duplicate: true`.

**Implications:**
- No `ALREADY_EXISTS` error for duplicate URLs
- `CreateLinkPostResponse` always contains `post` + `was_duplicate` flag
- Simplifies client logic; idempotent behavior
- Remove duplicate URL from ALREADY_EXISTS examples in error mapping

---

### 5. Dark Mode — Auto-Detect + Manual Override

**Decision:** Dark mode uses system preference detection AND provides a manual toggle for user override.

**Implications:**
- Store user preference in `user_preferences` table
- UI provides theme toggle in settings
- Default to system preference; override persists
- Update FF3 in implementation plan to include toggle

---

### 6. Voting History — Self-Only

**Decision:** Users can only view their own voting history. It is not visible to other users.

**Implications:**
- Profile voting history tab only appears for the authenticated user viewing their own profile
- `ListUserVotes` RPC restricted to self
- Privacy-focused default

---

### 7. Expired Posts in Feeds — Exclude

**Decision:** Expired posts (past best_before_at) do not appear in Hot/New/Following/Top/Golden feeds. They are only visible via search (with toggle) and on author profiles.

**Implications:**
- Feed queries filter out `best_before_at < NOW()`
- Search has `include_expired` toggle (default false)
- Profile shows all author posts regardless of expiration
- Add "expired" badge on post detail for direct links

---

### 8. "External" Badge — All Non-Internal

**Decision:** The "External" badge applies to all posts where `source_type != 'internal'`, not just RSS.

**Implications:**
- Badge shows for both `rss` and `external` source types
- Allows future ingestion methods to use `external` type
- UI checks `source_type` field, not specific values

---

### 9. RSS Posts — Defer to Post-v1

**Decision:** RSS ingestion is deferred entirely to post-v1.

**Implications:**
- Remove Phase 18 (RSS Ingestion) and Phase 19 (External Indicator) from launch scope
- Remove F-08, F-09, F-10 from v1 launch requirements
- Keep `rss_sources` table design for future
- Reduces v1 scope significantly
- `source_type` column still useful for future; default all v1 posts to `internal`

---

### 10. Admin Evergreen Override — Yes

**Decision:** Admins can manually set or unset the `is_evergreen` flag on posts, overriding LLM determination.

**Implications:**
- Add admin endpoint to toggle evergreen
- Audit log records manual changes
- LLM sets initial value; admin can override
- Golden feed respects manual overrides

---

### 11. Top Feeds (24h/7d/30d) — Include in v1

**Decision:** v1 includes separate Top feeds filtered by time window (24h, 7d, 30d) in addition to Hot/New/Following/Golden.

**Implications:**
- `FeedKind` enum includes TOP_24H, TOP_7D, TOP_30D
- UI shows all feed tabs
- Top = sorted by upvote_count within time window

---

### 12. Points Values — Implementation-Defined

**Decision:** Exact point values for actions are not specified in the spec. Implementers decide.

**Implications:**
- No contractual point values in spec
- Implementation should be consistent and documented in code
- Can tune post-launch without spec changes

---

### 13. Ranking Formula — Implementation-Defined

**Decision:** The exact Hot ranking formula and parameters are not specified. Implementers must use HN-style time decay but can tune parameters.

**Implications:**
- Spec requires "HN-style time decay"
- Specific formula, half-life, etc. are implementation details
- Tests verify behavioral properties, not exact scores

---

### 14. Profile Privacy — Single Toggle

**Decision:** Profile visibility uses a single "private profile" toggle, not per-field opt-outs.

**Implications:**
- `users.profile_opt_outs` becomes a single boolean or simple flag
- Private = hide Slack/hub/GitHub/follows from other users
- Points remain public regardless (gamification visibility)
- Simplifies UI and data model

---

### 15. Deleted Comments (Non-Mods) — Hide Entirely

**Decision:** Non-moderator users do not see deleted comments at all. No placeholder shown; thread collapses around deleted content.

**Implications:**
- Comment queries filter out `is_deleted = true` for non-mods
- Mods see deleted with visual indicator
- Authors do not see their own deleted comments (clean break)
- Thread structure preserved but deleted nodes hidden

---

### 16. Share Click Tracking — Pre-Auth Capture

**Decision:** Share link clicks capture referrer and user agent before the login redirect, not only after authentication.

**Implications:**
- Landing endpoint captures metadata immediately
- `user_id` populated after auth completes (may be null initially)
- `first_for_user` evaluated post-login when user_id known
- Preserves referrer data that would be lost in redirect chain

---

### 17. Image GC Grace Period — Yes

**Decision:** Orphaned image uploads have a grace period before GC deletion (e.g., 24 hours).

**Implications:**
- GC job only deletes images orphaned for > grace period
- Protects uploads in progress (form abandonment)
- Grace period is configurable (default 24h)
- Add `created_at` check to GC query

---

### 18. Soft-Deleted Content Images — Protected

**Decision:** Images attached to soft-deleted posts/comments are protected from GC until hard delete.

**Implications:**
- GC checks both live and soft-deleted content for references
- Images only orphaned when parent is hard-deleted or image explicitly removed
- Supports content restoration without image loss

---

### 19. Malformed Search Queries — Graceful Fallback

**Decision:** Invalid search queries (unbalanced quotes, bad operators) fall back to simple text search rather than returning an error.

**Implications:**
- Search parser attempts boolean/phrase parsing
- On parse failure, treat entire query as simple text
- No user-facing error for syntax issues
- Log parse failures for monitoring

---

### 20. Author Summary Edits — LLM Overwrites

**Decision:** When LLM regenerates a summary (manual re-gen or content update), it overwrites any author edits.

**Implications:**
- No "custom" flag to preserve author edits
- Authors warned that re-generation replaces their edits
- Simpler implementation; author can re-edit after
- Re-generation is an explicit admin/author action
