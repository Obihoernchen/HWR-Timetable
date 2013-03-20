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
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;
import com.bugsense.trace.BugSenseHandler;
import de.mprengemann.hwr.timetabel.Events;
import de.mprengemann.hwr.timetabel.R;
import de.mprengemann.hwr.timetabel.TimetableApplication;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class SubjectsAdapter extends BaseAdapter implements SectionIndexer {

  public final static long SECOND_MILLIS = 1000;
  public final static long MINUTE_MILLIS = SECOND_MILLIS * 60;
  public final static long HOUR_MILLIS = MINUTE_MILLIS * 60;
  public final static long DAY_MILLIS = HOUR_MILLIS * 24;
  public final static long YEAR_MILLIS = DAY_MILLIS * 365;
  private static final String TAG = SubjectsAdapter.class.getSimpleName();
  private static final int TYPE_ITEM = 0;
  private static final int TYPE_SEPARATOR = 1;
  private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;
  private OnDataChangeListener listener;
  private LayoutInflater mInflater;
  private SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.GERMANY);
  private SimpleDateFormat fullDateFormat = new SimpleDateFormat(
      "EE, dd.MM.yyyy", Locale.GERMANY);
  private SimpleDateFormat reallyShortFormat = new SimpleDateFormat("d.M.", Locale.GERMANY);
  private HashMap<Integer, Events> mData = new HashMap<Integer, Events>();
  private TreeMap<Integer, String> mSeparatorsSet = new TreeMap<Integer, String>(new SeparatorComparator());
  private Context context;
  private TimetableApplication app;
  private boolean isLoading = false;
  private long selection = -1;
  private OnSelectionChangeListener selListener;

  public SubjectsAdapter(Context context, TimetableApplication app) {
    mInflater = LayoutInflater.from(context);
    this.context = context;
    this.app = app;
  }

  public void addItem(final Events evt) {
    mData.put(Integer.valueOf(mData.size() + mSeparatorsSet.size()), evt);
    callListener();
    notifyDataSetChanged();
  }

  public void addSeparatorItem(final String str) {
    mSeparatorsSet.put(
        Integer.valueOf(mData.size() + mSeparatorsSet.size()), str);
    callListener();
    notifyDataSetChanged();
  }

  private void callListener() {
    if (listener != null) {
      listener.onDataChange(getCount(), getLoadingString());
    }
  }

  public void clear() {
    mData = new HashMap<Integer, Events>();
    mSeparatorsSet = new TreeMap<Integer, String>(new SeparatorComparator());

    callListener();
    notifyDataSetChanged();
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

  @Override
  public int getCount() {
    return (mData.size() + mSeparatorsSet.size());
  }

  @Override
  public Events getItem(int position) {
    return mData.get(Integer.valueOf(position));
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public int getItemViewType(int position) {
    return mSeparatorsSet.containsKey(Integer.valueOf(position)) ? TYPE_SEPARATOR
        : TYPE_ITEM;
  }

  private String getLoadingString() {
    if (isLoading) {
      return context.getString(R.string.text_please_wait);
    } else if (app.getSubjectCount() == 0) {
      return context.getString(R.string.text_no_subjects);
    } else {
      return context.getString(R.string.text_no_subjects_in_time);
    }
  }

  public int getSeparatorPosition(String item) {
    for (Entry<Integer, String> set : mSeparatorsSet.entrySet()) {
      if (set.getValue().contains(item)) {
        return set.getKey();
      }
    }
    return 0;
  }

  public String getSeperatorItem(int position) {
    return mSeparatorsSet.get(Integer.valueOf(position));
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    int type = getItemViewType(position);
    if (convertView == null) {
      holder = new ViewHolder();
      switch (type) {
        case TYPE_ITEM:
          convertView = mInflater.inflate(R.layout.fragment_subject_item,
              null);
          holder.titleView = (TextView) convertView
              .findViewById(R.id.text_subject_item_title);
          holder.dateView = (TextView) convertView
              .findViewById(R.id.text_subject_item_time);
          holder.roomView = (TextView) convertView
              .findViewById(R.id.text_subject_item_room);

          if (position % 2 == 0) {
            convertView.setBackgroundColor(context.getResources()
                .getColor(R.color.abs__background_holo_dark));
          } else {
            convertView.setBackgroundColor(context.getResources()
                .getColor(R.color.abs__background_holo_light));
          }

          break;
        case TYPE_SEPARATOR:
          convertView = mInflater.inflate(
              R.layout.fragment_subject_separator, null);
          holder.separatorView = (TextView) convertView
              .findViewById(R.id.text_subject_separator);
          break;
      }
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    if (type == TYPE_ITEM) {
      final Events item = mData.get(position);

      if (item != null) {
        if (selection == item.getId()) {
          convertView
              .setBackgroundResource(R.drawable.abs__list_activated_holo);
          int padding = context.getResources().getDimensionPixelSize(
              R.dimen.default_list_padding);
          convertView.setPadding(padding, padding, padding, padding);
        } else {
          convertView
              .setBackgroundResource(android.R.color.transparent);
        }

        try {
          holder.titleView.setText(item.getSubjects().getTitle());
        } catch (Exception e) {
          Log.e(TAG, item.getSubjectId() + " " + item.getSubjects());
          HashMap<String, String> extraData = new HashMap<String, String>();
          extraData.put("subjectsid",
              String.valueOf(item.getSubjectId()));
          extraData.put("subjects",
              String.valueOf(item.getSubjects()));

          BugSenseHandler.sendExceptionMap(extraData, e);
        }

        holder.roomView.setText(item.getRoom());
        holder.dateView.setText(df.format(item.getStart()) + " - "
            + df.format(item.getEnd()));
      }

    } else {
      try {
        Calendar nextSeperatorDate = Calendar.getInstance();
        nextSeperatorDate.setTime(fullDateFormat
            .parse(getSeperatorItem(position)));

        long dateDiff = daysDiff(Calendar.getInstance(),
            nextSeperatorDate);

        if (dateDiff == 0) {
          holder.separatorView.setText(context
              .getString(R.string.text_today));
        } else if (dateDiff == 1) {
          holder.separatorView.setText(context
              .getString(R.string.text_tomorrow));
        } else if (dateDiff == -1) {
          holder.separatorView.setText(context
              .getString(R.string.text_yesterday));
        } else if (dateDiff == -2) {
          holder.separatorView.setText(context
              .getString(R.string.text_twoago));
        } else {
          holder.separatorView.setText(getSeperatorItem(position));
        }
      } catch (Exception e) {
        holder.separatorView.setText(getSeperatorItem(position));
        BugSenseHandler.sendException(e);
      }

    }
    return convertView;
  }

  @Override
  public int getViewTypeCount() {
    return TYPE_MAX_COUNT;
  }

  public void setItems(List<Events> events) {
    clear();
    setItemsInternal(events);
  }

  public void setLoading(boolean b) {
    isLoading = b;
  }

  public void setOnDataChangeListener(OnDataChangeListener listener) {
    this.listener = listener;
  }

  public void setOnSelectionChangeListener(
      OnSelectionChangeListener onSelectionChangeListener) {
    this.selListener = onSelectionChangeListener;
  }

  public void setSelection(Events mEvent) {
    if (mEvent != null) {
      this.selection = mEvent.getId();
      if (selListener != null) {
        selListener.onSelected(selection);
      }
    } else {
      this.selection = -1;
    }
    notifyDataSetChanged();
  }

  public void setItems(List<Events> events, String headline) {
    clear();

    addSeparatorItem(headline);
    setItemsInternal(events);
  }

  private void setItemsInternal(List<Events> events) {
    String fillDate = fullDateFormat.format(Calendar.getInstance()
        .getTime());
    String actualDate;

    for (Events evt : events) {
      actualDate = fullDateFormat.format(evt.getEnd());

      if (actualDate.equals(fillDate) && mSeparatorsSet.size() > 0) {
        addItem(evt);
      } else {
        addSeparatorItem(actualDate);
        addItem(evt);
      }

      fillDate = actualDate;
    }

    callListener();
  }

  @Override
  public Object[] getSections() {
    Object[] sections = new Object[mSeparatorsSet.size()];
    List<String> entries = new ArrayList<String>(mSeparatorsSet.values());
    for (int i = 0; i < entries.size(); i++) {
      try {
        sections[i] = reallyShortFormat.format(fullDateFormat.parse(entries.get(i)));
        if (sections[i].toString().length() > 4) {
          sections[i] = new SimpleDateFormat("d.").format(fullDateFormat.parseObject(entries.get(i)));
        }
      } catch (Exception e) {
        sections[i] = entries.get(i);
      }
    }
    return sections;
  }

  @Override
  public int getPositionForSection(int section) {
    int i = 0;
    for (Entry<Integer, String> entry : mSeparatorsSet.entrySet()) {
      if (section == i) {
        return entry.getKey();
      }
      i++;
    }
    return 0;
  }

  @Override
  public int getSectionForPosition(int position) {
    int i = 0;
    switch (getItemViewType(position)) {
      case TYPE_ITEM:
        Events e = getItem(position);
        for (Entry<Integer, String> entry : mSeparatorsSet.entrySet()) {
          if (entry.getValue().equals(fullDateFormat.format(e.getEnd()))) {
            return i;
          }
          i++;
        }
        break;
      case TYPE_SEPARATOR:
        for (Entry<Integer, String> entry : mSeparatorsSet.entrySet()) {
          if (entry.getKey().equals(Integer.valueOf(position))) {
            return i;
          }
          i++;
        }
        break;
      default:
        return 0;
    }
    return 0;
  }

  public void removeEvent(Long id) {
    Integer key = null;
    if (mData != null) {
      for (Entry<Integer, Events> entry : mData.entrySet()) {
        if (entry.getValue().getId().longValue() == id.longValue()) {
          key = entry.getKey();
          break;
        }
      }
    }
    if (key != null) {
      mData.remove(key);

      List<Events> events = new ArrayList<Events>(mData.values());
      setItems(events);

      notifyDataSetChanged();
    }
  }

  public interface OnDataChangeListener {
    void onDataChange(int size, String text);
  }

  public interface OnSelectionChangeListener {
    void onSelected(long new_id);
  }

  class ViewHolder {
    TextView titleView;
    TextView dateView;
    TextView roomView;
    TextView separatorView;
  }

  private class SeparatorComparator implements Comparator<Integer> {
    @Override
    public int compare(Integer lhs, Integer rhs) {
      return lhs.compareTo(rhs);
    }
  }
}
