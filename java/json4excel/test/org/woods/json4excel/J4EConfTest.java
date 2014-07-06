package org.woods.json4excel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.woods.json4excel.bean.Person;

public class J4EConfTest {

    @Test
    public void test_from_class() throws Exception {
        J4EConf jc = J4EConf.FROM(Person.class);
        assertTrue(jc.getSheetName().equals("人员"));
        assertEquals(jc.getColumns().size(), 3);
    }
}
