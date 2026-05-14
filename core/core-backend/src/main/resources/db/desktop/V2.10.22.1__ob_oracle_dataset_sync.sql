CREATE TABLE IF NOT EXISTS `core_dataset_sync_task`
(
    `id`                     bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `dataset_group_id`       bigint       NOT NULL COMMENT '数据集ID',
    `name`                   varchar(255)          DEFAULT NULL COMMENT '任务名称',
    `update_type`            varchar(50)           DEFAULT 'all_scope' COMMENT '更新方式：all_scope 全量，add_scope 增量',
    `incremental_field_id`   bigint                DEFAULT NULL COMMENT '增量字段ID',
    `incremental_last_value` varchar(255)          DEFAULT NULL COMMENT '增量字段上次同步值',
    `start_time`             bigint                DEFAULT NULL COMMENT '开始时间',
    `sync_rate`              varchar(50)           DEFAULT 'RIGHTNOW' COMMENT '执行频率',
    `cron`                   varchar(255)          DEFAULT NULL COMMENT 'cron表达式',
    `simple_cron_value`      bigint                DEFAULT NULL COMMENT '简单重复间隔',
    `simple_cron_type`       varchar(50)           DEFAULT NULL COMMENT '简单重复类型：分、时、天',
    `end_time`               bigint                DEFAULT NULL COMMENT '结束时间',
    `create_time`            bigint                DEFAULT NULL COMMENT '创建时间',
    `update_time`            bigint                DEFAULT NULL COMMENT '更新时间',
    `last_exec_time`         bigint                DEFAULT NULL COMMENT '上次执行时间',
    `last_exec_status`       varchar(50)           DEFAULT NULL COMMENT '上次执行结果',
    `task_status`            varchar(50)           DEFAULT 'WaitingForExecution' COMMENT '任务状态',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_dataset_sync_task_dataset` (`dataset_group_id`)
) COMMENT = '数据集同步任务表';

CREATE TABLE IF NOT EXISTS `core_dataset_sync_task_log`
(
    `id`               bigint      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `dataset_group_id` bigint      NOT NULL COMMENT '数据集ID',
    `task_id`          bigint               DEFAULT NULL COMMENT '任务ID',
    `update_type`      varchar(50)          DEFAULT NULL COMMENT '更新方式',
    `table_name`       varchar(255)         DEFAULT NULL COMMENT '引擎表名',
    `start_time`       bigint               DEFAULT NULL COMMENT '开始时间',
    `end_time`         bigint               DEFAULT NULL COMMENT '结束时间',
    `task_status`      varchar(50)          DEFAULT NULL COMMENT '执行状态',
    `row_count`        bigint               DEFAULT 0 COMMENT '同步行数',
    `info`             text                 DEFAULT NULL COMMENT '执行信息',
    `create_time`      bigint               DEFAULT NULL COMMENT '创建时间',
    `trigger_type`     varchar(50)          DEFAULT NULL COMMENT '触发类型',
    PRIMARY KEY (`id`),
    KEY `idx_dataset_sync_task_log_A` (`dataset_group_id`, `task_id`, `start_time`)
) COMMENT = '数据集同步任务日志表';
