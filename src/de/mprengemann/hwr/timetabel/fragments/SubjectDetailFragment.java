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
package de.mprengemann.hwr.timetabel.fragments;

import java.text.SimpleDateFormat;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;

import de.mprengemann.hwr.timetabel.Events;
import de.mprengemann.hwr.timetabel.R;
import de.mprengemann.hwr.timetabel.Subjects;
import de.mprengemann.hwr.timetabel.TimetableApplication;
import de.mprengemann.hwr.timetabel.viewadapters.SubjectsAdapter;
import de.mprengemann.hwr.timetabel.viewadapters.SubjectsAdapter.OnSelectionChangeListener;

@EFragment(R.layout.fragment_subject_detail)
public class SubjectDetailFragment extends SherlockFragment {

	public interface OnItemClickListener {
		public void onClick(long id);

		public boolean onLongClick(long id);
	}
	@SuppressWarnings("unused")
	private static final String TAG = "SubjectDetailFragment";
	public static final String EXTRA_SUBJECT_ID = "ext_sub_detail_id";

	public static final String EXTRA_EVENT_ID = "ext_evt_detail_id";

	public static SubjectDetailFragment_ newInstance(long subject_id) {
		SubjectDetailFragment_ f = new SubjectDetailFragment_();

		Bundle b = new Bundle();
		b.putLong(EXTRA_SUBJECT_ID, subject_id);

		f.setArguments(b);

		return f;
	}

	@App
	TimetableApplication application;
	private static final SimpleDateFormat df = new SimpleDateFormat("HH:mm");

	private static final SimpleDateFormat df_date = new SimpleDateFormat(
			"dd.MM.yyyy");
	public static SubjectDetailFragment_ newInstance(long subject_id,
			long event_id) {
		SubjectDetailFragment_ f = new SubjectDetailFragment_();

		Bundle b = new Bundle();
		b.putLong(EXTRA_SUBJECT_ID, subject_id);
		b.putLong(EXTRA_EVENT_ID, event_id);

		f.setArguments(b);

		return f;
	}

	private Subjects mSubject = null;

	private Events mEvent = null;

	@ViewById(R.id.text_subject_title)
	TextView title;

	@ViewById(R.id.text_subject_type)
	TextView type;
	@ViewById(R.id.text_subject_lecturer)
	TextView lecturer;
	@ViewById(R.id.text_subject_room)
	TextView room;
	@ViewById(R.id.text_subject_time)
	TextView time;
	@ViewById(R.id.list_subject_events)
	ListView list;
	@ViewById(R.id.text_subject_events_empty)
	TextView empty;
	@AfterViews
	void initViews() {
		if (mSubject != null) {
			final SubjectsAdapter mAdapter = new SubjectsAdapter(getActivity(),
					application);
			mAdapter.setItems(mSubject.getEvents());
			mAdapter.setOnSelectionChangeListener(new OnSelectionChangeListener() {

				@Override
				@UiThread
				public void onSelected(long new_id) {
					title.setText(mSubject.getTitle());
					type.setText(mEvent.getType());
					lecturer.setText(mEvent.getLecturer());
					room.setText(mEvent.getRoom());

					StringBuilder sb = new StringBuilder();
					sb.append(df.format(mEvent.getStart()));
					sb.append(" - ");
					sb.append(df.format(mEvent.getEnd()));

					time.setText(sb.toString());
				}

			});

			if (mAdapter.getCount() <= 0) {
				empty.setVisibility(View.VISIBLE);
			} else {
				empty.setVisibility(View.GONE);
			}

			list.setAdapter(mAdapter);
			list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {

					mEvent = mAdapter.getItem(position);

					if (mEvent != null) {
						mAdapter.setSelection(mEvent);
					}
				}
			});

			mAdapter.setSelection(mEvent);
			list.setSelection(mAdapter.getSeparatorPosition(df_date
					.format(mEvent.getStart())));
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle b;
		long subject_id = 0;
		long event_id = 0;

		if ((b = getArguments()) != null) {
			subject_id = b.getLong(EXTRA_SUBJECT_ID);
			event_id = b.getLong(EXTRA_EVENT_ID, -1);
		}

		mSubject = application.getSubjectById(subject_id);

		if (event_id == -1) {
			mEvent = mSubject.getEvents().get(0);
		} else {
			mEvent = application.getEventById(event_id);
		}
	}
}
