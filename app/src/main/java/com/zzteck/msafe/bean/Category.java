package com.zzteck.msafe.bean;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/11/5 0005.
 */
public class Category implements Serializable{

    private String mCategoryName;

    public List<DeviceSetInfo> getmCategoryItem() {
        return mCategoryItem;
    }

    public void setmCategoryItem(List<DeviceSetInfo> mCategoryItem) {
        this.mCategoryItem = mCategoryItem;
    }

    private List<DeviceSetInfo> mCategoryItem = new ArrayList<DeviceSetInfo>();

    public Category(String mCategroyName) {
        mCategoryName = mCategroyName;
    }

    public String getmCategoryName() {
        return mCategoryName;
    }

    public void addItem(DeviceSetInfo pItemName) {
        mCategoryItem.add(pItemName);
    }

    /**
     *  获取Item内容
     *
     * @param pPosition
     * @return
     */
    public Object getItem(int pPosition) {
        // Category排在第一位
        if (pPosition == 0) {
          /*  DeviceSetInfo info = new DeviceSetInfo() ;
            info.setmDeviceName(mCategoryItem.get(pPosition).getmDeviceName());
            info.setmDeviceAddress(null);*/
            return mCategoryName ;
        } else {
            return mCategoryItem.get(pPosition - 1);
        }
    }

    /**
     * 当前类别Item总数。Category也需要占用一个Item
     * @return
     */
    public int getItemCount() {
        return mCategoryItem.size() + 1;
    }

}