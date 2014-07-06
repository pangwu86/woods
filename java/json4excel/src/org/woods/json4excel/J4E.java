package org.woods.json4excel;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.nutz.json.Json;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;

/**
 * 根据json配置文件, 读取或导出excel文件
 * 
 * @author pw
 * 
 */
public class J4E {

    private J4E() {}

    private final static Log log = Logs.get();

    /**
     * 将给定的数据列表datalist, 按照j4eConf中的配置, 输出到out
     * 
     * @param out
     *            输出流
     * @param objClz
     *            转换后的对象Class, 对应一行数据
     * @param j4eConf
     *            转换配置(非必须, 可自动生成)
     * 
     * @return 是否转换并写入成功
     */
    public static <T> boolean toExcel(OutputStream out, List<?> dataList, J4EConf j4eConf) {
        if (dataList == null || dataList.size() == 0) {
            log.warn("datalist is empty! can't convert to excel");
            return false;
        }
        if (null == j4eConf) {
            j4eConf = J4EConf.from(dataList.get(0).getClass());
        }

        return saveExcel(out, null);
    }

    /**
     * 解析输入流, 按照j4eConf中的配置, 读取后返回objClz类型的数量列表
     * 
     * @param in
     *            输入流
     * @param objClz
     *            转换后的对象Class, 对应一行数据
     * @param j4eConf
     *            转换配置(非必须, 可自动生成)
     * @return 数据列表
     */
    public static <T> List<T> fromExcel(InputStream in, Class<T> objClz, J4EConf j4eConf) {
        if (null == j4eConf) {
            j4eConf = J4EConf.from(objClz);
        }
        Workbook wb = loadExcel(in);
        Mirror<T> mc = Mirror.me(objClz);
        // 读取sheet
        Sheet sheet = null;
        if (!Strings.isBlank(j4eConf.getSheetName())) {
            sheet = wb.getSheet(j4eConf.getSheetName());
        }
        if (null == sheet) {
            sheet = wb.getSheetAt(j4eConf.getSheetIndex());
        }
        if (null == sheet) {
            log.errorf("excel not has sheet at [%d] or sheetName is [%s]",
                       j4eConf.getSheetIndex(),
                       j4eConf.getSheetName());
            return null;
        }
        // 按行读取数据
        List<T> dataList = new ArrayList<T>();
        Iterator<Row> rlist = sheet.rowIterator();
        boolean firstRow = true;
        while (rlist.hasNext()) {
            Row row = rlist.next();
            if (firstRow) {
                // 确定column的index
                Iterator<Cell> clist = row.cellIterator();
                int cindex = 0;
                Map<String, Integer> headIndexMap = new HashMap<String, Integer>();
                while (clist.hasNext()) {
                    Cell chead = clist.next();
                    headIndexMap.put(cellValue(chead), cindex++);
                }
                for (J4EColumn jcol : j4eConf.getColumns()) {
                    if (null != headIndexMap.get(jcol.getColumnName())) {
                        // by columnName
                        jcol.setColumnIndex(headIndexMap.get(jcol.getColumnName()));
                    } else if (null != headIndexMap.get(jcol.getFieldName())) {
                        // by field
                        jcol.setColumnIndex(headIndexMap.get(jcol.getFieldName()));
                    } else if (null != jcol.getColumnIndex() && jcol.getColumnIndex() >= 0) {
                        // 已经设置过的index ??? 这个提醒一下
                        log.warnf("J4EColumn has already set index[%d], but not sure It is right",
                                  jcol.getColumnIndex());
                    } else {
                        jcol.setColumnIndex(-1);
                    }
                    // 查找field
                    if (jcol.getColumnIndex() != null && jcol.getColumnIndex() >= 0) {
                        try {
                            Field cfield = mc.getField(jcol.getFieldName());
                            jcol.setField(cfield);
                        }
                        catch (NoSuchFieldException e) {
                            log.warnf("can't find Field[%s] in Class[%s]",
                                      jcol.getFieldName(),
                                      objClz.getName());
                        }
                    }
                }
                log.debugf("J4EConf-Columns : \n%s", Json.toJson(j4eConf.getColumns()));
                firstRow = false;
                continue;
            }
            // 从第二行开始读数据
            T rVal = rowValue(row, j4eConf, mc);
            dataList.add(rVal);
        }
        return dataList;
    }

    private static <T> T rowValue(Row row, J4EConf j4eConf, Mirror<T> mc) {
        // FIXME 必须有标准构造函数
        T rVal = mc.born();
        for (J4EColumn jcol : j4eConf.getColumns()) {
            Field jfield = jcol.getField();
            if (null != jfield) {
                String cVal = cellValue(row.getCell(jcol.getColumnIndex()));
                mc.setValue(rVal, jfield, cVal);
            }
        }
        return rVal;
    }

    private static String cellValue(Cell c) {
        try {
            int cType = c.getCellType();
            switch (cType) {
            case Cell.CELL_TYPE_NUMERIC: // 数字
                c.setCellType(Cell.CELL_TYPE_STRING);
                return c.getStringCellValue();
            case Cell.CELL_TYPE_STRING: // 字符串
                return c.getStringCellValue();
            case Cell.CELL_TYPE_BOOLEAN: // boolean
                return String.valueOf(c.getBooleanCellValue());
            default:
                return c.getStringCellValue();
            }
        }
        catch (Exception e) {
            log.error(String.format("cell [%d, %d] has error, value can't convert to string",
                                    c.getRowIndex(),
                                    c.getColumnIndex()),
                      e);
            return "";
        }
    }

    /**
     * 读取excel文件, 返回wb对象, 如果读取发生错误, 返回null
     * 
     * @param excel
     * @return
     */
    private static Workbook loadExcel(InputStream in) {
        Workbook wb = null;
        try {
            // 因为HSSF与XSSF的不同, 导致返回的sheet对象能有不同, 暂时先使用HSSF
            // FIXME 稍后实现两种, XSSF使用更少的内存, 但仅仅能访问xlsx
            wb = new HSSFWorkbook(in);
        }
        catch (Exception e) {
            log.error("can't load inputstream for a workbook", e);
        }
        return wb;
    }

    /**
     * 保存excel文件, 返回保存是否成功
     * 
     * @param out
     * @param wb
     * @return
     */
    private static boolean saveExcel(OutputStream out, Workbook wb) {
        try {
            wb.write(out);
            return true;
        }
        catch (Exception e) {
            log.error("can't write wookbook to outputstream", e);
        }
        return false;
    }
}
