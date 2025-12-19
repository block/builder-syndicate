# Builder Syndicate - Complete Commit List

Quick reference for all 96 commits in implementation order.

## Phase 1: Foundation (F1-F6)

1. `chore: initialize Misk application with base configuration`
2. `chore: setup database connection with HikariCP and jOOQ`
3. `chore: configure Flyway migrations`
4. `chore: setup Wire protobuf definitions and misk-grpc`
5. `chore: scaffold WebActions adapter layer`
6. `chore: setup Misk UI shell and routing`

## Phase 2: Auth & Users (A1)

7. `feat: setup $MISK_EXT local auth with pluggable provider interface`

## Phase 3: Posts - Link Type (P1-P5)

8. `feat: add link post creation with canonical URL resolution`
9. `feat: add canonical URL deduplication on post submission`
10. `feat: add post detail view`
11. `feat: add post editing with history tracking`
12. `feat: add post soft delete for authors`

## Phase 4: Posts - Markdown Type (P6)

13. `feat: add markdown post creation with rendering and sanitization`

## Phase 5: Posts - Listing (P7)

14. `feat: add New feed (reverse chronological post listing)`

## Phase 6: Voting - Posts (V1-V4)

15. `feat: add upvoting for posts with deduplication`
16. `feat: add denormalized upvote counters for posts`
17. `feat: add points ledger for post creation`
18. `feat: add points for received post upvotes`

## Phase 7: Comments (C1-C4)

19. `feat: add comment creation on posts`
20. `feat: add threaded comment listing`
21. `feat: add comment editing with history tracking`
22. `feat: add comment soft delete for authors`

## Phase 8: Voting - Comments (V5-V8)

23. `feat: add upvoting for comments`
24. `feat: add denormalized upvote counters for comments`
25. `feat: add points for comment creation`
26. `feat: add points for received comment upvotes`

## Phase 9: Tags (T1-T5)

27. `feat: add tag creation and normalization`
28. `feat: add tag application to posts`
29. `feat: add tag filtering in post lists`
30. `feat: add admin tag merge capability`
31. `feat: add admin tag rename with aliases`

## Phase 10: Search (S1-S3)

32. `feat: add FULLTEXT search over posts`
33. `feat: add hidden keywords field for posts`
34. `feat: add tag and author filters to search`

## Phase 11: Feeds & Ranking (FD1-FD3)

35. `feat: add Hot feed with HN-style time decay ranking`
36. `feat: add Top feeds (24h, 7d, 30d)`
37. `feat: add keyset pagination for all feeds`

## Phase 12: LLM Infrastructure (L1-L3)

38. `feat: setup LLM provider client with pluggable interface`
39. `feat: add JobQueue infrastructure with SQS/InMemory support`
40. `feat: add LLM prompt versioning and caching`

## Phase 13: LLM - Summaries (LS1-LS4)

41. `feat: add async LLM summary generation for posts`
42. `feat: add LLM summary retry and fail-open handling`
43. `feat: add manual summary regeneration trigger`
44. `feat: add admin LLM summary regeneration UI`

## Phase 14: LLM - Tags (LT1-LT2)

45. `feat: add async LLM tag suggestions for posts`
46. `feat: add manual tag suggestion regeneration`

## Phase 15: Personalization (PS1-PS5)

47. `feat: add follow tags capability`
48. `feat: add follow authors capability`
49. `feat: add Following feed`
50. `feat: add followed tags to profile display`
51. `feat: add followed authors to profile display`

## Phase 16: RSS Ingestion (R1-R5)

52. `feat: add RSS sources table and management`
53. `feat: add RSS source admin UI`
54. `feat: add RSS feed polling worker`
55. `feat: add RSS ingestion with canonical dedup`
56. `feat: add manual RSS poll trigger`

## Phase 17: Images (I1-I9)

57. `feat: add S3 presigned upload for images`
58. `feat: add image finalize and association to posts`
59. `feat: add image finalize and association to comments`
60. `feat: add async EXIF stripping for images`
61. `feat: add responsive image variants generation`
62. `feat: add CDN URL generation with srcset`
63. `feat: add image moderation plugin interface`
64. `feat: add manual image GC trigger`
65. `feat: add admin image GC UI`

## Phase 18: LLM - Comment Checks (LC1-LC4)

66. `feat: add LLM pre-post checks for comments (answer detection)`
67. `feat: add LLM pre-post checks for comments (tone)`
68. `feat: add per-user toggle for pre-post checks`
69. `feat: add admin override for pre-post checks`

## Phase 19: Shares (SH1-SH4)

70. `feat: add share link generation for posts`
71. `feat: add share click tracking`
72. `feat: add share click deduplication per user per post`
73. `feat: add referrer and user agent tracking for shares`

## Phase 20: Reading Progress (RP1-RP4)

74. `feat: add reading progress tracking (DOM marker)`
75. `feat: add reading progress scroll percentage`
76. `feat: add mark as read at 80% threshold`
77. `feat: add auto-resume reading position`

## Phase 21: Golden Feed (G1-G3)

78. `feat: add evergreen flag and eligibility calculation`
79. `feat: add Golden feed (all-time evergreen)`
80. `feat: add admin demotion from Golden feed`

## Phase 22: Admin - Posts (AP1-AP4)

81. `feat: add admin post lock capability`
82. `feat: add admin post pin capability`
83. `feat: add admin post soft delete`
84. `feat: add admin post restore`

## Phase 23: Admin - Comments (AC1-AC2)

85. `feat: add admin comment soft delete`
86. `feat: add admin comment restore`

## Phase 24: Audit (AU1-AU3)

87. `feat: add audit log for admin actions`
88. `feat: add audit log for tag operations`
89. `feat: add audit log UI for admins`

## Phase 25: Profile Enhancement (PR1-PR6)

90. `feat: add Slack handle to profiles`
91. `feat: add hub link to profiles`
92. `feat: add GitHub handle to profiles`
93. `feat: add profile field opt-outs`
94. `feat: add public points total to profiles`
95. `feat: add best-before date for posts`

---

**Total: 95 commits** (removed observability items already in Misk)

## Steel Thread (Minimum Viable - 11 commits)

For fastest path to working product:
1. F1: Initialize Misk
2. F2: Database + jOOQ
3. F3: Flyway
4. A1: $MISK_EXT auth
5. P1: Link post creation
6. P7: New feed listing
7. V1: Upvote posts
8. V2: Upvote counters
9. C1: Comment creation
10. C2: Threaded comments
11. V5: Upvote comments

After these 11, you have a functional link aggregator with voting and comments.
