package com.zzteck.msafe.view;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class LayoutHelper {

    public static final int MATCH_PARENT = -1;
    public static final int WRAP_CONTENT = -2;

    private static int getSize(float size) {
        return (int) (size < 0 ? size : AndroidUtilities.dp(size));
    }

    public static void setBounds(TextView view, int left, int top, int right, int bottom){
        if(LocaleController.isTextRTL()){
            view.setCompoundDrawablesWithIntrinsicBounds(right, top, left, bottom);
        } else {
            view.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        }
    }

    public static void setBounds(TextView view, Drawable left, Drawable top, Drawable right, Drawable bottom){
        if(LocaleController.isTextRTL()){
            view.setCompoundDrawablesWithIntrinsicBounds(right, top, left, bottom);
        } else {
            view.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        }
    }

    public static void setPadding(TextView view, int left, int top, int right, int bottom){
        if(LocaleController.isRTL){
            view.setPadding(right, top, left, bottom);
        } else {
            view.setPadding(left, top, right, bottom);
        }
    }

    public static void setPadding(View view, int left, int top, int right, int bottom){
        if(LocaleController.isRTL){
            view.setPadding(right, top, left, bottom);
        } else {
            view.setPadding(left, top, right, bottom);
        }
    }

    public static void setMargins(FrameLayout.LayoutParams layoutParams, int left, int top, int right, int bottom){
        if(LocaleController.isRTL){
            layoutParams.setMargins(right, top, left, bottom);
        } else {
            layoutParams.setMargins(left, top, right, bottom);
        }
    }

    public static void setMargins(LinearLayout.LayoutParams layoutParams, int left, int top, int right, int bottom){
        if(LocaleController.isRTL){
            layoutParams.setMargins(right, top, left, bottom);
        } else {
            layoutParams.setMargins(left, top, right, bottom);
        }
    }

    public static void setMargins(RelativeLayout.LayoutParams layoutParams, int left, int top, int right, int bottom){
        if(LocaleController.isRTL){
            layoutParams.setMargins(right, top, left, bottom);
        } else {
            layoutParams.setMargins(left, top, right, bottom);
        }
    }

    public static FrameLayout.LayoutParams createScroll(int width, int height, int gravity) {
        return new ScrollView.LayoutParams(getSize(width), getSize(height), gravity);
    }

    public static FrameLayout.LayoutParams createFrame(int width, float height, int gravity, float leftMargin, float topMargin, float rightMargin, float bottomMargin) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(getSize(width), getSize(height), gravity);
        if(LocaleController.isRTL){
            layoutParams.setMargins(AndroidUtilities.dp(rightMargin), AndroidUtilities.dp(topMargin), AndroidUtilities.dp(leftMargin), AndroidUtilities.dp(bottomMargin));
        } else {
            layoutParams.setMargins(AndroidUtilities.dp(leftMargin), AndroidUtilities.dp(topMargin), AndroidUtilities.dp(rightMargin), AndroidUtilities.dp(bottomMargin));
        }
        return layoutParams;
    }

    public static FrameLayout.LayoutParams createFrame(int width, int height, int gravity) {
        return new FrameLayout.LayoutParams(getSize(width), getSize(height), gravity);
    }

    public static FrameLayout.LayoutParams createFrame(int width, float height) {
        return new FrameLayout.LayoutParams(getSize(width), getSize(height));
    }

    private static RelativeLayout.LayoutParams createRelative(float width, float height, int leftMargin, int topMargin, int rightMargin, int bottomMargin, int alignParent, int alignRelative, int anchorRelative) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(getSize(width), getSize(height));
        if (alignParent >= 0) {
            if(alignParent == RelativeLayout.ALIGN_PARENT_LEFT || alignParent == RelativeLayout.ALIGN_PARENT_START){
                if (LocaleController.isRTL) {
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                } else {
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                }
            } else if(alignParent == RelativeLayout.ALIGN_PARENT_RIGHT || alignParent == RelativeLayout.ALIGN_PARENT_END){
                if (LocaleController.isRTL) {
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                } else {
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                }
            } else {
                layoutParams.addRule(alignParent);
            }
        }
        if (alignRelative >= 0 && anchorRelative >= 0) {
            if(alignRelative == RelativeLayout.ALIGN_LEFT && LocaleController.isRTL){
                alignRelative = RelativeLayout.ALIGN_RIGHT;
            } else if(alignRelative == RelativeLayout.ALIGN_RIGHT && LocaleController.isRTL){
                alignRelative = RelativeLayout.ALIGN_LEFT;
            }
            layoutParams.addRule(alignRelative, anchorRelative);
        }

        layoutParams.topMargin = AndroidUtilities.dp(topMargin);
        layoutParams.bottomMargin = AndroidUtilities.dp(bottomMargin);
        if(LocaleController.isRTL){
            layoutParams.leftMargin = AndroidUtilities.dp(rightMargin);
            layoutParams.rightMargin = AndroidUtilities.dp(leftMargin);
        } else {
            layoutParams.leftMargin = AndroidUtilities.dp(leftMargin);
            layoutParams.rightMargin = AndroidUtilities.dp(rightMargin);
        }
        return layoutParams;
    }

    public static RelativeLayout.LayoutParams createRelative(int width, int height, int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        return createRelative(width, height, leftMargin, topMargin, rightMargin, bottomMargin, -1, -1, -1);
    }

    public static RelativeLayout.LayoutParams createRelative(int width, int height, int leftMargin, int topMargin, int rightMargin, int bottomMargin, int alignParent) {
        return createRelative(width, height, leftMargin, topMargin, rightMargin, bottomMargin, alignParent, -1, -1);
    }

    public static RelativeLayout.LayoutParams createRelative(float width, float height, int leftMargin, int topMargin, int rightMargin, int bottomMargin, int alignRelative, int anchorRelative) {
        return createRelative(width, height, leftMargin, topMargin, rightMargin, bottomMargin, -1, alignRelative, anchorRelative);
    }

    public static RelativeLayout.LayoutParams createRelative(int width, int height, int alignParent, int alignRelative, int anchorRelative) {
        return createRelative(width, height, 0, 0, 0, 0, alignParent, alignRelative, anchorRelative);
    }

    public static RelativeLayout.LayoutParams createRelative2(int width, int height) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(getSize(width), getSize(height));
        return layoutParams ;
    }

    public static RelativeLayout.LayoutParams createRelative(int width, int height) {
        return createRelative(width, height, 0, 0, 0, 0, -1, -1, -1);
    }

    public static RelativeLayout.LayoutParams createRelative(int width, int height, int alignParent) {
        return createRelative(width, height, 0, 0, 0, 0, alignParent, -1, -1);
    }

    public static RelativeLayout.LayoutParams createRelative(int width, int height, int alignRelative, int anchorRelative) {
        return createRelative(width, height, 0, 0, 0, 0, -1, alignRelative, anchorRelative);
    }

    public static LinearLayout.LayoutParams createLinear(int width, int height, float weight, int gravity, int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getSize(width), getSize(height), weight);
        if(LocaleController.isRTL){
            layoutParams.setMargins(AndroidUtilities.dp(rightMargin), AndroidUtilities.dp(topMargin), AndroidUtilities.dp(leftMargin), AndroidUtilities.dp(bottomMargin));
        } else {
            layoutParams.setMargins(AndroidUtilities.dp(leftMargin), AndroidUtilities.dp(topMargin), AndroidUtilities.dp(rightMargin), AndroidUtilities.dp(bottomMargin));
        }

        layoutParams.gravity = gravity;
        return layoutParams;
    }

    public static LinearLayout.LayoutParams createLinear(int width, int height, float weight, int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getSize(width), getSize(height), weight);
        if(LocaleController.isRTL){
            layoutParams.setMargins(AndroidUtilities.dp(rightMargin), AndroidUtilities.dp(topMargin), AndroidUtilities.dp(leftMargin), AndroidUtilities.dp(bottomMargin));
        } else {
            layoutParams.setMargins(AndroidUtilities.dp(leftMargin), AndroidUtilities.dp(topMargin), AndroidUtilities.dp(rightMargin), AndroidUtilities.dp(bottomMargin));
        }
        return layoutParams;
    }

    public static LinearLayout.LayoutParams createLinear(int width, int height, int gravity, int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getSize(width), getSize(height));
        if(LocaleController.isRTL){
            layoutParams.setMargins(AndroidUtilities.dp(rightMargin), AndroidUtilities.dp(topMargin), AndroidUtilities.dp(leftMargin), AndroidUtilities.dp(bottomMargin));
        } else {
            layoutParams.setMargins(AndroidUtilities.dp(leftMargin), AndroidUtilities.dp(topMargin), AndroidUtilities.dp(rightMargin), AndroidUtilities.dp(bottomMargin));
        }
        layoutParams.gravity = gravity;
        return layoutParams;
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

    public static LinearLayout.LayoutParams createLinear(int width, int height, float weight, int gravity) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getSize(width), getSize(height), weight);
        layoutParams.gravity = gravity;
        return layoutParams;
    }

    public static LinearLayout.LayoutParams createLinear(int width, int height, int gravity) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getSize(width), getSize(height));
        layoutParams.gravity = gravity;
        return layoutParams;
    }

    public static LinearLayout.LayoutParams createLinear(int width, int height, float weight) {
        return new LinearLayout.LayoutParams(getSize(width), getSize(height), weight);
    }

    public static LinearLayout.LayoutParams createLinear(int width, int height) {
        return new LinearLayout.LayoutParams(getSize(width), getSize(height));
    }
}