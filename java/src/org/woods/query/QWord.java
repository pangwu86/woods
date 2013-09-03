package org.woods.query;

import java.util.ArrayList;

public class QWord {

    private ArrayList<Character> chars;

    private ArrayList<QCnd> cnds;

    public QWord() {
        chars = new ArrayList<Character>();
        cnds = new ArrayList<QCnd>();
    }
    
    /**
     * @param cnd 约束条件
     * @param nextAnd 下一个约束条件与自己的关系
     * @return
     */
    public QWord add(QCnd cnd, boolean nextAnd){
        return this;
    }

}
