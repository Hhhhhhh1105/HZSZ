package com.zju.hzsz.activity;

import java.io.IOException;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.sin.android.sinlibs.utils.InjectUtils;
import com.zju.hzsz.R;

public class CompPhotoActivity extends BaseActivity implements Callback {
	private SurfaceView sv_camera = null;
	private SurfaceHolder surfaceHolder = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compphoto);
		initHead(R.drawable.ic_head_back, 0);
		setTitle("拍照投诉");

		InjectUtils.injectViews(this, R.id.class);

		surfaceHolder = sv_camera.getHolder();
		surfaceHolder.addCallback(this);
//		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		
		// R.drawable.ic_photo_camera
		
		Camera camera = Camera.open();
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPictureFormat(PixelFormat.JPEG);
		parameters.setPreviewSize(320, 240);
		parameters.setPictureSize(640, 480);
		camera.setParameters(parameters);
		try {
			camera.setPreviewDisplay(surfaceHolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		camera.startPreview();
	}

	@Override
	public void surfaceChanged(SurfaceHolder sh, int arg1, int arg2, int arg3) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {

	}
}
