/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.db.lambda.query;
import net.hasor.db.dialect.BoundSql;
import net.hasor.db.jdbc.core.JdbcTemplate;
import net.hasor.db.lambda.LambdaOperations.LambdaUpdate;
import net.hasor.db.lambda.UpdateExecute;
import net.hasor.db.lambda.segment.MergeSqlSegment;
import net.hasor.db.mapping.FieldInfo;
import net.hasor.db.mapping.MappingRowMapper;
import net.hasor.db.mapping.TableInfo;
import net.hasor.utils.BeanUtils;
import net.hasor.utils.reflect.SFunction;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.hasor.db.lambda.segment.SqlKeyword.*;

/**
 * 提供 lambda update 能力，是 LambdaUpdate 接口的实现类。
 * @version : 2020-10-27
 * @author 赵永春 (zyc@hasor.net)
 */
public class LambdaUpdateWrapper<T> extends AbstractQueryCompare<T, LambdaUpdate<T>> implements LambdaUpdate<T> {
    protected final Map<String, FieldInfo> allowUpdateColumns;
    protected final Map<String, Object>    updateValueMap;
    private         boolean                allowEmptyWhere = false;

    public LambdaUpdateWrapper(Class<T> exampleType, JdbcTemplate jdbcTemplate) {
        super(exampleType, jdbcTemplate);
        this.allowUpdateColumns = this.getAllowUpdateColumns();
        this.updateValueMap = new HashMap<>();
    }

    protected Map<String, FieldInfo> getAllowUpdateColumns() {
        Map<String, FieldInfo> toUpdateField = new LinkedHashMap<>();
        MappingRowMapper<T> rowMapper = this.getRowMapper();
        List<String> columnNames = rowMapper.getColumnNames();
        for (String columnName : columnNames) {
            FieldInfo field = rowMapper.findWriteFieldByColumn(columnName);
            if (field.isUpdate()) {
                toUpdateField.put(field.getColumnName(), field);
            }
        }
        if (toUpdateField.size() == 0) {
            throw new IllegalStateException("no column require UPDATE.");
        }
        return toUpdateField;
    }

    @Override
    protected boolean supportPage() {
        return false;// update is disable Page;
    }

    @Override
    protected LambdaUpdate<T> getSelf() {
        return this;
    }

    @Override
    public LambdaUpdate<T> useQualifier() {
        this.enableQualifier();
        return this;
    }

    @Override
    public UpdateExecute<T> allowEmptyWhere() {
        this.allowEmptyWhere = true;
        return this;
    }

    @Override
    public UpdateExecute<T> applyNewValue(T newValue, List<SFunction<T>> propertyList) throws SQLException {
        if (propertyList == null || propertyList.isEmpty()) {
            throw new NullPointerException("propertyList not be null.");
        }
        Map<String, FieldInfo> updateFieldList = propertyList.stream().map(this::columnName)//
                .collect(Collectors.toMap(FieldInfo::getPropertyName, o -> o));
        //
        return this.applyNewValue(newValue, (Predicate<FieldInfo>) fieldInfo -> {
            return updateFieldList.containsKey(fieldInfo.getPropertyName());
        });
    }

    @Override
    public UpdateExecute<T> applyNewValue(T newValue, Predicate<FieldInfo> tester) throws SQLException {
        if (tester == null) {
            throw new NullPointerException("tester is null.");
        }
        //
        this.updateValueMap.clear();
        for (Map.Entry<String, FieldInfo> allowFieldEntry : allowUpdateColumns.entrySet()) {
            FieldInfo allowField = allowFieldEntry.getValue();
            if (!tester.test(allowField)) {
                continue;
            }
            Object fieldValue = BeanUtils.readPropertyOrField(newValue, allowField.getPropertyName());
            String columnName = allowField.getColumnName();
            if (this.updateValueMap.containsKey(columnName)) {
                throw new SQLException("Multiple property mapping to '" + columnName + "' column");
            } else {
                this.updateValueMap.put(columnName, fieldValue);
            }
        }
        return this;
    }

    @Override
    public BoundSql getOriginalBoundSql() {
        // must be clean , The getOriginalBoundSql will reinitialize.
        this.queryParam.clear();
        //
        // update
        MergeSqlSegment updateTemplate = new MergeSqlSegment();
        updateTemplate.addSegment(UPDATE);
        // tableName
        MappingRowMapper<T> rowMapper = this.getRowMapper();
        TableInfo tableInfo = rowMapper.getTableInfo();
        String tableName = dialect().tableName(isQualifier(), tableInfo.getCategory(), tableInfo.getTableName());
        updateTemplate.addSegment(() -> tableName);
        //
        updateTemplate.addSegment(SET);
        boolean isFirstColumn = false;
        for (String column : updateValueMap.keySet()) {
            if (!isFirstColumn) {
                updateTemplate.addSegment(() -> ",");
                isFirstColumn = true;
            }
            //
            FieldInfo columnInfo = allowUpdateColumns.get(column);
            String columnName = dialect().columnName(isQualifier(), tableInfo.getCategory(), tableInfo.getTableName(), column, columnInfo.getJdbcType(), columnInfo.getJavaType());
            Object columnValue = updateValueMap.get(column);
            updateTemplate.addSegment(() -> columnName, EQ, formatSegment(columnValue));
        }
        //
        if (!this.queryTemplate.isEmpty()) {
            updateTemplate.addSegment(WHERE);
            updateTemplate.addSegment(this.queryTemplate.sub(1));
        } else if (!this.allowEmptyWhere) {
            throw new UnsupportedOperationException("The dangerous UPDATE operation, You must call `allowEmptyWhere()` to enable UPDATE ALL.");
        }
        //
        String sqlQuery = updateTemplate.noFirstSqlSegment();
        Object[] args = this.queryParam.toArray().clone();
        return new BoundSql.BoundSqlObj(sqlQuery, args);
    }

    @Override
    public int doUpdate() throws SQLException {
        BoundSql boundSql = getBoundSql();
        String sqlString = boundSql.getSqlString();
        return this.getJdbcTemplate().executeUpdate(sqlString, boundSql.getArgs());
    }
}
