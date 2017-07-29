package com.zju.hzsz.model;

import android.content.Context;

import com.zju.hzsz.R;
import com.zju.hzsz.utils.ResUtils;
import com.zju.hzsz.utils.StrUtils;

import java.util.Date;
import java.util.Locale;

/**
 * 时间
 * 
 * @author Robin
 * 
 */
public class DateTime {
	public long time; // timestamp
	public int timezoneOffset;
	public int hours;
	public int minutes;
	public int seconds;
	public int month;
	public int day;
	public int date;
	public int year;

	public String getLastUpdate(Context context) {
		return StrUtils.renderText(context, R.string.fmt_lastupdate, date, hours, minutes);
	}

	public String getUpdateYMDHM(Context context) {
		return StrUtils.renderText(context, R.string.fmt_update_ymdhm, 1900 + year, month + 1, date, hours, minutes);
	}

	public String getLastUpdateMD(Context context) {
		return StrUtils.renderText(context, R.string.fmt_lastupdate_md, month + 1, date);
	}

	public String getLastUpdateYMD(Context context) {
		return StrUtils.renderText(context, R.string.fmt_lastupdate_ymd, 1900 + year, month + 1, date);
	}

	public String getYMD(Context context) {
		return StrUtils.renderText(context, R.string.fmt_ymd, 1900 + year, month + 1, date);
	}

	public String getYMD2(Context context) {
		return StrUtils.renderText(context, R.string.fmt_ymd2, 1900 + year, month + 1, date);
	}

	public String getYMDHM(Context context) {
		return StrUtils.renderText(context, R.string.fmt_ymdhm, 1900 + year, month + 1, date, hours, minutes);
	}

	public String getYMDHMS(Context context) {
		return StrUtils.renderText(context, R.string.fmt_ymdhms, 1900 + year, month + 1, date, hours, minutes, seconds);
	}

	public String getYMDHM() {
		return String.format(Locale.getDefault(), "%s。%02d。%02d %02d:%02d", 1900 + year, month + 1, date, hours, minutes);
	}

	public String getYM(Context context) {
		return StrUtils.renderText(context, R.string.fmt_date_ym, 1900 + year, month + 1);
	}

	public String getDateWeek(Context context) {
		return StrUtils.renderText(context, R.string.fmt_dateweek, month + 1, date, context.getString(ResUtils.getWeek(day)));
	}

	public String getHoursMinutes(Context context) {
		return hours + ":" + minutes;
	}

	public String getLittleDate(Context context) {
		return (month + 1) + "-" + date;
	}

	@SuppressWarnings("deprecation")
	public static DateTime getNow() {
		Date date = new Date();
		DateTime dt = new DateTime();
		dt.year = date.getYear();
		dt.month = date.getMonth();
		dt.day = date.getDay();
		dt.date = date.getDate();
		dt.hours = date.getHours();
		dt.minutes = date.getMinutes();
		dt.seconds = date.getSeconds();
		return dt;
	}
}
