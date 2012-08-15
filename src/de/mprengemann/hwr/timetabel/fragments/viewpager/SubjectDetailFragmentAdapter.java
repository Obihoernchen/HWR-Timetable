package de.mprengemann.hwr.timetabel.fragments.viewpager;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.mprengemann.hwr.timetabel.Subjects;
import de.mprengemann.hwr.timetabel.TimetableApplication;
import de.mprengemann.hwr.timetabel.fragments.SubjectDetailFragment_;

public class SubjectDetailFragmentAdapter extends FragmentPagerAdapter {

	private static final String TAG = "SubjectDetailFragmentAdapter";
	private List<Subjects> mSubjects;
	private long event_id = 0;
	private long subject_id = 0;

	TimetableApplication app;

	public SubjectDetailFragmentAdapter(TimetableApplication app,
			long subject_id, long event_id, FragmentManager fm) {
		super(fm);
		this.app = app;
		this.event_id = event_id;
		this.subject_id = subject_id;

		loadData();
	}

	void loadData() {
		this.mSubjects = app.getVisibleSubjects();
	}

	public void setSelectedEvent(long event_id) {
		this.event_id = event_id;
	}

	@Override
	public Fragment getItem(int position) {
		if (subject_id != getSubjectId(position)) {
			return SubjectDetailFragment_.newInstance(getSubjectId(position));
		} else {
			return SubjectDetailFragment_.newInstance(getSubjectId(position),
					event_id);
		}
	}

	private long getSubjectId(int position) {
		return mSubjects.get(position).getId();
	}

	@Override
	public int getCount() {
		return mSubjects.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {

		String s = mSubjects.get(position).getShortTitle();

		if (s.length() > 10) {
			s = s.substring(0, 10) + "..";
		}

		return s;
	}
}