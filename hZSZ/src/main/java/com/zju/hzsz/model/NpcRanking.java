package com.zju.hzsz.model;

/**
 * 人大代表-排名
 * Created by Wangli on 2017/4/23.
 */

public class NpcRanking {
    //代表姓名
    public String deputyName;
    //履职次数
    public int deputyJobSum;

    public void setNpcWorkSum(int npcWorkSum) {
        this.deputyJobSum = npcWorkSum;
    }

    public void setNpcName(String npcName) {
        this.deputyName = npcName;
    }
}
