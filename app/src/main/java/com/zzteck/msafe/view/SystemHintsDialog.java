package com.zzteck.msafe.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zzteck.msafe.R;
import com.zzteck.msafe.activity.AntilostCameraActivity;
import com.zzteck.msafe.activity.BaseActivity;
import com.zzteck.msafe.application.AppContext;
import com.zzteck.msafe.impl.ComfirmListener;
import com.zzteck.msafe.util.Constant;
import com.zzteck.msafe.util.KeyFunctionUtil;


/**
 * Created by Tan on 2018/8/14.
 */
public class SystemHintsDialog extends BaseActivity {
    private Context mContext;
    private LinearLayout nagtiveLayout;
    private TextView tvHintUp,tvNagtive,tvPositive;
    private ComfirmListener mListener;

   private int mType ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_systemhints_dialog);
        mContext = SystemHintsDialog.this ;
        mType = getIntent().getIntExtra("type",0) ;
        nagtiveLayout = findViewById(R.id.layout_systemhintsdialog_ll_nag);
        tvHintUp = findViewById(R.id.layout_systemhintsdialog_tv_hint_1);
        tvNagtive = findViewById(R.id.layout_systemhintsdialog_tv_nag);
        tvPositive = findViewById(R.id.layout_systemhintsdialog_tv_pos);
        tvNagtive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mType == 0){
                    KeyFunctionUtil.getInstance(mContext).releaseCamera() ;
                    Intent intent1 = new Intent(mContext, AntilostCameraActivity.class) ;
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
                    startActivity(intent1);
                    finish();
                }else{

                    for(int i = 0 ;i < AppContext.activityList.size();i++){
                        AppContext.activityList.get(i).finish();
                    }
                    AppContext.activityList.clear();

                    KeyFunctionUtil.getInstance(mContext).processCamera(true);
                    //mContext.sendBroadcast(new Intent(Constant.FINISH));
                    finish();
                }
            }
        });

        if(mType == 0){
            tvHintUp.setText("Flash is on");
        }else{
            tvHintUp.setText("Camera is on");
        }

        tvNagtive.setText(getString(R.string.cancel));
        tvPositive.setText(getString(R.string.ok));
    }

    public void setListener(ComfirmListener listener){
        this.mListener = listener;
    }
}
