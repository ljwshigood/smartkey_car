package com.zzteck.msafe.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zzteck.msafe.R;

public class AboutMeActivity extends BaseActivity implements OnClickListener {

	private ImageView mIvMore ;

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_me);
		initView();
	}

	
	private void initView() {
		mIvMore = findViewById(R.id.iv_more) ;
		mIvMore.setVisibility(View.GONE) ;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {

		default:
			break;
		}
	}
}
