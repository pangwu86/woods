package org.nutz.vfs.simple;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.vfs.ZFile;
import org.nutz.vfs.ZIO;

public class ZSimpleIO implements ZIO {

    @Override
    public void write(ZFile zf, InputStream ins) {
        File f = checkFile(zf);
        Files.write(f, ins);
    }

    @Override
    public void writeString(ZFile zf, Reader r) {
        File f = checkFile(zf);
        Files.write(f, r);
    }

    @Override
    public InputStream read(ZFile zf) {
        return Streams.fileIn(checkFile(zf));
    }

    @Override
    public Reader readString(ZFile zf) {
        return Streams.fileInr(checkFile(zf));
    }

    private File checkFile(ZFile zf) {
        File f = Files.findFile(zf.path());
        if (f == null)
            throw Lang.makeThrow("Fail to found %s", zf.path());
        return f;
    }
}
