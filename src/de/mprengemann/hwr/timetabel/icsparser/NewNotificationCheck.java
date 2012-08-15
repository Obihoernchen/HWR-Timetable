package de.mprengemann.hwr.timetabel.icsparser;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;

import android.util.Log;

public class NewNotificationCheck {
	private long lastChanged;

	public NewNotificationCheck(DataInputStream dis) {
		String line = null;
		String lastChangedString = null;

		try {
			while ((line = dis.readLine()) != null) {
				if (line.startsWith("DTSTAMP:")) {
					lastChangedString = line.substring(("DTSTAMP:").length());
					break;
				}
			}

			Integer year = Integer.parseInt(lastChangedString.substring(0, 4)) - 1900;
			Integer month = Integer.parseInt(lastChangedString.substring(4, 6)) - 1;
			Integer day = Integer.parseInt(lastChangedString.substring(6, 8));
			Integer hours = Integer
					.parseInt(lastChangedString.substring(9, 11));
			Integer minutes = Integer.parseInt(lastChangedString.substring(11,
					13));

			Date date = new Date(year, month, day, hours, minutes);

			this.lastChanged = date.getTime();

		} catch (IOException e) {
			Log.i("Catch", e.toString());
		}
	}

	public long getLastChanged() {
		return lastChanged;
	}

	public void setLastChanged(long lastChanged) {
		this.lastChanged = lastChanged;
	}
}
