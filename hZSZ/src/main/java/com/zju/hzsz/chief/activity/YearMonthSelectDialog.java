package com.zju.hzsz.chief.activity;

import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.zju.hzsz.R;

public class YearMonthSelectDialog extends AlertDialog implements DialogInterface.OnClickListener {
	public interface Callback {
		public void onYMSelected(int year, int month);
	}

	private Callback callback = null;
	private NumberPicker np_year = null;
	private NumberPicker np_month = null;

	public YearMonthSelectDialog(Context context, Callback callback) {
		super(context);
		this.callback = callback;
		setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), this);
		setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this);

		View view = LinearLayout.inflate(context, R.layout.dialog_ymselect, null);

		setView(view);

		np_year = (NumberPicker) view.findViewById(R.id.np_year);
		np_month = (NumberPicker) view.findViewById(R.id.np_month);

		np_year.setMinValue(1900);
		np_year.setMaxValue(9999);

		np_month.setMinValue(1);
		np_month.setMaxValue(12);

		np_year.setValue(Calendar.getInstance().get(Calendar.YEAR));
		np_month.setValue(Calendar.getInstance().get(Calendar.MONTH) + 1);

		np_year.setFormatter(new NumberPicker.Formatter() {

			@Override
			public String format(int v) {
				return v + "年";
			}
		});

		np_month.setFormatter(new NumberPicker.Formatter() {

			@Override
			public String format(int v) {
				return v + "月";
			}
		});
	}

	@Override
	public void onClick(DialogInterface arg0, int w) {
		if (w == BUTTON_POSITIVE && callback != null) {
			callback.onYMSelected(np_year.getValue(), np_month.getValue());
		}
	}
}
