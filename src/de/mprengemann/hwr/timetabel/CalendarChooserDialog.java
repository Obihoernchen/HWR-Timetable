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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import de.mprengemann.hwr.timetabel.data.CalendarUtils;
import de.mprengemann.hwr.timetabel.data.CalendarUtils.CalendarFetcherListener;
import de.mprengemann.hwr.timetabel.data.GoogleCalendar;
import de.mprengemann.hwr.timetabel.viewadapters.CalendarChooserAdapter;
import de.mprengemann.hwr.timetabel.viewadapters.CalendarChooserAdapter.OnCalendarSelectedListener;

public class CalendarChooserDialog extends AlertDialog.Builder {

	public interface OnSelectionListener {
		void onSelect(GoogleCalendar cal);
	}

	private static final String TAG = "CalendarChooserDialog";

	private OnSelectionListener listener;

	private ListView list;
	private Handler mHandler = new Handler();

	private GoogleCalendar selected;
	private CalendarChooserAdapter mAdapter;

	private Context context;

	public CalendarChooserDialog(Context c) {
		this(c, -1);
	}

	public CalendarChooserDialog(Context c, long sel_id) {
		super(c);
		this.context = c;

		Log.i(TAG, "Selected_id: " + sel_id);

		initViews();

		if (sel_id > -1) {
			CalendarUtils.getGoogleCalendar(c, sel_id,
					new CalendarFetcherListener() {

						@Override
						public void newCalendar(final GoogleCalendar cal) {
							selected = cal;

							mHandler.post(new Runnable() {

								@Override
								public void run() {
									mAdapter.setSelection(cal);
								}

							});
						}
					});
		}
	}

	void initViews() {
		setTitle(R.string.text_calendar_chooser_title);
		View v = LayoutInflater.from(context).inflate(
				R.layout.dialog_calendar_chooser, null);
		list = (ListView) v.findViewById(R.id.list_calendar_chooser);
		mAdapter = new CalendarChooserAdapter(context);
		list.setAdapter(mAdapter);

		setView(v);
		setPositiveButton(android.R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (listener != null) {
					listener.onSelect(selected);
				}
				dialog.dismiss();
			}
		});
		setNegativeButton(android.R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				selected = null;
				dialog.dismiss();
			}
		});

		CalendarUtils.getGoogleCalendars(context,
				new CalendarFetcherListener() {

					@Override
					public void newCalendar(final GoogleCalendar cal) {
						mHandler.post(new Runnable() {

							@Override
							public void run() {
								mAdapter.addItem(cal);
							}
						});
					}
				});

		mAdapter.setOnCalendarSelectedListener(new OnCalendarSelectedListener() {

			@Override
			public void onCalendarSelected(GoogleCalendar cal) {
				selected = cal;
			}

		});
	}

	public void setOnSelectionListener(OnSelectionListener listener) {
		this.listener = listener;
	}

}
