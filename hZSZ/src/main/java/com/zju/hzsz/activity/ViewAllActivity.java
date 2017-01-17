package com.zju.hzsz.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sin.android.sinlibs.adapter.SimpleListAdapter;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;

public class ViewAllActivity extends Activity {

	class Holder {
		public String name;
		public int id;

		public Holder(String name, int id) {
			super();
			this.name = name;
			this.id = id;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ListView lv = new ListView(this);
		this.setContentView(lv);

		List<Holder> ls = new ArrayList<Holder>();
		ls.add(new Holder("进入主界面", 0));
		try {
			Class<?> clz = R.layout.class;
			Object o = clz.newInstance();
			for (Field f : clz.getFields()) {
				// if (Modifier.isStatic(f.getModifiers()) &&
				// (f.getName().startsWith("activity_") ||
				// f.getName().startsWith("fragment_"))) {
				ls.add(new Holder(f.getName(), f.getInt(o)));
				// }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		ListAdapter la = new SimpleListAdapter(this, ls, new SimpleViewInitor() {

			@Override
			public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
				Holder h = (Holder) data;
				if (convertView == null) {
					convertView = new TextView(ViewAllActivity.this);
				}
				TextView tv = (TextView) convertView;
				tv.setPadding(10, 10, 10, 10);
				tv.setTextSize(20);
				tv.setText(h.name);
				tv.setTag(h);
				return convertView;
			}
		});
		lv.setAdapter(la);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View view, int arg2, long arg3) {
				Holder h = (Holder) view.getTag();
				if (h.id == 0) {
					startActivity(new Intent(ViewAllActivity.this, MainActivity.class));
				} else {
					Intent intent = new Intent(ViewAllActivity.this, ViewItActivity.class);
					intent.putExtra("id", h.id);
					startActivity(intent);
				}
			}
		});
	}
}
