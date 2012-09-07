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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.widget.TimePicker;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.bugsense.trace.BugSenseHandler;
import com.googlecode.androidannotations.annotations.EActivity;

import de.mprengemann.hwr.timetabel.CalendarChooserDialog.OnSelectionListener;
import de.mprengemann.hwr.timetabel.data.CalendarUtils;
import de.mprengemann.hwr.timetabel.data.CalendarUtils.CalendarExportListener;
import de.mprengemann.hwr.timetabel.data.CalendarUtils.CalendarSyncListener;
import de.mprengemann.hwr.timetabel.data.GoogleCalendar;

@EActivity
public class PreferenceActivity extends SherlockPreferenceActivity {

	@SuppressWarnings("unused")
	private static final String TAG = "PreferenceActivity";

	private SharedPreferences prefs;
	private boolean isChanged = false;
	private boolean refreshHistory = false;
	private String kurs;
	private String kursNeu;
	private String fachrichtung;
	private String fachrichtungNeu;
	private String semester;
	private String semesterNeu;
	private String matrikelNr;
	private String matrikelNrNeu;
	private String showPastString;
	private String showPastStringNeu;

	private Preference pref_sync_force;

	private Handler mHandler = new Handler();

	@Override
	public void finish() {
		kursNeu = prefs.getString(getString(R.string.prefs_kursKey), "n/a");
		fachrichtungNeu = prefs.getString(
				getString(R.string.prefs_fachrichtungKey), "n/a");
		semesterNeu = prefs.getString(getString(R.string.prefs_semesterKey),
				"n/a");
		matrikelNrNeu = prefs.getString(
				getString(R.string.prefs_matrikelNrKey), "n/a");
		showPastStringNeu = prefs.getString("showHistory", "n/a");

		if (!kursNeu.equals(kurs) || !fachrichtungNeu.equals(fachrichtung)
				|| !semesterNeu.equals(semester)
				|| !matrikelNrNeu.equals(matrikelNr)) {
			isChanged = true;
		}
		if (!showPastString.equals(showPastStringNeu)) {
			refreshHistory = true;
		}

		Intent data = new Intent();

		data.putExtra(getString(R.string.intent_data_preferences_changed),
				isChanged);
		data.putExtra(getString(R.string.intent_data_refresh_history),
				refreshHistory);

		setResult(RESULT_OK, data);

		super.finish();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.prefs);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		long lastUpdated = prefs.getLong(
				getString(R.string.prefs_lastUpdated_user), -1);
		Preference pref_lastUpdated = findPreference(getString(R.string.prefs_lastUpdated));

		if (lastUpdated <= 0) {
			pref_lastUpdated
					.setSummary(getString(R.string.text_prefs_lastUpdated_never));
		} else {
			SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			pref_lastUpdated.setSummary(df.format(new Date(lastUpdated)));
		}

		CheckBoxPreference sync = (CheckBoxPreference) findPreference(getString(R.string.prefs_cal_sync_Key));
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			sync.setEnabled(false);
		}

		kurs = prefs.getString(getString(R.string.prefs_kursKey), "n/a");
		fachrichtung = prefs.getString(
				getString(R.string.prefs_fachrichtungKey), "n/a");
		semester = prefs
				.getString(getString(R.string.prefs_semesterKey), "n/a");
		matrikelNr = prefs.getString(getString(R.string.prefs_matrikelNrKey),
				"n/a");
		showPastString = prefs.getString(
				getString(R.string.prefs_showInPastKey),
				getString(R.string.prefs_default_showInPast));

		EditTextPreference pref = (EditTextPreference) findPreference(getString(R.string.prefs_matrikelNrKey));
		pref.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);

		Preference pref_showPast = findPreference(getString(R.string.prefs_showInPastKey));
		if (pref_showPast != null) {
			pref_showPast
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {

						private final long DEFAULT_PAST = 1800000;
						private SimpleDateFormat df = new SimpleDateFormat(
								"HH:mm");

						@Override
						public boolean onPreferenceClick(Preference preference) {

							Calendar d = Calendar.getInstance();

							try {
								d.setTime(df.parse(prefs
										.getString(
												getString(R.string.prefs_showInPastKey),
												getString(R.string.prefs_default_showInPast))));
							} catch (ParseException e) {
								d.setTimeInMillis(DEFAULT_PAST);
								BugSenseHandler.sendException(e);
							}

							TimePickerDialog dialog = new TimePickerDialog(
									PreferenceActivity.this,
									new OnTimeSetListener() {

										@Override
										public void onTimeSet(TimePicker view,
												int hourOfDay, int minute) {
											GregorianCalendar newDate = new GregorianCalendar(
													0, 0, 0, hourOfDay, minute);

											Editor edit = prefs.edit();
											edit.putString(
													getString(R.string.prefs_showInPastKey),
													df.format(newDate.getTime()));
											edit.commit();
										}

									}, d.get(Calendar.HOUR_OF_DAY), d
											.get(Calendar.MINUTE), true);

							dialog.setTitle(R.string.text_prefs_showInPast_title);
							dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
									getString(android.R.string.cancel),
									new OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
										}

									});

							dialog.show();

							return false;
						}
					});
		}
		Preference pref_cal = findPreference(getString(R.string.prefs_cal_Key));
		if (pref_cal != null) {
			pref_cal.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					CalendarChooserDialog calDialog = new CalendarChooserDialog(
							PreferenceActivity.this, prefs.getLong(
									getString(R.string.prefs_cal_Key), -1));
					calDialog.setOnSelectionListener(new OnSelectionListener() {

						@Override
						public void onSelect(GoogleCalendar cal) {
							if (cal != null) {
								Editor edit = prefs.edit();
								edit.putLong(getString(R.string.prefs_cal_Key),
										cal.getId());
								edit.commit();

								mHandler.post(new Runnable() {
									@Override
									public void run() {
										if (pref_sync_force != null) {
											findPreference(
													getString(R.string.prefs_cal_force_sync_Key))
													.setEnabled(true);
										}
									}
								});
							} else {
								Editor edit = prefs.edit();
								edit.remove(getString(R.string.prefs_cal_Key));
								edit.commit();

								mHandler.post(new Runnable() {
									@Override
									public void run() {
										if (pref_sync_force != null) {
											findPreference(
													getString(R.string.prefs_cal_force_sync_Key))
													.setEnabled(false);
										}
									}
								});
							}
						}
					});
					calDialog.create().show();

					return false;
				}
			});
		}
		pref_sync_force = findPreference(getString(R.string.prefs_cal_force_sync_Key));
		if (pref_sync_force != null) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (prefs.getLong(getString(R.string.prefs_cal_Key), -1) == -1) {
						pref_sync_force.setEnabled(false);
					} else {
						pref_sync_force.setEnabled(true);
					}
				}
			});

			pref_sync_force
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {

						@Override
						public boolean onPreferenceClick(Preference preference) {
							CalendarUtils.syncTimetable(
									(TimetableApplication_) getApplication(),
									PreferenceActivity.this, prefs.getLong(
											getString(R.string.prefs_cal_Key),
											-1), new CalendarSyncListener() {

										ProgressDialog dialog;

										@Override
										public void onFinishPublishing() {
											if (dialog != null) {
												dialog.dismiss();
											}
										}

										@Override
										public void onFinishRemoving() {

										}

										@Override
										public void onStartPublishing() {

										}

										@Override
										public void onStartRemoving() {
											dialog = ProgressDialog
													.show(PreferenceActivity.this,
															getString(R.string.dialog_sync_title),
															getString(R.string.dialog_sync_msg),
															true);
										}
									});

							return false;
						}
					});
		}
		Preference pref_export = findPreference(getString(R.string.prefs_cal_export_Key));
		if (pref_export != null) {
			pref_export
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {

						@Override
						public boolean onPreferenceClick(Preference preference) {
							CalendarUtils.exportICSCalendar(
									(TimetableApplication_) getApplication(),
									PreferenceActivity.this,
									new CalendarExportListener() {

										ProgressDialog dialog;

										@Override
										public void onFinish(String path) {
											dialog.dismiss();
											Toast.makeText(
													PreferenceActivity.this,
													path, Toast.LENGTH_LONG)
													.show();
										}

										@Override
										public void onStart() {
											dialog = ProgressDialog
													.show(PreferenceActivity.this,
															getString(R.string.dialog_export_title),
															getString(R.string.dialog_export_msg),
															true);
										}
									});
							return false;
						}
					});
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
