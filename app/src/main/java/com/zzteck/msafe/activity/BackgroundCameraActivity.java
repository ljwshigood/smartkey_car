package com.zzteck.msafe.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.zzteck.msafe.R;
import com.zzteck.msafe.application.AppContext;
import com.zzteck.msafe.bean.CameraInfo;
import com.zzteck.msafe.bean.SosBean;
import com.zzteck.msafe.db.DatabaseManager;
import com.zzteck.msafe.manager.WebManager;
import com.zzteck.msafe.recevier.ButtonBroadcastReceiver;
import com.zzteck.msafe.util.ImageTools;
import com.zzteck.msafe.util.KeyFunctionUtil;
import com.zzteck.msafe.util.RecordManager;
import com.zzteck.msafe.view.Preview;
import com.zzteck.msafe.view.ToastBoxer;


public class BackgroundCameraActivity extends Activity implements WebManager.ICommit2Web,Preview.ITakePictureComplete {

	static final boolean LOG = false;
	private static final String TAG = "MainActivity";
	private SensorManager mSensorManager = null;
	private Sensor mSensorAccelerometer = null;
	private Sensor mSensorMagnetic = null;
	private LocationManager mLocationManager = null;
	private LocationListener locationListener = null;
	private Preview preview = null;
	private int current_orientation = 0;
	private OrientationEventListener orientationEventListener = null;
	private boolean supports_auto_stabilise = false;
	private boolean supports_force_video_4k = false;
	private ArrayList<String> save_location_history = new ArrayList<String>();
	private boolean camera_in_background = false; // whether the camera is covered by a fragment/dialog (such as settings or folder picker)
    private GestureDetector gestureDetector;
    private boolean screen_is_locked = false;
    private Map<Integer, Bitmap> preloaded_bitmap_resources = new Hashtable<Integer, Bitmap>();

    private ToastBoxer screen_locked_toast = new ToastBoxer();
    ToastBoxer changed_auto_stabilise_toast = new ToastBoxer();

	// for testing:
	public boolean is_test = false;
	public Bitmap gallery_bitmap = null;


	private void intentPreview(){
		Intent intent = new Intent(mContext,ImagePreviewActivity.class);
		intent.putExtra("filePath", mFileDirPath);
		mContext.startActivity(intent);
	}

	private Context mContext ;


	private void intentSet(){
		Intent intent = new Intent(mContext,DeviceSetActivity.class);
		intent.putExtra("filePath", mFileDirPath);
		//update
		intent.putExtra("newversion", newversion);
		startActivityForResult(intent, 200);
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	private Vibrator vibrator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		KeyFunctionUtil.getInstance(BackgroundCameraActivity.this).releaseCamera() ;

    	long time_s = System.currentTimeMillis();
    	vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    //	vibrator.vibrate(200);


    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bg_camera);
		getIntentData();
		mContext = BackgroundCameraActivity.this ;

		if(getIntent() != null && getIntent().getExtras() != null ) {
			is_test = getIntent().getExtras().getBoolean("test_project");
		}

		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		if( activityManager.getLargeMemoryClass() >= 128 ) {
			supports_auto_stabilise = true;
		}

		// hack to rule out phones unlikely to have 4K video, so no point even offering the option!
		// both S5 and Note 3 have 128MB standard and 512MB large heap (tested via Samsung RTL), as does Galaxy K Zoom
		// also added the check for having 128MB standard heap, to support modded LG G2, which has 128MB standard, 256MB large - see https://sourceforge.net/p/opencamera/tickets/9/
		if( activityManager.getMemoryClass() >= 128 || activityManager.getLargeMemoryClass() >= 512 ) {
			supports_force_video_4k = true;
		}

        setWindowFlagsForCamera();

        // read save locations
        save_location_history.clear();
        // also update, just in case a new folder has been set
		updateFolderHistory();
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		if( mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null ) {
			mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
		else {
		}
		if( mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null ) {
			mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		}

		mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		updateGalleryIcon();

		preview = new Preview(this, savedInstanceState);
		((ViewGroup) findViewById(R.id.preview)).addView(preview);

		orientationEventListener = new OrientationEventListener(this) {
			@Override
			public void onOrientationChanged(int orientation) {
				BackgroundCameraActivity.this.onOrientationChanged(orientation);
			}
        };

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        gestureDetector = new GestureDetector(this, new MyGestureDetector());

        mDatabaseManager = DatabaseManager.getInstance(mContext);
        mCameraInfo = mDatabaseManager.selectCameraInfo();

        WebManager.getInstance(mContext).setmICommit2Web(this);
		mRecordManger = RecordManager.getInstance(mContext);

		preview.setmTakePicComplete(this);

        initCamera();

        // 关闭闪光灯
		clickedFlash(null) ;
	}

	private CameraInfo mCameraInfo ;

	private RecordManager mRecordManger ;

	private DatabaseManager mDatabaseManager ;

	@Override
	protected void onDestroy() {

		closeCamera();

		for(Map.Entry<Integer, Bitmap> entry : preloaded_bitmap_resources.entrySet()) {
			entry.getValue().recycle();
		}
		preloaded_bitmap_resources.clear();
		if(preview != null){
			preview.closeSystemCamera();
		}
		vibrator.cancel();

		AppContext.isStart = true ;

		if(mBitmap != null && !mBitmap.isRecycled()){
			mBitmap.recycle();
			mBitmap = null ;
			System.gc();
		}

		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	private SensorEventListener accelerometerListener = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			preview.onAccelerometerSensorChanged(event);
		}
	};

	private SensorEventListener magneticListener = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			preview.onMagneticSensorChanged(event);
		}
	};


	private void setupLocationListener() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		// Define a listener that responds to location updates
		boolean store_location = sharedPreferences.getBoolean("preference_location", false);
		if( store_location && locationListener == null ) {
			locationListener = new LocationListener() {
			    public void onLocationChanged(Location location) {
			    	preview.locationChanged(location);
			    }

			    public void onStatusChanged(String provider, int status, Bundle extras) {
			    }

			    public void onProviderEnabled(String provider) {
			    }

			    public void onProviderDisabled(String provider) {
			    }
			};

			// see https://sourceforge.net/p/opencamera/tickets/1/
			if( mLocationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) ) {
				//mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
			}
			if( mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER) ) {
				//mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
			}
		}
		else if( !store_location && locationListener != null ) {
	        if( this.locationListener != null ) {
	            mLocationManager.removeUpdates(locationListener);
	            locationListener = null;
	        }
		}
	}

	@Override
    protected void onResume() {
        super.onResume();
    }


	private void initCamera() {
		mSensorManager.registerListener(accelerometerListener, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(magneticListener, mSensorMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
        orientationEventListener.enable();

        setupLocationListener();

        if( !this.camera_in_background ) {
			// immersive mode is cleared when app goes into background
			setImmersiveMode(true);
		}

		layoutUI();

		updateGalleryIcon(); // update in case images deleted whilst idle

		preview.onResume();


	}

    @Override
    protected void onPause() {
        super.onPause();
      //  closeCamera();
    }


	private void closeCamera() {
		mSensorManager.unregisterListener(accelerometerListener);
        mSensorManager.unregisterListener(magneticListener);
        orientationEventListener.disable();
        if( this.locationListener != null ) {
            mLocationManager.removeUpdates(locationListener);
            locationListener = null;
        }
		// reset location, as may be out of date when resumed - the location listener is reinitialised when resuming
        preview.resetLocation();
        //TODO
        
		preview.onPause();
	}

    public void layoutUI() {
		this.preview.updateUIPlacement();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String ui_placement = sharedPreferences.getString("preference_ui_placement", "ui_right");
		boolean ui_placement_right = ui_placement.equals("ui_right");
		// new code for orientation fixed to landscape
		// the display orientation should be locked to landscape, but how many degrees is that?
	    int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
	    int degrees = 0;
	    switch (rotation) {
	    	case Surface.ROTATION_0: degrees = 0; break;
	        case Surface.ROTATION_90: degrees = 90; break;
	        case Surface.ROTATION_180: degrees = 180; break;
	        case Surface.ROTATION_270: degrees = 270; break;
	    }
	    // getRotation is anti-clockwise, but current_orientation is clockwise, so we add rather than subtract
	    // relative_orientation is clockwise from landscape-left
    	//int relative_orientation = (current_orientation + 360 - degrees) % 360;
    	int relative_orientation = (current_orientation + degrees) % 360;
		int ui_rotation = (360 - relative_orientation) % 360;
		preview.setUIRotation(ui_rotation);
		int left_of = RelativeLayout.LEFT_OF;
		int right_of = RelativeLayout.RIGHT_OF;
		int above = RelativeLayout.ABOVE;
		int below = RelativeLayout.BELOW;
		int align_parent_left = RelativeLayout.ALIGN_PARENT_LEFT;
		int align_parent_right = RelativeLayout.ALIGN_PARENT_RIGHT;
		int align_parent_top = RelativeLayout.ALIGN_PARENT_TOP;
		int align_parent_bottom = RelativeLayout.ALIGN_PARENT_BOTTOM;
		if( !ui_placement_right ) {
			above = RelativeLayout.BELOW;
			below = RelativeLayout.ABOVE;
			align_parent_top = RelativeLayout.ALIGN_PARENT_BOTTOM;
			align_parent_bottom = RelativeLayout.ALIGN_PARENT_TOP;
		}
    }

    private void onOrientationChanged(int orientation) {
		if( orientation == OrientationEventListener.ORIENTATION_UNKNOWN )
			return;
		int diff = Math.abs(orientation - current_orientation);
		if( diff > 180 )
			diff = 360 - diff;
		if( diff > 60 ) {
		    orientation = (orientation + 45) / 90 * 90;
		    orientation = orientation % 360;
		    if( orientation != current_orientation ) {
			    this.current_orientation = orientation;
				layoutUI();
		    }
		}
	}
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        preview.setCameraDisplayOrientation(this);
        super.onConfigurationChanged(newConfig);
    }

    public void clickedTakePhoto(View view) {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
    	this.takePicture();
		audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
    }

    public void clickedSwitchCamera(View view) {
		this.preview.switchCamera();
    }

    public void clickedSwitchVideo(View view) {
		this.preview.switchVideo(true, true);
    }

    public void clickedFlash(View view) {
    	this.preview.cycleFlash();
    }
    
    public void clickedFocusMode(View view) {
    	this.preview.cycleFocusMode();
    }

    public void clickedExposureLock(View view) {
    	this.preview.toggleExposureLock();
    }
    
    public void clickedSettings(View view) {
		intentSet();
    }

    Bitmap getPreloadedBitmap(int resource) {
		Bitmap bm = this.preloaded_bitmap_resources.get(resource);
		return bm;
    }
    
    public void updateForSettings() {
    	updateForSettings(null);
    }

    public void updateForSettings(String toast_message) {
    	String saved_focus_value = null;
    	if( preview.getCamera() != null && preview.isVideo() && !preview.focusIsVideo() ) {
    		saved_focus_value = preview.getCurrentFocusValue(); // n.b., may still be null
			// make sure we're into continuous video mode
			// workaround for bug on Samsung Galaxy S5 with UHD, where if the user switches to another (non-continuous-video) focus mode, then goes to Settings, then returns and records video, the preview freezes and the video is corrupted
			// so to be safe, we always reset to continuous video mode, and then reset it afterwards
			preview.updateFocusForVideo(false);
    	}

		updateFolderHistory();

		// update camera for changes made in prefs - do this without closing and reopening the camera app if possible for speed!
		// but need workaround for Nexus 7 bug, where scene mode doesn't take effect unless the camera is restarted - I can reproduce this with other 3rd party camera apps, so may be a Nexus 7 issue...
		boolean need_reopen = false;
		if( preview.getCamera() != null ) {
			Camera.Parameters parameters = preview.getCamera().getParameters();
			String key = Preview.getSceneModePreferenceKey();
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			String value = sharedPreferences.getString(key, Camera.Parameters.SCENE_MODE_AUTO);
			if( !value.equals(parameters.getSceneMode()) ) {
				need_reopen = true;
			}
		}

		layoutUI(); // needed in case we've changed left/right handed UI
        setupLocationListener(); // in case we've enabled GPS
		if( need_reopen || preview.getCamera() == null ) { // if camera couldn't be opened before, might as well try again
			preview.onPause();
			preview.onResume(toast_message);
		}
		else {
			preview.setCameraDisplayOrientation(this); // need to call in case the preview rotation option was changed
			preview.pausePreview();
			preview.setupCamera(toast_message);
		}

    	if( saved_focus_value != null ) {
    		preview.updateFocus(saved_focus_value, true, false);
    	}
    }
    
    public boolean cameraInBackground() {
    	return this.camera_in_background;
    }
    
    @Override
    public void onBackPressed() {
        if( screen_is_locked ) {
			//preview.showToast(screen_locked_toast, R.string.screen_is_locked);
        	return;
        }
        super.onBackPressed();        
    }
    
    //@TargetApi(Build.VERSION_CODES.KITKAT)
	private void setImmersiveMode(boolean on) {
		// Andorid 4.4 immersive mode disabled for now, as not clear of a good way to enter and leave immersive mode, and "sticky" might annoy some users
        /*if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ) {
        	if( on )
        		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        	else
        		getWindow().getDecorView().setSystemUiVisibility(0);
        }*/
    	if( on )
    		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
    	else
    		getWindow().getDecorView().setSystemUiVisibility(0);
    }
    
    private void setWindowFlagsForCamera() {
    	
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		// force to landscape mode
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// keep screen active - see http://stackoverflow.com/questions/2131948/force-screen-on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if( sharedPreferences.getBoolean("preference_show_when_locked", true) ) {
	        // keep Open Camera on top of screen-lock (will still need to unlock when going to gallery or settings)
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		}
		else {
	        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		}

        // set screen to max brightness - see http://stackoverflow.com/questions/11978042/android-screen-brightness-max-value
		// done here rather than onCreate, so that changing it in preferences takes effect without restarting app
		{
	        WindowManager.LayoutParams layout = getWindow().getAttributes();
			/*if( sharedPreferences.getBoolean("preference_max_brightness", true) ) {
		        layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
	        }
			else {
		        layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
			}*/
	        getWindow().setAttributes(layout); 
		}
		
		setImmersiveMode(true);

		camera_in_background = false;
    }
    
    private void setWindowFlagsForSettings() {
		// allow screen rotation
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		// revert to standard screen blank behaviour
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // settings should still be protected by screen lock
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

		{
	        WindowManager.LayoutParams layout = getWindow().getAttributes();
	        layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
	        getWindow().setAttributes(layout); 
		}

		setImmersiveMode(false);

		camera_in_background = true;
    }
    
    class Media {
    	public long id;
    	public boolean video;
    	public Uri uri;
    	public long date;
    	public int orientation;

    	Media(long id, boolean video, Uri uri, long date, int orientation) {
    		this.id = id;
    		this.video = video;
    		this.uri = uri;
    		this.date = date;
    		this.orientation = orientation;
    	}
    }
    
    private Media getLatestMedia(boolean video) {
    	Media media = null;
		Uri baseUri = video ? Video.Media.EXTERNAL_CONTENT_URI : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		Uri query = baseUri.buildUpon().appendQueryParameter("limit", "1").build();
		String [] projection = video ? new String[] {VideoColumns._ID, VideoColumns.DATE_TAKEN} : new String[] {ImageColumns._ID, ImageColumns.DATE_TAKEN, ImageColumns.ORIENTATION};
		String selection = video ? "" : ImageColumns.MIME_TYPE + "='image/jpeg'";
		String order = video ? VideoColumns.DATE_TAKEN + " DESC," + VideoColumns._ID + " DESC" : ImageColumns.DATE_TAKEN + " DESC," + ImageColumns._ID + " DESC";
		Cursor cursor = null;
		try {
			cursor = getContentResolver().query(query, projection, selection, null, order);
			if( cursor != null && cursor.moveToFirst() ) {
				long id = cursor.getLong(0);
				long date = cursor.getLong(1);
				int orientation = video ? 0 : cursor.getInt(2);
				Uri uri = ContentUris.withAppendedId(baseUri, id);
				media = new Media(id, video, uri, date, orientation);
			}
		}
		finally {
			if( cursor != null ) {
				cursor.close();
			}
		}
		return media;
    }
    
    private Media getLatestMedia() {
		Media image_media = getLatestMedia(false);
		Media video_media = getLatestMedia(true);
		Media media = null;
		if( image_media != null && video_media == null ) {
			media = image_media;
		}
		else if( image_media == null && video_media != null ) {
			media = video_media;
		}
		else if( image_media != null && video_media != null ) {
			if( image_media.date >= video_media.date ) {
				media = image_media;
			}
			else {
				media = video_media;
			}
		}
		return media;
    }

    public void updateGalleryIconToBlank() {
		gallery_bitmap = null;
    }
    
    public void updateGalleryIconToBitmap(Bitmap bitmap) {
		gallery_bitmap = bitmap;
    }
    
    public void updateGalleryIcon() {
    	long time_s = System.currentTimeMillis();
    	Media media = getLatestMedia();
		Bitmap thumbnail = null;
    	if( media != null ) {
    		if( media.video ) {
    			  thumbnail = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(), media.id, MediaStore.Video.Thumbnails.MINI_KIND, null);
    		}
    		else {
    			  thumbnail = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), media.id, MediaStore.Images.Thumbnails.MINI_KIND, null);
    		}
    		if( thumbnail != null ) {
	    		if( media.orientation != 0 ) {
	    			Matrix matrix = new Matrix();
	    			matrix.setRotate(media.orientation, thumbnail.getWidth() * 0.5f, thumbnail.getHeight() * 0.5f);
	    			try {
	    				Bitmap rotated_thumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true);
	        		    // careful, as rotated_thumbnail is sometimes not a copy!
	        		    if( rotated_thumbnail != thumbnail ) {
	        		    	thumbnail.recycle();
	        		    	thumbnail = rotated_thumbnail;
	        		    }
	    			}
	    			catch(Throwable t) {
	    			}
	    		}
    		}
    	}
    	if( thumbnail != null ) {
			updateGalleryIconToBitmap(thumbnail);
    	}
    	else {
			updateGalleryIconToBlank();
    	}
    }
    
    public void clickedGallery(View view) {
    	intentPreview();
    }
    
    public void updateFolderHistory() {
		String folder_name = getSaveLocation();
		updateFolderHistory(folder_name);
    }
    
    private void updateFolderHistory(String folder_name) {
		while( save_location_history.remove(folder_name) ) {
		}
		save_location_history.add(folder_name);
		while( save_location_history.size() > 6 ) {
			save_location_history.remove(0);
		}
		writeSaveLocations();
    }
    
    public void clearFolderHistory() {
		save_location_history.clear();
		updateFolderHistory(); // to re-add the current choice, and save
    }
    
    private void writeSaveLocations() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt("save_location_history_size", save_location_history.size());
        for(int i=0;i<save_location_history.size();i++) {
        	String string = save_location_history.get(i);
    		editor.putString("save_location_history_" + i, string);
        }
		editor.apply();
    }
    

    static private void putBundleExtra(Bundle bundle, String key, List<String> values) {
		if( values != null ) {
			String [] values_arr = new String[values.size()];
			int i=0;
			for(String value: values) {
				values_arr[i] = value;
				i++;
			}
			bundle.putStringArray(key, values_arr);
		}
    }

    public void clickedShare(View view) {
    	this.preview.clickedShare();
    }

    public void clickedTrash(View view) {
    	this.preview.clickedTrash();
    	// Calling updateGalleryIcon() immediately has problem that it still returns the latest image that we've just deleted!
    	// But works okay if we call after a delay. 100ms works fine on Nexus 7 and Galaxy Nexus, but set to 500 just to be safe.
    	final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
		    	updateGalleryIcon();
			}
		}, 500);
    }

    private void takePicture() {
		this.preview.takePicturePressed();
    }
    
    
    public boolean isScreenLocked() {
    	return screen_is_locked;
    }

    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
        		final ViewConfiguration vc = ViewConfiguration.get(BackgroundCameraActivity.this);
        		//final int swipeMinDistance = 4*vc.getScaledPagingTouchSlop();
    			final float scale = getResources().getDisplayMetrics().density;
    			final int swipeMinDistance = (int) (160 * scale + 0.5f); // convert dps to pixels
        		final int swipeThresholdVelocity = vc.getScaledMinimumFlingVelocity();
                float xdist = e1.getX() - e2.getX();
                float ydist = e1.getY() - e2.getY();
                float dist2 = xdist*xdist + ydist*ydist;
                float vel2 = velocityX*velocityX + velocityY*velocityY;
                if( dist2 > swipeMinDistance*swipeMinDistance && vel2 > swipeThresholdVelocity*swipeThresholdVelocity ) {
                //	preview.showToast(screen_locked_toast, R.string.unlocked);
                	//unlockScreen();
                }
            }
            catch(Exception e) {
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
			//preview.showToast(screen_locked_toast, R.string.screen_is_locked);
			return true;
        }
    }	

	@Override
	protected void onSaveInstanceState(Bundle state) {
	    super.onSaveInstanceState(state);
	    if( this.preview != null ) {
	    	preview.onSaveInstanceState(state);
	    }
	}

    public void broadcastFile(File file, boolean is_new_picture, boolean is_new_video) {
    	// note that the new method means that the new folder shows up as a file when connected to a PC via MTP (at least tested on Windows 8)
    	if( file.isDirectory() ) {
    		//this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file)));
        	// ACTION_MEDIA_MOUNTED no longer allowed on Android 4.4! Gives: SecurityException: Permission Denial: not allowed to send broadcast android.intent.action.MEDIA_MOUNTED
    		// note that we don't actually need to broadcast anything, the folder and contents appear straight away (both in Gallery on device, and on a PC when connecting via MTP)
    		// also note that we definitely don't want to broadcast ACTION_MEDIA_SCANNER_SCAN_FILE or use scanFile() for folders, as this means the folder shows up as a file on a PC via MTP (and isn't fixed by rebooting!)
    	}
    	else {
        	// both of these work fine, but using MediaScannerConnection.scanFile() seems to be preferred over sending an intent
    		//this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        	MediaScannerConnection.scanFile(this, new String[] { file.getAbsolutePath() }, null,
        			new MediaScannerConnection.OnScanCompletedListener() {
    		 		public void onScanCompleted(String path, Uri uri) {
    		 		}
    			}
    		);
        	if( is_new_picture ) {
        		this.sendBroadcast(new Intent(Camera.ACTION_NEW_PICTURE, Uri.fromFile(file)));
        		// for compatibility with some apps - apparently this is what used to be broadcast on Android?
        		this.sendBroadcast(new Intent("com.android.camera.NEW_PICTURE", Uri.fromFile(file)));
        	}
        	else if( is_new_video ) {
        		this.sendBroadcast(new Intent(Camera.ACTION_NEW_VIDEO, Uri.fromFile(file)));
        	}
    	}
	}
    
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private String getSaveLocation() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		//liujw modify 
		String folder_name = sharedPreferences.getString("preference_save_location", "Camera");
		return folder_name;
    }
    
    private static String mFileDirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
    
    static File getBaseFolder() {
    	return new File(mFileDirPath);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    }
    

    static File getImageFolder(String folder_name) {
		File file = null;
		if( folder_name.length() > 0 && folder_name.lastIndexOf('/') == folder_name.length()-1 ) {
			// ignore final '/' character
			folder_name = folder_name.substring(0, folder_name.length()-1);
		}
		//if( folder_name.contains("/") ) {
		if( folder_name.startsWith("/") ) {
			file = new File(folder_name);
		}
		else {
	        file = new File(getBaseFolder(), folder_name);
		}
        return file;
    }
    
    public File getImageFolder() {
		String folder_name = getSaveLocation();
		return getImageFolder(folder_name);
    }

    /** Create a File for saving an image or video */
    @SuppressLint("SimpleDateFormat")
	public File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

    	File mediaStorageDir = getImageFolder();
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if( !mediaStorageDir.exists() ) {
            if( !mediaStorageDir.mkdirs() ) {
                return null;
            }
            broadcastFile(mediaStorageDir, false, false);
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String index = "";
        File mediaFile = null;
        for(int count=1;count<=100;count++) {
            if( type == MEDIA_TYPE_IMAGE ) {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + index + ".jpg");
            }
            else if( type == MEDIA_TYPE_VIDEO ) {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "VID_"+ timeStamp + index + ".mp4");
            }
            else {
                return null;
            }
            if( !mediaFile.exists() ) {
            	break;
            }
            index = "_" + count; // try to find a unique filename
        }
        return mediaFile;
    }
    
    public boolean supportsAutoStabilise() {
    	return this.supports_auto_stabilise;
    }

    public boolean supportsForceVideo4K() {
    	return this.supports_force_video_4k;
    }

    @SuppressWarnings("deprecation")
	public long freeMemory() { // return free memory in MB
    	try {
    		File image_folder = this.getImageFolder();
	        StatFs statFs = new StatFs(image_folder.getAbsolutePath());
	        // cast to long to avoid overflow!
	        long blocks = statFs.getAvailableBlocks();
	        long size = statFs.getBlockSize();
	        long free  = (blocks*size) / 1048576;
	        return free;
    	}
    	catch(IllegalArgumentException e) {
    		// can fail on emulator, at least!
    		return -1;
    	}
    }

    public Preview getPreview() {
    	return this.preview;
    }

    // for testing:
	public ArrayList<String> getSaveLocationHistory() {
		return this.save_location_history;
	}
	
	public LocationListener getLocationListener() {
		return this.locationListener;
	}
	
	//add liujw
	private SharedPreferences preferences=null;
	private String today=null;
	
	private Boolean newversion=false;
	private AlertDialog.Builder builder=null;
	
	public static boolean isNetworkAvailable(Context context) {
		boolean connected = false;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if (ni != null) {
				connected = ni.isConnected();
			}
		}
		return connected;
	}
	
	private int second = 0;

	private Timer timer;
	
	private File mAudioFile ;
	
	private String mUrl ; 
	
	
	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			if(msg.what == 1){
				if(timer != null){
					timer.cancel();
				}
				mRecordManger.saveRecord();
				mAudioFile = mRecordManger.getMyRecAudioFile();
				try {
					mUrl = "http://maps.google.com/maps?q="+ AppContext.mLatitude+","+AppContext.mLongitude ;
					SosBean sosBean = DatabaseManager.getInstance(mContext).selectSOSInfo();
					SmsManager smsManager = SmsManager.getDefault();
					List<String> texts = smsManager.divideMessage(sosBean.getMessage());
					for(String text:texts){
						//Toast.makeText(mContext, "发送短信", Toast.LENGTH_SHORT).show();
						smsManager.sendTextMessage(sosBean.getContact(), null, text+mUrl, null, null);
						timer.cancel();
					}
					finish();
					//WebManager.getInstance(mContext).getUploadFileIp();
					//WebManager.getInstance(mContext).uploadFile2Web(mPictureFile.getAbsolutePath(), mAudioFile.getAbsolutePath(), AppContext.mLongitude, AppContext.mLatitude);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	};
	
	private void startTimeRecord() {

		TimerTask timerTask = new TimerTask() {

			@Override
			public void run() {
				second++;
				
				Log.e("liujw","####################startTimeRecord "+second);
				Log.e("liujw","####################startTimeRecord "+second);
				Log.e("liujw","####################startTimeRecord "+second);
				
				if (second >= 10) {
					second = 0;
					mHandler.sendEmptyMessage(1);
				}
			
			}
		};
		timer = new Timer();
		timer.schedule(timerTask, 1000, 1000);
	}


	@Override
	public void updateSuccess(String url) {
	//	String finalUrl = "http://14.217.218.183:81/bbInfo/"+url;
		SosBean sosBean = DatabaseManager.getInstance(mContext).selectSOSInfo();
		SmsManager smsManager = SmsManager.getDefault();
		List<String> texts = smsManager.divideMessage(sosBean.getMessage());
		for(String text:texts){
			//Toast.makeText(mContext, "发送短信", Toast.LENGTH_SHORT).show();
			smsManager.sendTextMessage(sosBean.getContact(), null, text+url, null, null);
			timer.cancel();
		}
		finish();
		
	}

	private int mAction ;
	
	private void getIntentData(){
		Intent intent = getIntent();
		mAction = intent.getIntExtra("action", 0);
	}

	private File mPictureFile ;
	
	@Override
	public void takePictureComplete(File file) {
		mPictureFile = file ;
		
		if(mAction == 4){
			startTimeRecord();
			Toast.makeText(BackgroundCameraActivity.this, "开启录音", Toast.LENGTH_SHORT).show();;
			mRecordManger.startRecord();
		}else{
		    showButtonNotify();		
		    finish();
		}
	}
	public final static String INTENT_BUTTONID_TAG = "ButtonId";
	
	public NotificationManager mNotificationManager;
	
	public final static String ACTION_BUTTON = "com.notifications.intent.action";
	
	public final static int BUTTON_PRIEW_ID = 1;
	
	public PendingIntent getDefalutIntent(int flags){
		PendingIntent pendingIntent= PendingIntent.getActivity(this, 1, new Intent(), flags);
		return pendingIntent;
	}
	
	private Bitmap mBitmap ;


	@RequiresApi(api = Build.VERSION_CODES.O)
	public void createNotificationChannel() {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// 通知渠道的id
		String id = "my_channel_01";
		// 用户可以看到的通知渠道的名字.
		CharSequence name = getString(R.string.app_name);
//         用户可以看到的通知渠道的描述
		String description = getString(R.string.app_name);
		int importance = NotificationManager.IMPORTANCE_HIGH;
		NotificationChannel mChannel = new NotificationChannel(id, name, importance);
//         配置通知渠道的属性
		mChannel.setDescription(description);
//         设置通知出现时的闪灯（如果 android 设备支持的话）
		mChannel.enableLights(true); mChannel.setLightColor(Color.RED);
//         设置通知出现时的震动（如果 android 设备支持的话）
		mChannel.enableVibration(true);
		mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
//         最后在notificationmanager中创建该通知渠道 //
		mNotificationManager.createNotificationChannel(mChannel);

		// 为该通知设置一个id
		int notifyID = 1;
		// 通知渠道的id
		String CHANNEL_ID = "my_channel_01";

		RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.view_custom_button);

		Intent intent = new Intent (this,ButtonBroadcastReceiver.class);
		intent.setAction(ACTION_BUTTON) ;
		intent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PRIEW_ID);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

		//Intent buttonIntent = new Intent(ACTION_BUTTON);
		//PendingIntent intent_prev = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		//mRemoteViews.setOnClickPendingIntent(R.id.ll_notification, intent_prev);

		if (mBitmap != null && !mBitmap.isRecycled()) {
			mBitmap.recycle();
			mBitmap = null;
			System.gc();
		}

		mBitmap = ImageTools.getBitmapFromFile(mPictureFile.getAbsolutePath(), 2);
		mRemoteViews.setImageViewBitmap(R.id.iv_notify, mBitmap);


		// Create a notification and set the notification channel.
		Notification notification = new Notification.Builder(this).setCustomContentView(mRemoteViews)
				//.setContentTitle(mContext.getString(R.string.app_name)) .setContentText("You've received new messages.")
				.setSmallIcon(R.mipmap.ic_launcher)
				.setChannelId(CHANNEL_ID)
				.setContentIntent(pendingIntent)
				.build();

		mNotificationManager.notify(200, notification);
	}



	public void showButtonNotify(){

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createNotificationChannel();
		}else {

			NotificationCompat.Builder mBuilder = new Builder(this);
			RemoteViews mRemoteViews = new RemoteViews(getPackageName(), R.layout.view_custom_button);

			Intent buttonIntent = new Intent(ACTION_BUTTON);

			buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PRIEW_ID);

			PendingIntent intent_prev = PendingIntent.getBroadcast(this, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			mRemoteViews.setOnClickPendingIntent(R.id.ll_notification, intent_prev);

			if (mBitmap != null && !mBitmap.isRecycled()) {
				mBitmap.recycle();
				mBitmap = null;
				System.gc();
			}

			mBitmap = ImageTools.getBitmapFromFile(mPictureFile.getAbsolutePath(), 2);
			mRemoteViews.setImageViewBitmap(R.id.iv_notify, mBitmap);

			mBuilder.setContent(mRemoteViews)
					.setContentIntent(getDefalutIntent(Notification.FLAG_ONGOING_EVENT))
					.setWhen(System.currentTimeMillis())
					.setPriority(Notification.PRIORITY_DEFAULT)
					.setOngoing(true)
					.setAutoCancel(true)
					.setSmallIcon(R.drawable.ic_launcher);

			Notification notify = mBuilder.build();
			notify.flags = Notification.FLAG_ONGOING_EVENT;

			mNotificationManager.notify(200, notify);
		}
	}
	

	@Override
	public void pareComplete() {
		
		if(mCameraInfo.getFront() == 1){
			this.preview.setCameraId(0);
		}else{
			this.preview.setCameraId(1);
		}
	}

	
	Runnable runnable = new Runnable() {
		
		@Override
		public void run() {
			takePicture();
		}
	};
	

	@Override
	public void tokenPicture() {
		mHandler.postDelayed(runnable, 1000);
		
	}


	@Override
	public void getIPSuccess() {
		try {
			WebManager.getInstance(mContext).uploadFile2Web(mPictureFile.getAbsolutePath(), mAudioFile.getAbsolutePath(), AppContext.mLongitude, AppContext.mLatitude);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
