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
	
	public interface OnSubmitListener{
		void onSubmit(HashMap<Long, Boolean> changed);
	}
	private OnSubmitListener listener;
	private HashMap<Long, Boolean> changed = new HashMap<Long, Boolean>();
		
	@App
	TimetableApplication application;
	@ViewById(R.id.list_subject_chooser)
	ListView list;	
	
	private SubjectChooserAdapter mAdapter;
	
	public static SubjectChooserFragment_ newInstance(){
		SubjectChooserFragment_ f = new SubjectChooserFragment_();

		Bundle b = new Bundle();
		f.setArguments(b);

		return f;	
	}
		
	@AfterViews
	void initViews(){	
		getDialog().setTitle(R.string.text_subject_chooser_title);				
		mAdapter = new SubjectChooserAdapter(getActivity());		
		mAdapter.setItems(application.getSubjects());		
		mAdapter.setOnSubjectCheckedChangeListener(new OnSubjectCheckedChangeListener() {
			
			@Override
			public void onCheckedChange(Long id, Boolean isChecked) {
				if (changed.containsKey(id)){
					changed.remove(id);
				}else{
					changed.put(id, isChecked);
				}
			}
			
		});
		list.setAdapter(mAdapter);
	}	
		
	@Click(R.id.btn_subject_chooser_ok)
	void okClicked(){
		if (listener != null){
			listener.onSubmit(changed);
		}
		dismiss();
	}
	
	public void setOnSubmitListener(OnSubmitListener listener){
		this.listener = listener;
	}
	
}
