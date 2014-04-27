package org.woods.day;

import java.util.ArrayList;
import java.util.List;

/**
 * 假期放假的日子与对应的调休日子
 * 
 * <pre>
 * {
 *     name : "元旦",
 *     holiday : ["01-01"],
 *     workday : ["01-05"]
 * }
 * </pre>
 * 
 * @author pw
 */
public class Holiday {

    private String name;

    private List<String> holidays;

    private List<String> workdays;

    public String getName() {
        return name;
    }

    public List<String> getHolidays() {
        return holidays;
    }

    public List<String> getWorkdays() {
        return workdays;
    }

    public Holiday(String name) {
        this.name = name;
        this.holidays = new ArrayList<String>();
        this.workdays = new ArrayList<String>();
    }

    public Holiday addHoliday(String holiday) {
        holidays.add(holiday);
        return this;
    }

    public Holiday addWorkday(String workday) {
        workdays.add(workday);
        return this;
    }
}
