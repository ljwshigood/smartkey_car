package com.zzteck.msafe.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zzteck.msafe.R;
import com.zzteck.msafe.view.AndroidUtilities;
import com.zzteck.msafe.view.LocaleController;

/**
 * Created by Tan on 2018/8/14.
 */
public class SystemHintsDialog_dialog extends Dialog {
    private Context mContext;
    private LinearLayout nagtiveLayout;
    /*private ImageView ivHint;*/
    private TextView tvHintUp,tvNagtive,tvPositive;
    private int resid = 0;
    private String hintUP,nagtive,positive;
    private boolean needChangeIconSize = false;
    public SystemHintsDialog_dialog(@NonNull Context context, String hint1, String nagtive, String postive, int resid) {
        super(context, R.style.CustomDialogStyle);
        this.mContext = context;
        this.hintUP = hint1;
        this.nagtive = nagtive;
        this.positive = postive;
        this.resid = resid;
        needChangeIconSize = false;
    }
    public SystemHintsDialog_dialog(@NonNull Context context, String hint1, String postive, int resid) {
        super(context, R.style.CustomDialogStyle);
        this.mContext = context;
        this.hintUP = hint1;
        this.positive = postive;
        this.resid = resid;
    }

    public SystemHintsDialog_dialog(@NonNull Context context, String hint1, String postive, int resid, boolean needIconSize) {
        super(context, R.style.CustomDialogStyle);
        this.mContext = context;
        this.hintUP = hint1;
        this.positive = postive;
        this.resid = resid;
        needChangeIconSize = needIconSize;
    }

    private static int getSize(float size) {
        return (int) (size < 0 ? size : AndroidUtilities.dp(size));
    }

    public static LinearLayout.LayoutParams createLinear(int width, int height, float leftMargin, float topMargin, float rightMargin, float bottomMargin) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getSize(width), getSize(height));
        if(LocaleController.isRTL){
            layoutParams.setMargins(AndroidUtilities.dp(rightMargin), AndroidUtilities.dp(topMargin), AndroidUtilities.dp(leftMargin), AndroidUtilities.dp(bottomMargin));
        } else {
            layoutParams.setMargins(AndroidUtilities.dp(leftMargin), AndroidUtilities.dp(topMargin), AndroidUtilities.dp(rightMargin), AndroidUtilities.dp(bottomMargin));
        }
        return layoutParams;
    }


    public static final int MATCH_PARENT = -1;

    public static final int WRAP_CONTENT = -2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_systemhints_dialog,null,false);
        view.setLayoutParams(createLinear(MATCH_PARENT, WRAP_CONTENT, 20, 0, 20, 0));
        this.setContentView(view);
        nagtiveLayout = findViewById(R.id.layout_systemhintsdialog_ll_nag);
        tvHintUp = findViewById(R.id.layout_systemhintsdialog_tv_hint_1);
        tvNagtive = findViewById(R.id.layout_systemhintsdialog_tv_nag);
        tvPositive = findViewById(R.id.layout_systemhintsdialog_tv_pos);
        tvNagtive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIDialogListener != null){
                    mIDialogListener.dialogOk();
                }
                dismiss();
            }
        });
        tvPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    dismiss();
            }
        });
        if (resid != 0) {
            if(needChangeIconSize){
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_HORIZONTAL;
                params.topMargin = 10;
            }
        }
        tvHintUp.setText(hintUP);
        if (!TextUtils.isEmpty(nagtive)) {
            tvNagtive.setText(nagtive);
            nagtiveLayout.setVisibility(View.VISIBLE);
        }else {
            nagtiveLayout.setVisibility(View.GONE);
        }
        tvPositive.setText(positive);
        Window window = getWindow();
        assert window != null;
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = window.getWindowManager().getDefaultDisplay().getWidth() - 90;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        //liujianwei
        lp.format = PixelFormat.TRANSLUCENT
                | WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW;
        window.setAttributes(lp);
        window.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.systemhintdialogstyle));
    }

    public IDialogListener getmIDialogListener() {
        return mIDialogListener;
    }

    public void setmIDialogListener(IDialogListener mIDialogListener) {
        this.mIDialogListener = mIDialogListener;
    }

    private IDialogListener mIDialogListener ;

    public interface  IDialogListener{
        public void dialogOk() ;
    }

}
