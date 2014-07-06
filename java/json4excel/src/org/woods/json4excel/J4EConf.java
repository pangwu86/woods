package org.woods.json4excel;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Disks;
import org.woods.json4excel.annotation.J4EName;

public class J4EConf {

    // excel中的sheet的index, 从1开始
    private Integer sheetIndex;

    // excel中sheet的名字
    private String sheetName;

    // sheet中对应的列
    private List<J4EColumn> columns;

    public Integer getSheetIndex() {
        return sheetIndex;
    }

    public void setSheetIndex(Integer sheetIndex) {
        this.sheetIndex = sheetIndex;
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

    public static J4EConf from(Class<?> clz) {
        J4EConf jc = new J4EConf();
        // sheet
        String sheetName = clz.getSimpleName();
        J4EName cName = clz.getAnnotation(J4EName.class);
        if (cName != null && !Strings.isBlank(cName.value())) {
            sheetName = cName.value();
        }
        jc.setSheetName(sheetName);
        // columns
        List<J4EColumn> jclist = new ArrayList<J4EColumn>();
        int index = 0;
        Mirror<?> mc = Mirror.me(clz);
        for (Field cf : mc.getFields()) {
            J4EColumn jcol = new J4EColumn();
            jcol.setFieldName(cf.getName());
            jcol.setColumnName(cf.getName());
            jcol.setColumnIndex(index++);
            J4EName fName = cf.getAnnotation(J4EName.class);
            if (fName != null && !Strings.isBlank(fName.value())) {
                jcol.setColumnName(fName.value());
            }
            jclist.add(jcol);
        }
        jc.setColumns(jclist);
        return jc;
    }

    public static J4EConf from(File confFile) {
        return Json.fromJsonFile(J4EConf.class, confFile);
    }

    public static J4EConf from(String confPath) {
        return Json.fromJsonFile(J4EConf.class, new File(Disks.absolute(confPath)));
    }

    public static J4EConf from(Reader confReader) {
        return Json.fromJson(J4EConf.class, confReader);
    }

    public static J4EConf from(InputStream confInputStream) {
        return Json.fromJson(J4EConf.class, new InputStreamReader(confInputStream));
    }

    public static J4EConf fromConfStr(CharSequence confStr) {
        return Json.fromJson(J4EConf.class, confStr);
    }
}
