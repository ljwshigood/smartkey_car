package com.zzteck.msafe.adapter;

import com.zzteck.msafe.R;
import com.zzteck.msafe.adapter.AppAdapter.ViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FunctionAdapter extends BaseAdapter implements OnItemClickListener{

	private Context mContext;

	private LayoutInflater mLayoutInflater;

	private int[] mRes;

	private String[] info;

	public FunctionAdapter(Context context,int[] res, String[] info) {
		mLayoutInflater = LayoutInflater.from(context);
		this.mRes = res ;
		this.info = info ;
	}

	@Override
	public int getCount() {
		return mRes == null ? 0 : mRes.length;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mLayoutInflater.inflate(R.layout.item_gv_function,null);
			viewHolder.mIvIcon = (ImageView) convertView.findViewById(R.id.iv_function);
			viewHolder.mTvInfo = (TextView) convertView.findViewById(R.id.tv_function);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.mIvIcon.setImageResource(mRes[position]);
		viewHolder.mTvInfo.setText(info[position]);

		/*viewHolder.mIvIcon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if(mIIconClickListener != null){
					mIIconClickListener.iconClickListener(position);
				}


			}
		});*/

		return convertView;
	}

	public IIconClickListener getmIIconClickListener() {
		return mIIconClickListener;
	}

	public void setmIIconClickListener(IIconClickListener mIIconClickListener) {
		this.mIIconClickListener = mIIconClickListener;
	}

	private IIconClickListener mIIconClickListener ;

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
		if(mIIconClickListener != null){
			mIIconClickListener.iconClickListener(position);
		}
	}

	public interface  IIconClickListener{
		public void iconClickListener(int position) ;
	}

	class ViewHolder {
		TextView mTvInfo;
		ImageView mIvIcon;
	}
}
