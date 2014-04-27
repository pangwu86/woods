package org.woods.day;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Streams;

/**
 * 加载默认配置, 获取每个年份的假期设置
 * 
 * 
 * @author pw
 * 
 */
public class Days {

    /**
     * key : 2013
     * 
     * value : [{}, {}, {}]
     */
    private static Map<String, List<Holiday>> daysMap = new HashMap<String, List<Holiday>>();

    private static String[] yyyylist = new String[]{"2014"};

    static {
        // 加载内置的配置
        for (String yyyy : yyyylist) {
            Streams.readAndClose(new InputStreamReader(Days.class.getResourceAsStream("/holiday"
                                                                                      + yyyy)));
        }
    }

    /**
     * 配置项加入到对应的年份中
     * 
     * @param yyyy
     *            例如2013
     * @param holiConf
     *            一年中各种假期的配置信息
     */
    public static void addHolidayConf(String yyyy, String holiConf) {
        addHolidayConf(yyyy, Json.fromJsonAsList(Holiday.class, holiConf));
    }

    /**
     * 配置项加入到对应的年份中
     * 
     * @param yyyy
     *            例如2013
     * @param holidays
     *            一年中各种假期的配置信息
     */
    public static void addHolidayConf(String yyyy, List<Holiday> holidays) {

    }

}
