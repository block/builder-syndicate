# <type(scope)>: Concise, action-oriented title

**Branch:** `branch-name`
**Base:** `base-branch`
**Stack:** depends on #<issue> (if stacked)

## Summary
1–2 lines describing what landed. Keep it terse; link to the ticket via footer.

<!-- Omit this section entirely if there is no divergence from the ticket. -->
## Divergence from ticket
- Deviations from acceptance criteria or plan
- Key trade-offs/decisions made during implementation

### Key files to review first
<details><summary>expand</summary>

- path/to/important/File1.ext — why it matters
- path/to/important/File2.ext — why it matters

</details>

<!-- Include this section ONLY if there are DB migrations. Omit otherwise. -->
### DB migrations
<details><summary>expand</summary>

- Migration(s): `db/migration/V00X__name.sql`
- Data impact: brief description of what changes
- Rollback strategy: how to revert or mitigate

</details>

## Feel it.
Minimal steps to experience or validate the change end-to-end (copy/paste).

```bash
# API / server change
just run
curl -f localhost:8080/healthz

# Code-only / refactor / internal change
just test

# No-op change (e.g., docs, config)
# This change is intended to have no observable runtime effect.
```

## Caveats / Follow-ups
- Known limitations or immediate next steps

---

ref: #<primary-issue>
ref: #<related-issue> (if applicable)
Closes: #<issue> (if fully implemented)

<!-- For significant UI changes, consider adding screenshots/GIFs when it materially improves understanding. -->
