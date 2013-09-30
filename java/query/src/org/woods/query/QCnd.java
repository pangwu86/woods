package org.woods.query;

import java.util.Date;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Region;

public class QCnd {

    private String key;

    private String origin;

    private String plain;

    private QCndType type;

    private Object value;

    public String toValueString() {
        switch (type) {
        case IntRegion:
        case LongRegion:
        case DateRegion:
        case Regex:
        case INT:
        case String:
            return value.toString();
        case StringEnum:
            return "(" + Castors.me().castToString(value) + ")";
        case IntEnum:
            return "(" + Castors.me().castToString(value) + ")";
        case Json:
            return Json.toJson(value, JsonFormat.compact());
        }
        return "<NULL TYPE>";
    }

    @SuppressWarnings("unchecked")
    public Region<Integer> asIntRegion() {
        return (Region<Integer>) value;
    }

    @SuppressWarnings("unchecked")
    public Region<Long> asLongRegion() {
        return (Region<Long>) value;
    }

    @SuppressWarnings("unchecked")
    public Region<Date> asDateRegion() {
        return (Region<Date>) value;
    }

    public Pattern asRegex() {
        return (Pattern) value;
    }

    public String asString() {
        return (String) value;
    }

    public int asInt() {
        return (Integer) value;
    }

    public int[] asIntEnum() {
        return (int[]) value;
    }

    public String[] asStringEnum() {
        return (String[]) value;
    }

    public String[] asStringLowerEnum() {
        String[] ss = (String[]) value;
        String[] arr = new String[ss.length];
        for (int i = 0; i < ss.length; i++) {
            arr[i] = Strings.sNull(ss[i].toLowerCase(), "");
        }
        return arr;
    }

    public String[] asStringUpperEnum() {
        String[] ss = (String[]) value;
        String[] arr = new String[ss.length];
        for (int i = 0; i < ss.length; i++) {
            arr[i] = Strings.sNull(ss[i].toUpperCase(), "");
        }
        return arr;
    }

    public NutMap asJson() {
        return (NutMap) value;
    }

    public String getKey() {
        return key;
    }

    public QCnd setKey(String key) {
        this.key = key;
        return this;
    }

    public String getOrigin() {
        return origin;
    }

    public QCnd setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public String getPlain() {
        return plain;
    }

    public QCnd setPlain(String plain) {
        this.plain = plain;
        return this;
    }

    public QCndType getType() {
        return type;
    }

    public QCnd setType(QCndType type) {
        this.type = type;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public QCnd setValue(Object value) {
        this.value = value;
        return this;
    }

}
