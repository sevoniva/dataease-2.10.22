# OB Oracle Dataset Extract Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete OB Oracle dataset-level extract so dashboards query DE internal tables while existing Excel/API mode `1` remains intact.

**Architecture:** Add a narrow routing guard around the dataset extract path. Keep the current dataset sync task/log/scheduler implementation, but make it explicitly OB Oracle dataset extract rather than generic datasource sync.

**Tech Stack:** Java/Spring Boot, MyBatis Plus, DataEase dataset SQL engine, Quartz, Vue 3/Element Plus.

---

### Task 1: Guard Extract Routing

**Files:**
- Modify: `core/core-backend/src/main/java/io/dataease/dataset/sync/DatasetSyncUtils.java`
- Modify: `core/core-backend/src/main/java/io/dataease/dataset/sync/DatasetSyncQueryManage.java`
- Test: `core/core-backend/src/test/java/io/dataease/dataset/sync/DatasetSyncUtilsTest.java`

- [x] Add tests proving only single-source, non-cross OB Oracle mode `1` datasets route to cache.
- [x] Run the targeted JUnit test and confirm the new tests fail before implementation.
- [x] Implement the routing guard.
- [x] Require `cache_ready = 1` before routing to the internal table.
- [x] Re-run the targeted JUnit test and confirm it passes.

### Task 2: Tighten Refresh SQL Utilities

**Files:**
- Modify: `core/core-backend/src/main/java/io/dataease/dataset/sync/DatasetSyncUtils.java`
- Test: `core/core-backend/src/test/java/io/dataease/dataset/sync/DatasetSyncUtilsTest.java`

- [x] Cover cache select SQL, Oracle paging SQL, and incremental predicates.
- [x] Ensure calculated fields are excluded from materialized columns.
- [x] Re-run the targeted JUnit test.

### Task 3: Verify Backend Build

**Files:**
- Existing backend module only.

- [x] Run focused JUnit tests for datasource and dataset sync helpers.
- [x] Run backend compile/package with frontend skipped if needed.
- [x] Record any unrelated failures separately.

### Task 4: Verify Frontend Build

**Files:**
- Modify only dataset form/API files if labels or payload shape need adjustment.

- [x] Run the frontend base build.
- [x] Run TypeScript check if possible and record known unrelated failures.

### Task 5: Local Runtime Validation

**Files:**
- No source edits unless validation reveals a bug.

- [x] Start local DataEase v2.
- [x] Validate OB Oracle direct and OBProxy datasource connections.
- [x] Create OB Oracle direct and OBProxy datasets.
- [x] Enable extract mode, run full refresh, verify cache table is used by preview/chart SQL.
- [x] Run incremental refresh with a watermark field and verify only newer rows are appended.
