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

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;
import com.bugsense.trace.BugSenseHandler;
import de.mprengemann.hwr.timetabel.R;
import de.mprengemann.hwr.timetabel.TimetableActivity_;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WidgetProvider extends AppWidgetProvider {
  @SuppressWarnings("unused")
  private static final String TAG = "WidgetProvider";

  public static String PREV_ACTION = "de.mprengemann.hwr.timetabel.widget.PREV";
  public static String NEXT_ACTION = "de.mprengemann.hwr.timetabel.widget.NEXT";
  public static String REFRESH_ACTION = "de.mprengemann.hwr.timetabel.widget.REFRESH";
  public static final String EXTRA_FIRST_END_DATE = "de.mprengemann.hwr.timetabel.widget.first_end";

  private final SimpleDateFormat df_day = new SimpleDateFormat("EEEEEEEEEE",
      Locale.GERMANY);
  private final SimpleDateFormat df_date = new SimpleDateFormat("dd.MM.yyyy",
      Locale.GERMANY);

  @Override
  public void onReceive(Context ctx, Intent intent) {
    final String action = intent.getAction();

    if (action.equals(REFRESH_ACTION)) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        onRefreshHC(ctx);
      }
    } else if (action.equals(NEXT_ACTION)) {

    } else if (action.equals(PREV_ACTION)) {

    }

    super.onReceive(ctx, intent);
  }

  @TargetApi(11)
  private void onRefreshHC(Context context) {
    final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
    final ComponentName cn = new ComponentName(context,
        WidgetProvider.class);
    mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn),
        R.id.list_widget);
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                       int[] appWidgetIds) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      onUpdateHC(context, appWidgetManager, appWidgetIds);
    } else {
      onUpdateStatic(context, appWidgetManager, appWidgetIds);
    }

    super.onUpdate(context, appWidgetManager, appWidgetIds);
  }

  @SuppressWarnings("deprecation")
  @TargetApi(11)
  private void onUpdateHC(Context context, AppWidgetManager appWidgetManager,
                          int[] appWidgetIds) {

    for (int i = 0; i < appWidgetIds.length; i++) {
      final Intent intent = new Intent(context, WidgetServiceHC.class);
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
          appWidgetIds[i]);
      intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
      final RemoteViews rv = new RemoteViews(context.getPackageName(),
          R.layout.widget);
      rv.setRemoteAdapter(appWidgetIds[i], R.id.list_widget, intent);
      rv.setEmptyView(R.id.list_widget, R.id.empty_view);

      rv.setViewVisibility(R.id.btn_widget_next, View.GONE);
      rv.setViewVisibility(R.id.btn_widget_prev, View.GONE);

      Date dt = Calendar.getInstance().getTime();

      rv.setTextViewText(R.id.txt_widget_date_1, df_day.format(dt));
      rv.setTextViewText(R.id.txt_widget_date_2, df_date.format(dt));

      final Intent homeIntent = new Intent(context,
          TimetableActivity_.class);
      final PendingIntent homePendingIntent = PendingIntent.getActivity(
          context, 0, homeIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
      rv.setOnClickPendingIntent(R.id.img_widget_home, homePendingIntent);

      final Intent refreshIntent = new Intent(context,
          WidgetProvider.class);
      refreshIntent.setAction(WidgetProvider.REFRESH_ACTION);
      final PendingIntent refreshPendingIntent = PendingIntent
          .getBroadcast(context, 0, refreshIntent,
              PendingIntent.FLAG_UPDATE_CURRENT);
      rv.setOnClickPendingIntent(R.id.btn_widget_refresh,
          refreshPendingIntent);

      final Intent prevIntent = new Intent(context, WidgetProvider.class);
      prevIntent.setAction(WidgetProvider.PREV_ACTION);
      final PendingIntent prevPendingIntent = PendingIntent.getBroadcast(
          context, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
      rv.setOnClickPendingIntent(R.id.btn_widget_prev, prevPendingIntent);

      final Intent nextIntent = new Intent(context, WidgetProvider.class);
      nextIntent.setAction(WidgetProvider.NEXT_ACTION);
      final PendingIntent nextPendingIntent = PendingIntent.getBroadcast(
          context, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
      rv.setOnClickPendingIntent(R.id.btn_widget_next, nextPendingIntent);

      appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
    }
  }

  private void onUpdateStatic(Context context,
                              AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    ComponentName thisWidget = new ComponentName(context,
        WidgetProvider.class);
    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

    Intent intent = new Intent(context.getApplicationContext(),
        WidgetServiceStatic.class);
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

    context.startService(intent);
  }

  @Override
  public void onEnabled(Context context) {
    super.onEnabled(context);
    BugSenseHandler.initAndStartSession(context,
        context.getString(R.string.bugtracking_api));
  }

}