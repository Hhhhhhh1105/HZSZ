package com.zju.hzsz.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.adapter.PagerItem;
import com.zju.hzsz.adapter.SimplePagerAdapter;
import com.zju.hzsz.fragment.lake.BaseLakePagerItem;
import com.zju.hzsz.fragment.lake.LakeInfoItem;
import com.zju.hzsz.fragment.lake.LakePolicyItem;
import com.zju.hzsz.fragment.lake.LakeQualityItem;
import com.zju.hzsz.model.Lake;
import com.zju.hzsz.utils.ArrUtils;
import com.zju.hzsz.utils.StrUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZJTLM4600l on 2017/12/20.
 */

public class LakeActivity extends BaseActivity implements ViewPager.OnPageChangeListener,RadioGroup.OnCheckedChangeListener {
    private Lake lake = null;
    private List<PagerItem> pagerItems = null;
    private SimplePagerAdapter adapter = null;

    // private int[] rdids = new int[] { R.id.rb_river_dzgsp,
    // R.id.rb_river_hdsz, R.id.rb_river_yhyc, R.id.rb_river_hdfw };
    //rdids：电子公示牌-基本信息，河道水质，一河一策，信息公开；
    private int[] rdids = new int[] { R.id.rb_river_dzgsp, R.id.rb_river_hdsz, R.id.rb_river_yhyc};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_river);

        // initHead(R.drawable.ic_head_back, R.drawable.ic_head_refresh);
        initHead(R.drawable.ic_head_back, 0);

        ((TextView)findViewById(R.id.rb_river_hdsz)).setText("湖泊水质");
        ((TextView)findViewById(R.id.rb_river_yhyc)).setText("一湖一策");
        findViewById(R.id.rb_river_tsxx).setVisibility(View.GONE);

        lake = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_LAKE), Lake.class);
        if (lake != null) {
            setTitle(StrUtils.renderText(this, R.string.fmt_riverinfo, lake.lakeName));
            pagerItems = new ArrayList<PagerItem>();
            pagerItems.add(new LakeInfoItem(lake, this, null));
            pagerItems.add(new LakeQualityItem(lake, this));
            pagerItems.add(new LakePolicyItem(lake, this));
//            pagerItems.add(new LakeInfoPubItem(river, this));

            ((ViewPager) findViewById(R.id.vp_river_tab)).setOnPageChangeListener(this);
            adapter = new SimplePagerAdapter(pagerItems);
            ((ViewPager) findViewById(R.id.vp_river_tab)).setAdapter(adapter);

            ((RadioGroup) findViewById(R.id.rg_river_showwith)).setOnCheckedChangeListener(this);

            findViewById(R.id.iv_head_right).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    BaseLakePagerItem brpi = ((BaseLakePagerItem) pagerItems.get(((ViewPager) findViewById(R.id.vp_river_tab)).getCurrentItem()));
                    brpi.loadData();
                }
            });

            if (pagerItems.size() != rdids.length)
                showToast("Tab和Pager数目不一样");
        } else {
            showToast("没有传入河道参数");
            finish();
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int ix) {
        ((RadioButton) findViewById(rdids[ix])).setChecked(true);
    }

    @Override
    public void onCheckedChanged(RadioGroup rg, int rdid) {
        int ix = ArrUtils.indexOf(rdids, rdid);
        if (ix >= 0) {
            pagerItems.get(ix).getView();
            ((BaseLakePagerItem) pagerItems.get(ix)).readyView();
            ((ViewPager) findViewById(R.id.vp_river_tab)).setCurrentItem(ix);
        }
    }
}
