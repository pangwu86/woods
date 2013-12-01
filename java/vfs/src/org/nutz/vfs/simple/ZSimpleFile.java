package org.nutz.vfs.simple;

import java.io.File;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Disks;
import org.nutz.vfs.ZFile;

public class ZSimpleFile implements ZFile {

    protected File f;

    protected ZSimpleFile() {}

    public ZSimpleFile(File f) {
        if (null == f)
            throw Lang.makeThrow("NULL File");
        if (!f.isFile())
            throw Lang.makeThrow("'%s' should be Dir", f.getAbsolutePath());
        this.f = f;
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public boolean isDir() {
        return false;
    }

    @Override
    public boolean isHidden() {
        return f.isHidden();
    }

    @Override
    public boolean matchType(String regex) {
        return typeLower().matches(regex);
    }

    @Override
    public String relative(ZFile f) {
        return relative(f.path());
    }

    @Override
    public String relative(String path) {
        return Disks.getRelativePath(this.path(), path);
    }

    @Override
    public String name() {
        return f.getName();
    }

    @Override
    public String path() {
        return f.getAbsolutePath();
    }

    private String _type;

    public String toString() {
        return path();
    }

    @Override
    public String type() {
        if (null == _type) {
            _type = Files.getSuffixName(f);
        }
        return _type;
    }

    private String _type_lower;

    @Override
    public String typeLower() {
        if (null == _type_lower) {
            _type_lower = type().toLowerCase();
        }
        return _type_lower;
    }

}
