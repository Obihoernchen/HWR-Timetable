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

	private static final String TAG = "SubjectListFragment";

	public interface OnItemClickListener {
		public void onClick(long subject_id, long event_id);

		public boolean onLongClick(long subject_id, long event_id);
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

	public static SubjectListFragment_ newInstance() {
		SubjectListFragment_ f = new SubjectListFragment_();
		return f;
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
			public void onLoadingStarted() {
				Log.i(TAG, "Start!!");

				getSherlockActivity().setSupportProgress(Window.PROGRESS_END);
				mAdapter.setLoading(true);
				getSherlockActivity()
						.setSupportProgressBarIndeterminateVisibility(true);
			}

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

	public void showAfter(final MenuItem item) {
		this.menuItemToShow = item;
	}

	public void scrollTo(String item) {
		if (mAdapter != null) {
			if (listView != null) {
				listView.setSelection(mAdapter.getSeparatorPosition(item));
			}
		}
	}

	public void fillList() {
		mAdapter.setItems(application.getEvents());
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.onItemClickListener = listener;
	}

	public void setInitalState() {
		clear();
		empty.setVisibility(View.VISIBLE);
		empty.setText(R.string.text_no_subjects);
	}
}
