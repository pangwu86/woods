package org.nutz.vfs;

public interface ZFile {

    String name();

    String path();

    String type();

    String typeLower();

    /**
     * 根据一个文件，得到相对于自己的路径
     * 
     * @param f
     *            文件
     * @return 相对路径
     */
    String relative(ZFile f);

    /**
     * 计算一个绝对路径相对于自己的相对路径
     * 
     * @param path
     *            绝对路径
     * @return 相对路径
     */
    String relative(String path);

    boolean isFile();

    boolean isDir();

    boolean isHidden();

    /**
     * @param regex
     *            正则表达式（匹配全小写的情况）
     * @return 是否匹配上类型
     */
    boolean matchType(String regex);

}
