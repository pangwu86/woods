package org.nutz.vfs.simple;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Disks;
import org.nutz.vfs.ZDir;
import org.nutz.vfs.ZFile;
import org.nutz.vfs.ZIO;

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

    public int hashCode() {
        if (null == f)
            return 0;
        return f.hashCode();
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
    public boolean exists() {
        return f.exists();
    }

    @Override
    public long lastModified() {
        return f.lastModified();
    }

    @Override
    public ZDir parent() {
        return new ZSimpleDir(f.getParentFile());
    }

    @Override
    public void createIfNoExists() {
        Files.createFileIfNoExists(f);
    }

    @Override
    public void copyTo(ZIO fromIO, ZIO toIO, ZFile zf) {
        zf.createIfNoExists();
        InputStream ins = fromIO.read(this);
        toIO.write(zf, ins);
    }

    @Override
    public void moveTo(ZFile zf) {
        if (zf instanceof ZSimpleFile) {
            try {
                Files.move(f, ((ZSimpleFile) zf).f);
            }
            catch (IOException e) {
                throw Lang.wrapThrow(e);
            }
            return;
        }
        throw Lang.makeThrow("ZSimpleFile can moveTo another ZSimpleFile only!");
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
