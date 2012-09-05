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
package de.mprengemann.hwr.timetabel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Application;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.bugsense.trace.BugSenseHandler;
import com.googlecode.androidannotations.annotations.EApplication;

import de.mprengemann.hwr.timetabel.DaoMaster.DevOpenHelper;
import de.mprengemann.hwr.timetabel.SubjectsDao.Properties;

@EApplication
public class TimetableApplication extends Application {

	public interface OnTimetableDataListener {
		void onLoadingFinished(List<Events> result);

		void onLoadingStarted();
	}

	protected static final String TAG = "TimetableApplication";

	private OnTimetableDataListener listener;

	private final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");

	private SQLiteDatabase db;

	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private SubjectsDao subjectsDao;
	private EventsDao eventsDao;

	private SharedPreferences prefs;

	public Events getEventById(long event_id) {

		if (eventsDao == null) {
			return null;
		}

		return eventsDao
				.queryBuilder()
				.where(de.mprengemann.hwr.timetabel.EventsDao.Properties.Id
						.eq(event_id)).list().get(0);
	}

	public List<Events> getEvents() {
		SimpleDateFormat df = new SimpleDateFormat("HH:mm");

		Date d = new Date();
		long past = 1800000;
		try {
			past = df.parse(
					prefs.getString(getString(R.string.prefs_showInPastKey),
							getString(R.string.prefs_default_showInPast)))
					.getTime();
		} catch (ParseException e) {
			past = 1800000;
			BugSenseHandler.log(TAG, e);
		}

		if (eventsDao != null) {
			return eventsDao
					.queryBuilder()
					.where(de.mprengemann.hwr.timetabel.EventsDao.Properties.Start
							.ge(d.getTime() - past),
							EventsDao.Properties.SubjectId
									.in(getVisibleSubjectsId())).build().list();
		} else {
			return new ArrayList<Events>();
		}
	}

	public List<Events> getEvents(Long end) {
		SimpleDateFormat df = new SimpleDateFormat("HH:mm");

		long past = 1800000;
		try {
			past = df.parse(
					prefs.getString(getString(R.string.prefs_showInPastKey),
							getString(R.string.prefs_default_showInPast)))
					.getTime();
		} catch (ParseException e) {
			past = 1800000;
			BugSenseHandler.log(TAG, e);
		}

		if (eventsDao != null) {
			return eventsDao
					.queryBuilder()
					.where(de.mprengemann.hwr.timetabel.EventsDao.Properties.Start
							.ge(end - past),
							EventsDao.Properties.SubjectId
									.in(getVisibleSubjectsId())).build().list();
		} else {
			return new ArrayList<Events>();
		}
	}

	public String[] getEventsDates() {
		if (eventsDao != null) {
			List<Events> distEvents = new ArrayList<Events>();

			Calendar last = Calendar.getInstance();
			last.setTime(new Date(0));

			for (Events e : getEvents()) {
				try {
					if (isNewDistinctDate(last, e)) {
						distEvents.add(e);
						last.setTime(e.getStart());
					}
				} catch (ParseException exc) {
					BugSenseHandler.log(TAG, exc);
				}
			}

			String[] distDates = new String[distEvents.size()];
			for (int i = 0; i < distEvents.size(); i++) {
				distDates[i] = df.format(distEvents.get(i).getStart());
			}

			return distDates;
		}

		return null;
	}

	public Subjects getSubjectById(long subject_id) {

		if (subjectsDao == null) {
			return null;
		}

		return subjectsDao.queryBuilder().where(Properties.Id.eq(subject_id))
				.list().get(0);
	}

	public long getSubjectCount() {
		return subjectsDao.queryBuilder().list().size();
	}

	public int getSubjectPosition(long sub_id) {
		return getVisibleSubjects().indexOf(getSubjectById(sub_id));
	}

	public List<Subjects> getSubjects() {

		if (subjectsDao == null) {
			return new ArrayList<Subjects>();
		}

		return subjectsDao.queryBuilder().orderAsc(Properties.ShortTitle)
				.list();
	}

	public List<Subjects> getVisibleSubjects() {

		if (subjectsDao == null) {
			return new ArrayList<Subjects>();
		}

		return subjectsDao.queryBuilder()
				.where(SubjectsDao.Properties.Show.eq(Boolean.valueOf(true)))
				.orderAsc(Properties.ShortTitle).list();
	}

	private List<Long> getVisibleSubjectsId() {

		if (subjectsDao == null) {
			return new ArrayList<Long>();
		}

		List<Subjects> visibleSubjects = getVisibleSubjects();

		List<Long> result = new ArrayList<Long>();

		for (Subjects s : visibleSubjects) {
			result.add(s.getId());
		}

		return result;
	}

	void initData() {
		DevOpenHelper helper = new DevOpenHelper(this, "timetable-db", null);
		db = helper.getWritableDatabase();

		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();
		subjectsDao = daoSession.getSubjectsDao();
		eventsDao = daoSession.getEventsDao();
	}

	private boolean isNewDistinctDate(Calendar last, Events e)
			throws ParseException {

		String a = df.format(last.getTime());
		String b = df.format(e.getStart());

		return df.parse(a).before(df.parse(b));
	}

	public void onCreate() {
		super.onCreate();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		initData();
	}

	public void onLoadingFinished() {
		if (listener != null) {
			listener.onLoadingFinished(getEvents());
		}
	}

	public void onLoadingStarted() {
		if (listener != null) {
			listener.onLoadingStarted();
		}
	}

	public void onNewItem(Subjects s, Events e) {
		if (subjectsDao.queryBuilder().where(Properties.Title.eq(s.getTitle()))
				.count() == 1) {
			s.setId(subjectsDao.queryBuilder()
					.where(Properties.Title.eq(s.getTitle())).list().get(0)
					.getId());
			subjectsDao.update(s);
		} else {
			s.setId(subjectsDao.insert(s));
		}

		if (eventsDao
				.queryBuilder()
				.where(de.mprengemann.hwr.timetabel.EventsDao.Properties.Uid
						.eq(e.getUid())).count() == 1) {
			e.setId(eventsDao
					.queryBuilder()
					.where(de.mprengemann.hwr.timetabel.EventsDao.Properties.Uid
							.eq(e.getUid())).list().get(0).getId());
			e.setSubjectId(s.getId());
			eventsDao.update(e);
		} else {
			e.setSubjectId(s.getId());
			eventsDao.insert(e);
			s.getEvents().add(e);
			s.update();
		}
	}

	public void removeAllData() {
		eventsDao.deleteAll();
		subjectsDao.deleteAll();
	}

	public void setOnTimetableDataListener(OnTimetableDataListener listener) {
		this.listener = listener;
	}

}
