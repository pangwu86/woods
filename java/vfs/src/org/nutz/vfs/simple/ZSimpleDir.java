package org.nutz.vfs.simple;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Disks;
import org.nutz.vfs.ZDir;
import org.nutz.vfs.ZFWalker;
import org.nutz.vfs.ZFile;
import org.nutz.vfs.ZIO;

public class ZSimpleDir extends ZSimpleFile implements ZDir {

    public ZSimpleDir(File d) {
        if (null == d)
            throw Lang.makeThrow("NULL Dir");
        if (!d.isDirectory())
            throw Lang.makeThrow("'%s' should be Dir", d.getAbsolutePath());
        this.f = d;
    }

    @Override
    public String type() {
        return "$DIR";
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public boolean isDir() {
        return true;
    }

    @Override
    public void createIfNoExists() {
        Files.createDirIfNoExists(f);
    }

    @Override
    public void copyTo(final ZIO fromIO, final ZIO toIO, final ZFile dDest) {
        if (!dDest.isDir())
            throw Lang.makeThrow("Dir(%s) can not copy to dir(%s)",
                                 path(),
                                 dDest.path());
        final ZFile dSrc = this;
        this.walk(true, new ZFWalker() {
            public boolean invoke(int i, ZFile zf) {
                if (zf.isDir())
                    return true;
                String rph = dSrc.relative(zf);
                ZFile zfDest = ((ZDir) dDest).createFileIfNoExists(rph);
                zf.copyTo(fromIO, toIO, zfDest);
                return true;
            }
        });
    }

    @Override
    public List<ZFile> ls() {
        return ls(null, true);
    }

    @Override
    public List<ZFile> ls(String regex, boolean ignoreHidden) {
        Pattern p = null == regex ? null : Pattern.compile(regex);
        File[] fs = f.listFiles();
        List<ZFile> list = new ArrayList<ZFile>(fs.length);
        for (File f : fs) {
            if (ignoreHidden && f.isHidden())
                continue;
            if (null != p && !p.matcher(f.getName().toLowerCase()).find())
                continue;
            // 文件
            if (f.isFile()) {
                list.add(new ZSimpleFile(f));
            }
            // 目录
            else if (f.isDirectory()) {
                list.add(new ZSimpleDir(f));
            }
        }
        return list;
    }

    @Override
    public List<ZFile> lsFile(String regex, boolean ignoreHidden) {
        Pattern p = null == regex ? null : Pattern.compile(regex);
        File[] fs = f.listFiles();
        List<ZFile> list = new ArrayList<ZFile>(fs.length);
        for (File f : fs) {
            if (f.isDirectory())
                continue;
            if (ignoreHidden && f.isHidden())
                continue;
            if (null != p && !p.matcher(f.getName().toLowerCase()).find())
                continue;
            // 添加
            list.add(new ZSimpleFile(f));
        }
        return list;
    }

    @Override
    public ZFile getFile(String path) {
        File f = _get_file(path);
        if (f.exists() && f.isFile())
            return new ZSimpleFile(f);
        return null;
    }

    @Override
    public boolean existsFile(String path) {
        File f = _get_file(path);
        return f.isFile() && f.exists();
    }

    @Override
    public ZDir getDir(String path) {
        File f = _get_file(path);
        if (f.exists() && f.isDirectory())
            return new ZSimpleDir(f);
        return null;
    }

    @Override
    public boolean existsDir(String path) {
        File f = _get_file(path);
        return f.exists() && f.isDirectory();
    }

    @Override
    public ZFile get(String path) {
        File f = _get_file(path);
        if (f.isFile())
            return new ZSimpleFile(f);
        if (f.isDirectory())
            return new ZSimpleDir(f);
        return null;
    }

    @Override
    public boolean exists(String path) {
        File f = _get_file(path);
        return f.exists();
    }

    @Override
    public ZFile check(String path) {
        File f = _get_file(path);
        if (f.isFile())
            return new ZSimpleFile(f);
        if (f.isDirectory())
            return new ZSimpleDir(f);
        throw Lang.makeThrow("Fail to find '%s' in '%s'",
                             path,
                             f.getAbsolutePath());
    }

    @Override
    public ZFile checkFile(String path) {
        File f = _get_file(path);
        if (f.isFile())
            return new ZSimpleFile(f);
        if (f.isDirectory())
            throw Lang.makeThrow("'%s' in '%s' is a DIR", path, f);
        throw Lang.makeThrow("Fail to find '%s' in '%s'", path, f);
    }

    @Override
    public ZFile checkDir(String path) {
        File f = _get_file(path);
        if (f.isDirectory())
            return new ZSimpleDir(f);
        if (f.isFile())
            throw Lang.makeThrow("'%s' in '%s' is a FILE", path, f);
        throw Lang.makeThrow("Fail to find '%s' in '%s'", path, f);
    }

    @Override
    public ZFile createFileIfNoExists(String path) {
        File f = _get_file(path);
        if (f.isDirectory())
            throw Lang.makeThrow("'%s' already exists directory", f);
        if (!f.exists())
            Files.createFileIfNoExists(f);
        return new ZSimpleFile(f);
    }

    @Override
    public ZDir createDirIfNoExists(String path) {
        File f = _get_file(path);
        if (f.isFile())
            throw Lang.makeThrow("'%s' already exists file", f);
        if (!f.exists())
            Files.createDirIfNoExists(f);
        return new ZSimpleDir(f);
    }

    @Override
    public void walk(boolean ignoreHidden, ZFWalker walker) {
        this._walk_children(ignoreHidden, walker, 0);
    }

    private int _walk_children(boolean ignoreHidden, ZFWalker walker, int i) {
        List<ZFile> list = ls(null, ignoreHidden);
        for (ZFile zf : list) {
            if (ignoreHidden && zf.isHidden()) {
                continue;
            }
            if (zf.isFile()) {
                walker.invoke(i++, zf);
            } else if (zf.isDir()) {
                if (walker.invoke(i++, zf)) {
                    i = ((ZSimpleDir) zf)._walk_children(ignoreHidden,
                                                         walker,
                                                         i);
                }
            }
        }
        return i;
    }

    @Override
    public void clear() {
        Files.clearDir(f);
    }

    @Override
    public void remove() {
        Files.deleteDir(f);
    }

    private File _get_file(String path) {
        // 如果标识了home
        if (path.startsWith("~")) {
            return new File(Disks.absolute(path));
        }
        // 如果是绝对路径
        if (path.matches("^([a-zA-Z]:|/)(.+)$")) {
            return new File(Disks.getCanonicalPath(path));
        }
        // 如果是相对路径
        String ph = Disks.getCanonicalPath(path() + "/" + path);
        return new File(ph);
    }
}
