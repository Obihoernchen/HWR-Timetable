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
import android.widget.CheckedTextView;
import com.bugsense.trace.BugSenseHandler;
import de.mprengemann.hwr.timetabel.R;
import de.mprengemann.hwr.timetabel.Subjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubjectChooserAdapter extends BaseAdapter {

  public interface OnSubjectCheckedChangeListener {
    void onCheckedChange(Long id, Boolean isChecked);
  }

  static class ViewHolder {
    CheckedTextView checkView;
  }

  private static final String TAG = "SubjectChooserAdapter";

  private List<Long> changed = new ArrayList<Long>();

  private LayoutInflater mInflater;

  private List<Subjects> mData = new ArrayList<Subjects>();

  private OnSubjectCheckedChangeListener listener;

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
      holder.checkView = (CheckedTextView) convertView
          .findViewById(R.id.checkedText_subject_chooser);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    final Subjects item = mData.get(position);

    if (item != null) {
      try {
        holder.checkView.setText(item.getTitle());
      } catch (Exception e) {
        Log.e(TAG, String.valueOf(item.getId()));
        HashMap<String, String> extraData = new HashMap<String, String>();
        extraData.put("itemid", String.valueOf(item.getId()));

        BugSenseHandler.sendExceptionMap(extraData, e);
      }

      if (changed.contains(Long.valueOf(item.getId()))) {
        holder.checkView.setChecked(!item.getShow().booleanValue());
      } else {
        holder.checkView.setChecked(item.getShow().booleanValue());
      }

      holder.checkView.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          if (changed.contains(Long.valueOf(item.getId()))) {
            ((CheckedTextView) v).setChecked(item.getShow()
                .booleanValue());
            changed.remove(Long.valueOf(item.getId()));
          } else {
            ((CheckedTextView) v).setChecked(!item.getShow()
                .booleanValue());
            changed.add(Long.valueOf(item.getId()));
          }

          if (listener != null) {
            listener.onCheckedChange(getItemId(position), Boolean
                .valueOf(((CheckedTextView) v).isChecked()));
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
