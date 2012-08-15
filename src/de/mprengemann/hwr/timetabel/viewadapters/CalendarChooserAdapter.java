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
import android.widget.TextView;
import de.mprengemann.hwr.timetabel.R;
import de.mprengemann.hwr.timetabel.data.GoogleCalendar;

public class CalendarChooserAdapter extends BaseAdapter {

	private static final String TAG = "SubjectChooserAdapter";

	public interface OnCalendarSelectedListener {
		void onCalendarSelected(GoogleCalendar cal);
	}

	private LayoutInflater mInflater;
	private GoogleCalendar selection;
	private Context context;

	private List<GoogleCalendar> mData = new ArrayList<GoogleCalendar>();

	private OnCalendarSelectedListener listener;

	static class ViewHolder {
		TextView titleView;
	}

	public CalendarChooserAdapter(Context context) {
		this.mInflater = LayoutInflater.from(context);
		this.context = context;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public GoogleCalendar getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(
					R.layout.dialog_calendar_chooser_item, null);
			holder.titleView = (TextView) convertView
					.findViewById(R.id.txt_calendar_chooser_item);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final GoogleCalendar item = mData.get(position);

		if (item != null) {
			if (selection != null) {
				if (selection.equals(item)) {
					convertView
							.setBackgroundResource(R.drawable.abs__list_activated_holo);
					int padding = context.getResources().getDimensionPixelSize(
							R.dimen.default_list_padding);
					convertView.setPadding(padding, padding, padding, padding);
				}else{
					convertView.setBackgroundResource(android.R.color.transparent);
				}
			} else {
				convertView.setBackgroundResource(android.R.color.transparent);
			}

			try {
				holder.titleView.setText(item.getDisplayName());
			} catch (Exception e) {
				Log.e(TAG, String.valueOf(item.getId()));
			}

			holder.titleView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (selection != null){
						if (selection.equals(getItem(position))){
							selection = null;
						}else{
							selection = getItem(position);
						}
					}else{
						selection = getItem(position);
					}					

					if (listener != null) {
						listener.onCalendarSelected(selection);
					}
					
					notifyDataSetChanged();
				}
			});
		}

		return convertView;
	}

	public void setItems(List<GoogleCalendar> cals) {
		if (mData == null) {
			mData = new ArrayList<GoogleCalendar>();
		}

		mData.clear();

		for (GoogleCalendar s : cals) {
			mData.add(s);
		}

		notifyDataSetChanged();
	}

	public void setOnCalendarSelectedListener(
			OnCalendarSelectedListener listener) {
		this.listener = listener;
	}

	public void addItem(GoogleCalendar cal) {
		if (mData == null) {
			mData = new ArrayList<GoogleCalendar>();
		}
		
		mData.add(cal);
		
		notifyDataSetChanged();
	}

	public void setSelection(GoogleCalendar cal) {
		selection = cal;
		notifyDataSetChanged();
	}
}
