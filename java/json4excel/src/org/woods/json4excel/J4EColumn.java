package org.woods.json4excel;

import java.lang.reflect.Field;

import org.nutz.json.JsonIgnore;

public class J4EColumn {

    // 在对应obj中的属性名称
    private String fieldName;

    // 在excel中的列, 从1开始计算
    private Integer columnIndex;

    // 在excel中的标题名字
    private String columnName;

    // 真实的field
    @JsonIgnore
    private Field field;

    void setField(Field field) {
        this.field = field;
    }

    Field getField() {
        return field;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Integer getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(Integer columnIndex) {
        this.columnIndex = columnIndex;
    }

}
