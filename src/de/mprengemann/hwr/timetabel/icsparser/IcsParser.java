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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;

public class IcsParser {

	public interface OnCalendarParsingListener {
		void onNewItem(Component c);
	}

	@SuppressWarnings("unused")
	private static final String TAG = "IcsParser";

	public IcsParser(InputStream is, OnCalendarParsingListener listener)
			throws IOException, ParserException {

		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(is);

		for (@SuppressWarnings("rawtypes")
		Iterator i = calendar.getComponents().iterator(); i.hasNext();) {
			listener.onNewItem((Component) i.next());
		}
	}

}
