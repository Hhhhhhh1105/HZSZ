package com.zju.hzsz.activity;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.sin.android.sinlibs.utils.DateUtils;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.clz.ViewWarp;
import com.zju.hzsz.model.RiverQualityDataRes;
import com.zju.hzsz.model.Section;
import com.zju.hzsz.model.SectionDataRes;
import com.zju.hzsz.model.SectionIndex;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.IndexENUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.ResUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.utils.ValUtils;
import com.zju.hzsz.utils.ViewUtils;

/**
 * 河道Activity<br/>
 * 传入参数 Tags.TAG_SECTION: Section对象
 * 
 * @author Robin
 * 
 */
public class SectionActivity extends BaseActivity implements OnCheckedChangeListener, android.widget.RadioGroup.OnCheckedChangeListener {
	private Section section = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_section);

		initHead(R.drawable.ic_head_back, R.drawable.ic_head_refresh);

		section = StrUtils.Str2Obj(getIntent().getStringExtra(Tags.TAG_SECTION), Section.class);
		if (section != null) {
			setTitle(StrUtils.renderText(this, R.string.fmt_title_sectioninfo, section.sectionName));
		}

		ViewUtils.setSwipeRefreshLayoutColorScheme(((SwipeRefreshLayout) this.findViewById(R.id.srl_main)));
		((SwipeRefreshLayout) this.findViewById(R.id.srl_main)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

			@Override
			public void onRefresh() {
				loadIndexValues();
			}
		});

		this.findViewById(R.id.iv_head_right).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				loadIndexValues();
			}
		});

		((RadioButton) findViewById(R.id.rb_1month)).setChecked(true);

		((RadioGroup) findViewById(R.id.rg_period)).setOnCheckedChangeListener(this);

		this.updateUIInfo();
		if (section.indexDataJson == null)
			this.loadSectionInfo();
	}

	private SectionIndex curIndex = null;

	private void loadSectionInfo() {
		((SwipeRefreshLayout) this.findViewById(R.id.srl_main)).setRefreshing(true);
		JSONObject p = new JSONObject();
		try {
			p.put("sectionId", section.sectionId);
			getRequestContext().add("section_data_get", new Callback<SectionDataRes>() {
				@Override
				public void callback(SectionDataRes o) {
					if (o != null && o.isSuccess()) {
						section = o.data;
						updateUIInfo();
					}
					((SwipeRefreshLayout) findViewById(R.id.srl_main)).setRefreshing(false);
				}
			}, SectionDataRes.class, p);
		} catch (JSONException e) {
			e.printStackTrace();
			((SwipeRefreshLayout) this.findViewById(R.id.srl_main)).setRefreshing(false);
		}
	}

	public void loadIndexValues() {
		if (curIndex == null)
			return;
		final int days = ((RadioButton) findViewById(R.id.rb_1month)).isChecked() ? 30 : 7;
//		String dt = DateUtils.getTimeString(new Date());
		((SwipeRefreshLayout) this.findViewById(R.id.srl_main)).setRefreshing(true);
		getRequestContext().add("section_index_value_get", new Callback<RiverQualityDataRes>() {
			@Override
			public void callback(RiverQualityDataRes o) {
				if (o != null && o.isSuccess()) {
					ViewUtils.loadIndexChart(SectionActivity.this, (LineChart) findViewById(R.id.lc_chart), o.data.indexValues, true, null);
					ViewUtils.setQuilityLineV(SectionActivity.this, (LinearLayout) findViewById(R.id.inc_quality_line_v), "DO".equals(curIndex.indexNameEN), ValUtils.getYVals(curIndex.indexNameEN));
				}
				((SwipeRefreshLayout) findViewById(R.id.srl_main)).setRefreshing(false);
			}

		}, RiverQualityDataRes.class, ParamUtils.freeParam(null, "sectionId", section.sectionId, "indexId", curIndex.indexId == 0 ? 1 : curIndex.indexId, "timePeriod", days));
	}

	private void updateUIInfo() {
		ViewWarp warp = new ViewWarp(findViewById(R.id.sv_root), this);
		warp.setText(R.id.tv_name, section.sectionName);
		warp.setText(R.id.tv_name2, ResUtils.getSectionCLevel(section.sectionType));
		warp.setText(R.id.tv_lastupdate, section.uploadTime != null ? section.uploadTime.getLastUpdateYMD(this) : "");
		warp.setImage(R.id.iv_quality, ResUtils.getQuiltySmallImg(section.waterType));

		if (section.indexDataJson != null) {
			ViewUtils.initIndexTable(getBaseContext(), (LinearLayout) findViewById(R.id.ll_indexs), section.indexDataJson);

			ViewUtils.initTabLine(this, (RadioGroup) findViewById(R.id.rg_indexs), section.indexDataJson, new ViewUtils.NameGetter() {
				@Override
				public CharSequence getName(Object o) {
					SectionIndex si = (SectionIndex) o;
					return IndexENUtils.getString(si.indexNameEN);
				}
			}, this);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton rb_index, boolean checked) {
		if (checked && rb_index.getTag() instanceof SectionIndex) {
			curIndex = (SectionIndex) rb_index.getTag();
			loadIndexValues();
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup arg0, int arg1) {
		loadIndexValues();
	}
}
