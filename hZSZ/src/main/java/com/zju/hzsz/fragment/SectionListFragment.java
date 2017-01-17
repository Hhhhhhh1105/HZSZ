package com.zju.hzsz.fragment;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.activity.SectionActivity;
import com.zju.hzsz.adapter.PagerItem;
import com.zju.hzsz.clz.ViewWarp;
import com.zju.hzsz.model.Section;
import com.zju.hzsz.model.SectionIndex;
import com.zju.hzsz.model.SectionListDataRes;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.IndexENUtils;
import com.zju.hzsz.utils.ResUtils;
import com.zju.hzsz.utils.StrUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 断面水质
 * 
 * @author Robin
 * 
 */
public class SectionListFragment extends TabPagerListFragment {

	class SectionPagerItem extends TabPagerListFragment.ListPagerItem implements OnItemClickListener {
		private int sectionType;
		private TextView lv_last = null;

		public SectionPagerItem(SimpleViewInitor initor, int sectionType) {
			super(initor);//继承
			this.sectionType = sectionType;
		}

		@Override
		protected void loadData(final boolean refresh) {
			JSONObject p = getPageParam(refresh);
			try {
				p.put("sectionType", sectionType);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			getRequestContext().add("section_list_get", new Callback<SectionListDataRes>() {
				@Override
				public void callback(SectionListDataRes o) {
					setLoadResult(refresh, o != null && o.isSuccess(), o != null && o.data != null ? o.data.pageInfo : null, o != null && o.data != null ? o.data.sectionJsons : null);
					if (o != null && o.isSuccess() && lv_last != null) {
						updateTime();
					}
				}
			}, SectionListDataRes.class, p);
		}

		private void updateTime() {
			if (datas.size() > 0) {
				lv_last.setVisibility(View.VISIBLE);
				lv_last.setText(((Section) datas.get(0)).uploadTime.getLastUpdateYMD(getBaseActivity()));
			} else {
				lv_last.setVisibility(View.GONE);
			}

		}

		@Override
		protected void onViewInited() {
			super.onViewInited();
			this.listViewWarp.getListView().setOnItemClickListener(this);
			lv_last = (TextView) LinearLayout.inflate(getBaseActivity(), R.layout.tv_lastupdate, null);
			// lv_last.setText("最后更新于 2014-05-12");
			lv_last.setGravity(Gravity.RIGHT);
			// lv_last.setWidth(android.view.ViewGroup.LayoutParams.MATCH_PARENT);
			this.listViewWarp.getListView().addHeaderView(lv_last);
			updateTime();
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			--position; // remover header...
			if (position >= 0 && position < datas.size()) {
				Intent intent = new Intent(getBaseActivity(), SectionActivity.class);
				intent.putExtra(Tags.TAG_SECTION, StrUtils.Obj2Str(datas.get(position)));
				startActivity(intent);
			}
		}

		public void refresh() {
			this.listViewWarp.setRefreshing(true);
			loadData(true);
		}
	}

	public SectionListFragment() {
		super(R.layout.fragment_section_list, R.id.vp_sections, R.id.rg_section_type);
	}

	// private final int[] tabids = new int[] { R.id.rb_section_main,
	// R.id.rb_section_important, R.id.rb_section_drinking,
	// R.id.rb_section_admin };
	private final int[] tabids = new int[] { R.id.rb_section_main };

	@Override
	protected int[] getTabIds() {
		return tabids;
	}

	private List<PagerItem> pagerItems = null;

	@Override
	public List<PagerItem> getPagerItems() {
		if (pagerItems == null) {
			pagerItems = new ArrayList<PagerItem>();
			SimpleViewInitor initor = new SimpleViewInitor() {
				@Override
				public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
					if (convertView == null) {
						convertView = LinearLayout.inflate(context, R.layout.item_section_list, null);
					}

					Section section = (Section) data;

					int count = ((ListView) parent).getAdapter().getCount();
					convertView.findViewById(R.id.v_spliter).setVisibility(position <= (count - 3) ? View.VISIBLE : View.GONE);

					LinearLayout ll_indexs = (LinearLayout) convertView.findViewById(R.id.ll_indexs);
					ll_indexs.removeAllViews();
					for (SectionIndex ix : section.indexDataJson) {
						View kv = LinearLayout.inflate(context, R.layout.item_section_index, null);
						((TextView) kv.findViewById(R.id.tv_index_name)).setText(IndexENUtils.getString(ix.indexNameEN));
						((TextView) kv.findViewById(R.id.tv_index_value)).setText(StrUtils.floatS2Str(ix.indexValue));

						((TextView) kv.findViewById(R.id.tv_index_value)).setBackgroundColor(context.getResources().getColor(ResUtils.getQuiltyColor(ix.indexLevel)));
						kv.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1));
						ll_indexs.addView(kv);
					}

					ViewWarp warp = new ViewWarp(convertView, context);
					warp.setText(R.id.tv_site_name, section.sectionName);
					// warp.setText(R.id.tv_site_name2,
					// section.getLevelName(context));
					warp.setText(R.id.tv_site_name2, ResUtils.getSectionCLevel(section.sectionType));
					warp.setImage(R.id.iv_quality, ResUtils.getQuiltySmallImg(section.waterType));
					return convertView;
				}
			};
			pagerItems.add(new SectionPagerItem(initor, 1));
			// pagerItems.add(new SectionPagerItem(initor, 2));
			// pagerItems.add(new SectionPagerItem(initor, 3));
			// pagerItems.add(new SectionPagerItem(initor, 4));
		}
		return pagerItems;
	}

	public void onHeadRefresh() {
		((SectionPagerItem) pagerItems.get(vpPagers.getCurrentItem())).refresh();
	}
}
