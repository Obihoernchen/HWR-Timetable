package de.mprengemann.hwr.timetabel.viewadapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import de.mprengemann.hwr.timetabel.R;
import de.mprengemann.hwr.timetabel.Subjects;

public class SubjectChooserAdapter extends BaseAdapter {

	private static final String TAG = "SubjectChooserAdapter";
	
	private List<Long> changed = new ArrayList<Long>();

	public interface OnSubjectCheckedChangeListener {
		void onCheckedChange(Long id, Boolean isChecked);
	}

	private LayoutInflater mInflater;

	private List<Subjects> mData = new ArrayList<Subjects>();

	private OnSubjectCheckedChangeListener listener;

	static class ViewHolder {
		TextView titleView;
		CheckBox checkboxView;
	}

	public SubjectChooserAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Subjects getItem(int position) {
		return mData.get(Integer.valueOf(position));
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId().longValue();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(
					R.layout.fragment_subject_chooser_item, null);
			holder.titleView = (TextView) convertView
					.findViewById(R.id.txt_subject_chooser_item);
			holder.checkboxView = (CheckBox) convertView
					.findViewById(R.id.checkbox_subject_chooser_item);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final Subjects item = mData.get(position);

		if (item != null) {
			try {
				holder.titleView.setText(item.getTitle());
			} catch (Exception e) {
				Log.e(TAG, String.valueOf(item.getId()));
			}

			if (changed.contains(Long.valueOf(item.getId()))){
				//Log.i(TAG, "Hier1 " + item.getId() + item.getShortTitle());
				holder.checkboxView.setChecked(!item.getShow().booleanValue());
			}else{
				//Log.i(TAG, "Hier2 " + item.getId() + item.getShortTitle());
				holder.checkboxView.setChecked(item.getShow().booleanValue());
			}
			
			holder.checkboxView
					.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {							
							if (changed.contains(Long.valueOf(item.getId()))){
								Log.i(TAG, "Hier3 " + item.getId() + item.getShortTitle());
								changed.remove(Long.valueOf(item.getId()));
							}else{
								Log.i(TAG, "Hier4 " + item.getId() + item.getShortTitle());
								changed.add(Long.valueOf(item.getId()));
							}							
							
							if (listener != null){								
								listener.onCheckedChange(getItemId(position), Boolean.valueOf(((CheckBox) v).isChecked()));
							}
						}
					});
		}

		return convertView;
	}

	public void setItems(List<Subjects> subjects) {
		if (mData == null) {
			mData = new ArrayList<Subjects>();
		}

		mData.clear();

		for (Subjects s : subjects) {
			mData.add(s);
		}

		notifyDataSetChanged();
	}

	public void setOnSubjectCheckedChangeListener(
			OnSubjectCheckedChangeListener listener) {
		this.listener = listener;
	}
}
