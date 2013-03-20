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
package de.mprengemann.hwr.timetabel.viewadapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.bugsense.trace.BugSenseHandler;
import de.mprengemann.hwr.timetabel.R;
import de.mprengemann.hwr.timetabel.data.GoogleCalendar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CalendarChooserAdapter extends BaseAdapter {

  public interface OnCalendarSelectedListener {
    void onCalendarSelected(GoogleCalendar cal);
  }

  static class ViewHolder {
    TextView titleView;
  }

  private static final String TAG = "SubjectChooserAdapter";
  private LayoutInflater mInflater;
  private GoogleCalendar selection;

  private Context context;

  private List<GoogleCalendar> mData = new ArrayList<GoogleCalendar>();

  private OnCalendarSelectedListener listener;

  public CalendarChooserAdapter(Context context) {
    this.mInflater = LayoutInflater.from(context);
    this.context = context;
  }

  public void addItem(GoogleCalendar cal) {
    if (mData == null) {
      mData = new ArrayList<GoogleCalendar>();
    }

    mData.add(cal);

    notifyDataSetChanged();
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
        } else {
          convertView
              .setBackgroundResource(android.R.color.transparent);
        }
      } else {
        convertView.setBackgroundResource(android.R.color.transparent);
      }

      try {
        holder.titleView.setText(item.getDisplayName());
      } catch (Exception e) {
        Log.e(TAG, String.valueOf(item.getId()));
        HashMap<String, String> extraData = new HashMap<String, String>();
        extraData.put("itemid", String.valueOf(item.getId()));

        BugSenseHandler.sendExceptionMap(extraData, e);
      }

      holder.titleView.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          if (selection != null) {
            if (selection.equals(getItem(position))) {
              selection = null;
            } else {
              selection = getItem(position);
            }
          } else {
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

  public void setSelection(GoogleCalendar cal) {
    selection = cal;
    notifyDataSetChanged();
  }
}
