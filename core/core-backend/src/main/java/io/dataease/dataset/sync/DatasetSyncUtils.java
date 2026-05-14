package io.dataease.dataset.sync;

import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.dataset.dao.auto.entity.CoreDatasetSyncTask;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DatasetSyncUtils {

    public static final String CACHE_TABLE_PREFIX = "de_sync_dataset_";

    private DatasetSyncUtils() {
    }

    public static String cacheTableName(Long datasetGroupId) {
        return CACHE_TABLE_PREFIX + datasetGroupId;
    }

    public static String tmpCacheTableName(Long datasetGroupId) {
        return "tmp_" + cacheTableName(datasetGroupId);
    }

    public static boolean shouldRouteToCache(DatasetGroupInfoDTO dataset, Map<String, Object> sqlMap) {
        if (dataset == null || sqlMap == null || !Objects.equals(dataset.getMode(), 1)
                || Objects.equals(dataset.getIsCross(), true)) {
            return false;
        }
        Object dsMapObject = sqlMap.get("dsMap");
        if (!(dsMapObject instanceof Map<?, ?> dsMap) || dsMap.size() != 1) {
            return false;
        }
        Object datasourceObject = dsMap.values().iterator().next();
        if (!(datasourceObject instanceof DatasourceSchemaDTO datasource)) {
            return false;
        }
        return StringUtils.equalsIgnoreCase(datasource.getType(), DatasourceConfiguration.DatasourceType.obOracle.name());
    }

    public static boolean isCacheReady(CoreDatasetSyncTask task) {
        return task != null && Objects.equals(task.getCacheReady(), 1);
    }

    public static List<TableField> toEngineTableFields(List<DatasetTableFieldDTO> fields) {
        List<TableField> tableFields = new ArrayList<>();
        if (fields == null) {
            return tableFields;
        }
        for (DatasetTableFieldDTO field : fields) {
            if (!Objects.equals(field.getChecked(), true)) {
                continue;
            }
            if (!Objects.equals(field.getExtField(), ExtFieldConstant.EXT_NORMAL)) {
                continue;
            }
            String columnName = StringUtils.defaultIfBlank(field.getDataeaseName(), field.getFieldShortName());
            if (StringUtils.isBlank(columnName)) {
                continue;
            }
            TableField tableField = new TableField();
            tableField.setName(columnName);
            tableField.setOriginName(columnName);
            tableField.setFieldType(field.getName());
            tableField.setType(field.getType());
            tableField.setDeExtractType(field.getDeExtractType());
            tableField.setDeType(field.getDeType());
            tableField.setChecked(true);
            tableFields.add(tableField);
        }
        return tableFields;
    }

    public static String buildIncrementalPredicate(DatasetTableFieldDTO field, String lastValue, String prefix, String suffix) {
        String column = prefix + StringUtils.defaultIfBlank(field.getDataeaseName(), field.getFieldShortName()) + suffix;
        if (Objects.equals(field.getDeExtractType(), 1) || Objects.equals(field.getDeType(), 1)) {
            return column + " > TO_TIMESTAMP('" + escapeSqlLiteral(lastValue) + "', 'YYYY-MM-DD HH24:MI:SS.FF')";
        }
        if (Objects.equals(field.getDeExtractType(), 2) || Objects.equals(field.getDeExtractType(), 3)
                || Objects.equals(field.getDeType(), 2) || Objects.equals(field.getDeType(), 3)) {
            return column + " > " + lastValue;
        }
        return column + " > '" + escapeSqlLiteral(lastValue) + "'";
    }

    public static String buildCacheSelectSql(Long datasetGroupId, List<DatasetTableFieldDTO> fields, String prefix, String suffix, String schemaAlias) {
        String selectFields = fields.stream()
                .filter(field -> Objects.equals(field.getChecked(), true))
                .filter(field -> Objects.equals(field.getExtField(), ExtFieldConstant.EXT_NORMAL))
                .map(field -> StringUtils.defaultIfBlank(field.getDataeaseName(), field.getFieldShortName()))
                .filter(StringUtils::isNotBlank)
                .map(fieldName -> quote(fieldName, prefix, suffix) + " AS " + quote(fieldName, prefix, suffix))
                .collect(Collectors.joining(","));
        String table = quote(cacheTableName(datasetGroupId), prefix, suffix);
        if (StringUtils.isNotBlank(schemaAlias)) {
            table = quote(schemaAlias, prefix, suffix) + "." + table;
        }
        return "SELECT " + selectFields + " FROM " + table;
    }

    public static String buildOraclePageSql(String sourceSql, int limit, int offset, List<DatasetTableFieldDTO> fields, String prefix, String suffix) {
        String selectFields = fields.stream()
                .filter(field -> Objects.equals(field.getChecked(), true))
                .filter(field -> Objects.equals(field.getExtField(), ExtFieldConstant.EXT_NORMAL))
                .map(field -> StringUtils.defaultIfBlank(field.getDataeaseName(), field.getFieldShortName()))
                .filter(StringUtils::isNotBlank)
                .map(fieldName -> "DE_SYNC_PAGE." + quote(fieldName, prefix, suffix))
                .collect(Collectors.joining(","));
        return "SELECT " + selectFields
                + " FROM (SELECT DE_SYNC_SRC.*, ROWNUM AS DE_ROWNUM FROM (" + sourceSql + ") DE_SYNC_SRC WHERE ROWNUM <= "
                + (offset + limit) + ") DE_SYNC_PAGE WHERE DE_ROWNUM > " + offset;
    }

    public static String quote(String value, String prefix, String suffix) {
        return prefix + value + suffix;
    }

    public static String escapeSqlLiteral(String value) {
        return StringUtils.defaultString(value).replace("'", "''");
    }
}
