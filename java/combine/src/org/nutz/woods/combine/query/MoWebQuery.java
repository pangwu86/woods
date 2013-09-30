package org.nutz.woods.combine.query;

import org.nutz.lang.util.Region;
import org.nutz.mongo.ZMoDoc;
import org.nutz.web.query.WebOrderField;
import org.nutz.web.query.WebQuery;

import com.mongodb.BasicDBList;
import com.mongodb.DBCursor;

public class MoWebQuery extends WebQuery {

    /**
     * 根据自身的配置，设置 DBCursor 的限制条件
     * 
     * @param cu
     *            游标
     * 
     * @return 游标对象
     */
    public DBCursor setup(DBCursor cu) {
        cu.skip(offset()).limit(pageSize);
        setupOrder(cu);
        return cu;
    }

    public DBCursor setupOrder(DBCursor cu) {
        if (hasOrder()) {
            ZMoDoc sort = ZMoDoc.NEW(orderFields.length);
            for (WebOrderField of : orderFields) {
                sort.put(of.getName(), of.getSort().value());
            }
            cu.sort(sort);
        }
        return cu;
    }

    protected void _join_region(final BasicDBList list, String key, Region<?> rg) {
        // 如果是一个范围
        if (rg.isRegion()) {
            ZMoDoc q = ZMoDoc.NEW();
            if (rg.left() != null) {
                q.put(rg.leftOpt("$gt", "$gte"), rg.left());
            }
            if (rg.right() != null) {
                q.put(rg.rightOpt("$lt", "$lte"), rg.right());
            }
            list.add(ZMoDoc.NEW(key, q));
        }
        // 如果是一个精确的值
        else if (!rg.isNull()) {
            list.add(ZMoDoc.NEW(key, rg.left()));
        }
    }

    protected void _join_str_enum(final BasicDBList list,
                                  String key,
                                  String[] ss) {
        ZMoDoc r = _enum_to_Doc(key, ss);
        if (null != r)
            list.add(r);
    }

    protected ZMoDoc _enum_to_Doc(String key, String[] ss) {
        ZMoDoc r = null;
        if (ss.length == 1) {
            r = ZMoDoc.NEW(key, ss[0]);
        } else if (ss.length > 0) {
            r = ZMoDoc.NEW().in(key, ss);
        }
        return r;
    }

}
