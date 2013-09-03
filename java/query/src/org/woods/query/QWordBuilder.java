package org.woods.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;

import org.nutz.lang.Files;
import org.nutz.lang.Streams;

public class QWordBuilder {

    public QWordBuilder(File f) {
        this(Streams.fileInr(f));
    }

    public QWordBuilder(Reader r) {
        this.loadRules(r);
    }

    public QWordBuilder(String path) {
        this(Files.findFile(path));
    }

    public QWordBuilder() {}

    public QWordBuilder loadRules(Reader r) {
        BufferedReader br = Streams.buffr(r);

        return this;
    }

    public QWord parse(String kwd) {
        return null;
    }

}
