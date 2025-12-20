# Specs - Agent Guide

## GitHub Issue Management

### Issue Format
```markdown
{Description paragraph}

## ✅ Acceptance Criteria

- [ ] Testable behavior 1
- [ ] Testable behavior 2

### Internal Coding

`{TASK_ID}`

- Implementation hint 1
- Implementation hint 2
```

### Required Metadata
- **Label:** `ex machina` on all AI-created issues
- **Milestone:** Assign to appropriate milestone (e.g., `micro steel thread`)
- **Dependencies:** Use native GitHub relationships (not text)

### Creating Dependency Relationships

The `gh` CLI doesn't support issue dependencies directly. Use the REST API:

```bash
# Get issue ID (not issue number)
gh api repos/{owner}/{repo}/issues/{number} --jq '.id'

# Create blocked_by relationship (use -F for integer, not -f)
gh api repos/{owner}/{repo}/issues/{issue_number}/dependencies/blocked_by \
  -X POST \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  -F issue_id={blocking_issue_id}
```

### Task Files
- Task files in `tasks/` are source of truth for issue content
- Move implementation hints (file paths) to `### Internal Coding` footer
- Keep OSS/architectural notes in body (they're requirements)
- Title: human-readable, no "feat:" prefix or task ID
