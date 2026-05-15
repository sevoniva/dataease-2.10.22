package io.dataease.job.schedule;

import io.dataease.dataset.sync.DatasetSyncManage;
import io.dataease.utils.CommonBeanFactory;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
public class DatasetSyncJob extends DeScheduleJob {

    private final DatasetSyncManage datasetSyncManage;

    public DatasetSyncJob() {
        datasetSyncManage = CommonBeanFactory.getBean(DatasetSyncManage.class);
    }

    @Override
    void businessExecute(JobExecutionContext context) {
        datasetSyncManage.execute(datasetTableId, taskId, context);
    }
}
