package com.zzteck.msafe.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.zzteck.msafe.R;

public class FeedBackActivity extends BaseActivity implements OnClickListener {


	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed_back);
		initView();
	}

	private EditText mEtContact ;

	private EditText mEtContent ;

	private void initView() {
		mEtContact = findViewById(R.id.et_contact) ;
		mEtContent = findViewById(R.id.et_content) ;
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
