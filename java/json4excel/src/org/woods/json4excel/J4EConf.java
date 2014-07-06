package org.woods.json4excel;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.nutz.json.Json;

public class J4EConf {

    // excel中的sheet的index, 从1开始
    private int sheetNum;

    // excel中sheet的名字
    private String sheetName;

    //
    private List<J4EColumn> columns;

    public int getSheetNum() {
        return sheetNum;
    }

    public void setSheetNum(int sheetNum) {
        this.sheetNum = sheetNum;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public List<J4EColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<J4EColumn> columns) {
        this.columns = columns;
    }

    // ===================== 生成J4EConf的快捷方法

    public static J4EConf FROM(Class<?> clz) {
        return null;
    }

    public static J4EConf FROM(File confFile) {
        return Json.fromJsonFile(J4EConf.class, confFile);
    }

    public static J4EConf FROM(String confPath) {
        return Json.fromJsonFile(J4EConf.class, new File(confPath));
    }

    public static J4EConf FROM(Reader confReader) {
        return Json.fromJson(J4EConf.class, confReader);
    }

    public static J4EConf FROM(InputStream confInputStream) {
        return Json.fromJson(J4EConf.class, new InputStreamReader(confInputStream));
    }

    public static J4EConf FROMConfStr(CharSequence confStr) {
        return Json.fromJson(J4EConf.class, confStr);
    }
}
