package io.dataease.datasource.provider;

import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CalciteProviderObOracleCommentsTest {

    @Test
    public void applyColumnCommentsUsesOracleDictionaryName() {
        TableField id = field("ID", "ID");
        TableField name = field("USER_NAME", "USER_NAME");
        Map<String, String> comments = new HashMap<>();
        comments.put("ID", "主键ID");
        comments.put("USER_NAME", "用户名称");

        CalciteProvider.applyOracleColumnComments(java.util.Arrays.asList(id, name), comments);

        assertEquals("主键ID", id.getName());
        assertEquals("用户名称", name.getName());
    }

    @Test
    public void applyColumnCommentsKeepsFallbackNameWhenCommentIsBlank() {
        TableField amount = field("AMOUNT", "AMOUNT");
        Map<String, String> comments = new HashMap<>();
        comments.put("AMOUNT", " ");

        CalciteProvider.applyOracleColumnComments(java.util.Collections.singletonList(amount), comments);

        assertEquals("AMOUNT", amount.getName());
    }

    @Test
    public void parseDatasourceConfigurationUsesObOracleDefaultSchema() throws Exception {
        CalciteProvider provider = new CalciteProvider();
        Method method = CalciteProvider.class.getDeclaredMethod("parseDatasourceConfiguration", String.class, String.class);
        method.setAccessible(true);

        DatasourceConfiguration configuration = (DatasourceConfiguration) method.invoke(
                provider,
                "{\"username\":\"test@obora#obdemo\"}",
                "obOracle"
        );

        assertEquals("TEST", configuration.getSchema());
    }

    private TableField field(String originName, String name) {
        TableField field = new TableField();
        field.setOriginName(originName);
        field.setName(name);
        return field;
    }
}
