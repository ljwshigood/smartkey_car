package com.zzteck.msafe.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zzteck.msafe.R;
import com.zzteck.msafe.bean.Category;
import com.zzteck.msafe.bean.DeviceSetInfo;
import com.zzteck.msafe.db.DatabaseManager;
import com.zzteck.msafe.util.AlarmManager;

import java.util.ArrayList;

public class CategoryAdapter extends BaseAdapter {

    private static final int TYPE_CATEGORY_ITEM = 0;
    private static final int TYPE_ITEM = 1;

    private ArrayList<Category> mListData;
    private LayoutInflater mInflater;

    private Context mContext ;

    public CategoryAdapter(Context context, ArrayList<Category> pData) {
        mListData = pData;
        this.mContext = context ;
        mInflater = LayoutInflater.from(context);
    }

    public void notifyCategoryList(ArrayList<Category> list){
        mListData = list ;
        notifyDataSetChanged();
    }

    public void addDevice(Category category){
        if(mListData == null){
            mListData = new ArrayList<>() ;
        }
        mListData.add(category) ;
    }

    @Override
    public int getCount() {
        int count = 0;

        if (null != mListData) {
            //  所有分类中item的总和是ListVIew  Item的总个数
            for (Category category : mListData) {
                count += category.getItemCount();
            }
        }

        return count;
    }

    @Override
    public Object getItem(int position) {

        // 异常情况处理
        if (null == mListData || position <  0|| position > getCount()) {
            return null;
        }

        // 同一分类内，第一个元素的索引值
        int categroyFirstIndex = 0;

        for (Category category : mListData) {
            int size = category.getItemCount();
            // 在当前分类中的索引值
            int categoryIndex = position - categroyFirstIndex;
            // item在当前分类内
            if (categoryIndex < size) {
                return  category.getItem( categoryIndex );
            }

            // 索引移动到当前分类结尾，即下一个分类第一个元素索引
            categroyFirstIndex += size;
        }

        return null;
    }

    @Override
    public int getItemViewType(int position) {
        // 异常情况处理
        if (null == mListData || position <  0|| position > getCount()) {
            return TYPE_ITEM;
        }


        int categroyFirstIndex = 0;

        for (Category category : mListData) {
            int size = category.getItemCount();
            // 在当前分类中的索引值
            int categoryIndex = position - categroyFirstIndex;
            if (categoryIndex == 0) {
                return TYPE_CATEGORY_ITEM;
            }

            categroyFirstIndex += size;
        }

        return TYPE_ITEM;
    }

    private boolean isConnectDevice(Context context ,String address){
        ArrayList<DeviceSetInfo> deviceList = DatabaseManager.getInstance(context).selectDeviceInfo(address);
        boolean isExist = false ;
        for(int i = 0;i < deviceList.size();i++){
            DeviceSetInfo info = deviceList.get(i) ;
            if(info.getmDeviceAddress().equals(address) && info.isConnected()){
                isExist = true ;
                break ;
            }
        }
       return isExist ;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int itemViewType = getItemViewType(position);
        switch (itemViewType) {
            case TYPE_CATEGORY_ITEM:
                if (null == convertView) {
                    convertView = mInflater.inflate(R.layout.listview_item_header, null);
                }

                TextView textView = (TextView) convertView.findViewById(R.id.tv_category);
                String  itemValue = (String) getItem(position);
                Log.e("liujw","#####################itemViewType #itemValue : "+itemValue);
                textView.setText( itemValue );
                break;

            case TYPE_ITEM:
                ViewHolder viewHolder = null;
                if (null == convertView) {

                    convertView = mInflater.inflate(R.layout.list_item_device, null);

                    viewHolder = new ViewHolder();
                    viewHolder.content =  convertView.findViewById(R.id.tv_name);
                    viewHolder.contentIcon =  convertView.findViewById(R.id.iv_device);
                    viewHolder.status = convertView.findViewById(R.id.tv_status) ;
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                // 绑定数据
                DeviceSetInfo deviceSetInfo = (DeviceSetInfo) getItem(position);
                viewHolder.content.setText( deviceSetInfo.getmDeviceName() );
                boolean connectStatus = isConnectDevice(mContext,deviceSetInfo.getmDeviceAddress()) ;

                if(connectStatus){
                    viewHolder.status.setText("已连接");
                }else {
                    viewHolder.status.setText("");
                }

                DeviceSetInfo info = new DeviceSetInfo();
                info.setFilePath("null");
              //  Bitmap circleBitmap = AlarmManager.getInstance(mContext).getDeviceBitmap(info, mContext);
                viewHolder.contentIcon.setBackgroundResource(R.drawable.ic_launcher_icon);

                break;
        }

        return convertView;
    }


    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != TYPE_CATEGORY_ITEM;
    }

    private class ViewHolder {
        TextView content;
        TextView status ;
        ImageView contentIcon;
    }

}