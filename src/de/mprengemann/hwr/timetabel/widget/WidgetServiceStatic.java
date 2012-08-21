/*******************************************************************************
 * Copyright 2012 Marc Prengemann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.mprengemann.hwr.timetabel.widget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;
import de.mprengemann.hwr.timetabel.Events;
import de.mprengemann.hwr.timetabel.R;
import de.mprengemann.hwr.timetabel.TimetableActivity_;
import de.mprengemann.hwr.timetabel.TimetableApplication_;

public class WidgetServiceStatic extends Service {

	@SuppressWarnings("unused")
	private static final String TAG = "WidgetServiceStatic";
	
	private final SimpleDateFormat df_day = new SimpleDateFormat("EEEEEEEEEE");
	private final SimpleDateFormat df_date = new SimpleDateFormat("dd.MM.yyyy");

	private TimetableApplication_ application;
	private DateFormat df = new SimpleDateFormat("HH:mm");;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		application = (TimetableApplication_) getApplication();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent i, int startId) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
				.getApplicationContext());

		int[] allWidgetIds = i
				.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		for (int widgetId : allWidgetIds) {
			List<Events> evts = application.getEvents();

			final RemoteViews rv = new RemoteViews(this.getPackageName(),
					R.layout.widget);

			int eCount = 0;
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			Calendar eCal = Calendar.getInstance();
			rv.removeAllViews(R.id.frame_widget);

			for (Events e : evts) {
				eCal.setTime(e.getStart());

				eCal.set(Calendar.HOUR_OF_DAY, 0);
				eCal.set(Calendar.MINUTE, 0);
				eCal.set(Calendar.SECOND, 0);
				eCal.set(Calendar.MILLISECOND, 0);

				if (!cal.equals(eCal)) {
					if (eCount == 0) {
						rv.setViewVisibility(R.id.frame_widget, View.GONE);
					}
					break;
				} else {
					rv.setViewVisibility(R.id.frame_widget, View.VISIBLE);
				}

				final RemoteViews row = new RemoteViews(this.getPackageName(),
						R.layout.widget_item);

				row.setTextViewText(R.id.txt_widget_item_title, e.getSubjects()
						.getTitle());
				row.setTextViewText(R.id.txt_widget_item_room, e.getRoom());
				row.setTextViewText(R.id.txt_widget_item_time,
						df.format(e.getStart()) + " - " + df.format(e.getEnd()));

				rv.addView(R.id.frame_widget, row);

				eCount++;
				if (eCount == 3) {
					break;
				}
			}

			Date dt = Calendar.getInstance().getTime();

			rv.setTextViewText(R.id.txt_widget_date_1, df_day.format(dt));
			rv.setTextViewText(R.id.txt_widget_date_2, df_date.format(dt));

			final Intent homeIntent = new Intent(this, TimetableActivity_.class);
			final PendingIntent homePendingIntent = PendingIntent.getActivity(
					this, 0, homeIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
			rv.setOnClickPendingIntent(R.id.img_widget_home, homePendingIntent);

			final Intent refreshIntent = new Intent(this, WidgetProvider.class);
			refreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
					allWidgetIds);
			final PendingIntent refreshPendingIntent = PendingIntent
					.getBroadcast(this, 0, refreshIntent,
							PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.btn_widget_refresh,
					refreshPendingIntent);

			final Intent prevIntent = new Intent(this, WidgetProvider.class);
			prevIntent.setAction(WidgetProvider.PREV_ACTION);
			final PendingIntent prevPendingIntent = PendingIntent.getBroadcast(
					this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.btn_widget_prev, prevPendingIntent);

			final Intent nextIntent = new Intent(this, WidgetProvider.class);
			nextIntent.setAction(WidgetProvider.NEXT_ACTION);
			final PendingIntent nextPendingIntent = PendingIntent.getBroadcast(
					this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.btn_widget_next, nextPendingIntent);

			appWidgetManager.updateAppWidget(widgetId, rv);
		}
		stopSelf();

		super.onStart(i, startId);
	}

}
