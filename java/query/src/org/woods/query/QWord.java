package org.woods.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;

public class QWord {

    private List<Character> rels;

    private List<QCnd> cnds;

    public QWord() {
        rels = new ArrayList<Character>();
        cnds = new ArrayList<QCnd>();
    }

    public List<Character> rels() {
        return rels;
    }

    public List<QCnd> cnds() {
        return cnds;
    }

    public boolean isAllAnd() {
        for (Character c : rels) {
            if (c.charValue() != '&')
                return false;
        }
        return true;
    }

    public boolean isAllOr() {
        for (Character c : rels) {
            if (c.charValue() != '|')
                return false;
        }
        return true;
    }

    public QWord setAll(char c) {
        c = c == '&' ? '&' : '|';
        for (int i = 0; i < rels.size(); i++)
            rels.set(i, c);
        return this;
    }

    public int size() {
        return cnds.size();
    }

    public boolean isEmpty() {
        return cnds.isEmpty();
    }

    public void each(EachCnd each) {
        if (null != each && !isEmpty()) {
            each.invoke(0, cnds.get(0), false);
            for (int i = 1; i < cnds.size(); i++) {
                each.invoke(i,
                            cnds.get(i),
                            (rels.get(i - 1).charValue() == '&'));
            }
        }
    }

    /**
     * 添加一个约束条件
     * 
     * @param cnd
     *            约束条件
     * @param nextAnd
     *            下一个约束条件与自己的关系
     * @return 自身
     */
    public QWord add(QCnd cnd, boolean nextAnd) {
        if (null != cnd) {
            cnds.add(cnd);
            rels.add(nextAnd ? '&' : '|');
        }
        return this;
    }

    /**
     * 当解析结束后调用
     * 
     * @param cnd
     *            最后一个约束条件
     * 
     * @return 自身
     */
    public QWord done(QCnd cnd) {
        // 最后一个约束为空，则弹出最后一个连接符
        if (null == cnd) {
            if (rels.size() > 0)
                rels.remove(rels.size() - 1);
        }
        // 正常增加最后一个约束
        else {
            cnds.add(cnd);
        }
        return this;
    }

    public Map<String, Object> toDumpMap() {
        final NutMap map = new NutMap();
        each(new EachCnd() {
            public void invoke(int index, QCnd cnd, boolean prevIsAnd) {
                if (cnd.getType() == QCndType.IntRegion
                    || cnd.getType() == QCndType.DateRegion) {
                    map.put(cnd.getKey(), cnd.getValue().toString());
                } else {
                    map.put(cnd.getKey(), cnd.getValue());
                }
            }
        });
        return map;
    }

    public String toString() {
        return Json.toJson(toDumpMap(), JsonFormat.compact());
    }

}
