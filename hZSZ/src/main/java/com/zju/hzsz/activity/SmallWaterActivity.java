package com.zju.hzsz.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.model.SmallWater;
import com.zju.hzsz.model.SmallWaterRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;

/**
 * 小微水体具体信息页
 * Created by Wangli on 2017/3/27.
 */

public class SmallWaterActivity extends BaseActivity {

    private SmallWater smallWater;
    private TextView smallwater_position;
    private TextView smallwater_area;
    private TextView smallwater_problem;
    private TextView smallwater_suggestion;
    private TextView smallwater_time;
    private TextView smallwater_linkman;
    private ImageView smallwater_phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smallwater);

        initHead(R.drawable.ic_head_back, 0);
        smallWater = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_SMALLWATER), SmallWater.class);

        initData();

        if (smallWater != null) {
            setTitle(smallWater.waterName);
            loadSmallWater();
        }

    }

    private void initData() {
        smallwater_area = (TextView) findViewById(R.id.tv_smallwater_area);
        smallwater_linkman = (TextView) findViewById(R.id.tv_smallwater_linkman);
        smallwater_phone = (ImageView) findViewById(R.id.iv_smallwater_phone);
        smallwater_position = (TextView) findViewById(R.id.tv_smallwater_position);
        smallwater_suggestion = (TextView) findViewById(R.id.tv_smallwater_suggestion);
        smallwater_time = (TextView) findViewById(R.id.tv_smallwater_comptime);
        smallwater_problem = (TextView) findViewById(R.id.tv_smallwater_problem);
     }

    private View.OnClickListener telclik = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            //如果tag不为空，则显示拨打电话的dialog，tag值为电话号码
            if (v.getTag() != null) {
                final String tel = v.getTag().toString().trim();
                if (tel.length() > 0) {
                    Dialog dlg = createMessageDialog(getString(R.string.tips), StrUtils.renderText(SmallWaterActivity.this, R.string.fmt_make_call_query, tel), getString(R.string.call), getString(R.string.cancel), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + tel));
                            startActivity(intent);
                        }
                    }, null, null);
                    dlg.show();
                }
            }
        }
    };

    private void loadSmallWater() {
        showOperating();
        getRequestContext().add("Get_SmallWaterContent", new Callback<SmallWaterRes>() {
            @Override
            public void callback(SmallWaterRes o) {

                hideOperating();

                if (o != null && o.isSuccess()) {

                    SmallWater sw = o.data;

                    smallwater_position.setText(sw.position);
                    smallwater_area.setText(sw.area + "米/平方米");
                    smallwater_problem.setText(sw.existingProblems);
                    smallwater_time.setText(sw.completionTime);
                    smallwater_suggestion.setText(sw.rectificationSuggestions);
                    smallwater_linkman.setText(sw.linkman);
                    smallwater_phone.setTag(sw.phonenumber);
                    smallwater_phone.setOnClickListener(telclik);


                }

            }
        }, SmallWaterRes.class, ParamUtils.freeParam(null, "waterId", smallWater.waterId));
    }
}
