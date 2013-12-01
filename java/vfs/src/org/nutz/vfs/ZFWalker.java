package org.nutz.vfs;

public interface ZFWalker {

    /**
     * @param i
     *            迭代计数，0 base
     * @param zf
     *            当前文件或者目录
     * @return 是否跳过这个项目，如果是目录，就不再深入下去了
     */
    boolean invoke(int i, ZFile zf);

}
