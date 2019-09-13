package com.CarOCR;

import java.io.*;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.Camera;
import android.hardware.*;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.*;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MySurfaceViewActivity extends Activity {

	private final int[] SCANNER_ALPHA = { 0, 64, 128, 192, 255, 192, 128, 64 };
	private final long ANIMATION_DELAY = 80L;
	private final int POINT_SIZE = 6;
	private static final int CURRENT_POINT_OPACITY = 0xA0;

	private Bitmap resultBitmap;
	private Bitmap pictureBitmap = null;
	private Paint paint;
	private int maskColor;
	private int resultColor;
	private int frameColor;
	private int laserColor;
	private int resultPointColor;
	private int scannerAlpha;

	private int m_nDisplayWidth = 0;
	private int m_nDisplayHeight = 0;

	private Button btnCamera;
	private TextView lblRecogResult;
	private Preview mPreview;

	CarOCREngine myCarOCREngine;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		//杈呭姪绫诲疄渚嬪璞�		
		myCarOCREngine = new CarOCREngine();

		super.onCreate(savedInstanceState);
		//鍘绘帀鏍囬鍐呭
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//鑷畾涔夎鍥剧被
		mPreview = new Preview(this);
		//璁剧疆鏂扮殑瑙嗗浘灞曠ず
		setContentView(mPreview);
		//鑷畾涔夎鍥惧唴瀹圭被
		DrawOnTop mDraw = new DrawOnTop(this);
		//璁剧疆鏂扮殑瑙嗗浘鐨勬樉绀烘晥鏋滅旱鍚戝拰妯悜閮藉厖婊�		
		addContentView(mDraw, new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		//绐楀彛绠＄悊鍣ㄥ璞″疄渚�	
		WindowManager manager = (WindowManager) MySurfaceViewActivity.this
				.getSystemService(Context.WINDOW_SERVICE);
		//鏄剧ず灞忓箷灏哄鍜屽瘑搴︾殑鐩稿叧绫诲璞�		
		Display display = manager.getDefaultDisplay();
		//鑾峰彇灞忓箷鐨勫搴�		
		m_nDisplayWidth = display.getWidth();
		//鑾峰彇灞忓箷鐨勯珮搴�		
		m_nDisplayHeight = display.getHeight();
		//璋冪敤鑷畾涔夌被涓殑璁剧疆灞忓箷鍒嗚鲸鐜囩殑鏂规硶鏉ヨ缃睆骞曠殑澶у皬灞曠ず
		ResolutionSet._instance
				.setResolution(m_nDisplayWidth, m_nDisplayHeight);
		//甯冨眬鍔犺浇鍣ㄥ璞″疄渚�	
		LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//寰�柊鐨勮鍥句腑鍔犺浇甯冨眬鏂囦欢
		View v = vi.inflate(R.layout.main, null);
		//涓烘柊鐨勮鍥惧湪鎵嬫満椤甸潰鐨勫睍绀鸿缃睍绀烘晥鏋�	
		this.addContentView(v, new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		//璋冪敤鑷畾涔夌被涓殑杩唬瀛愬厓绱犺鍥剧殑鏂规硶
		ResolutionSet._instance.iterateChild(findViewById(R.id.rlBack));
		//鎸夐挳瀹炰緥
		btnCamera = (Button) findViewById(R.id.btnShutter);
		//鎸夐挳鐨勭洃鍚�		
		btnCamera.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//鐐瑰嚮鎸夐挳鍚庤皟鐢ㄦ崟鑾峰浘鍍忕殑鏂规硶
				mPreview.captureImage();
			}
		});
		//鏂囨湰妗嗗疄渚嬪璞�		
		lblRecogResult = (TextView) findViewById(R.id.lblRecogResult);
		
		scannerAlpha = 0;
		//鍒涘缓缁樺埗绫诲疄渚嬪璞＄敤鏉ョ粯鍒舵柊鐨勮鍥�		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//鑾峰彇搴旂敤绋嬪簭璧勬簮鐨勫疄渚嬬被
		Resources resources = getResources();
		//璁剧疆鍑犱釜璧勬簮甯搁噺鍊�		
		maskColor = resources.getColor(R.color.viewfinder_mask);
		resultColor = resources.getColor(R.color.result_view);
		frameColor = resources.getColor(R.color.viewfinder_frame);
		laserColor = resources.getColor(R.color.viewfinder_laser);
		resultPointColor = resources.getColor(R.color.possible_result_points);
		scannerAlpha = 0;
		//璋冪敤杈呭姪绫昏繘琛岄〉闈㈠垵濮嬪寲
		myCarOCREngine.init();
		//璋冪敤鏂规硶鍔犺浇璧勬簮
		loadAssets();
	}

	private void loadAssets() {
		int size = 0;
		String assetNames = "mPcaLda.dic";
		byte[] pDicData;
		try {
			//杈撳叆娴佸璞￠偅涓殑鍒涘缓
			InputStream is = getAssets().open(assetNames);
			//杩斿洖鍙互璇诲彇鎴栬烦杩囩殑浼拌瀛楄妭鏁帮紝鏃犻渶闃诲杈撳叆
			size = is.available();
			//鍒涘缓瀛楄妭鏁扮粍杩涜鏁版嵁缂撳瓨
			pDicData = new byte[size];
			//璇绘祦
			is.read(pDicData);
			//鍏抽棴娴�			
			is.close();
			//鍔犺浇鏁版嵁杩涗竴姝ュ垵濮嬪寲灞曠ず
			myCarOCREngine.loadDiction(pDicData, size);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class DrawOnTop extends View {
		public DrawOnTop(Context context) {
			super(context);
		}

		//鍦║I鐨勪富绾跨▼涓洿鏂扮敾闈㈢殑onDraw鏂规硶鐨勯噸鍐�		
		@SuppressWarnings("unused")
		@Override
		protected void onDraw(Canvas canvas) {
			int nRealX = 0;
			int nRealY = 0;

			nRealX = m_nDisplayWidth * 3 / 10;
			nRealY = m_nDisplayHeight * 5 / 14;

			Rect frame = new Rect(nRealX, nRealY, m_nDisplayWidth * 7 / 10,
					m_nDisplayHeight * 9 / 14);
			if (frame == null) {
				return;
			}
			int width = canvas.getWidth();
			int height = canvas.getHeight();

			paint.setColor(resultBitmap != null ? resultColor : maskColor);
			canvas.drawRect(0, 0, width, frame.top, paint);
			canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
			canvas.drawRect(frame.right + 1, frame.top, width,
					frame.bottom + 1, paint);
			canvas.drawRect(0, frame.bottom + 1, width, height, paint);

			if (resultBitmap != null) {
				paint.setAlpha(CURRENT_POINT_OPACITY);
				canvas.drawBitmap(resultBitmap, null, frame, paint);
			} else {
				paint.setColor(frameColor);
				canvas.drawRect(frame.left, frame.top, frame.right + 1,
						frame.top + 2, paint);
				canvas.drawRect(frame.left, frame.top + 2, frame.left + 2,
						frame.bottom - 1, paint);
				canvas.drawRect(frame.right - 1, frame.top, frame.right + 1,
						frame.bottom - 1, paint);
				canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1,
						frame.bottom + 1, paint);

				paint.setColor(laserColor);
				paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
				scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
				int middle = frame.height() / 2 + frame.top;
				canvas.drawRect(frame.left + 2, middle - 1, frame.right - 1,
						middle + 2, paint);
			}

			postInvalidateDelayed(ANIMATION_DELAY, frame.left - POINT_SIZE,
					frame.top - POINT_SIZE, frame.right + POINT_SIZE,
					frame.bottom + POINT_SIZE);

			super.onDraw(canvas);
		}
	}

	class Preview extends SurfaceView implements SurfaceHolder.Callback {
		public SurfaceHolder mHolder;
		public android.hardware.Camera mCamera;
		android.hardware.Camera.PictureCallback rawCallback;
		android.hardware.Camera.ShutterCallback shutterCallback;
		android.hardware.Camera.PictureCallback jpegCallback;

		public Preview(Context context) {
			this(context, null);

			setDrawingCacheEnabled(false);

			rawCallback = new android.hardware.Camera.PictureCallback() {
				public void onPictureTaken(byte[] data,
						android.hardware.Camera camera) {
				}
			};

			shutterCallback = new android.hardware.Camera.ShutterCallback() {
				public void onShutter() {
				}
			};

			jpegCallback = new android.hardware.Camera.PictureCallback() {
				public void onPictureTaken(byte[] data,
						android.hardware.Camera camera) {
					File tempDir = Environment.getExternalStorageDirectory();
					tempDir = new File(tempDir.getAbsolutePath() + "/CarOCR/");
					if (!tempDir.exists()) {
						tempDir.mkdir();
					}
					String strPath = tempDir + "/CarNumber.jpg";

					FileOutputStream outStream = null;
					try {
						outStream = new FileOutputStream(String.format(strPath,
								System.currentTimeMillis()));
						outStream.write(data);
						outStream.close();

						char resultString[] = new char[100];
						myCarOCREngine.recogpageFile(strPath, resultString);
						String resultStr = "";
						int i;
						for (i = 0; i < 100; i++) {
							if (resultString[i] == 0x0)
								break;
							resultStr += resultString[i];
						}
						lblRecogResult.setText(resultStr);

						mCamera.stopPreview();
						mCamera.startPreview();

					} catch (FileNotFoundException e) {
						e.printStackTrace();
						return;
					} catch (IOException e) {
						e.printStackTrace();
						return;
					} finally {
					}
				}
			};

			mHolder = getHolder();
			mHolder.addCallback(this);
			// mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		public Preview(Context context, AttributeSet attrs) {
			this(context, attrs, 0);
		}

		public Preview(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}

		public void captureImage() {
			mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			mCamera = android.hardware.Camera.open();
			try {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
			} catch (Exception e) {
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			mCamera.stopPreview();
			mCamera = null;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			try {
				android.hardware.Camera.Parameters parameters = mCamera
						.getParameters();
				parameters.setPreviewSize(w, h);
				mCamera.startPreview();
			} catch (Exception e) {
			}
		}
	}
}
