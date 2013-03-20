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
package de.mprengemann.hwr.timetabel.icsparser;

import android.util.Log;
import com.bugsense.trace.BugSenseHandler;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;

public class NewNotificationCheck {
  private static final String TAG = "NewNotificationCheck";
  private long lastChanged;

  @SuppressWarnings("deprecation")
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
      Log.i(TAG, e.toString());
      BugSenseHandler.sendException(e);
    }
  }

  public long getLastChanged() {
    return lastChanged;
  }

  public void setLastChanged(long lastChanged) {
    this.lastChanged = lastChanged;
  }
}
