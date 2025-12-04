# Builder Syndicate Gap Analysis

> **Note:** This document is historical. See [DECISIONS.md](DECISIONS.md) for authoritative decisions that supersede this analysis.

Based on PRD v1 and Engineering Spec review with product clarifications.

## Gaps in PRD/Spec (Missing Requirements)

### Authentication & Session
| Gap | Clarification | Priority |
|-----|---------------|----------|
| Logout functionality | Current session only (not all devices) | Must |

### Posts & Content Creation
| Gap | Clarification | Priority |
|-----|---------------|----------|
| Post preview before publish | Shows AI-generated summary + tags for review | Must |
| Author can edit AI summary before publish | User can modify AI output | Must |
| Author can edit AI tags before publish | User can modify AI suggestions | Must |
| Drafts system | Separate from scheduled; auto-save enabled | ~~Must~~ Fast-follow |
| Draft sharing | Shareable via link; requires login; no comments on drafts | Should |
| Scheduled publishing | Posts can be scheduled; editable before publish time | Should |
| Post merging (duplicates) | Admins can merge posts; combines comments + upvotes | Should |
| Show existing post on duplicate URL | Immediate redirect/display when canonical URL exists | Must |

### RSS Ingestion
| Gap | Clarification | Priority |
|-----|---------------|----------|
| "External" indicator on imported posts | Visual badge for RSS-sourced content | Must |
| RSS posts editable after import | Authors/admins can modify imported content | Must |

### Voting & Points
| Gap | Clarification | Priority |
|-----|---------------|----------|
| Undo/remove upvote | Users can retract upvotes | Must |
| Points deduction on undo | Removing upvote deducts points from author | Must |
| Points floor at zero | Points cannot go negative | Must |
| Points visible to all | Public on profiles (already in spec but confirm) | Must |

### Comments
| Gap | Clarification | Priority |
|-----|---------------|----------|
| Collapsible comments | Users can collapse/expand threads | Should |
| Remember collapse state | Per-user persistence | Should |

### Search
| Gap | Clarification | Priority |
|-----|---------------|----------|
| Quoted exact phrase search | "exact phrase" syntax | Should |
| Boolean operators | AND/OR/NOT support | Should |

### Feeds & Navigation
| Gap | Clarification | Priority |
|-----|---------------|----------|
| Infinite scroll | Not paginated "load more" buttons | Should |
| Remember scroll position | Persists across sessions | Should |
| Expired posts in search | Visible with toggle filter | Should |

### User Profiles & History
| Gap | Clarification | Priority |
|-----|---------------|----------|
| View own voting history | Users can see posts/comments they upvoted | Should |
| View own comment history | Users can see their comments | Should |

### Best-Before / Expiration
| Gap | Clarification | Priority |
|-----|---------------|----------|
| Expired posts visible on profile | Still shown on author profile | Must |
| Authors can extend best-before | Editable by author | Should |
| Toggle for expired in search | Filter option in search UI | Should |

### Moderation
| Gap | Clarification | Priority |
|-----|---------------|----------|
| Deleted content visible to mods | Inline with visual indicator (not separate queue) | Must |
| Admin impersonation | For debugging; logged in audit_log | Should |

### Admin
| Gap | Clarification | Priority |
|-----|---------------|----------|
| Dashboard with stats | Posts/day, active users, etc. | Should |
| Dashboard uses cached data | Not real-time | Should |
| Dashboard exportable | CSV/JSON export | Should |

### UI/UX
| Gap | Clarification | Priority |
|-----|---------------|----------|
| Dark mode | Auto-detect system preference | Should |

---

## Clarifications (Not Gaps, But Worth Documenting)

| Item | Clarification |
|------|---------------|
| User onboarding | Later feature |
| Account deletion | Not supported |
| Comment images | **In v1 (lower priority)** — see DECISIONS.md |
| Max comment depth | Not enforced (perf note only) |
| LLM opt-out per post | Later feature, special permission |
| User blocking | Not supported |
| Data export (GDPR) | Not supported in v1 |
| In-app notifications | Not in v1 |
| Report button | Not in v1 |
| Content warnings | Not supported |
| Mobile responsive | Not required |
| Max tags per post | None |
| Max image size | None |
| Max images per post | None |
| Max comment length | None |
| Rate limits | None |
| Shadow-banning | Not supported |
| Unread indicators | Not supported |
| RSS ingestion | **Deferred to post-v1** — see DECISIONS.md |
| Drafts/Preview | **Fast-follow (post-launch)** — see DECISIONS.md |
| Voting history | **Self-only** — see DECISIONS.md |
| Deleted comments | **Hidden from non-mods** — see DECISIONS.md |

---

## Schema Implications

New/modified tables needed:

```sql
-- Drafts system
CREATE TABLE drafts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  author_user_id BIGINT NOT NULL,
  type ENUM('link', 'markdown') NOT NULL,
  title VARCHAR(500),
  url VARCHAR(2048),
  markdown_body TEXT,
  tags_json JSON,
  hidden_keywords TEXT,
  scheduled_at DATETIME,
  share_token VARCHAR(64) UNIQUE,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

-- User preferences (collapse state, scroll position)
CREATE TABLE user_preferences (
  user_id BIGINT PRIMARY KEY,
  collapsed_comments_json JSON,
  feed_scroll_positions_json JSON,
  updated_at DATETIME NOT NULL
);

-- Modify votes table to support removal
-- (already supports this via DELETE, but confirm app logic)
```

---

## New API Endpoints Needed

### Drafts
- `CreateDraft` / `UpdateDraft` / `DeleteDraft` / `ListDrafts`
- `GetDraftByShareToken`
- `PublishDraft` (creates post from draft)
- `ScheduleDraft`

### Votes
- `RemoveUpvote` (or modify `Upvote` to be toggle)

### User Preferences
- `UpdatePreferences` / `GetPreferences`

### Admin
- `MergePosts`
- `GetDashboardStats`
- `ExportDashboard`
- `ImpersonateUser` / `EndImpersonation`

---

## UI Components Needed (Not in Original Plan)

1. **Draft editor** with auto-save indicator
2. **AI preview panel** showing summary + tags before publish
3. **Schedule picker** datetime input
4. **Share draft modal** with copy link
5. **Duplicate post modal** showing existing post
6. **Merge posts admin dialog**
7. **Collapsible comment threads** with expand/collapse controls
8. **Dark mode toggle** (respects system, allows override?)
9. **Expired posts filter** toggle in search
10. **Admin dashboard** with charts + export button
11. **Impersonation banner** when admin is impersonating
12. **Voting history tab** on profile
13. **Comment history tab** on profile
14. **"External" badge** for RSS posts
15. **Deleted content indicator** for moderators
