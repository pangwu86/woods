package org.woods.query;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;

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
