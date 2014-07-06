package org.woods.json4excel;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hslf.model.Sheet;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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

    public final String version = "0.1";

    /**
     * 将给定的数据列表datalist, 按照j4eConf中的配置, 生成excel文件
     * 
     * @param dataList
     * @param j4eConf
     * @param excel
     * @return
     */
    public static boolean toExcel(List<T> dataList, J4EConf j4eConf, File excel) {
        // TODO

        return saveExcel(excel, null);
    }

    /**
     * 解析excel文件, 按照j4eConf中的配置, 读取后返回objClz类型的数量列表
     * 
     * @param excel
     * @param j4eConf
     * @param objClz
     * @return
     */
    public static <T> List<T> fromExcel(File excel, J4EConf j4eConf, Class<T> objClz) {
        Workbook wb = loadExcel(excel);
        Mirror<T> mi = Mirror.me(objClz);

        // 读取sheet
        Sheet sheet = null;
        if (!Strings.isBlank(j4eConf.getSheetName())) {
            wb.getSheet(j4eConf.getSheetName());
        }
        if (null == sheet) {
            wb.getSheetAt(j4eConf.getSheetNum());
        }
        if (null == sheet) {
            log.errorf("excel not has sheet at [%d] or name is [%s]",
                       j4eConf.getSheetNum(),
                       j4eConf.getSheetName());
        }
        // 按行读取数据
        List<T> dataList = new ArrayList<T>();
        return dataList;
    }

    /**
     * 读取excel文件, 返回wb对象, 如果读取发生错误, 返回null
     * 
     * @param excel
     * @return
     */
    private static Workbook loadExcel(File excel) {
        Workbook wb = null;
        try {
            wb = WorkbookFactory.create(excel);
        }
        catch (Exception e) {
            log.error(String.format("file %s can't be load to a workbook", excel.getPath()), e);
        }
        return wb;
    }

    /**
     * 保存excel文件, 返回保存是否成功
     * 
     * @param excel
     * @param wb
     * @return
     */
    private static boolean saveExcel(File excel, Workbook wb) {
        try {
            wb.write(new FileOutputStream(excel));
            return true;
        }
        catch (Exception e) {
            log.error(String.format("file %s can't be save as excel", excel.getPath()), e);
        }
        return false;
    }
}
