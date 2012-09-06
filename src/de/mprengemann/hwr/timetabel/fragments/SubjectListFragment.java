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

import java.util.List;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;

import de.mprengemann.hwr.timetabel.Events;
import de.mprengemann.hwr.timetabel.R;
import de.mprengemann.hwr.timetabel.TimetableApplication;
import de.mprengemann.hwr.timetabel.TimetableApplication.OnTimetableDataListener;
import de.mprengemann.hwr.timetabel.viewadapters.SubjectsAdapter;
import de.mprengemann.hwr.timetabel.viewadapters.SubjectsAdapter.OnDataChangeListener;

@EFragment(R.layout.fragment_subject_list)
public class SubjectListFragment extends SherlockFragment {

	public interface OnItemClickListener {
		public void onClick(long subject_id, long event_id);

		public boolean onLongClick(long subject_id, long event_id);
	}

	private static final String TAG = "SubjectListFragment";

	public static SubjectListFragment_ newInstance() {
		SubjectListFragment_ f = new SubjectListFragment_();
		return f;
	}
	@ViewById(R.id.list_subjects)
	ListView listView;
	@ViewById(R.id.text_subjects_empty)
	TextView empty;

	@App
	TimetableApplication application;
	private SubjectsAdapter mAdapter;
	private OnItemClickListener onItemClickListener;

	private MenuItem menuItemToShow;

	public void addItem(Events e) {
		if (mAdapter != null) {
			mAdapter.addItem(e);
		}
	}

	public void addSeparatorItem(String str) {
		if (mAdapter != null) {
			mAdapter.addSeparatorItem(str);
		}
	}

	public void clear() {
		if (mAdapter != null) {
			mAdapter.clear();
		}
	}

	public void fillList() {
		mAdapter.setItems(application.getEvents());
	}

	@AfterViews
	public void initViews() {
		mAdapter = new SubjectsAdapter(getActivity(), application);
		mAdapter.setOnDataChangeListener(new OnDataChangeListener() {

			@Override
			public void onDataChange(int size, final String text) {
				if (size == 0) {
					empty.setVisibility(View.VISIBLE);
					empty.post(new Runnable() {

						@Override
						public void run() {
							empty.setText(text);
						}

					});
				} else {
					empty.setVisibility(View.GONE);
				}
			}

		});

		application.setOnTimetableDataListener(new OnTimetableDataListener() {

			@Override
			public void onLoadingFinished(final List<Events> result) {
				Log.i(TAG, "End!!");

				mAdapter.setLoading(false);
				mAdapter.setItems(result);
				if (menuItemToShow != null) {
					menuItemToShow.setVisible(true);
				}

				getSherlockActivity()
						.setSupportProgressBarIndeterminateVisibility(false);
			}

			@Override
			public void onLoadingStarted() {
				Log.i(TAG, "Start!!");
				
				mAdapter.setLoading(true);
				
				if (getSherlockActivity() != null){
					getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
					getSherlockActivity().setSupportProgress(Window.PROGRESS_END);
				}				
			}

		});

		listView.setAdapter(mAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (onItemClickListener != null) {
					if (mAdapter.getItem(position) != null) {
						onItemClickListener.onClick(mAdapter.getItem(position)
								.getSubjectId(), mAdapter.getItem(position)
								.getId());
					}

				}
			}
		});
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (onItemClickListener != null) {
					if (mAdapter.getItem(position) != null) {
						return onItemClickListener.onLongClick(mAdapter
								.getItem(position).getSubjectId(), mAdapter
								.getItem(position).getId());
					}
				}
				return false;
			}

		});

		fillList();
	}

	public void scrollTo(String item) {
		if (mAdapter != null) {
			if (listView != null) {
				listView.setSelection(mAdapter.getSeparatorPosition(item));
			}
		}
	}

	public void setInitalState() {
		clear();
		empty.setVisibility(View.VISIBLE);
		empty.setText(R.string.text_no_subjects);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.onItemClickListener = listener;
	}

	public void showAfter(final MenuItem item) {
		this.menuItemToShow = item;
	}
}
