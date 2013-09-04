package org.woods.query;

import org.nutz.lang.ContinueLoop;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.LoopException;

public interface EachCnd {

    /**
     * 回调接口
     * 
     * @param index
     *            当前约束的下标，0 开始
     * @param cnd
     *            当前约束
     * @param prevIsAnd
     *            与前一个约束的关系是否是 AND
     * @throws ExitLoop
     *             抛出这个异常，表示你打算退出循环
     * @throws ContinueLoop
     *             抛出这个异常，表示你打算停止递归，但是不会停止循环
     * @throws LoopException
     *             抛出这个异常，表示你打算退出循环，并且将会让迭代器替你将包裹的异常抛出
     */
    void invoke(int index, QCnd cnd, boolean prevIsAnd) throws ExitLoop,
            ContinueLoop, LoopException;

}
