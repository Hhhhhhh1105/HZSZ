package com.zju.hzsz.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.model.CompPublicity;
import com.zju.hzsz.model.CompSearchDataRes;
import com.zju.hzsz.model.CompSugs;
import com.zju.hzsz.model.District;
import com.zju.hzsz.model.RiverQuickSearchRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.view.ListViewWarp;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Wangli on 2017/3/1.
 */

public class SearchCompActivity extends BaseActivity implements CheckBox.OnCheckedChangeListener, TextView.OnEditorActionListener{

    class DistrictWarper {
        public District district;
        public boolean checked;

        public DistrictWarper(District district){
            super();
            this.district = district;
            this.checked = false;
        }
    }

    private List<DistrictWarper> dwItems = new ArrayList<DistrictWarper>();
    private SimpleListAdapter dwAdpter = null;

    private List<CompPublicity> comps= new ArrayList<CompPublicity>();
    private SimpleListAdapter compAdapter = null;

    private ListViewWarp listViewWarp = null;
    private DistrictWarper curDw = null;

    private TextView startTime;
    private String startTimeStr;
    private TextView endTime;
    private String endTimeStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_searchcomp);
        initHead(R.drawable.ic_head_back, 0);

        Calendar c = Calendar.getInstance();

        startTime = (TextView) findViewById(R.id.tv_selstartdate);
        startTimeStr = "2016-01-01";
        startTime.setText(startTimeStr);
        endTime = (TextView) findViewById(R.id.tv_selenddate);
        //month本来就小1，原本是大于9不要加个0，现在是大于8不要加个0 -> （9开始，9+1=10）
        endTimeStr = "" + c.get(Calendar.YEAR) + ((c.get(Calendar.MONTH ) > 8 ) ? '-' : "-0")
                + (c.get(Calendar.MONTH) + 1) +
                ((c.get(Calendar.DAY_OF_MONTH) > 9 ) ? '-' : "-0")
                + c.get(Calendar.DAY_OF_MONTH);
        endTime.setText(endTimeStr);

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
                new DatePickerDialog(SearchCompActivity.this,
                        // 绑定监听器
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                startTimeStr = year + (monthOfYear > 8 ? "-" : "-0") + (monthOfYear + 1) + (dayOfMonth > 9 ? "-" : "-0") + dayOfMonth;
                                startTime.setText(startTimeStr);
                            }
                        }
                        // 设置初始日期
                        , c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });



        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
                new DatePickerDialog(SearchCompActivity.this,
                        // 绑定监听器
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                endTimeStr = year + (monthOfYear > 8 ? "-" : "-0") + (monthOfYear + 1) + (dayOfMonth > 9 ? "-" : "-0") + dayOfMonth;
                                endTime.setText(endTimeStr);
                            }
                        }
                        // 设置初始日期
                        , c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
                        .get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        final View.OnClickListener goCompDetail = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CompSugs comp = new CompSugs();
                comp.complaintsId = ((CompPublicity) v.getTag()).getId();
                comp.complaintsPicPath = ((CompPublicity) v.getTag()).getCompPicPath();
                comp.compStatus = ((CompPublicity) v.getTag()).compStatus;
                comp.compTheme = ((CompPublicity) v.getTag()).compTheme;
                comp.complaintsContent = ((CompPublicity) v.getTag()).compContent;

                Intent intent = new Intent(SearchCompActivity.this, CompDetailActivity.class);
                intent.putExtra(Tags.TAG_COMP, StrUtils.Obj2Str(comp));
                startActivity(intent);
            }
        };

        findViewById(R.id.iv_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始搜索
                startSearch(true);
                hideInput();
            }
        });

        compAdapter = new SimpleListAdapter(this, comps, new SimpleViewInitor() {
            @Override
            public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
                if (convertView == null){
                    convertView = LinearLayout.inflate(context, R.layout.item_comppublicity, null);
                }

                getViewRender().renderView(convertView, data);
                CompPublicity cp = (CompPublicity) data;
                ((ImageView) convertView.findViewById(R.id.iv_status)).setImageResource(cp.isHandled() ? R.drawable.im_cp_handled : R.drawable.im_cp_unhandle);
                ((TextView) convertView.findViewById(R.id.tv_status)).setTextColor(getResources().getColor(cp.isHandled() ? R.color.green : R.color.red));

                convertView.setOnClickListener(goCompDetail);
                convertView.setTag(cp);
                return convertView;
            }
        });

        listViewWarp = new ListViewWarp(this, compAdapter, new ListViewWarp.WarpHandler() {
            @Override
            public boolean onRefresh() {
                return startSearch(true);
            }

            @Override
            public boolean onLoadMore() {
                return startSearch(false);
            }
        });

        ((LinearLayout)findViewById(R.id.ll_contain)).addView(listViewWarp.getRootView());

        ((EditText)findViewById(R.id.et_keyword)).setOnEditorActionListener(this);

        dwAdpter = new SimpleListAdapter(SearchCompActivity.this, dwItems, new SimpleViewInitor() {
            @Override
            public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {

                if (convertView == null){
                    convertView = LinearLayout.inflate(context, R.layout.item_keyword, null);
                }

                DistrictWarper dw = (DistrictWarper) data;

                ((CheckBox) convertView).setText(dw.district.districtName);
                ((CheckBox) convertView).setChecked(dw.checked);
                ((CheckBox) convertView).setTag(dw);
                ((CheckBox) convertView).setOnCheckedChangeListener(SearchCompActivity.this);

                return convertView;
            }
        });

        GridView gv = (GridView) findViewById(R.id.gv_areas);
        gv.setAdapter(dwAdpter);

        getRequestContext().add("riverquicksearch_data_get", new Callback<RiverQuickSearchRes>() {
            @Override
            public void callback(RiverQuickSearchRes o) {
                if (o != null && o.isSuccess()){
                    dwItems.clear();
                    District ds = new District();
                    ds.districtId = 0;
                    ds.districtName = getString(R.string.unlimeited);
                    curDw = new DistrictWarper(ds);
                    dwItems.add(curDw);

                    for (District d : o.data.districtLists){
                        DistrictWarper dw = new DistrictWarper(d);
                        dwItems.add(dw);
                    }

                    //此处可加开始搜索
                    startSearch(true);

                    dwAdpter.notifyDataSetInvalidated();
                }
            }
        }, RiverQuickSearchRes.class, ParamUtils.freeParam(null));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked){
            curDw = (DistrictWarper) buttonView.getTag();
            for (DistrictWarper d : dwItems){
                d.checked = false;
            }
            curDw.checked = true;
            dwAdpter.notifyDataSetChanged();

            //可在此添加开始搜索的代码：startSearch(true);
            startSearch(true);
        }

        hideInput();

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH){
            hideInput();
            startSearch(true);
        }
        return false;
    }

    /**
     * 隐藏输入键盘
     */
    private void hideInput(){
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(((EditText) findViewById(R.id.et_keyword)).getApplicationWindowToken(), 0);
        }
    }

    private String getKeyword(){
        return ((EditText) findViewById(R.id.et_keyword)).getText().toString();
    }


    private boolean startSearch(final boolean refresh){

        JSONObject p = null;
        if (curDw == null || curDw.district == null || curDw.district.districtId == 0){
            p = ParamUtils.freeParam(null, "searchContent", getKeyword(),
                    "startTime", startTimeStr, "endTime", endTimeStr);
        }else {
            p = ParamUtils.freeParam(null, "searchContent", getKeyword(),
                    "districtId", curDw.district.districtId, "startTime", startTimeStr,
                    "endTime", endTimeStr);
        }

        showOperating();

        if (refresh)
            listViewWarp.setRefreshing(true);
        else
            listViewWarp.setLoadingMore(true);

        getRequestContext().add("Get_SearchComplaint_List", new Callback<CompSearchDataRes>() {
            @Override
            public void callback(CompSearchDataRes o) {

                listViewWarp.setRefreshing(false);
                listViewWarp.setLoadingMore(false);

                if (o != null && o.isSuccess() && o.data != null){
                    if (refresh)
                        comps.clear();
                    for (CompPublicity c : o.data){
                        comps.add(c);
                    }
                    compAdapter.notifyDataSetChanged();
                }

                hideOperating();

                if (comps.size() == 0){
                    showToast("没有找到相关投诉信息");
                }

            }
        }, CompSearchDataRes.class, p);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (compAdapter != null){
            compAdapter.notifyDataSetChanged();
            compAdapter.notifyDataSetInvalidated();
        }
    }
}
