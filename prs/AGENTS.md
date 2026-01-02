# PR authoring guidance for agents (Builder Syndicate)

Last updated: 2025-01-02

This document captures conventions for opening GitHub PRs as an agent. When in doubt, prioritize clarity for a human 
reviewer and avoid duplicating ticket content.

## Core principles

- **Be ticket-centric:** The ticket remains the source of truth for problem and acceptance criteria. The PR should be 
  concise and focus on what landed and how to validate it quickly.
- **Always include a reference:** Every PR must include a `ref:` footer to the relevant issue/ticket. If the work fully 
  implements and closes a ticket, also include `Closes:` following the `ref:` footer.
- **Guide the reviewer:** Highlight "Key files to review first" only when it materially reduces cognitive load. Keep it 
  in a collapsible section.
- **Make it feelable:** Add a minimal, copy/paste "Feel it." section with steps to experience the change end-to-end.
- **Use GitHub-native affordances:** collapsible `<details>`, image/video drag-and-drop, and auto-closing via `Closes:`.

## Template

- **Preferred template:** `prs/00-PR-TEMPLATE.md`
- Example PR write-ups for reference: `prs/*.draft.md`

## Required PR sections

- **Header:** Conventional Commit title, Branch, Base, Stack (use issue numbers, never branch names)
- **Summary:** 1–2 lines describing what landed
- **Feel it.:** Minimal steps to experience the change
- **Caveats / Follow-ups:** Known limitations and immediate next steps
- **Footers:** `ref: #<issue>` (always) and `Closes: #<issue>` (when applicable)

## Conditional sections

- **Divergence from ticket:** Include ONLY if the implementation differs from ticket AC or plan. Omit entirely if none.
- **Key files to review first (collapsible):** Include only when it materially helps navigation. Omit for small PRs.
- **DB migrations (collapsible):** Include only if migrations exist. Omit otherwise.

## Optional elements

- **UI screenshots/GIFs:** Add when the change affects UI in ways text alone won't convey (layout, animation, contrast).
  The agent should proactively suggest visuals when additive, not include them by default.

## "Feel it." guidance

The "Feel it." section should give a human a quick way to validate the change:

| Change type | Example |
|-------------|---------|
| API / server | `just run && curl -f localhost:8080/endpoint` |
| Code-only / refactor | `just test` |
| UI | Steps to click through + optional screenshot |
| No-op (docs, config) | State: "This change is intended to have no observable runtime effect." |

## Multiple references

PRs ideally map 1:1 to tickets, but real life happens. When a PR touches multiple issues:

```md
ref: #<primary-issue>
ref: #<related-issue>
Closes: #<issue-fully-implemented>
```

## Stacked PRs

- Set the correct base branch when opening a stacked PR
- Reference the dependency with issue numbers: `Stack: depends on #<issue>`
- **Never use branch names in public PR content** — branches are internal; issues are the public contract
- After the base lands, rebase and change base to `main`

## Labels

- **Always add the `ex machina` label** to PRs created by agents
- Add other labels as appropriate (e.g., facet labels like `posts`, `auth`)

## Foldable sections

Use `###` heading followed by `<details><summary>expand</summary>`:

```md
### Key files to review first
<details><summary>expand</summary>

- file list here

</details>
```

## Do / Don't

| Do | Don't |
|----|-------|
| Link to issues using `#<number>` | Use branch names in Stack or PR body |
| Keep PR descriptions brief and action-oriented | Restate the entire ticket |
| Omit sections that don't apply | Include empty sections |
| Use outcomes or automated checks | Add checklists requiring manual clicking |
| Add the `ex machina` label | Forget to label agent-authored PRs |

## Questions or updates

If conventions evolve, update this file and the template accordingly.
