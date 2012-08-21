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

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.googlecode.androidannotations.annotations.EActivity;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;

import de.mprengemann.hwr.timetabel.fragments.SubjectDetailFragment_;
import de.mprengemann.hwr.timetabel.fragments.viewpager.SubjectDetailFragmentAdapter;

@EActivity
public class SubjectDetailActivity extends SherlockFragmentActivity {

	@SuppressWarnings("unused")
	private static final String TAG = "SubjectDetailActivity";
	
	private TimetableApplication application;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		application = (TimetableApplication) getApplication();

		long evt_id = 0;
		long sub_id = 0;
		Bundle b;

		if ((b = getIntent().getExtras()) != null) {
			evt_id = b.getLong(SubjectDetailFragment_.EXTRA_EVENT_ID, -1);
			sub_id = b.getLong(SubjectDetailFragment_.EXTRA_SUBJECT_ID, -1);
		}

		setContentView(R.layout.fragment_subject_detail_pager);

		getSupportActionBar().setTitle(getString(R.string.menu_detail));

		SubjectDetailFragmentAdapter mAdapter = new SubjectDetailFragmentAdapter(
				(TimetableApplication) getApplication(), sub_id, evt_id,
				getSupportFragmentManager());

		ViewPager mPager = (ViewPager) findViewById(R.id.pager_subject);
		mPager.setAdapter(mAdapter);

		TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator_subject);
		indicator.setViewPager(mPager);
		indicator.setFooterIndicatorStyle(IndicatorStyle.None);

		indicator.setCurrentItem(application.getSubjectPosition(sub_id));

		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}
}
