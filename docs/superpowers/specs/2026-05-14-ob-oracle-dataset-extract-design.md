# OB Oracle Dataset Extract Design

## Goal

Support OB Oracle datasets in a DataEase-native extract mode: DataEase executes the dataset rule SQL, stores the dataset result in an internal DE Engine table, and dashboards read that internal table on schedule.

## Product Semantics

- OB Oracle datasources remain normal live datasources.
- Extract mode is a dataset-level option, not datasource-level table replication.
- The extracted object is the dataset result after DataEase modeling rules are applied.
- Dashboards and dataset preview route to the internal extracted table only for OB Oracle datasets that explicitly enable mode `1`.
- Routing to the internal table requires a completed cache initialization (`cache_ready = 1`), so a newly enabled extract dataset still remains readable before its first successful sync.
- Existing Excel/API mode `1` behavior must not be rerouted to the OB Oracle extract table naming scheme.

## Refresh Modes

- Full refresh creates a temporary internal table, loads the full dataset result, then atomically swaps it into the active table.
- Incremental refresh is allowed only when the user selects a normal checked dataset field as the watermark.
- The first incremental run falls back to full refresh to establish the cache and watermark.
- Incremental refresh appends rows where the watermark is greater than the last saved value.
- Delete synchronization and generic CDC are out of scope.

## Internal Table

- Table name: `de_sync_dataset_<datasetGroupId>`.
- Temporary table: `tmp_de_sync_dataset_<datasetGroupId>`.
- Columns use DataEase field aliases (`dataeaseName`/`fieldShortName`) so chart SQL can query the cache table with the same semantic field names.

## Scheduling

- Manual, simple cron, and cron schedules reuse DataEase Quartz scheduling.
- Task state and logs are stored in `core_dataset_sync_task` and `core_dataset_sync_task_log`.
- `core_dataset_sync_task.cache_ready` is set after the first successful sync and is not cleared by later failures.
- On dataset deletion, schedule metadata, logs, and internal tables are cleaned up.

## Safety Boundaries

- Only non-cross OB Oracle datasets are eligible.
- Cross-source datasets, existing Excel/API datasets, and non-OB database datasets stay on their current DataEase paths.
- Failed full refresh keeps the previous active cache table.
- Failed incremental refresh does not advance the saved watermark.
- If a later scheduled sync fails after a prior success, dashboards continue to use the last ready cache.

## Local Verification

- Runtime URL: `http://127.0.0.1:18100`.
- Source table: `TEST.DE_V2_SYNC_TEST`.
- Direct datasource: `127.0.0.1:2881`, user `test@obora`.
- OBProxy datasource: `127.0.0.1:2883`, user `test@obora#obdemo`.
- Created datasets:
  - `OB Oracle 直连 抽取数据集 20260514174810`, id `1252358882693615616`.
  - `OB Oracle Proxy 抽取数据集 20260514174811`, id `1252358885470244864`.
- Verified full sync loaded 3 rows for both datasets.
- Verified incremental sync appended rows 4, 5, and 6 using `UPDATED_AT` as watermark.
- Verified cache routing by inserting row 6 into OB first: dataset preview stayed at 5 rows before sync and changed to 6 rows only after incremental sync.
