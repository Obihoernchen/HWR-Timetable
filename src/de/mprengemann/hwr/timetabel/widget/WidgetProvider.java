package de.mprengemann.hwr.timetabel.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import de.mprengemann.hwr.timetabel.R;
import de.mprengemann.hwr.timetabel.TimetableActivity_;

public class WidgetProvider extends AppWidgetProvider {

	private static final String TAG = "WidgetProvider";

	@TargetApi(14)
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		Log.i(TAG, "Update!!" + appWidgetIds.length);

		for (int i = 0; i < appWidgetIds.length; ++i) {
			int appWidgetId = appWidgetIds[i];

			Intent intent = new Intent(context, TimetableActivity_.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					intent, 0);

			Intent svcIntent = new Intent(context, WidgetService.class);
			svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetIds[i]);
			svcIntent.setData(Uri.parse(svcIntent
					.toUri(Intent.URI_INTENT_SCHEME)));

			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget);
			views.setOnClickPendingIntent(R.id.img_widget_home, pendingIntent);
			views.setRemoteAdapter(R.id.btn_widget_refresh, svcIntent);

			Intent update = new Intent(context, WidgetProvider.class);

			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

			PendingIntent pend = PendingIntent.getBroadcast(context, 0, update,
					PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(R.id.btn_widget_refresh, pend);

			appWidgetManager.updateAppWidget(appWidgetId, views);
		}

		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}