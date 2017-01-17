package com.zju.hzsz.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

public class ImgUtils {
	public static void loadImage(Context context, ImageView iv, String url) {
		loadImage(context, iv, url, 0, 0);
	}

	public static void loadImage(Context context, ImageView iv, String url, int defimg, int errimg) {
		if (url != null && url.length() > 0) {
			try {
				RequestCreator rc = Picasso.with(context).load(url).transform(new Transformation() {

					@Override
					public Bitmap transform(Bitmap src) {
						int MAX_WIDTH = 1000;
						Bitmap res = src;
						if (src.getWidth() > MAX_WIDTH) {
							int w = MAX_WIDTH;
							int h = (int) ((float) src.getHeight() / (float) src.getWidth() * MAX_WIDTH);
							if (h < 0)
								h = 1;
							res = Bitmap.createScaledBitmap(src, w, h, false);
							src.recycle();
						}
						return res;
					}

					@Override
					public String key() {
						return "ttt";
					}
				});
				if (defimg > 0)
					rc = rc.placeholder(defimg);
				if (errimg > 0)
					rc = rc.error(errimg);
				rc.into(iv);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
