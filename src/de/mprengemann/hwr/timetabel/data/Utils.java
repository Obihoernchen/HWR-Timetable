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
package de.mprengemann.hwr.timetabel.data;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.bugsense.trace.BugSenseHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import de.mprengemann.hwr.timetabel.R;

public class Utils {

	@SuppressWarnings("unused")
	private static final String TAG = "Utils";

	private static final int CASE_KURS = 0;
	private static final int CASE_KURS_A = 1;
	private static final int CASE_KURS_B = 2;
	private static final int CASE_KURS_C = 3;

	public static String buildURL(Context context, SharedPreferences preferences) {
		int fachrichtung = Integer.parseInt(preferences.getString(
				context.getString(R.string.prefs_fachrichtungKey), "1"));
		int semester = Integer.parseInt(preferences.getString(
				context.getString(R.string.prefs_semesterKey), "1"));
		int kurs = Integer.parseInt(preferences.getString(
				context.getString(R.string.prefs_kursKey), "1"));

		int course = 0;

		switch (kurs) {
		case CASE_KURS:
			// Versicherung
			if (fachrichtung == 14) {
				course = semester + 1;
			// Tourismus
			} else if (fachrichtung == 13) {
				if (semester > 1){
					course = semester + 1;
				} else {
					course = semester - 1;
				}
			} else {
				course = semester - 1;
			}

			break;
		case CASE_KURS_A:
			// Spedition/Logistik oder Versicherung
			if ((fachrichtung == 11) || (fachrichtung == 14)) {
				course = (semester - 1) * 2;
			} else if (fachrichtung == 13){
				course = (semester - 1) * 2 + 1;
			} else {
				course = (semester - 1) * 3;
			}
			break;
		case CASE_KURS_B:
			// Spedition/Logistik oder Versicherung
			if ((fachrichtung == 11) || (fachrichtung == 14)) {
				course = (semester - 1) * 2 + 1;
			} else if (fachrichtung == 13){
				course = (semester - 1) * 3 + 2;
			} else {
				course = (semester - 1) * 3 + 1;
			}
			break;
		case CASE_KURS_C:
			course = (semester - 1) * 3 + 2;
			break;
		}

		return "http://ipool.ba-berlin.de/stundenplaene.anzeige.php?faculty="
				+ fachrichtung + "&course=" + course + "&type=ics";
	}

	public static boolean connectionChecker(Context context) {
		ConnectivityManager connec = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean connected = false;
		try {
			connected = connec.getActiveNetworkInfo().isConnected();
		} catch (Exception e) {
			connected = false;
			BugSenseHandler.log(TAG, e);
		}

		return connected;
	}

	public static boolean shouldCheckForDate(long last) {
		Calendar calendar = GregorianCalendar.getInstance();

		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return (last < calendar.getTimeInMillis());
	}
}
