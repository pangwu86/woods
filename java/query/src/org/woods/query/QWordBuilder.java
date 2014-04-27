package org.woods.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Nums;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.Region;

public class QWordBuilder {

    private String gOr;
    private String gAnd;

    private char[] sepOr;
    private char[] sepAnd;

    private char[] quoteBegin;
    private char[] quoteEnd;

    private char[] bracketBegin;
    private char[] bracketEnd;

    private List<QWordRule> rules;

    public QWordBuilder(File f) {
        this(Streams.fileInr(f));
    }

    public QWordBuilder(Reader r) {
        this();
        this.loadRules(r);
    }

    public QWordBuilder() {
        gOr = "OR";
        gAnd = "AND";
        sepOr = new char[]{' ', '\t', '|', '\n'};
        sepAnd = new char[]{',', '&'};
        quoteBegin = new char[]{'"', '\''};
        quoteEnd = new char[]{'"', '\''};
        bracketBegin = new char[]{'[', '(', '{'};
        bracketEnd = new char[]{']', ')', '}'};

        rules = new ArrayList<QWordRule>();
    }

    public QWordBuilder setup(String json) {
        return setup(Lang.map(json));
    }

    public QWordBuilder setup(Map<String, Object> map) {
        gOr = Strings.sBlank(map.get("gOr"), gOr);
        gAnd = Strings.sBlank(map.get("gAnd"), gAnd);
        sepOr = Strings.sBlank(map.get("sepOr"), new String(sepOr)).toCharArray();
        sepAnd = Strings.sBlank(map.get("sepAnd"), new String(sepAnd)).toCharArray();
        return this;
    }

    public QWordBuilder loadRulesStr(String rule) {
        return this.loadRules(new StringReader(rule));
    }

    public QWordBuilder loadRules(Reader r) {
        BufferedReader br = Streams.buffr(r);

        // 按行读取
        try {
            String line;
            int lineNumber = 0;
            while (null != (line = br.readLine())) {
                lineNumber++;
                line = Strings.trim(line);
                // 忽略空行和注释行
                if (Strings.isEmpty(line) || line.startsWith("#"))
                    continue;

                // 如果行以 $ 开头表示一个约束描述
                if (line.startsWith("$")) {
                    QWordRule qr = new QWordRule();
                    // 寻找到第一个 :
                    int pos = line.indexOf(':');
                    // 神码？竟敢木油冒号？！ 抛错对付你 >:D
                    if (pos == -1)
                        throw Lang.makeThrow("invalid rule line %d : %s", lineNumber, line);
                    // 获取名称
                    qr.key = Strings.trim(line.substring(1, pos));
                    String regex;

                    // 看看是否是简要模式
                    if (line.charAt(pos + 1) == ':') {
                        regex = "^(" + Strings.trim(line.substring(pos + 2)) + ")(.*)$";
                    }
                    // 普通模式
                    else {
                        regex = Strings.trim(line.substring(pos + 1));
                    }
                    // 得到正则表达式
                    qr.regex = Pattern.compile(regex);

                    // 再读一行作为详细描述
                    line = Strings.trim(br.readLine());
                    lineNumber++;

                    // 分隔
                    pos = line.lastIndexOf('=');

                    // 神码？竟敢木油等号？！ 抛错对付你 >:D
                    if (pos == -1)
                        throw Lang.makeThrow("invalid rule line %d : %s", lineNumber, line);

                    qr.type = QCndType.valueOf(Strings.trim(line.substring(pos + 1)));
                    qr.seg = Segments.create(Strings.trim(line.substring(0, pos)));

                    rules.add(qr);

                }
                // 其他的忽略

            }
        }
        catch (IOException e) {
            throw Lang.wrapThrow(e);
        }

        return this;
    }

    public QWord parse(String kwd) {
        if (Strings.isBlank(kwd))
            return null;

        // 建立返回值
        QWord w = new QWord();

        // 判断是否全局为 OR
        Boolean gIsOr = null;
        Boolean gIsAnd = null;
        if (null != gOr && kwd.startsWith(gOr + ":")) {
            kwd = kwd.substring(gOr.length() + 1);
            gIsOr = true;
            w.setDefaultAllAnd(false);
        }
        if (null != gAnd && kwd.startsWith(gAnd + ":")) {
            kwd = kwd.substring(gAnd.length() + 1);
            gIsAnd = true;
            w.setDefaultAllAnd(true);
        }

        // 拆分字符串
        List<String> flds = new ArrayList<String>();
        List<Boolean> seps = new ArrayList<Boolean>();

        _kwd_to_flds(kwd, flds, seps);

        // 准备解析
        Context c = Lang.context();
        int i = 0;
        for (; i < seps.size(); i++) {
            String fld = flds.get(i);
            QCnd cnd = _eval_cnd(c, fld);

            // 加入到关键字中
            if (null != cnd) {
                boolean nextIsAnd = seps.get(i);
                // AND:xxxxx
                if (null != gIsAnd && gIsAnd.booleanValue()) {
                    nextIsAnd = true;
                }
                // OR:xxxx
                else if (null != gIsOr && gIsOr.booleanValue()) {
                    nextIsAnd = false;
                }

                w.add(cnd, nextIsAnd);
            }
        }

        // 处理最后一个
        w.done(_eval_cnd(c, flds.get(i)));

        return w;
    }

    private QCnd _eval_cnd(Context c, String fld) {
        // 首先匹配各个规则
        for (QWordRule rule : rules) {
            Matcher m = rule.regex.matcher(fld);
            if (m.find()) {
                // 收集各个组
                c.clear();
                for (int x = 0; x <= m.groupCount(); x++) {
                    c.set("" + x, m.group(x));
                }

                // 得到抽出字符串
                String str = rule.seg.render(c).toString();

                // FIXME, 空字符串也是一种信息哟
                // 抽出空字符串，那么就表示这个规则啥都木有
                // if (Strings.isBlank(str))
                // return null;

                // 设置与约束
                QCnd cnd = new QCnd();
                cnd.setKey(rule.key);
                cnd.setOrigin(fld);
                cnd.setPlain(str);
                cnd.setType(rule.type);

                // 生成值
                switch (rule.type) {
                case INT:
                    return cnd.setValue(Integer.valueOf(str));
                case IntRegion:
                    Region<Integer> rI = Region.Int(str);
                    return rI.isNull() ? null : cnd.setValue(rI);
                case LongRegion:
                    Region<Long> rL = Region.Long(str);
                    return rL.isNull() ? null : cnd.setValue(rL);
                case DateRegion:
                    Region<Date> rD = Region.Date(str);
                    return rD.isNull() ? null : cnd.setValue(rD);
                case StringEnum:
                    String[] ss = Strings.splitIgnoreBlank(str);
                    return ss == null || ss.length == 0 ? null : cnd.setValue(ss);
                case IntEnum:
                    int[] ii = Nums.splitInt(str);
                    return ii == null || ii.length == 0 ? null : cnd.setValue(ii);
                case Regex:
                    try {
                        return cnd.setValue(Pattern.compile(str));
                    }
                    catch (Exception e) {
                        return null;
                    }
                case Json:
                    try {
                        return cnd.setValue(Lang.map(str));
                    }
                    catch (Exception e) {
                        return null;
                    }

                default:
                    return cnd.setType(QCndType.String).setValue(str);
                }
            }
        }
        return null;
    }

    private void _kwd_to_flds(String kwd, List<String> flds, List<Boolean> seps) {

        char[] cs = kwd.toCharArray();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < cs.length; i++) {
            char c = cs[i];
            // 如果是 AND 的连接符
            if (Nums.isin(sepAnd, c)) {
                if (!Strings.isBlank(sb)) {
                    flds.add(sb.toString());
                    seps.add(Boolean.TRUE);
                    sb = new StringBuilder();
                }
                // 如果已经有字段了，那么将最后一个连接符设为 "AND"
                else if (!flds.isEmpty()) {
                    seps.set(flds.size() - 1, Boolean.TRUE);
                }
                continue;
            }
            // 如果是 OR 的连接符
            if (Nums.isin(sepOr, c)) {
                if (!Strings.isBlank(sb)) {
                    flds.add(sb.toString());
                    seps.add(Boolean.FALSE);
                    sb = new StringBuilder();
                }
                continue;
            }
            // 空白字符，忽略
            if (Character.isWhitespace(c)) {
                continue;
            }
            // 如果是字符串或者开始，一直读到字符串的结尾
            int pos;
            if ((pos = Nums.indexOf(quoteBegin, c)) >= 0) {
                for (i++; i < cs.length; i++) {
                    c = cs[i];
                    // 读完了字符串
                    if (quoteEnd[pos] == c) {
                        break;
                    }
                    // 逃逸字符
                    else if (c == '\\') {
                        i++;
                        sb.append(cs[i]);
                    }
                    // 其他字符统统读入
                    else {
                        sb.append(c);
                    }
                }
                continue;
            }
            // 如果开始括弧，一直读到括弧结束
            if ((pos = Nums.indexOf(bracketBegin, c)) >= 0) {
                sb.append(c);
                for (i++; i < cs.length; i++) {
                    c = cs[i];
                    sb.append(c);
                    // 读完了括弧
                    if (bracketEnd[pos] == c) {
                        break;
                    }
                }
                continue;
            }
            // 其他的字符，就是普通的增加就是
            sb.append(c);
        }
        // 确保最后一个字段加入列表
        if (sb.length() > 0) {
            flds.add(Strings.trim(sb.toString()));
        }

    }

}
