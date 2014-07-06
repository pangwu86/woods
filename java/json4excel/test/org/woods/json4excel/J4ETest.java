package org.woods.json4excel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.util.Disks;
import org.woods.json4excel.bean.Person;

public class J4ETest {

    public static final String TMPDIR = System.getProperty("java.io.tmpdir");

    @Test
    public void test_fromExcel() throws Exception {
        File e = new File(Disks.absolute("test.xls"));
        List<Person> plist = J4E.fromExcel(Streams.fileIn(e), Person.class, null);
        assertEquals(3, plist.size());
        _test_excel_(e, 3);
    }

    @Test
    public void test_toExcel_01() throws Exception {
        int pn = 1;
        File e = new File(TMPDIR, "e.xls");
        Files.createFileIfNoExists(e);
        assertTrue(J4E.toExcel(new FileOutputStream(e), PL(pn), null));
        _test_excel_(e, pn);
        Files.deleteFile(e);
    }

    @Test
    public void test_toExcel_02() throws Exception {
        int pn = 2;
        File e = new File(TMPDIR, "e2.xls");
        Files.createFileIfNoExists(e);
        assertTrue(J4E.toExcel(new FileOutputStream(e), PL(pn), null));
        _test_excel_(e, pn);
        Files.deleteFile(e);
    }

    @Test
    public void test_toExcel_03() throws Exception {
        int pn = 3;
        File e = new File(TMPDIR, "e3.xls");
        Files.createFileIfNoExists(e);
        assertTrue(J4E.toExcel(new FileOutputStream(e), PL(pn), null));
        _test_excel_(e, pn);
        Files.deleteFile(e);
    }

    public void _test_excel_(File ef, int dn) {
        List<Person> plist = J4E.fromExcel(Streams.fileIn(ef), Person.class, null);
        if (dn >= 1) {
            Person p = plist.get(0);
            assertTrue("pw".equals(p.getName()));
            assertEquals(28, p.getAge());
            assertTrue("1986-08-26".equals(FD(p.getBirthday())));
        }
        if (dn >= 2) {
            Person p = plist.get(1);
            assertTrue("zozoh".equals(p.getName()));
            assertEquals(37, p.getAge());
            assertTrue("1979-09-21".equals(FD(p.getBirthday())));
        }
        if (dn >= 3) {
            Person p = plist.get(2);
            assertTrue("wendal".equals(p.getName()));
            assertEquals(29, p.getAge());
            assertTrue("1985-10-01".equals(FD(p.getBirthday())));
        }
    }

    public String FD(Date d) {
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
    }

    // date by format
    public Date DBF(String dstr) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dstr);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    // person-list
    public List<Person> PL(int num) {
        List<Person> plist = new ArrayList<Person>();
        if (num >= 1) {
            Person p = new Person();
            p.setName("pw");
            p.setAge(28);
            p.setBirthday(DBF("1986-08-26"));
            plist.add(p);
        }
        if (num >= 2) {
            Person p = new Person();
            p.setName("zozoh");
            p.setAge(37);
            p.setBirthday(DBF("1979-09-21"));
            plist.add(p);
        }
        if (num >= 3) {
            Person p = new Person();
            p.setName("wendal");
            p.setAge(29);
            p.setBirthday(DBF("1985-10-01"));
            plist.add(p);
        }

        return plist;
    }
}
