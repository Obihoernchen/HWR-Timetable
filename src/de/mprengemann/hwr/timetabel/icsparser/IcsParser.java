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

	private static final String TAG = "IcsParser";

	public IcsParser(InputStream is, OnCalendarParsingListener listener)
			throws IOException, ParserException {

		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(is);

		for (Iterator i = calendar.getComponents().iterator(); i.hasNext();) {
			listener.onNewItem((Component) i.next());
		}
	}

}
