package org.nutz.vfs;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * 封装了对于 ZPage IO的操作，通过这个接口的不通实现，<br>
 * 可以将 ZPage 移植到特殊的文件系统甚至数据库上。
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface ZIO {

    /**
     * 将一个输入流写入到文件中
     * 
     * @param zf
     *            文件
     * @param ins
     *            内容输入流，本函数会关闭它
     */
    void write(ZFile zf, InputStream ins);

    /**
     * 将一个文本输入流写入到文件中
     * 
     * @param zf
     *            文件
     * @param r
     *            文本内容输入流，本函数会关闭它
     */
    void writeString(ZFile zf, Reader r);

    /**
     * @param zf
     *            文件
     * @return 文本写入流，调用者负责关闭
     */
    Writer openWriter(ZFile zf);

    /**
     * @param zf
     *            文件
     * @return 写入流，调用者负责关闭
     */
    OutputStream openOutputStream(ZFile zf);

    /**
     * 打开一个文件的输入流准备读取文件内容，调用者负责关闭这个流
     * 
     * @param zf
     *            文件对象
     * @return 输入流
     */
    InputStream read(ZFile zf);

    /**
     * 打开一个文件的文本输入流准备读取文件内容，调用者负责关闭这个流
     * 
     * @param zf
     *            文件对象
     * @return 文本输入流
     */
    Reader readString(ZFile zf);

}
