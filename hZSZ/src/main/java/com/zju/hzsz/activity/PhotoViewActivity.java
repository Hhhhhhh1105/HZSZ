package com.zju.hzsz.activity;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.sin.android.sinlibs.adapter.PagerItem;
import com.sin.android.sinlibs.adapter.SimplePagerAdapter;
import com.zju.hzsz.R;
import com.zju.hzsz.utils.ImgUtils;

public class PhotoViewActivity extends BaseActivity {
	android.support.v4.view.ViewPager vp_photos = null;

	// SimplePagerAdapter adapter = new SimplePagerAdapter(items)

	class PhotoViewPagerItem extends PagerItem {
		private Context context;
		private String imgurl;
		private View view = null;

		public PhotoViewPagerItem(Context context, String imgurl) {
			super();
			this.context = context;
			this.imgurl = imgurl;
		}

		@Override
		public View getView() {
			if (view == null) {
				ImageView iv = new PhotoView(context);
				ImgUtils.loadImage(context, iv, imgurl, R.drawable.im_loading, R.drawable.im_failed);
				view = iv;
			}
			return view;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photoview);

		vp_photos = (ViewPager) findViewById(R.id.vp_photos);

		List<PagerItem> items = new ArrayList<PagerItem>();

		String[] urls = getIntent().getStringArrayExtra("URLS");

		if (urls != null && urls.length > 0) {
			for (String url : urls) {
				Log.i("NET", "PIC " + url);
				items.add(new PhotoViewPagerItem(this, url));
			}
			vp_photos.setAdapter(new SimplePagerAdapter(items));
			int cur = getIntent().getIntExtra("CUR", 0);
			vp_photos.setCurrentItem(cur);
		}
	}
}
