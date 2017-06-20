package com.zju.hzsz.model;

/**
 * Created by Wangli on 2017/6/20.
 */

public class QuestionListRes extends BaseRes {

    public QuestionList data;

    @Override
    public boolean isSuccess() {
        return super.isSuccess() && data != null && data.commonQuestionJsons != null;
    }
}
