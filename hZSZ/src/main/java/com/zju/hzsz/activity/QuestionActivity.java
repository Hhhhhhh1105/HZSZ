package com.zju.hzsz.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.model.Question;
import com.zju.hzsz.model.QuestionListRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

import java.util.ArrayList;
import java.util.List;

/**
 * 常见问题
 * Created by Wangli on 2017/6/20.
 */

public class QuestionActivity extends BaseActivity {

    private ListViewWarp listViewWarp = null;
    private SimpleListAdapter adapter = null;
    private List<Question> questions = new ArrayList<Question>();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        setTitle("常见问题列表");
        initHead(R.drawable.ic_head_back, 0);

        if (listViewWarp == null) {
            adapter = new SimpleListAdapter(this, questions, new SimpleViewInitor() {
                @Override
                public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {

                    Question question = (Question) data;

                    if (convertView == null) {
                        convertView = LinearLayout.inflate(context, R.layout.item_question, null);
                    }

                    ((TextView) convertView.findViewById(R.id.tv_question_title)).setText("" +
                            question.id + ". " + question.title);

                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(QuestionActivity.this, QuestionDetailActivity.class);
                            intent.putExtra("question", StrUtils.Obj2Str(v.getTag()));
                            startActivity(intent);
                        }
                    });
                    convertView.setTag(question);

                    return convertView;
                }
            });

            listViewWarp = new ListViewWarp(this, adapter, new ListViewWarp.WarpHandler() {
                @Override
                public boolean onRefresh() {
                    loadData(true);
                    return true;
                }

                @Override
                public boolean onLoadMore() {

                    return false;
                }
            });

            listViewWarp.startRefresh();
        }

        ((LinearLayout) findViewById(R.id.ll_main)).addView(listViewWarp.getRootView());
        loadData(true);

    }

    private boolean loadData(final boolean refresh) {
        if (refresh)
            listViewWarp.setRefreshing(true);
        if (refresh) {
            questions.clear();
            adapter.notifyDataSetInvalidated();
        }


        getRequestContext().add("Get_CommonQuestion_List", new Callback<QuestionListRes>() {
            @Override
            public void callback(QuestionListRes o) {
                listViewWarp.setRefreshing(false);
                if (o != null && o.isSuccess() && o.data != null) {
                    if (refresh)
                        questions.clear();
                    for (Question question : o.data.commonQuestionJsons) {
                        questions.add(question);
                    }

                    // adapter.notifyDataSetChanged();
                    adapter.notifyDataSetInvalidated();
                }
                if (questions.size() == 0) {
                    listViewWarp.setNoMore(true);
                } else {
                    listViewWarp.setNoMore(false);
                }
            }
        }, QuestionListRes.class, ParamUtils.freeParam(null));


        return true;
    }
}
