package org.woods.query;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.nutz.lang.Lang.map;

import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;

public class QWordBuilderRule1Test {

    @Test
    public void test_g_and_or() {
        _c(Q("lv(0,4) , T(sin,org)"), "{lv:'(0,4)', tp:['sin','org']}", true);
        _c(Q("OR:lv(0,4)  T(sin,org)"), "{lv:'(0,4)', tp:['sin','org']}", false);
        _c(Q("lv(0,4)  T(sin,org)"), "{lv:'(0,4)', tp:['sin','org']}", false);
        _c(Q("AND:lv(0,4)  T(sin,org)"), "{lv:'(0,4)', tp:['sin','org']}", true);
        _c(Q("OR:lv(0,4)"), "{lv:'(0,4)'}", false);
    }

    @Test
    public void test_by_d() {
        _c(Q("d0"), "{d:0}");
        _c(Q("d23"), "{d:23}");
    }

    @Test
    public void test_by_alias() {
        _c(Q("Peter"), "{text:'Peter'}");
    }

    @Test
    public void test_by_live() {
        _c_nil(Q("lv()"));
        _c_nil(Q("lv(  \t )"));
        _c(Q("lv(0,4)"), "{lv:'(0,4)'}");
        _c(Q("lv[4,0)"), "{lv:'[0,4)'}");
        _c(Q("lv[3)"), "{lv:'[3]'}");
    }

    @Test
    public void test_by_label() {
        _c_nil(Q("#()"));
        _c_nil(Q("#(  \t )"));
        _c(Q("#(A,B)"), "{lbs:['A','B']}");
        _c(Q("#(A)"), "{lbs:['A']}");
    }

    @Test
    public void test_by_lastModified() {
        _c_nil(Q("L()"));
        _c_nil(Q("L(  \t )"));
        _c(Q("L(2013-9-21,2014-2-16)"), "{lm:'(2013-09-21,2014-02-16)'}");
        _c(Q("L[2013-9-21,2014-2-16)"), "{lm:'[2013-09-21,2014-02-16)'}");
        _c(Q("L[2013-9-21)"), "{lm : '[2013-09-21]'}");
    }

    @Test
    public void test_by_createTime() {
        _c_nil(Q("C()"));
        _c_nil(Q("C(  \t )"));
        _c(Q("C(2013-9-21, 2013-8-1]"), "{ctm:'(2013-08-01,2013-09-21]'}");
        _c(Q("C[2013-9-21,2013-8-1)"), "{ctm:'[2013-08-01,2013-09-21)'}");
        _c(Q("C[2013-9-21)"), "{ctm:'[2013-09-21]'}");
    }

    @Test
    public void test_by_mode() {
        _c_nil(Q("M()"));
        _c_nil(Q("M(  \t )"));
        _c(Q("M(PUBLIC)"), "{md:['PUBLIC']}");
        _c(Q("M(PUBLIC,privete)"), "{md: ['PUBLIC' , 'privete']}");
    }

    @Test
    public void test_by_type() {
        _c_nil(Q("T()"));
        _c_nil(Q("T(  \t )"));
        _c(Q("T(sin)"), "{tp:['sin']}");
        _c(Q("T(SIN,Org)"), "{tp: ['SIN','Org'] }");
    }

    @Test
    public void test_by_friends() {
        _c(Q("->danoo"), "{frs:'danoo'}");
    }

    @Test
    public void test_by_ancestor() {
        _c(Q(">>danoo"), "{ans:'danoo'}");
    }

    @Test
    public void test_by_parent() {
        _c(Q(">danoo"), "{pa:'danoo'}");
    }

    @Test
    public void test_by_regex() {
        _c(Q("^a.*"), map("nm", Pattern.compile("^a.*")));
    }

    @Test
    public void test_by_user() {
        _c(Q("@zozoh"), "{ow:'zozoh'}");
        _c(Q("@A:zozoh"), "{admin:'zozoh'}");
        _c(Q("@M:zozoh"), "{member:'zozoh'}");
        _c(Q("@C:zozoh"), "{contributor:'zozoh'}");
        _c(Q("@W:zozoh"), "{watcher:'zozoh'}");
        _c(Q("@AMW:zozoh"), "{roles:{nm:'zozoh',r:'AMW'}}");
    }

    {
        Castors.me();
    }

    private QWordBuilder qb;

    @Before
    public void before() {
        qb = new QWordBuilder(Files.findFile("org/woods/query/rule1.txt"));
    }

    private void _c_nil(QWord q) {
        assertTrue(q.isEmpty());
    }

    private void _c(QWord q, String json) {
        Map<String, Object> m0 = Lang.map(json);
        _c(q, m0);
    }

    private void _c(QWord q, String json, boolean expectAllAnd) {
        if (expectAllAnd) {
            assertTrue(q.isAllAnd());
        } else {
            assertFalse(q.isAllAnd());
        }
        Map<String, Object> m0 = Lang.map(json);
        _c(q, m0);
    }

    @SuppressWarnings("unchecked")
    private void _c(QWord q, Map<String, Object> map) {
        try {
            Map<String, Object> m0 = (Map<String, Object>) Json.fromJson(Json.toJson(map));
            Map<String, Object> m1 = (Map<String, Object>) Json.fromJson(Json.toJson(q.toDumpMap()));
            assertTrue(Lang.equals(m0, m1));
        }
        catch (AssertionError e) {
            System.out.printf("expect '%s', but '%s'",
                              Json.toJson(map, JsonFormat.compact()),
                              q);
            throw e;
        }
    }

    private QWord Q(String kwd) {
        return qb.parse(kwd);
    }

}
