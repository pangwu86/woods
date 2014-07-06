package org.woods.json4excel;

public class J4EColumn {

    // 在对应obj中的属性名称
    private String name;

    // 在excel中的列, 从1开始计算
    private int columnNum;

    // 在excel中的标题名字
    private String columnName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public int getColumnNum() {
        return columnNum;
    }

    public void setColumnNum(int columnNum) {
        this.columnNum = columnNum;
    }

}
