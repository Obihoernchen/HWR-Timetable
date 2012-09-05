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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.bugsense.trace.BugSenseHandler;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.EActivity;

import de.mprengemann.hwr.timetabel.data.CalendarUtils;
import de.mprengemann.hwr.timetabel.data.CalendarUtils.CalendarSyncListener;
import de.mprengemann.hwr.timetabel.data.Parser;
import de.mprengemann.hwr.timetabel.data.Parser.OnLoadingListener;
import de.mprengemann.hwr.timetabel.data.Utils;
import de.mprengemann.hwr.timetabel.fragments.SubjectChooserFragment.OnSubmitListener;
import de.mprengemann.hwr.timetabel.fragments.SubjectChooserFragment_;
import de.mprengemann.hwr.timetabel.fragments.SubjectDetailFragment_;
import de.mprengemann.hwr.timetabel.fragments.SubjectListFragment;
import de.mprengemann.hwr.timetabel.fragments.SubjectListFragment.OnItemClickListener;

@EActivity
public class TimetableActivity extends SherlockFragmentActivity {

	private static final String TAG = "TimetableActivity";
	private static final int CONTENT_VIEW_ID = 1010101010;
	private static final int PREFERENCE_REQUEST = 1212;
	private static final int DONATE_REQUEST = 13131;
	private static final int NEW_TIMETABLEPLAN = 110011;
	private static final int ERROR_DIALOG = 110022;
	private static final int LICENSE_DIALOG = 110033;

	private SubjectListFragment subjectFragment;
	private OnLoadingListener parsingListener;

	private OnLoadingListener resultPassedListener;

	@App
	TimetableApplication application;
	private MenuItem refreshItem;
	
	private void initListNavigation() {
		Context context = getSupportActionBar().getThemedContext();
		final ArrayAdapter<CharSequence> listAdapter = new ArrayAdapter<CharSequence>(
				context, R.layout.sherlock_spinner_item,
				application.getEventsDates());

		listAdapter
				.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

		if (listAdapter.isEmpty()) {
			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_STANDARD);
		} else {
			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_LIST);
		}

		getSupportActionBar().setListNavigationCallbacks(listAdapter,
				new OnNavigationListener() {

					@Override
					public boolean onNavigationItemSelected(int itemPosition,
							long itemId) {
						subjectFragment.scrollTo(String.valueOf(listAdapter
								.getItem(itemPosition)));
						return true;
					}
				});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case PREFERENCE_REQUEST:
			if (resultCode == RESULT_OK) {
				if (data.getBooleanExtra(
						getString(R.string.intent_data_preferences_changed),
						false)) {
					getSupportActionBar().setNavigationMode(
							ActionBar.NAVIGATION_MODE_STANDARD);

					refreshItem.setVisible(false);

					Parser p = new Parser(TimetableActivity.this,
							parsingListener);
					p.execute();

					subjectFragment.clear();
				} else if (data.getBooleanExtra(
						getString(R.string.intent_data_refresh_history), false)) {
					subjectFragment.clear();
					subjectFragment.fillList();
				}
			}
			break;
		case DONATE_REQUEST:
			Toast.makeText(this, getString(R.string.dialog_donate),
					Toast.LENGTH_LONG).show();
			break;
		default:
			break;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

		BugSenseHandler.setup(this, getString(R.string.bugtracking_api));
		
		FrameLayout frame = new FrameLayout(this);
		frame.setId(CONTENT_VIEW_ID);
		setContentView(frame, new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));

		subjectFragment = SubjectListFragment.newInstance();
		subjectFragment.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onClick(long subject_id, long event_id) {
				Intent i = new Intent(TimetableActivity.this,
						SubjectDetailActivity_.class);

				i.putExtra(SubjectDetailFragment_.EXTRA_EVENT_ID, event_id);
				i.putExtra(SubjectDetailFragment_.EXTRA_SUBJECT_ID, subject_id);

				TimetableActivity.this.startActivity(i);
			}

			@Override
			public boolean onLongClick(long subject_id, long event_id) {
				return false;
			}

		});
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(CONTENT_VIEW_ID, subjectFragment).commit();

		setSupportProgressBarIndeterminateVisibility(false);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		initListNavigation();
		try {
			startUpCheck();
		} catch (Exception e) {
			Log.e(TAG, "Can't show Dialog on startup check! " + e.getMessage());
			BugSenseHandler.log(TAG, e);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {

		AlertDialog.Builder builder;

		switch (id) {
		case ERROR_DIALOG:
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.dialog_error_message);
			builder.setTitle(R.string.dialog_error_title);
			builder.setCancelable(false);
			builder.setPositiveButton(android.R.string.yes,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Parser parser = new Parser(TimetableActivity.this,
									resultPassedListener);
							parser.execute();
						}
					});
			builder.setNegativeButton(android.R.string.no,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							refreshItem.setVisible(true);
							getSupportActionBar().setNavigationMode(
									ActionBar.NAVIGATION_MODE_STANDARD);
							subjectFragment.setInitalState();
							setSupportProgressBarIndeterminateVisibility(false);

							dialog.cancel();

						}
					});
			return builder.create();
		case NEW_TIMETABLEPLAN:
			builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.dialog_new_message);
			builder.setTitle(R.string.dialog_new_title);
			builder.setCancelable(false);
			builder.setPositiveButton(android.R.string.yes,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							subjectFragment.clear();
							getSupportActionBar().setNavigationMode(
									ActionBar.NAVIGATION_MODE_STANDARD);

							refreshItem.setVisible(false);

							Parser p = new Parser(TimetableActivity.this,
									parsingListener);
							p.execute();
						}
					});
			builder.setNegativeButton(android.R.string.no,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							SharedPreferences prefs = PreferenceManager
									.getDefaultSharedPreferences(TimetableActivity.this);

							Calendar calendar = Calendar.getInstance();

							Editor edit = prefs.edit();
							edit.putLong(
									getString(R.string.prefs_lastUpdated_user),
									calendar.getTimeInMillis());

							calendar.set(Calendar.HOUR_OF_DAY, 0);
							calendar.set(Calendar.MINUTE, 0);
							calendar.set(Calendar.SECOND, 0);
							calendar.set(Calendar.MILLISECOND, 0);

							edit.putLong(getString(R.string.prefs_lastUpdated),
									calendar.getTimeInMillis());
							edit.commit();

							dialog.cancel();
						}
					});
			return builder.create();
		case LICENSE_DIALOG:
			builder = new AlertDialog.Builder(this);

			TextView textView = new TextView(this);
			textView.setMovementMethod(LinkMovementMethod.getInstance());
			textView.setText(R.string.text_license);
			textView.setLinksClickable(true);
			textView.setPadding(10, 10, 10, 10);

			builder.setView(textView);
			builder.setTitle(R.string.menu_license);
			builder.setCancelable(true);
			builder.setNeutralButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			return builder.create();
		default:
			return super.onCreateDialog(id);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getSupportMenuInflater().inflate(R.menu.menu, menu);
		refreshItem = menu.findItem(R.id.menu_refresh);

		return super.onCreateOptionsMenu(menu);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item != null) {
			Intent i;

			switch (item.getItemId()) {
			case R.id.menu_refresh:
				Parser p = new Parser(TimetableActivity.this, parsingListener);
				p.execute();

				getSupportActionBar().setNavigationMode(
						ActionBar.NAVIGATION_MODE_STANDARD);
				item.setVisible(false);

				subjectFragment.clear();
				subjectFragment.showAfter(item);
				break;
			case R.id.menu_selection:
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				SubjectChooserFragment_ chooserFragment = SubjectChooserFragment_
						.newInstance();
				chooserFragment.setOnSubmitListener(new OnSubmitListener() {

					@Override
					public void onSubmit(HashMap<Long, Boolean> changed) {
						Subjects s;

						for (Long id : changed.keySet()) {
							s = application.getSubjectById(id);
							s.setShow(changed.get(id));
							s.update();
						}

						subjectFragment.clear();
						subjectFragment.fillList();
						initListNavigation();
					}

				});

				chooserFragment.show(ft, "dialog");
				break;
			case R.id.menu_settings:
				i = new Intent(TimetableActivity.this,
						PreferenceActivity_.class);
				startActivityForResult(i, PREFERENCE_REQUEST);
				break;
			case R.id.menu_feedback:
				sendFeedBack();
				break;
			case R.id.menu_license:
				showDialog(LICENSE_DIALOG);
				break;
			case R.id.menu_donate:
				i = new Intent(
						Intent.ACTION_VIEW,
						Uri.parse("https://sites.google.com/site/hwrberlinstundenplanapp/spenden"));
				startActivityForResult(i, DONATE_REQUEST);

				break;
			default:
				return super.onOptionsItemSelected(item);
			}

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void sendFeedBack() {
		final Intent emailIntent = new Intent(
				android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				new String[] { "marcprengemann@web.de" });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				"[Feedback]HWR Berlin Stundenplanapp");

		String myBodyText = "Vorname: \r\n" + "Nachname: \r\n"
				+ "Mailadresse: \r\n" + "Gerät: \r\n" + "Android-Version: \r\n"
				+ "Fachkombination: \r\n\r\n"
				+ "Bewertung: X von 5 Sternen \r\n" + "Bug: \r\n"
				+ "Verbesserungsvorschläge: \r\n\r\n"
				+ "Vielen Dank für dein Feedback!\r\n" + "Marc Prengemann";

		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, myBodyText);
		startActivity(Intent.createChooser(emailIntent, "e-Mail senden ..."));
	}

	@SuppressWarnings("deprecation")
	private void startUpCheck() {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		parsingListener = new OnLoadingListener() {

			ProgressDialog dialog;
			List<Subjects> visibleSubjects = new ArrayList<Subjects>();
			List<Subjects> allSubjects = new ArrayList<Subjects>();

			@Override
			public void onError(final OnLoadingListener listener) {
				resultPassedListener = listener;
				dialog.dismiss();
				showDialog(ERROR_DIALOG);
			}

			@Override
			public void onLoadingFinished(boolean hasError) {
				if (!hasError) {
					Calendar calendar = GregorianCalendar.getInstance();

					Editor edit = prefs.edit();
					edit.putLong(getString(R.string.prefs_lastUpdated_user),
							calendar.getTimeInMillis());

					calendar.set(Calendar.HOUR_OF_DAY, 0);
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 0);

					edit.putLong(getString(R.string.prefs_lastUpdated),
							calendar.getTimeInMillis());
					edit.commit();

					initListNavigation();

					if (prefs.getBoolean(
							getString(R.string.prefs_cal_sync_Key), false)) {
						if (prefs
								.getLong(getString(R.string.prefs_cal_Key), -1) > -1) {
							CalendarUtils.syncTimetable(
									(TimetableApplication_) getApplication(),
									TimetableActivity.this, prefs.getLong(
											getString(R.string.prefs_cal_Key),
											-1), new CalendarSyncListener() {

										@Override
										public void onFinishPublishing() {
											dialog.dismiss();
										}

										@Override
										public void onFinishRemoving() {

										}

										@Override
										public void onStartPublishing() {

										}

										@Override
										public void onStartRemoving() {
										}
									});
						} else {
							dialog.dismiss();
						}
					} else {
						dialog.dismiss();
					}
				} else {
					dialog.dismiss();
				}

				refreshItem.setVisible(true);
				application.onLoadingFinished();
			}

			@Override
			public void onLoadingStarted(String msg) {
				dialog = ProgressDialog.show(TimetableActivity.this,
						getString(R.string.dialog_sync_title),
						getString(R.string.dialog_sync_msg), true, false);

				visibleSubjects = application.getVisibleSubjects();
				allSubjects = application.getSubjects();

				application.removeAllData();
				application.onLoadingStarted();
			}

			@Override
			public void onNewItem(Subjects s, Events e) {
				if (!visibleSubjects.contains(s) && !allSubjects.contains(s)) {
					s.setShow(true);
				} else if (visibleSubjects.contains(s)) {
					s.setShow(true);
				} else {
					s.setShow(false);
				}

				application.onNewItem(s, e);
			}

		};

		if (Utils.shouldCheckForDate(prefs.getLong(
				getString(R.string.prefs_lastUpdated), 0))) {
			if (prefs.getString(getString(R.string.prefs_matrikelNrKey), null) == null) {
				Intent i = new Intent(TimetableActivity.this,
						PreferenceActivity_.class);
				startActivityForResult(i, PREFERENCE_REQUEST);
			} else {
				if (prefs.getBoolean(getString(R.string.prefs_notifyKey), true)) {
					showDialog(NEW_TIMETABLEPLAN);
				}
			}
		}
	}
}