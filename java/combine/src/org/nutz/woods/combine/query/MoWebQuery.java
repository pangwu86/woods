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
        ZMoDoc r = _region_to_doc(key, rg);
        if (r != null) {
            list.add(ZMoDoc.NEW(key, r));
        }
    }

    protected ZMoDoc _region_to_doc(String key, Region<?> rg) {
        ZMoDoc r = ZMoDoc.NEW();
        if (rg.isRegion()) {
            if (rg.left() != null) {
                r.put(rg.leftOpt("$gt", "$gte"), rg.left());
            }
            if (rg.right() != null) {
                r.put(rg.rightOpt("$lt", "$lte"), rg.right());
            }
        } else if (!rg.isNull()) {
            r.put(key, rg.left());
        }
        return r.isEmpty() ? null : r;
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
