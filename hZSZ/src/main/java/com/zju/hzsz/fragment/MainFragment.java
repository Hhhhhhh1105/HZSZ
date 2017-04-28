package com.zju.hzsz.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.zju.hzsz.R;
import com.zju.hzsz.Tags;
import com.zju.hzsz.Values;
import com.zju.hzsz.activity.MainActivity;
import com.zju.hzsz.activity.NpcListActivity;
import com.zju.hzsz.activity.NpcMemberActivity;
import com.zju.hzsz.activity.OutletActivity;
import com.zju.hzsz.activity.OutletListActivity;
import com.zju.hzsz.activity.PhotoViewActivity;
import com.zju.hzsz.activity.RiverActivity;
import com.zju.hzsz.activity.RiverListActivity;
import com.zju.hzsz.activity.SectionActivity;
import com.zju.hzsz.activity.SectionListActivity;
import com.zju.hzsz.activity.SmallWaterActivity;
import com.zju.hzsz.activity.SmallWaterListActivity;
import com.zju.hzsz.activity.SugOrComtActivity;
import com.zju.hzsz.clz.RootViewWarp;
import com.zju.hzsz.model.CheckNotify;
import com.zju.hzsz.model.District;
import com.zju.hzsz.model.IndexData;
import com.zju.hzsz.model.IndexDataRes;
import com.zju.hzsz.model.Industrialport;
import com.zju.hzsz.model.Npc;
import com.zju.hzsz.model.River;
import com.zju.hzsz.model.RiverListRes;
import com.zju.hzsz.model.Section;
import com.zju.hzsz.model.SmallWater;
import com.zju.hzsz.model.StartInfo;
import com.zju.hzsz.net.Callback;
import com.zju.hzsz.utils.ImgUtils;
import com.zju.hzsz.utils.ParamUtils;
import com.zju.hzsz.utils.ResUtils;
import com.zju.hzsz.utils.StrUtils;
import com.zju.hzsz.utils.ViewUtils;

import static com.zju.hzsz.R.id.ll_npc;
import static com.zju.hzsz.R.id.ll_outlets;
import static com.zju.hzsz.R.id.ll_rivers;
import static com.zju.hzsz.R.id.ll_smallwater;

public class MainFragment extends BaseFragment implements OnRefreshListener {

	/*
	 * class SectionBoxAdapter extends PagerAdapter { ArrayList<View>
	 * viewContainter = new ArrayList<View>();
	 * 
	 * @Override public int getCount() { return viewContainter.size(); }
	 * 
	 * @Override public boolean isViewFromObject(View arg0, Object arg1) {
	 * return arg0 == arg1; }
	 * 
	 * @Override public void destroyItem(ViewGroup container, int position,
	 * Object object) { ((ViewPager)
	 * container).removeView(viewContainter.get(position)); }
	 * 
	 * @Override public Object instantiateItem(ViewGroup container, int
	 * position) { container.addView(viewContainter.get(position)); return
	 * viewContainter.get(position); }
	 * 
	 * @Override public void notifyDataSetChanged() {
	 * this.viewContainter.clear(); for (int i = 0; indexData != null && i <
	 * indexData.riverJsons.length; ++i) { River section =
	 * indexData.riverJsons[i]; View view = LinearLayout.inflate(getActivity(),
	 * R.layout.item_mainpage_sectionbox, null); ((TextView)
	 * view.findViewById(R.id.tv_section_name)).setText(section.riverName);
	 * ((TextView)
	 * view.findViewById(R.id.tv_lastupdate)).setText(section.uploadTime == null
	 * ? "" : section.uploadTime.getLastUpdate(getActivity())); ((TextView)
	 * view.findViewById(R.id.tv_datetime)).setText(section.uploadTime == null ?
	 * "" : section.uploadTime.getDateWeek(getActivity()));
	 * 
	 * ((TextView)
	 * view.findViewById(R.id.tv_section_level)).setText(ResUtils.getRiverSLevel
	 * (section.riverType)); ((ImageView)
	 * view.findViewById(R.id.iv_quality)).setImageResource
	 * (ResUtils.getQuiltySmallImg(section.waterType));
	 * 
	 * String img = StrUtils.getImgUrl(section.picPath);
	 * ImgUtils.loadImage(getActivity(), ((ImageView)
	 * view.findViewById(R.id.iv_picture)), img); viewContainter.add(view); }
	 * super.notifyDataSetChanged(); }
	 * 
	 * @Override public int getItemPosition(Object object) { return
	 * POSITION_NONE; } }
	 */
	// private PagerAdapter adapter = null;

	//广播接收器
	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			CheckNotify cn = intent == null ? null : StrUtils.Str2Obj(intent.getStringExtra(Tags.TAG_PARAM), CheckNotify.class);
			if (cn != null) {
				boolean nt = cn.sumUndealAdv > 0 || cn.sumUndealComp > 0 || cn.sumUnReadMail > 0;
				if (rootView != null) {
					//若有通知则进行提醒
					//((ImageView) rootView.findViewById(R.id.iv_head_left)).setImageResource(nt ? R.drawable.ic_head_user_notify : R.drawable.ic_head_user);
				}
			}
		}
	};

	private IndexData indexData = null;
	private SwipeRefreshLayout swipeRefreshLayout = null;
	StartInfo startInfo = null;

	private LocationClient mLocationClient;
	private BDLocationListener mBDLocationListener;
	private double latitude;
	private double longitude;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_main, container, false);
			// new PhotoViewAttacher(((ImageView)
			// rootView.findViewById(R.id.iv_banner)), true);

			RootViewWarp warp = getRootViewWarp();
			warp.setHeadImage(R.drawable.ic_head_sug, R.drawable.ic_head_share);


			//点击查看水质说明
			rootView.findViewById(R.id.tv_qualityexplain).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					//单纯展示XML界面
					getBaseActivity().startXmlActivity(R.layout.activity_qualityexplain, R.string.qualityexplain, 0, 0);
				}
			});
			//点击查看所有河道
			rootView.findViewById(R.id.tv_selriver).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// showAreaPop();
					// 河道列表
					// Intent intent = new Intent(getBaseActivity(),
					// SearchRiverActivity.class);
					// intent.putExtra(Tags.TAG_CODE, Tags.CODE_ALLRIVER);
					// intent.putExtra(Tags.TAG_TITLE, "所有河道");
					// startActivityForResult(intent, Tags.CODE_ALLRIVER);
					Intent intent = new Intent(getBaseActivity(), RiverListActivity.class);
					//startActivityForResult(Intent intent, int requestCode)
					startActivityForResult(intent, Tags.CODE_ALLRIVER);
				}
			});

			//查看所有断面
			rootView.findViewById(R.id.tv_selsection).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// showAreaPop();
					// 所有断面
					Intent intent = new Intent(getBaseActivity(), SectionListActivity.class);
					// intent.putExtra(Tags.TAG_CODE, Tags.CODE_SELECTRIVER);
					// intent.putExtra(Tags.TAG_TITLE, "");
					startActivityForResult(intent, Tags.CODE_SELECTRIVER);
				}
			});
			rootView.findViewById(R.id.iv_head_left).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					//跳转至个人中心
					//getBaseActivity().startActivity(new Intent(getBaseActivity(), MeActivity.class), false);
					// if (getBaseActivity().getUser().isLogined()) {
					// getBaseActivity().startActivity(new
					// Intent(getBaseActivity(), MeActivity.class), false);
					// } else {
					// Intent intent = new Intent(getBaseActivity(),
					// LoginActivity.class);
					// getBaseActivity().startActivityForResult(intent,
					// Tags.CODE_LOGIN, false);
					// }
					//跳转至一键投诉
					Intent intent = new Intent(getBaseActivity(), SugOrComtActivity.class);
					intent.putExtra(Tags.TAG_ABOOLEAN, true);
					startActivity(intent);
				}
			});
			rootView.findViewById(R.id.ib_photocomp).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// startActivity(new Intent(getActivity(),
					// CompPhotoActivity.class));
					Intent intent = new Intent(MainFragment.this.getBaseActivity(), PhotoViewActivity.class);
					// List<String> list = new ArrayList<String>();
					// intent.setStringArrayExtra("URLS", new String[]{});
					intent.putExtra("URLS", new String[] { "http://f.hiphotos.baidu.com/image/h%3D300/sign=05cb47c0aac3793162688029dbc5b784/a1ec08fa513d269724d58d9b50fbb2fb4216d8f6.jpg", "http://b.hiphotos.baidu.com/image/h%3D300/sign=be5a59d2bd99a90124355d362d940a58/2934349b033b5bb56880a53833d3d539b700bca5.jpg", "http://b.hiphotos.baidu.com/image/h%3D300/sign=92f2730bf9039245beb5e70fb795a4a8/b8014a90f603738df84b89e4b61bb051f919ecf7.jpg", "http://e.hiphotos.baidu.com/image/h%3D360/sign=2a7b0f0d0f7b020813c939e752d8f25f/14ce36d3d539b6007e8e281fec50352ac75cb7d1.jpg" });
					startActivity(intent);
				}
			});
			rootView.findViewById(R.id.iv_head_right).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// String appID = "wx967daebe835fbeac";
					// String appSecret = "5fa9e68ca3970e87a1f83e563c8dcbce";

					// String appID = "wx10215a83b40826f4";
					// String appSecret = "542cbe3dfd98c54810046a73";

					if (getBaseActivity() instanceof MainActivity) {
						((MainActivity) getBaseActivity()).startShare();
						return;
					}

					// 发布版本
					String appID = "wx2997236d4d70a3aa";
					String appSecret = "08e8bc26bc021917d9caf2082931fa58";

					// 测试版本
					// String appID = "wx7c34b978e3add950";
					// String appSecret = "bb00b86efe44f194380d40eb001cd0b4";

					// 添加微信朋友圈
					UMWXHandler wxCircleHandler = new UMWXHandler(getBaseActivity(), appID, appSecret);
					wxCircleHandler.setToCircle(true);
					wxCircleHandler.addToSocialSDK();

					UMSocialService umSocialService = UMServiceFactory.getUMSocialService("com.umeng.share");
					umSocialService.getConfig().removePlatform(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.QQ);
					umSocialService.getConfig().removePlatform(SHARE_MEDIA.QZONE, SHARE_MEDIA.SINA, SHARE_MEDIA.TENCENT);

					// 设置微信朋友圈分享内容
					CircleShareContent circleMedia = new CircleShareContent();

					circleMedia.setTitle(getString(R.string.app_name));
					circleMedia.setTargetUrl("http://220.191.208.69:8080/ShuiHuanJingFabu/download/download.jsp");

					circleMedia.setShareImage(new UMImage(getBaseActivity(), "http://7xiw6r.com1.z0.glb.clouddn.com/ic_launcher-web.png"));
					// circleMedia.setShareContent("杭州河道水质App正式发布，大家一起来关注身边的水环境！");

					circleMedia.setShareContent("杭州河道水质App首发，全国首个城市河道水质数据和河长制信息公开发布App!");

					umSocialService.setShareMedia(circleMedia);
					umSocialService.postShare(getBaseActivity(), SHARE_MEDIA.WEIXIN_CIRCLE, null);
				}
			});

			rootView.findViewById(R.id.iv_onekey_comp).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(getBaseActivity(), SugOrComtActivity.class);
					intent.putExtra(Tags.TAG_ABOOLEAN, true);
					startActivity(intent);
				}
			});

			//gone
			rootView.findViewById(R.id.iv_wbanner).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (startInfo != null) {
						Uri uri = Uri.parse(startInfo.linkPath);
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(intent);
					}
				}
			});
			//“查看所有排放口”跳转
			rootView.findViewById(R.id.tv_seloutlet).setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getBaseActivity(), OutletListActivity.class);
					startActivity(intent);
				}
			});
			//“查看所有小微水体”跳转
			rootView.findViewById(R.id.tv_selsmallwater).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(getBaseActivity(), SmallWaterListActivity.class);
					startActivity(intent);
				}
			});
			//“查看所有代表”跳转
			rootView.findViewById(R.id.tv_selnpc).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(getBaseActivity(), NpcListActivity.class);
					startActivity(intent);
				}
			});

			/*
			 * adapter = new SectionBoxAdapter(); ViewPager vp_sections =
			 * (ViewPager) rootView.findViewById(R.id.vp_sections);
			 * vp_sections.setAdapter(adapter);
			 * vp_sections.setOnPageChangeListener(new
			 * ViewPager.OnPageChangeListener() {
			 * 
			 * @Override public void onPageSelected(int arg0) { Log.e(getTag(),
			 * "sel:" + arg0); }
			 * 
			 * @Override public void onPageScrolled(int arg0, float arg1, int
			 * arg2) {
			 * 
			 * }
			 * 
			 * @Override public void onPageScrollStateChanged(int arg0) { if
			 * (swipeRefreshLayout != null) swipeRefreshLayout.setEnabled(arg0
			 * == ViewPager.SCROLL_STATE_IDLE); } });
			 */

			swipeRefreshLayout = ((SwipeRefreshLayout) rootView.findViewById(R.id.srl_main));
			ViewUtils.setSwipeRefreshLayoutColorScheme(swipeRefreshLayout);
			swipeRefreshLayout.setOnRefreshListener(this);
			((LinearLayout) rootView.findViewById(R.id.ll_sections)).removeAllViews();
			((LinearLayout) rootView.findViewById(ll_rivers)).removeAllViews();
			((LinearLayout) rootView.findViewById(ll_outlets)).removeAllViews();
			((LinearLayout) rootView.findViewById(ll_smallwater)).removeAllViews();
			((LinearLayout) rootView.findViewById(ll_npc)).removeAllViews();

			refreshData();

			//注册广播
			IntentFilter filter = new IntentFilter(Tags.ACT_USERSTATUSCHANGED);
			receiver.onReceive(getBaseActivity(), null);
			getBaseActivity().registerReceiver(receiver, filter);
		}
		return rootView;
	}

	private void refreshData() {
		if (indexData == null) {
			showOperating();
		}
		getBaseActivity().checkChiefNotify();

/*		getRequestContext().add("Get_FirstLittlePic", new Callback<StartInfoRes>() {
			@Override
			public void callback(StartInfoRes o) {
				Log.d("cll","解析成功");
				if (o != null && o.isSuccess() && o.data != null) {
					startInfo = o.data;
					ImgUtils.loadImage(getActivity(), (ImageView) rootView.findViewById(R.id.iv_wbanner), com.zju.hzsz.utils.StrUtils.getImgUrl(o.data.picPath));
					rootView.findViewById(R.id.iv_wbanner).setVisibility(View.VISIBLE);
					rootView.findViewById(R.id.ll_qualityexplain).setVisibility(View.GONE);
				}
			}
		}, StartInfoRes.class, null);*/

		//获取当前位置信息相关
		mLocationClient = new LocationClient(getBaseActivity().getApplicationContext());
		mBDLocationListener = new MyBDLocationListner();
		mLocationClient.registerLocationListener(mBDLocationListener);
		getLocation();

		if (curDistrict == null) {
			/*getRequestContext().add("index_data_get", new Callback<IndexDataRes>() {
				@Override
				public void callback(IndexDataRes o) {
					Log.i(getTag(), "" + o);
					if (o != null && o.isSuccess()) {
						indexData = o.data;
						String bannerimg = StrUtils.getImgUrl(indexData.sloganPicPath);
						if (bannerimg != null) {
							ImgUtils.loadImage(getBaseActivity(), ((ImageView) rootView.findViewById(R.id.iv_banner)), bannerimg);
						}

						Values.districtLists = indexData.districtLists;

						refreshSectionUI();
						refreshRiverUI();
						refreashOutletUI();
//						 refreshRiverUI2();
					}

					if (o == null){
						System.out.println("返回值为空");
					}

					((SwipeRefreshLayout) rootView.findViewById(R.id.srl_main)).setRefreshing(false);
					hideOperating();
				}
			}, IndexDataRes.class, ParamUtils.freeParam(null, "latitude", latitude, "longitude", longitude));*/
		} else {
			((SwipeRefreshLayout) rootView.findViewById(R.id.srl_main)).setRefreshing(true);
			getRequestContext().add("currentareariver_list_get", new Callback<RiverListRes>() {
				@Override
				public void callback(RiverListRes o) {
					Log.i(getTag(), "" + o);
					if (o != null && o.isSuccess()) {
						indexData.riverJsons = o.data;
						// adapter.notifyDataSetChanged();
						// ((ViewPager)
						// rootView.findViewById(R.id.vp_sections)).setCurrentItem(0);

						refreshRiverUI();
					}
					((SwipeRefreshLayout) rootView.findViewById(R.id.srl_main)).setRefreshing(false);
					hideOperating();
				}
			}, RiverListRes.class, ParamUtils.freeParam(null, "districtId", curDistrict.districtId));
		}
	}

	private void refreshSectionUI() {
		LinearLayout ll_sections = (LinearLayout) rootView.findViewById(R.id.ll_sections);
		ll_sections.removeAllViews();

		View.OnClickListener gosec = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getTag() != null) {
					Intent intent = new Intent(getBaseActivity(), SectionActivity.class);
					intent.putExtra(Tags.TAG_SECTION, StrUtils.Obj2Str(v.getTag()));
					startActivity(intent);
				}
			}
		};

		for (int i = 0; i < indexData.sectionJsons.length; i += 2) {
			Section section_l = indexData.sectionJsons[i];
			Section section_r = (i + 1) < indexData.sectionJsons.length ? indexData.sectionJsons[i + 1] : null;

			View view = LinearLayout.inflate(getBaseActivity(), R.layout.item_mainpage_section, null);

			((TextView) view.findViewById(R.id.tv_name_l)).setText(section_l.sectionName);
			((TextView) view.findViewById(R.id.tv_level_l)).setText(ResUtils.getSectionCLevel(section_l.sectionType));
			((ImageView) view.findViewById(R.id.iv_quality_l)).setImageResource(ResUtils.getQuiltyImg(section_l.waterType));

			view.findViewById(R.id.rl_section_left).setOnClickListener(gosec);
			view.findViewById(R.id.rl_section_left).setTag(section_l);

			if (section_r != null) {
				((TextView) view.findViewById(R.id.tv_name_r)).setText(section_r.sectionName);
				((TextView) view.findViewById(R.id.tv_level_r)).setText(ResUtils.getSectionCLevel(section_r.sectionType));
				((ImageView) view.findViewById(R.id.iv_quality_r)).setImageResource(ResUtils.getQuiltyImg(section_r.waterType));

				view.findViewById(R.id.rl_section_right).setOnClickListener(gosec);
				view.findViewById(R.id.rl_section_right).setTag(section_r);
			} else {
				view.findViewById(R.id.rl_section_right).setVisibility(View.INVISIBLE);
			}
			ll_sections.addView(view);
		}
	}

	//这个方法并没有用到
	protected void refreshRiverUI2() {
		LinearLayout ll_rivers = (LinearLayout) rootView.findViewById(R.id.ll_rivers);
		ll_rivers.removeAllViews();

		View.OnClickListener riverClick = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getTag() != null) {
					Intent intent = new Intent(getBaseActivity(), RiverActivity.class);
					intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(v.getTag()));
					startActivity(intent);
				}
			}
		};

		for (int i = 0; i < indexData.riverJsons.length; ++i) {
			River river = indexData.riverJsons[i];
			View view = LinearLayout.inflate(getBaseActivity(), R.layout.item_mainpage_river, null);

			((TextView) view.findViewById(R.id.tv_river)).setText(river.riverName);
			((TextView) view.findViewById(R.id.tv_level)).setText(ResUtils.getRiverSLittleLevel(river.riverType));
			((ImageView) view.findViewById(R.id.iv_quality)).setImageResource(ResUtils.getQuiltyImg(river.waterType));

			//这里是由于i是从0开始计数的
			if (i % 2 == 0) {
				view.setBackgroundResource(R.drawable.btn_white_bg_gray);
			} else {
				view.setBackgroundResource(R.drawable.btn_gray_white);
			}
			view.setTag(river);
			view.setOnClickListener(riverClick);
			ll_rivers.addView(view);
		}

	}

	private void refreshRiverUI() {
		LinearLayout ll_rivers = (LinearLayout) rootView.findViewById(R.id.ll_rivers);
		ll_rivers.removeAllViews();

		View.OnClickListener riverClick = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getTag() != null) {
					Intent intent = new Intent(getBaseActivity(), RiverActivity.class);
					intent.putExtra(Tags.TAG_RIVER, StrUtils.Obj2Str(v.getTag()));
					startActivity(intent);
				}
			}
		};

		for (int i = 0; i < indexData.riverJsons.length; i += 2) {
			River river_l = indexData.riverJsons[i];
			River river_r = (i + 1) < indexData.riverJsons.length ? indexData.riverJsons[i + 1] : null;
			//用的其实是断面的布局
			View view = LinearLayout.inflate(getBaseActivity(), R.layout.item_mainpage_section, null);

			((TextView) view.findViewById(R.id.tv_name_l)).setText(river_l.riverName);
			((TextView) view.findViewById(R.id.tv_level_l)).setText(ResUtils.getRiverSLevel(river_l.riverType));
			((ImageView) view.findViewById(R.id.iv_quality_l)).setImageResource(ResUtils.getQuiltyImg(river_l.waterType));

			view.findViewById(R.id.rl_section_left).setOnClickListener(riverClick);
			view.findViewById(R.id.rl_section_left).setTag(river_l);

			if (river_r != null) {
				((TextView) view.findViewById(R.id.tv_name_r)).setText(river_r.riverName);
				((TextView) view.findViewById(R.id.tv_level_r)).setText(ResUtils.getRiverSLevel(river_r.riverType));
				((ImageView) view.findViewById(R.id.iv_quality_r)).setImageResource(ResUtils.getQuiltyImg(river_r.waterType));

				view.findViewById(R.id.rl_section_right).setOnClickListener(riverClick);
				view.findViewById(R.id.rl_section_right).setTag(river_r);
			} else {
				view.findViewById(R.id.rl_section_right).setVisibility(View.INVISIBLE);
			}
			ll_rivers.addView(view);
		}
	}

	//更新阳光排放口的信息
	private void refreashOutletUI(){
		LinearLayout ll_outlets = (LinearLayout) rootView.findViewById(R.id.ll_outlets);
		ll_outlets.removeAllViews();

		//点击单个item，跳转页面
		View.OnClickListener outletClick = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getTag() != null) {
					Intent intent = new Intent(getBaseActivity(), OutletActivity.class);
					intent.putExtra(Tags.TAG_OUTLET, StrUtils.Obj2Str(v.getTag()));
					startActivity(intent);
				}
			}
		};

		for (int i = 0; i < indexData.industrialportlists.length; i += 2) {
			Industrialport industrialport_l = indexData.industrialportlists[i];
			Industrialport industrialport_r = (i + 1) < indexData.industrialportlists.length ? indexData.industrialportlists[i + 1] : null;
			//用的其实是断面的布局
			View view = LinearLayout.inflate(getBaseActivity(), R.layout.item_mainpage_section, null);

			//设置所需要显示的内容，排放口+流域名称，去掉水质等级
			((TextView) view.findViewById(R.id.tv_name_l)).setText(industrialport_l.sourceName);
			((TextView) view.findViewById(R.id.tv_level_l)).setText(industrialport_l.basin);
			((ImageView) view.findViewById(R.id.iv_quality_l)).setImageResource(R.drawable.umeng_socialize_location_ic);

			view.findViewById(R.id.rl_section_left).setOnClickListener(outletClick);
			view.findViewById(R.id.rl_section_left).setTag(industrialport_l);

			if (industrialport_r != null) {
				((TextView) view.findViewById(R.id.tv_name_r)).setText(industrialport_r.sourceName);
				((TextView) view.findViewById(R.id.tv_level_r)).setText(industrialport_r.basin);
//				view.findViewById(R.id.iv_quality_r).setVisibility(View.GONE);
				((ImageView) view.findViewById(R.id.iv_quality_r)).setImageResource(R.drawable.umeng_socialize_location_ic);

				view.findViewById(R.id.rl_section_right).setOnClickListener(outletClick);
				view.findViewById(R.id.rl_section_right).setTag(industrialport_r);
			} else {
				view.findViewById(R.id.rl_section_right).setVisibility(View.INVISIBLE);
			}
			ll_outlets.addView(view);
		}
	}

	//更新小微水体信息
	private void refreshSmallWaterUI(){
		LinearLayout ll_smallwater = (LinearLayout) rootView.findViewById(R.id.ll_smallwater);
		ll_smallwater.removeAllViews();

		//点击单个item，进行跳转
		View.OnClickListener smallWaterClick = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (view.getTag() != null) {
					Intent intent = new Intent(getBaseActivity(), SmallWaterActivity.class);
					intent.putExtra(Tags.TAG_SMALLWATER, StrUtils.Obj2Str(view.getTag()));
					startActivity(intent);
				}
			}
		};

		for (int i = 0; i < indexData.smallWaterSums.length; i += 2) {
			SmallWater smallWater_l = indexData.smallWaterSums[i];
			SmallWater smallWater_r = indexData.smallWaterSums[i + 1];

			View view = LinearLayout.inflate(getBaseActivity(), R.layout.item_mainpage_section, null);

			((TextView) view.findViewById(R.id.tv_name_l)).setText(smallWater_l.waterName);
			((TextView) view.findViewById(R.id.tv_level_l)).setText(smallWater_l.position);
			((ImageView) view.findViewById(R.id.iv_quality_l)).setImageResource(R.drawable.ic_smallwater_2);

			view.findViewById(R.id.rl_section_left).setTag(smallWater_l);
			view.findViewById(R.id.rl_section_left).setOnClickListener(smallWaterClick);

			if (smallWater_r != null) {
				((TextView) view.findViewById(R.id.tv_name_r)).setText(smallWater_r.waterName);
				((TextView) view.findViewById(R.id.tv_level_r)).setText(smallWater_r.position);
				((ImageView) view.findViewById(R.id.iv_quality_r)).setImageResource(R.drawable.ic_smallwater_2);

				view.findViewById(R.id.rl_section_right).setTag(smallWater_r);
				view.findViewById(R.id.rl_section_right).setOnClickListener(smallWaterClick);
			} else {
				view.findViewById(R.id.rl_section_right).setVisibility(View.INVISIBLE);
			}

			ll_smallwater.addView(view);
		}

	}

	//更新人大代表监督信息
	private void refreshNpcUI() {
		LinearLayout ll_npc = (LinearLayout) rootView.findViewById(R.id.ll_npc);
		ll_npc.removeAllViews();

		View.OnClickListener npcClick = new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (view.getTag() != null) {
					Intent intent = new Intent(getBaseActivity(), NpcMemberActivity.class);
					intent.putExtra(Tags.TAG_NPC, StrUtils.Obj2Str(view.getTag()));
					startActivity(intent);
				}
			}
		};

		/*for (int i = 0; i < indexData.riverJsons.length; i += 2) {
			River river_l = indexData.riverJsons[i];
			River river_r = (i + 1) < indexData.riverJsons.length ? indexData.riverJsons[i + 1] : null;
			View view = LinearLayout.inflate(getBaseActivity(), R.layout.item_mainpage_npc, null);

			((TextView) view.findViewById(R.id.tv_river_l)).setText("于1天前巡过" + river_l.riverName);


			view.findViewById(R.id.rl_npc_left).setOnClickListener(npcClick);
			view.findViewById(R.id.rl_npc_left).setTag(river_l);

			if (river_r != null) {

				((TextView) view.findViewById(R.id.tv_river_r)).setText("于1天前巡过" + river_r.riverName);

				view.findViewById(R.id.rl_npc_right).setOnClickListener(npcClick);
				view.findViewById(R.id.rl_npc_right).setTag(river_r);
			} else {
				view.findViewById(R.id.rl_npc_right).setVisibility(View.INVISIBLE);
			}

			ll_npc.addView(view);
		}*/

		for (int i = 0; i < indexData.deputiesJsons.length; i += 2) {
			Npc npc_l = indexData.deputiesJsons[i];
			Npc npc_r = (i + 1) < indexData.deputiesJsons.length ? indexData.deputiesJsons[i + 1] : null;
			View view = LinearLayout.inflate(getBaseActivity(), R.layout.item_mainpage_npc, null);

			((TextView) view.findViewById(R.id.tv_name_l)).setText(npc_l.name);  //人大姓名
			((TextView) view.findViewById(R.id.tv_title_l)).setText(ResUtils.getNpcTitle(npc_l.authority));   //管理等级
			if (npc_l.lastRecordDays > 0)
				((TextView) view.findViewById(R.id.tv_river_l)).setText("于" + npc_l.lastRecordDays + "天前巡过" + npc_l.riverName);  //巡河信息
			else
				((TextView) view.findViewById(R.id.tv_river_l)).setText("" + "于今天巡过" + npc_l.riverName);

			view.findViewById(R.id.rl_npc_left).setOnClickListener(npcClick);
			view.findViewById(R.id.rl_npc_left).setTag(npc_l);

			if (npc_r != null) {

				((TextView) view.findViewById(R.id.tv_name_r)).setText(npc_r.name);  //人大姓名
				((TextView) view.findViewById(R.id.tv_title_r)).setText(ResUtils.getNpcTitle(npc_r.authority));   //管理等级
				if (npc_r.lastRecordDays > 0)
					((TextView) view.findViewById(R.id.tv_river_r)).setText("于" + npc_r.lastRecordDays + "天前巡过" + npc_r.riverName);  //巡河信息
				else
					((TextView) view.findViewById(R.id.tv_river_r)).setText("" + "于今天巡过" + npc_r.riverName);

				view.findViewById(R.id.rl_npc_right).setOnClickListener(npcClick);
				view.findViewById(R.id.rl_npc_right).setTag(npc_r);
			} else {
				view.findViewById(R.id.rl_npc_right).setVisibility(View.INVISIBLE);
			}

			ll_npc.addView(view);
		}
	}

	@Override
	public void onRefresh() {
		refreshData();
	}

	private District curDistrict = null;

	//
	@SuppressWarnings("unused")
	private void showAreaPop() {
		if (indexData == null || indexData.districtLists == null || indexData.districtLists.length == 0)
			return;
		String[] names = new String[indexData.districtLists.length];
		int i = 0;
		for (District d : indexData.districtLists) {
			names[i++] = d.districtName;
		}
		Dialog alertDialog = new AlertDialog.Builder(getBaseActivity()).setTitle(R.string.select_area).setItems(names,  new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				curDistrict = indexData.districtLists[which];
				((TextView) rootView.findViewById(R.id.tv_selriver)).setText(curDistrict.districtName);
				refreshData();
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).create();
		alertDialog.show();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		try {
			getActivity().unregisterReceiver(receiver);
		} catch (Exception e) {
		}
		//取消监听函数
		if (mLocationClient != null) {
			mLocationClient.unRegisterLocationListener(mBDLocationListener);
		 }

	}


	public void getLocation(){
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
		option.setCoorType("bd09ll");
		option.setScanSpan(5000);

		mLocationClient.setLocOption(option);
		mLocationClient.start();
	}

	/**
	 * 获取当前的位置信息&获取位置信息后发送请求
	 */
	private class MyBDLocationListner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation bdLocation) {
			if (bdLocation != null){
				latitude = bdLocation.getLatitude();
				longitude = bdLocation.getLongitude();
				//将经纬度传给BaseAcitivity
				getBaseActivity().setLatitude(latitude);
				getBaseActivity().setLongitude(longitude);
				System.out.println("latitude:" + getBaseActivity().getLatitude() + "," + "longitude:" + getBaseActivity().getLongitude());
				initData();
				if (mLocationClient.isStarted()){
					mLocationClient.stop();
					mLocationClient.unRegisterLocationListener(mBDLocationListener);
				}

			}
		}
	}

	/**
	 * 获取首页数据所发送的请求
	 */
	public void initData(){
		getRequestContext().add("index_data_get", new Callback<IndexDataRes>() {
			@Override
			public void callback(IndexDataRes o) {
				Log.i(getTag(), "" + o);
				if (o != null && o.isSuccess()) {
					indexData = o.data;
					String bannerimg = StrUtils.getImgUrl(indexData.sloganPicPath);
					if (bannerimg != null) {
						ImgUtils.loadImage(getBaseActivity(), ((ImageView) rootView.findViewById(R.id.iv_banner)), bannerimg);
					}

					Values.districtLists = indexData.districtLists;

					refreshSectionUI();
					refreshRiverUI();
					refreashOutletUI();
					refreshSmallWaterUI();
					//如果是人大代表，则显示人大代表监督模块
					if (getBaseActivity().getUser().isNpc()){
						rootView.findViewById(R.id.ll_npc).setVisibility(View.VISIBLE);
						rootView.findViewById(R.id.ll_npc_text).setVisibility(View.VISIBLE);
						((RadioButton) getBaseActivity().findViewById(R.id.rd_panhang)).setText("代表监督");
						refreshNpcUI();
					} else {
						rootView.findViewById(R.id.ll_npc_text).setVisibility(View.GONE);
						rootView.findViewById(R.id.ll_npc).setVisibility(View.GONE);
					}
				}

				if (o == null){
					System.out.println("返回值为空");
				}

				((SwipeRefreshLayout) rootView.findViewById(R.id.srl_main)).setRefreshing(false);
				hideOperating();
			}
		}, IndexDataRes.class, ParamUtils.freeParam(null, "latitude", latitude, "longitude", longitude));
	}
}
