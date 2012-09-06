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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.PropertyList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

import de.mprengemann.hwr.timetabel.Events;
import de.mprengemann.hwr.timetabel.R;
import de.mprengemann.hwr.timetabel.Subjects;
import de.mprengemann.hwr.timetabel.exceptions.ConnectionException;
import de.mprengemann.hwr.timetabel.exceptions.ConnectionTimeoutException;
import de.mprengemann.hwr.timetabel.exceptions.IPoolFormatException;
import de.mprengemann.hwr.timetabel.exceptions.StorageException;
import de.mprengemann.hwr.timetabel.exceptions.TimetableException;
import de.mprengemann.hwr.timetabel.exceptions.UnknownTimetableException;
import de.mprengemann.hwr.timetabel.icsparser.IcsParser;
import de.mprengemann.hwr.timetabel.icsparser.IcsParser.OnCalendarParsingListener;

public class Parser extends AsyncTask<Void, Void, Void> {

	private static final String META_URL = "metadata_url";
	private static final String META_EVENT = "metadata_event";
	private static final String META_SUBJECT = "metadata_subject";
	private static final String META_KURS = "metadata_kurs";
	private static final String META_SEMESTER = "metadata_semester";
	private static final String META_FACHRICHTUNG = "metadata_fachrichtung";

	public interface OnLoadingListener {
		void onLoadingFinished(TimetableException e);

		void onLoadingStarted(String msg);

		void onNewItem(Subjects s, Events evt);
	}

	private static final String TAG = "Parser";
	public OnLoadingListener listener;

	private Context context;
	private TimetableException exception = null;

	private SharedPreferences preferences;

	public Parser(Context context, OnLoadingListener listener) {
		this.context = context;
		this.preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		this.listener = listener;
		this.exception = null;
	}

	protected Void doInBackground(Void... params) {
		this.exception = null;

		if (Utils.connectionChecker(context)) {
			try {
				final String matrikelnr = preferences.getString(
						context.getString(R.string.prefs_matrikelNrKey), "");

				DefaultHttpClient httpclient = new DefaultHttpClient();
				if (preferences.getBoolean(
						context.getString(R.string.prefs_proxyFlagKey), false)) {
					HttpHost proxy = new HttpHost(preferences.getString(
							context.getString(R.string.prefs_proxyKey),
							"localhost"), Integer.parseInt(preferences
							.getString(context
									.getString(R.string.prefs_proxyPortKey),
									"8080")));
					httpclient.getParams().setParameter(
							ConnRoutePNames.DEFAULT_PROXY, proxy);
				}

				HttpGet httpget = new HttpGet(Utils.buildURL(context,
						preferences));

				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();

				if (entity != null) {
					entity.consumeContent();
				}

				List<Cookie> cookies = httpclient.getCookieStore().getCookies();

				HttpPost httpost = new HttpPost(
						"http://ipool.ba-berlin.de/main.php?action=login");

				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				nvps.add(new BasicNameValuePair("FORM_LOGIN_NAME", "student"));
				nvps.add(new BasicNameValuePair("FORM_LOGIN_PASS", matrikelnr));
				nvps.add(new BasicNameValuePair("FORM_LOGIN_PAGE", "home"));
				nvps.add(new BasicNameValuePair("FORM_LOGIN_REDIRECTION", Utils
						.buildURL(context, preferences)));
				nvps.add(new BasicNameValuePair("FORM_ACCEPT", "1"));
				nvps.add(new BasicNameValuePair("LOGIN", "login"));

				httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

				response = httpclient.execute(httpost);
				entity = response.getEntity();

				if (entity != null) {
					entity.consumeContent();
				}

				cookies = httpclient.getCookieStore().getCookies();

				final URL url;
				InputStream is = null;
				try {
					url = new URL(Utils.buildURL(context, preferences));
					URLConnection c = url.openConnection();

					c.setRequestProperty("Cookie", cookies.get(0).getName()
							+ "=" + cookies.get(0).getValue());
					c.connect();
					is = c.getInputStream();

					if (is != null) {
						try {
							new IcsParser(is, new OnCalendarParsingListener() {

								@Override
								public void onNewItem(Component c) {
									if (c.getName().equals(Component.VEVENT)) {
										PropertyList p = c.getProperties();

										Subjects s = new Subjects();
										s.setTitle(p.getProperty("SUMMARY")
												.getValue());
										s.setShortTitle(s.getTitle().substring(
												s.getTitle().indexOf("-") + 1));
										s.setShow(true);

										SimpleDateFormat df = new SimpleDateFormat(
												"yyyyMMdd HHmmss");

										Events evt = new Events();
										try {
											evt.setStart(df.parse(p
													.getProperty("DTSTART")
													.getValue()
													.replace("T", " ")));
											evt.setEnd(df.parse(p
													.getProperty("DTEND")
													.getValue()
													.replace("T", " ")));
										} catch (ParseException e) {
											exception = new IPoolFormatException(
													context.getString(R.string.dialog_error_message_ipool));
											sendBugReport(e, url.toString());
										}

										evt.setRoom(p.getProperty("LOCATION")
												.getValue());
										evt.setUid(p.getProperty("UID")
												.getValue());

										String description = p.getProperty(
												"DESCRIPTION").getValue();
										for (String desc : description
												.split("\\n")) {
											if (desc.startsWith("Dozent: ")) {
												evt.setLecturer(desc.replace(
														"Dozent: ", ""));
												break;
											} else if (desc.startsWith("Art: ")) {
												evt.setType(desc.replace(
														"Art: ", ""));
											}
										}
										try {
											listener.onNewItem(s, evt);
										} catch (SQLiteConstraintException e) {
											exception = new StorageException(
													context.getString(R.string.dialog_error_message_storage));
											sendBugReport(e, url.toString(),
													s.toString(),
													evt.toString());
										}

									}
								}
							});
						} catch (ParserException e) {
							exception = new UnknownTimetableException(
									context.getString(R.string.dialog_error_message_timetable));
							sendBugReport(e);
						} catch (IOException e) {
							exception = new ConnectionException(
									context.getString(R.string.dialog_error_message));
							sendBugReport(e);
						}
					} else {
						throw new IOException();
					}
				} catch (IOException e) {
					if (e instanceof ConnectTimeoutException) {
						exception = new ConnectionTimeoutException(
								context.getString(R.string.dialog_error_message_timeout));
					} else if (e instanceof MalformedURLException) {
						exception = new ConnectionException(
								context.getString(R.string.dialog_error_message));
					} else {
						exception = new ConnectionException(
								context.getString(R.string.dialog_error_message));
						sendBugReport(e);
					}
				} finally {
					try {
						if (is != null) {
							is.close();
						}
					} catch (IOException e) {
						if (e instanceof ConnectTimeoutException) {
							exception = new ConnectionTimeoutException(
									context.getString(R.string.dialog_error_message_timeout));
						} else {
							exception = new ConnectionException(
									context.getString(R.string.dialog_error_message));
							sendBugReport(e);
						}

					}
				}

				httpclient.getConnectionManager().shutdown();
			} catch (Exception e) {
				if (e instanceof ConnectTimeoutException) {
					exception = new ConnectionTimeoutException(
							context.getString(R.string.dialog_error_message_timeout));
				} else {
					exception = new ConnectionException(
							context.getString(R.string.dialog_error_message));
					sendBugReport(e);
				}

			}
		} else {
			exception = new ConnectionException(
					context.getString(R.string.dialog_error_message));
		}

		return null;
	}

	protected void onPostExecute(Void result) {
		listener.onLoadingFinished(exception);
	}

	protected void onPreExecute() {
		exception = null;
		listener.onLoadingStarted("");
	}

	private void sendBugReport(Exception e, Map<String, String> extraData) {
		final String fachrichtung = preferences.getString(
				context.getString(R.string.prefs_fachrichtungKey), "1");
		final String semester = preferences.getString(
				context.getString(R.string.prefs_semesterKey), "1");
		final String kurs = preferences.getString(
				context.getString(R.string.prefs_kursKey), "1");

		extraData.put(META_FACHRICHTUNG, fachrichtung);
		extraData.put(META_SEMESTER, semester);
		extraData.put(META_KURS, kurs);

		BugSenseHandler.log(TAG, extraData, e);
	}

	private void sendBugReport(Exception e) {
		sendBugReport(e, new HashMap<String, String>());
	}

	private void sendBugReport(Exception e, String url, String subject,
			String event) {

		Map<String, String> extraData = new HashMap<String, String>();
		extraData.put(META_URL, url);
		extraData.put(META_SUBJECT, subject);
		extraData.put(META_EVENT, event);

		sendBugReport(e, extraData);
	}

	private void sendBugReport(Exception e, String url) {
		Map<String, String> extraData = new HashMap<String, String>();
		extraData.put(META_URL, url);

		sendBugReport(e, extraData);
	}
}
