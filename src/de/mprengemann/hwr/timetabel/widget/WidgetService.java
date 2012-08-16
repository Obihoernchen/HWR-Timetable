package de.mprengemann.hwr.timetabel.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import de.mprengemann.hwr.timetabel.Events;
import de.mprengemann.hwr.timetabel.R;
import de.mprengemann.hwr.timetabel.TimetableApplication_;

@TargetApi(11)
public class WidgetService extends RemoteViewsService {

	private static final String TAG = "WidgetService";

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		
		Log.i(TAG, "WidgetService called");
		
		return new ListRemoteViewsFactory(this.getApplicationContext(),
				(TimetableApplication_) this.getApplication(), intent);
	}
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
	private static final String TAG = "ListRemoteViewsFactory";

	private Context mContext;

	private TimetableApplication_ application;
	private List<Events> events = new ArrayList<Events>();

	public ListRemoteViewsFactory(Context context,
			TimetableApplication_ application, Intent intent) {
		this.mContext = context;
		this.application = application;
	}

	public int getCount() {
		return events.size();
	}

	public RemoteViews getViewAt(int position) {
		Log.i(TAG, ""+getCount());
		
		String title = "";
		String description = "";
		Events e;

		if ((e = events.get(position)) != null) {
			title = e.getSubjects().getTitle();
			description = e.getStart().toGMTString();
		}

		RemoteViews rv = new RemoteViews(mContext.getPackageName(),
				R.layout.widget_item);
		rv.setTextViewText(R.id.txt_widget_item_title, title);
		rv.setTextViewText(R.id.txt_widget_item_description, description);

		return rv;
	}

	public RemoteViews getLoadingView() {
		return null;
	}

	public int getViewTypeCount() {
		return 1;
	}

	public long getItemId(int position) {
		return position;
	}

	public boolean hasStableIds() {
		return true;
	}

	public void onDataSetChanged() {
		if (events != null) {
			events.clear();
		}

		events = application.getEvents();
	}

	@Override
	public void onCreate() {
	}

	@Override
	public void onDestroy() {
	}
}