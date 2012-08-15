package de.mprengemann.hwr.timetabel.data;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import de.mprengemann.hwr.timetabel.R;

public class Utils {

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
			if (fachrichtung == 14) {
				course = semester + 1;
			} else {
				course = semester - 1;
			}
			break;
		case CASE_KURS_A:
			// Spedition/Logistik oder Versicherung
			if ((fachrichtung == 11) || (fachrichtung == 14)) {
				course = (semester - 1) * 2;
			} else {
				course = (semester - 1) * 3;
			}
			break;
		case CASE_KURS_B:
			if ((fachrichtung == 11) || (fachrichtung == 14)) {
				course = (semester - 1) * 2 + 1;
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

	public static void createNotification(final Context context,
			OnClickListener listener) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("Es ist ein neuer Stundenplan verf�gbar.\r\nWollen Sie jetzt aktualisieren?");
		builder.setTitle("Neuer Stundenplan");
		builder.setCancelable(false);
		builder.setPositiveButton("Ja, sofort", listener);
		builder.setNegativeButton("Nein, sp�ter",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(context, Notification.class);
						PendingIntent activity = PendingIntent.getActivity(
								context, 0, intent, 0);

						NotificationManager notificationManager = (NotificationManager) context
								.getSystemService(Context.NOTIFICATION_SERVICE);
						Notification.Builder notification = new Notification.Builder(
								context);
						notification.setSmallIcon(R.drawable.ic_launcher);
						notification
								.setContentTitle("Neuer Stundenplan vorhanden..");
						notification.setWhen(System.currentTimeMillis());
						notification.setAutoCancel(true);
						notification.setContentText("Bitte synchronisieren!");
						notification
								.setContentInfo("Es ist ein neuer Stundenplan vorhanden. Bitte synchronisieren Sie!");
						notification.setContentIntent(activity);

						notificationManager.notify(0,
								notification.getNotification());
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public static boolean connectionChecker(Context context) {
		ConnectivityManager connec = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean connected = false;
		try {
			connected = connec.getActiveNetworkInfo().isConnected();
		} catch (Exception e) {
			connected = false;
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
