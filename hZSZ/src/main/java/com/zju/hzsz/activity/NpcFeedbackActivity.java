package com.zju.hzsz.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.zju.hzsz.R;
import com.zju.hzsz.chief.activity.*;

/**
 * Created by Wangli on 2017/4/19.
 */

public class NpcFeedbackActivity extends com.zju.hzsz.chief.activity.BaseActivity {

    public String year = "2017";
    public String month = "4";
    private YearMonthSelectDialog selectDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_npcfeedback);

        setTitle("监督反馈");
        initHead(R.drawable.ic_head_back, 0);

        findViewById(R.id.tv_seldate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectDialog == null) {
                    selectDialog = new YearMonthSelectDialog(NpcFeedbackActivity.this, new YearMonthSelectDialog.Callback() {
                        @Override
                        public void onYMSelected(int y, int m) {
                            year = "" + y;
                            month = "" + m;
                            refreshDateView();
                        }
                    });
                }
                selectDialog.show();
            }
        });


    }

    private void refreshDateView() {
        ((TextView) findViewById(R.id.tv_seldate)).setText(year + "年" + month + "月");
    }

}
