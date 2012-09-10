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
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
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
import de.mprengemann.hwr.timetabel.data.Timetables;

@EActivity
public class PreferenceActivity extends SherlockPreferenceActivity {

	@SuppressWarnings("unused")
	private static final String TAG = "PreferenceActivity";

	private SharedPreferences prefs;
	private boolean isChanged = false;
	private boolean refreshHistory = false;
	private String kursSemester;
	private String kursSemesterNeu;
	private String fachrichtung;
	private String fachrichtungNeu;
	private String matrikelNr;
	private String matrikelNrNeu;
	private String showPastString;
	private String showPastStringNeu;
	
	private String tempFachrichtung;

	private Preference pref_sync_force;

	private Handler mHandler = new Handler();

	@Override
	public void finish() {
		kursSemesterNeu = prefs.getString(
				getString(R.string.prefs_semester_kurs_key), "n/a");
		fachrichtungNeu = prefs.getString(
				getString(R.string.prefs_fachrichtungKey), "n/a");		
		matrikelNrNeu = prefs.getString(
				getString(R.string.prefs_matrikelNrKey), "n/a");
		showPastStringNeu = prefs.getString("showHistory", "n/a");

		if (!kursSemesterNeu.equals(kursSemester)
				|| !fachrichtungNeu.equals(fachrichtung)
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

		kursSemester = prefs
				.getString(getString(R.string.prefs_semester_kurs_key), "n/a");
		fachrichtung = prefs.getString(
				getString(R.string.prefs_fachrichtungKey), "n/a");
		tempFachrichtung = String.valueOf(fachrichtung);
		matrikelNr = prefs.getString(getString(R.string.prefs_matrikelNrKey),
				"n/a");
		showPastString = prefs.getString(
				getString(R.string.prefs_showInPastKey),
				getString(R.string.prefs_default_showInPast));

		EditTextPreference pref_matrikel = (EditTextPreference) findPreference(getString(R.string.prefs_matrikelNrKey));
		pref_matrikel.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);

		final ListPreference pref_fachrichtung = (ListPreference) findPreference(getString(R.string.prefs_fachrichtungKey));
		final ListPreference pref_semesterKurs = (ListPreference) findPreference(getString(R.string.prefs_semester_kurs_key));

		if (!matrikelNr.equals("n/a") || !matrikelNr.equals("")) {
			pref_fachrichtung.setEnabled(true);
		}

		if (!fachrichtung.equals("n/a") || !fachrichtung.equals("")) {
			pref_semesterKurs.setEnabled(true);
			fillKursSemesterPreference(pref_semesterKurs);
		}

		pref_matrikel
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						if (!newValue.equals("")) {
							pref_fachrichtung.setEnabled(true);

							if (!fachrichtung.equals("n/a")
									|| !fachrichtung.equals("")) {
								pref_semesterKurs.setEnabled(true);
								fillKursSemesterPreference(pref_semesterKurs);
							}
						} else {
							pref_fachrichtung.setEnabled(false);
							pref_semesterKurs.setEnabled(false);
						}

						return true;
					}
				});

		pref_fachrichtung
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						
						if (!newValue.equals("")) {
							pref_semesterKurs.setEnabled(true);
							pref_semesterKurs
									.setEntries(Timetables.timetable_matrix[Integer
											.parseInt(String.valueOf(newValue))]);

							String[] values = new String[pref_semesterKurs
									.getEntries().length];
							for (int i = 0; i < values.length; i++) {
								values[i] = String.valueOf(i);
							}
							pref_semesterKurs.setEntryValues(values);
						} else {
							pref_semesterKurs.setEnabled(false);
						}
						
						if (!tempFachrichtung.equals(newValue)) {
							Editor edit = prefs.edit();
							edit.remove(getString(R.string.prefs_semester_kurs_key));
							edit.apply();
							
							pref_semesterKurs.setValue("-1");
						}
						
						tempFachrichtung = String.valueOf(newValue);

						return true;
					}

				});
		
		
		
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

	private void fillKursSemesterPreference(
			final ListPreference pref_semesterKurs) {
		pref_semesterKurs
				.setEntries(Timetables.timetable_matrix[Integer
						.parseInt(fachrichtung)]);

		String[] values = new String[pref_semesterKurs
				.getEntries().length];
		for (int i = 0; i < values.length; i++) {
			values[i] = String.valueOf(i);
		}
		pref_semesterKurs.setEntryValues(values);
	}
}
