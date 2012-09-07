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
package de.mprengemann.hwr.timetabel.fragments;

import java.util.HashMap;

import android.os.Bundle;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.ViewById;

import de.mprengemann.hwr.timetabel.R;
import de.mprengemann.hwr.timetabel.TimetableApplication;
import de.mprengemann.hwr.timetabel.viewadapters.SubjectChooserAdapter;
import de.mprengemann.hwr.timetabel.viewadapters.SubjectChooserAdapter.OnSubjectCheckedChangeListener;

@EFragment(R.layout.fragment_subject_chooser)
public class SubjectChooserFragment extends SherlockDialogFragment {

	public interface OnSubmitListener {
		void onSubmit(HashMap<Long, Boolean> changed);
	}

	public static SubjectChooserFragment_ newInstance() {
		SubjectChooserFragment_ f = new SubjectChooserFragment_();

		Bundle b = new Bundle();
		f.setArguments(b);

		return f;
	}

	private OnSubmitListener listener;

	private HashMap<Long, Boolean> changed = new HashMap<Long, Boolean>();
	@App
	TimetableApplication application;

	@ViewById(R.id.list_subject_chooser)
	ListView list;

	private SubjectChooserAdapter mAdapter;

	@AfterViews
	void initViews() {
		getDialog().setTitle(R.string.text_subject_chooser_title);
		mAdapter = new SubjectChooserAdapter(getActivity());
		mAdapter.setItems(application.getSubjects());
		mAdapter.setOnSubjectCheckedChangeListener(new OnSubjectCheckedChangeListener() {

			@Override
			public void onCheckedChange(Long id, Boolean isChecked) {
				if (changed.containsKey(id)) {
					changed.remove(id);
				} else {
					changed.put(id, isChecked);
				}
			}

		});
		list.setAdapter(mAdapter);
	}

	@Click(R.id.btn_subject_chooser_ok)
	void okClicked() {
		if (listener != null) {
			listener.onSubmit(changed);
		}
		dismiss();
	}

	public void setOnSubmitListener(OnSubmitListener listener) {
		this.listener = listener;
	}

}
