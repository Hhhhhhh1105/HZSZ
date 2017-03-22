package com.zju.hzsz.chief.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.activity.RiverActivity;
import com.zju.hzsz.model.River;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wangli on 2017/3/22.
 */

public class ChiefRivermanageActivity extends BaseActivity {

    private ListViewWarp listViewWarp = null;
    private SimpleListAdapter adapter = null;

    private List<River> rivers = new ArrayList<River>();

    private View.OnClickListener lowLeverRiverClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getTag() != null) {
                Intent intent = new Intent(ChiefRivermanageActivity.this, RiverActivity.class);
                intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(v.getTag()));
                startActivity(intent);
            }
        }
    };

    private SimpleViewInitor riverInitor = new SimpleViewInitor() {
        @Override
        public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {

            if (convertView == null) {
                convertView = LinearLayout.inflate(context, R.layout.item_chief_rivermanage, null);
            }

            River river = (River) data;




            convertView.setTag(river);
            convertView.setOnClickListener(lowLeverRiverClick);

            return convertView;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chief_rivermanage);
        setTitle("所有管辖河道");
        initHead(R.drawable.ic_head_back, 0);


    }
}
