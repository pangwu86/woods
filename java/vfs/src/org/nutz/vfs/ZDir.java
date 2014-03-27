package org.nutz.vfs;

import java.util.List;

/**
 * 封装了对于 ZPage 目录的操作，通过这个接口的不通实现，<br>
 * 可以将 ZPage 移植到特殊的文件系统甚至数据库上。
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface ZDir extends ZFile {
    
    List<ZFile> ls();

    /**
     * @param regex
     *            一个正则表达式匹配文件名
     * @param ignoreHidden
     *            是否忽略隐藏项目
     * 
     * @return 本目录下所有的文件和子目录
     */
    List<ZFile> ls(String regex, boolean ignoreHidden);

    /**
     * 
     * @param regex
     *            一个正则表达式匹配文件名
     * @param ignoreHidden
     *            是否忽略隐藏项目
     * 
     * @return 本目录下所有的文件
     */
    List<ZFile> lsFile(String regex, boolean ignoreHidden);

    /**
     * 获取一个文件对象
     * 
     * @param path
     *            相对于本目录的路径
     * @return 文件对象，null 表示不存在或者不是文件对象
     */
    ZFile getFile(String path);

    /**
     * 判断是否存在一个文件对象
     * 
     * @param path
     *            相对于本目录的路径
     * @return 是否存在一个给定路径的文件
     */
    boolean existsFile(String path);

    /**
     * @param path
     *            相对于本目录的路径
     * @return 目录对象，null 表示不存在或者不是目录对象
     */
    ZDir getDir(String path);

    /**
     * 判断是否存在一个目录对象
     * 
     * @param path
     *            相对于本目录的路径
     * @return 是否存在一个给定路径的目录
     */
    boolean existsDir(String path);

    /**
     * @param path
     *            相对于本目录的文件或路径
     * @return 目录或文件对象, null 表示不存在
     */
    ZFile get(String path);

    /**
     * @param path
     *            相对于本目录的文件或路径
     * @return 是否存在一个给定的目录或文件
     */
    boolean exists(String path);

    /**
     * @param path
     *            相对于本目录的文件或路径
     * @return 目录或文件对象, 如果不存在抛错
     */
    ZFile check(String path);

    /**
     * @param path
     *            相对于本目录的文件或路径
     * @return 文件对象, 如果不存在抛错
     */
    ZFile checkFile(String path);

    /**
     * @param path
     *            相对于本目录的文件或路径
     * @return 目录对象, 如果不存在抛错
     */
    ZFile checkDir(String path);

    /**
     * 获取一个文件，如果不存在就创建
     * 
     * @param path
     *            相对于本目录的路径
     * @return 文件对象
     */
    ZFile createFileIfNoExists(String path);

    /**
     * 获取一个目录，如果不存在就创建
     * 
     * @param path
     *            相对于本目录的路径
     * @return 目录对象
     */
    ZDir createDirIfNoExists(String path);

    /**
     * 遍历本目录内所包含的所有文件
     * 
     * @param ignoreHidden
     *            是否忽略隐藏项目
     * 
     * @param walker
     *            遍历器
     */
    void walk(boolean ignoreHidden, ZFWalker walker);

    /**
     * 清除本目录
     */
    void clear();

    /**
     * 删除本目录
     */
    void remove();

}
