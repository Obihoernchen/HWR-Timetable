package de.mprengemann.hwr.timetabel.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.PropertyList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import de.mprengemann.hwr.timetabel.Events;
import de.mprengemann.hwr.timetabel.R;
import de.mprengemann.hwr.timetabel.Subjects;
import de.mprengemann.hwr.timetabel.icsparser.IcsParser;
import de.mprengemann.hwr.timetabel.icsparser.IcsParser.OnCalendarParsingListener;

public class Parser extends AsyncTask<Void, Void, Void> {

	public interface OnLoadingListener {
		void onLoadingStarted(String msg);
		void onError(OnLoadingListener listener);
		void onLoadingFinished(boolean hasError);
		void onNewItem(Subjects s, Events evt);
	}

	private static final String TAG = "Parser";
	public OnLoadingListener listener;

	private Context context;
	private boolean connectionAvail = true;
	private ProgressDialog mLoadingDialog;
	private SharedPreferences preferences;

	public Parser(Context context, OnLoadingListener listener) {
		this.context = context;
		this.preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		this.listener = listener;
	}

	protected void onPreExecute() {
		listener.onLoadingStarted("");
	}

	protected Void doInBackground(Void... params) {
		if (Utils.connectionChecker(context)) {
			try {

				String matrikelnr = preferences.getString(
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

				URL url;
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
										s.setShortTitle(s.getTitle().substring(s.getTitle().indexOf("-") + 1));
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
											Log.e(TAG,
													"Error while parsing date");
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
												s.setType(desc.replace("Art: ",
														""));
											}
										}

										listener.onNewItem(s, evt);
									}
								}

							});
						} catch (Exception e) {
							connectionAvail = false;
						}
					} else {
						connectionAvail = false;
					}
				} catch (MalformedURLException mue) {
					connectionAvail = false;
				} catch (IOException ioe) {
					connectionAvail = false;
				} finally {
					try {
						is.close();
					} catch (IOException ioe) {
						connectionAvail = false;
					}
				}

				httpclient.getConnectionManager().shutdown();
			} catch (Exception e) {
				connectionAvail = false;
			}
		}
		return null;
	}

	protected void onPostExecute(Void result) {
		if (!connectionAvail) {
			listener.onError(listener);
		} else {
			listener.onLoadingFinished(!connectionAvail);
		}
	}
}
