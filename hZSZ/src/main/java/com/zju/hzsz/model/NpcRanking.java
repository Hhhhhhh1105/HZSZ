package com.zju.hzsz.model;

/**
 * 人大代表-排名
 * Created by Wangli on 2017/4/23.
 */

public class NpcRanking {
    //代表姓名
    public String npcName;
    //履职次数
    public int npcWorkSum;

    public void setNpcWorkSum(int npcWorkSum) {
        this.npcWorkSum = npcWorkSum;
    }

    public void setNpcName(String npcName) {
        this.npcName = npcName;
    }
}
