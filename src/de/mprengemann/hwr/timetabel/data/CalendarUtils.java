package de.mprengemann.hwr.timetabel.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.util.Log;
import de.mprengemann.hwr.timetabel.R;
import de.mprengemann.hwr.timetabel.Subjects;
import de.mprengemann.hwr.timetabel.TimetableApplication_;

public class CalendarUtils {

	public interface CalendarFetcherListener {
		void newCalendar(GoogleCalendar cal);
	}

	public interface CalendarSyncListener {
		void onStartRemoving();

		void onFinishRemoving();

		void onStartPublishing();

		void onFinishPublishing();
	}
	
	public interface CalendarExportListener {
		void onStart();

		void onFinish(String path);
	}

	public static void getGoogleCalendars(Context c,
			CalendarFetcherListener listener) {
		CalendarFetcherTask test = new CalendarFetcherTask(listener);
		test.execute(c);
	}

	public static void getGoogleCalendar(Context c, long sel_id,
			CalendarFetcherListener calendarFetcherListener) {
		CalendarFetcherTask test = new CalendarFetcherTask(sel_id,
				calendarFetcherListener);
		test.execute(c);
	}

	public static void removeAllEvents(Context c, long id,
			CalendarSyncListener listener) {
		EventsRemoverTask test = new EventsRemoverTask(id, listener);
		test.execute(c);
	}

	public static void syncTimetable(TimetableApplication_ application,
			Context c, long id, CalendarSyncListener listener) {
		removeAllEvents(c, id, listener);
		TimetableWriterTask task = new TimetableWriterTask(application, id,
				listener);
		task.execute(c);
	}

	public static void exportICSCalendar(TimetableApplication_ application,
			Context c, CalendarExportListener listener) {
		TimetableExportTask task = new TimetableExportTask(application, listener);
		task.execute(c);
	}

	@TargetApi(14)
	private static class CalendarFetcherTask extends
			AsyncTask<Context, GoogleCalendar, Void> {

		private static final String TAG = "CalendarFetcherTask";

		public static final String[] CALENDAR_PROJECTION = new String[] {
				Calendars._ID, Calendars.ACCOUNT_NAME,
				Calendars.CALENDAR_DISPLAY_NAME, Calendars.OWNER_ACCOUNT };

		private static final int PROJECTION_ID_INDEX = 0;
		private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
		private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
		private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

		private CalendarFetcherListener listener;
		private long selectionId = -1;

		public CalendarFetcherTask(CalendarFetcherListener listener) {
			this(-1, listener);
		}

		public CalendarFetcherTask(long sel_id,
				CalendarFetcherListener calendarFetcherListener) {
			this.selectionId = sel_id;
			this.listener = calendarFetcherListener;
		}

		@Override
		protected Void doInBackground(Context... params) {
			List<String> googleAccounts = new ArrayList<String>();

			AccountManager accountManager = AccountManager.get(params[0]);
			Account[] accounts = accountManager.getAccounts();
			for (int i = 0; i < accounts.length; i++) {
				Log.i(TAG, accounts[i].name + " " + accounts[i].type);

				if (accounts[i].type.equals("com.google")) {
					googleAccounts.add(accounts[i].name);
				}
			}

			Cursor cur = null;
			ContentResolver cr = params[0].getContentResolver();
			String selection;

			if (selectionId == -1) {
				selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
						+ Calendars.ACCOUNT_TYPE + " = ?))";
			} else {
				selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
						+ Calendars.ACCOUNT_TYPE + " = ?) AND ("
						+ Calendars._ID + " = ?))";
			}

			String[] selectionArgs;

			for (String acc : googleAccounts) {
				if (selectionId == -1) {
					selectionArgs = new String[] { acc, "com.google" };
				} else {
					selectionArgs = new String[] { acc, "com.google",
							String.valueOf(selectionId) };
				}

				cur = cr.query(Calendars.CONTENT_URI, CALENDAR_PROJECTION,
						selection, selectionArgs, null);

				long calID = 0;
				String displayName = null;
				String accountName = null;
				String ownerName = null;

				while (cur.moveToNext()) {
					calID = cur.getLong(PROJECTION_ID_INDEX);
					displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
					accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
					ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);

					onProgressUpdate(new GoogleCalendar(calID, displayName,
							accountName, ownerName));
				}
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(GoogleCalendar... values) {
			super.onProgressUpdate(values);

			if (listener != null) {
				listener.newCalendar(values[0]);
			}
		}
	}

	@TargetApi(14)
	private static class EventsRemoverTask extends
			AsyncTask<Context, Void, Void> {

		private static final String TAG = "EventsRemoverTask";

		public static final String[] EVENT_PROJECTION = new String[] { Events._ID };
		private static final int PROJECTION_ID_INDEX = 0;
		private long calId = -1;

		private CalendarSyncListener listener;

		@SuppressWarnings("unused")
		public EventsRemoverTask(long id) {
			this(id, null);
		}

		public EventsRemoverTask(long id, CalendarSyncListener listener) {
			this.calId = id;
			this.listener = listener;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			if (listener != null) {
				listener.onStartRemoving();
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (listener != null) {
				listener.onFinishRemoving();
			}
		}

		@Override
		protected Void doInBackground(Context... params) {
			ContentResolver cr = params[0].getContentResolver();
			String selection = "(" + Events.CALENDAR_ID + " = ?)";

			String[] selectionArgs = new String[] { String.valueOf(calId) };
			Cursor cur = cr.query(Events.CONTENT_URI, EVENT_PROJECTION,
					selection, selectionArgs, null);

			long evtID = 0;

			Log.i(TAG, "Start managing cursor for Calendar " + calId + " with "
					+ cur.getCount() + " events");

			while (cur.moveToNext()) {
				evtID = cur.getLong(PROJECTION_ID_INDEX);

				Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI,
						evtID);
				cr.delete(deleteUri, null, null);
			}

			return null;
		}
	}

	@TargetApi(14)
	private static class TimetableWriterTask extends
			AsyncTask<Context, Void, Void> {

		@SuppressWarnings("unused")
		private static final String TAG = "TimetableWriterTask";
		private long calId = -1;
		private TimetableApplication_ application;
		private CalendarSyncListener listener;

		@SuppressWarnings("unused")
		public TimetableWriterTask(TimetableApplication_ application, long id) {
			this(application, id, null);
		}

		public TimetableWriterTask(TimetableApplication_ application, long id,
				CalendarSyncListener listener) {
			this.calId = id;
			this.application = application;
			this.listener = listener;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			if (listener != null) {
				listener.onStartPublishing();
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (listener != null) {
				listener.onFinishPublishing();
			}
		}

		@Override
		protected Void doInBackground(Context... params) {

			ContentResolver cr = params[0].getContentResolver();
			ContentValues values;

			for (Subjects s : application.getVisibleSubjects()) {
				for (de.mprengemann.hwr.timetabel.Events e : s.getEvents()) {
					values = new ContentValues();
					values.put(Events.DTSTART, e.getStart().getTime());
					values.put(Events.DTEND, e.getEnd().getTime());
					values.put(Events.TITLE, s.getTitle());
					values.put(Events.DESCRIPTION, e.getLecturer());
					values.put(Events.EVENT_LOCATION, e.getRoom());
					values.put(Events.CALENDAR_ID, String.valueOf(calId));
					values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault()
							.getDisplayName());
					cr.insert(Events.CONTENT_URI, values);
				}
			}

			return null;
		}
	}

	private static class TimetableExportTask extends
			AsyncTask<Context, Void, String> {

		private static final String TAG = "TimetableExportTask";

		private TimetableApplication_ application;
		private Context context;

		private CalendarExportListener listener;

		@SuppressWarnings("unused")
		public TimetableExportTask(TimetableApplication_ application) {
			this(application, null);
		}
		
		public TimetableExportTask(TimetableApplication_ application, CalendarExportListener listener) {
			this.application = application;
			this.listener = listener;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			if (listener != null){
				listener.onStart();
			}
		}

		@Override
		protected String doInBackground(Context... params) {
			context = params[0];

			TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance()
					.createRegistry();
			TimeZone timezone = registry.getTimeZone("Europe/Berlin");

			Calendar calendar = new Calendar();
			calendar.getProperties().add(
					new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
			calendar.getProperties().add(Version.VERSION_2_0);
			calendar.getProperties().add(Method.PUBLISH);
			calendar.getProperties().add(CalScale.GREGORIAN);

			VEvent evt;
			UidGenerator ug;
			try {
				ug = new UidGenerator("uidGen");
				DateTime start;
				DateTime end;
				for (Subjects s : application.getVisibleSubjects()) {
					for (de.mprengemann.hwr.timetabel.Events e : s.getEvents()) {
						start = new DateTime(e.getStart().getTime());
						end = new DateTime(e.getEnd().getTime());

						start.setTimeZone(timezone);
						end.setTimeZone(timezone);

						evt = new VEvent(start, end, s.getTitle());
						evt.getProperties().add(new Location(e.getRoom()));
						evt.getProperties()
								.add(new Description(
										String.format(
												"Art: %s\nVeranstaltung: %s\nDozent: %s\nRaum: %s",
												s.getType(), s.getTitle(),
												e.getLecturer(), e.getRoom())));
						evt.getProperties().add(ug.generateUid());
						calendar.getComponents().add(evt);
					}
				}

				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(context);

				String filename = "hwr_berlin_stundenplan_"
						+ prefs.getString(
								context.getString(R.string.prefs_matrikelNrKey),
								"0") + ".ics";
				File file = new File(Environment.getExternalStorageDirectory(),
						filename);
				if (file.exists()) {
					file.delete();
				}
				FileOutputStream fos = new FileOutputStream(file);

				CalendarOutputter outputter = new CalendarOutputter();
				outputter.setValidating(false);
				outputter.output(calendar, fos);

				return context.getString(R.string.dialog_export_success) + " "
						+ file.getAbsolutePath();
			} catch (SocketException e) {
				Log.e(TAG, e.toString());
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.toString());
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			} catch (ValidationException e) {
				Log.e(TAG, e.toString());
			}

			return context.getString(R.string.dialog_export_error);
		}

		@Override
		protected void onPostExecute(final String result) {
			super.onPostExecute(result);

			if (listener != null){
				listener.onFinish(result);
			}
		}
	}
}
