ALTER TABLE `core_dataset_sync_task`
    ADD COLUMN `cache_ready` tinyint(1) DEFAULT 0 COMMENT '缓存表是否已可用';

UPDATE `core_dataset_sync_task`
SET `cache_ready` = 1
WHERE `last_exec_status` = 'Completed';
