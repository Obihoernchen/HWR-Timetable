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
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.bugsense.trace.BugSenseHandler;
import de.mprengemann.hwr.timetabel.Events;
import de.mprengemann.hwr.timetabel.R;
import de.mprengemann.hwr.timetabel.TimetableApplication_;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

  @SuppressWarnings("unused")
  private static final String TAG = "ListRemoteViewsFactory";

  public final static long SECOND_MILLIS = 1000;
  public final static long MINUTE_MILLIS = SECOND_MILLIS * 60;
  public final static long HOUR_MILLIS = MINUTE_MILLIS * 60;
  public final static long DAY_MILLIS = HOUR_MILLIS * 24;
  public final static long YEAR_MILLIS = DAY_MILLIS * 365;

  private static final int TYPE_ITEM = 0;
  private static final int TYPE_SEPARATOR = 1;
  private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;

  private Context mContext;

  private TimetableApplication_ application;

  private SparseArray<Events> mData = new SparseArray<Events>();
  private SparseArray<String> mSeparatorsSet = new SparseArray<String>();

  private SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.GERMANY);
  private SimpleDateFormat fullDateFormat = new SimpleDateFormat(
      "dd.MM.yyyy", Locale.GERMANY);

  public ListRemoteViewsFactory(Context context,
                                TimetableApplication_ application, Intent intent) {
    this.mContext = context;
    this.application = application;
  }

  public void addItem(final Events evt) {
    mData.put(Integer.valueOf(mData.size() + mSeparatorsSet.size()), evt);
  }

  public void addSeparatorItem(final String str) {
    mSeparatorsSet.put(
        Integer.valueOf(mData.size() + mSeparatorsSet.size()), str);
  }

  private void clear() {
    mData = new SparseArray<Events>();
    mSeparatorsSet = new SparseArray<String>();
  }

  private long daysDiff(final Calendar today, final Calendar nextSeperatorDate) {
    Calendar date = (Calendar) today.clone();
    date.set(Calendar.HOUR_OF_DAY, 0);
    date.set(Calendar.MINUTE, 0);
    date.set(Calendar.SECOND, 0);
    date.set(Calendar.MILLISECOND, 0);

    Calendar next = (Calendar) nextSeperatorDate.clone();
    next.set(Calendar.HOUR_OF_DAY, 0);
    next.set(Calendar.MINUTE, 0);
    next.set(Calendar.SECOND, 0);
    next.set(Calendar.MILLISECOND, 0);

    long daysBetween = 0;
    int multi = 1;

    if (!date.before(next)) {
      if (date.compareTo(next) != 0) {
        multi = -1;
        Calendar temp = (Calendar) date.clone();
        date = (Calendar) next.clone();
        next = (Calendar) temp.clone();
      }
    }

    while (date.before(next)) {
      date.add(Calendar.DAY_OF_MONTH, 1);
      daysBetween++;
    }

    return multi * daysBetween;
  }

  public int getCount() {
    return (mData.size() + mSeparatorsSet.size());
  }

  public Events getItem(int position) {
    return mData.get(Integer.valueOf(position));
  }

  public long getItemId(int position) {
    return position;
  }

  public int getItemViewType(int position) {
    return (mSeparatorsSet.indexOfKey(Integer.valueOf(position)) >= 0) ? TYPE_SEPARATOR
        : TYPE_ITEM;
  }

  public RemoteViews getLoadingView() {
    return null;
  }

  public String getSeperatorItem(int position) {
    return mSeparatorsSet.get(Integer.valueOf(position));
  }

  public RemoteViews getViewAt(int position) {
    RemoteViews rv = null;

    int type = getItemViewType(position);
    switch (type) {
      case TYPE_ITEM:
        rv = new RemoteViews(mContext.getPackageName(),
            R.layout.widget_item);

        String title = "";
        String room = "";
        String date = "";
        Events e;

        if ((e = getItem(position)) != null) {
          title = e.getSubjects().getTitle();
          room = e.getRoom();
          date = df.format(e.getStart()) + " - " + df.format(e.getEnd());
        }

        rv.setTextViewText(R.id.txt_widget_item_title, title);
        rv.setTextViewText(R.id.txt_widget_item_room, room);
        rv.setTextViewText(R.id.txt_widget_item_time, date);

        break;
      case TYPE_SEPARATOR:
        rv = new RemoteViews(mContext.getPackageName(),
            R.layout.widget_separator);

        try {
          Calendar nextSeperatorDate = Calendar.getInstance();
          nextSeperatorDate.setTime(fullDateFormat
              .parse(getSeperatorItem(position)));

          long dateDiff = daysDiff(Calendar.getInstance(),
              nextSeperatorDate);

          if (dateDiff == 0) {
            rv.setTextViewText(R.id.txt_widget_separator,
                mContext.getString(R.string.text_today));
          } else if (dateDiff == 1) {
            rv.setTextViewText(R.id.txt_widget_separator,
                mContext.getString(R.string.text_tomorrow));
          } else if (dateDiff == -1) {
            rv.setTextViewText(R.id.txt_widget_separator,
                mContext.getString(R.string.text_yesterday));
          } else if (dateDiff == -2) {
            rv.setTextViewText(R.id.txt_widget_separator,
                mContext.getString(R.string.text_twoago));
          } else {
            rv.setTextViewText(R.id.txt_widget_separator,
                getSeperatorItem(position));
          }
        } catch (Exception exp) {
          rv.setTextViewText(R.id.txt_widget_separator,
              getSeperatorItem(position));
          BugSenseHandler.sendException(exp);
        }

        break;
    }

    return rv;
  }

  public int getViewTypeCount() {
    return TYPE_MAX_COUNT;
  }

  public boolean hasStableIds() {
    return true;
  }

  @Override
  public void onCreate() {
  }

  public void onDataSetChanged() {
    clear();

    String fillDate = fullDateFormat.format(Calendar.getInstance()
        .getTime());
    String actualDate = "";

    for (Events evt : application.getEvents()) {
      actualDate = fullDateFormat.format(evt.getEnd());

      if (actualDate.equals(fillDate) && mSeparatorsSet.size() > 0) {
        addItem(evt);
      } else {
        addSeparatorItem(actualDate);
        addItem(evt);
      }

      fillDate = actualDate;
    }
  }

  @Override
  public void onDestroy() {
  }
}

@TargetApi(11)
public class WidgetServiceHC extends RemoteViewsService {

  @SuppressWarnings("unused")
  private static final String TAG = "WidgetService";

  @Override
  public RemoteViewsFactory onGetViewFactory(Intent intent) {
    return new ListRemoteViewsFactory(this.getApplicationContext(),
        (TimetableApplication_) this.getApplication(), intent);
  }
}