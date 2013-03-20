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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.googlecode.androidannotations.annotations.*;
import de.mprengemann.hwr.timetabel.Events;
import de.mprengemann.hwr.timetabel.R;
import de.mprengemann.hwr.timetabel.TimetableActivity;
import de.mprengemann.hwr.timetabel.TimetableApplication;
import de.mprengemann.hwr.timetabel.TimetableApplication.OnTimetableDataListener;
import de.mprengemann.hwr.timetabel.viewadapters.SubjectsAdapter;
import de.mprengemann.hwr.timetabel.viewadapters.SubjectsAdapter.OnDataChangeListener;

import java.util.List;

@EFragment(R.layout.fragment_subject_list)
public class SubjectListFragment extends SherlockFragment {

  private static final String TAG = "SubjectListFragment";
  @ViewById(R.id.list_subjects)
  ListView listView;
  @ViewById(R.id.text_subjects_empty)
  TextView empty;
  @App
  TimetableApplication application;
  private SubjectsAdapter mAdapter;
  private OnItemClickListener onItemClickListener;
  private MenuItem menuItemToShow;
  private ActionMode actionMode;
  private TimetableActionMode mTimetableActionModeCallback;

  public static SubjectListFragment_ newInstance() {
    SubjectListFragment_ f = new SubjectListFragment_();
    return f;
  }

  public void addItem(Events e) {
    if (mAdapter != null) {
      mAdapter.addItem(e);
    }
  }

  public void addSeparatorItem(String str) {
    if (mAdapter != null) {
      mAdapter.addSeparatorItem(str);
    }
  }

  public void clear() {
    if (mAdapter != null) {
      mAdapter.clear();
    }
  }

  public void fillList() {
    mAdapter.setItems(application.getEvents());
  }

  @AfterViews
  public void initViews() {
    setHasOptionsMenu(true);
    listView.setFastScrollEnabled(true);

    mAdapter = new SubjectsAdapter(getActivity(), application);
    mAdapter.setOnDataChangeListener(new OnDataChangeListener() {

      @Override
      public void onDataChange(int size, final String text) {
        if (size == 0) {
          empty.setVisibility(View.VISIBLE);
          empty.post(new Runnable() {

            @Override
            public void run() {
              empty.setText(text);
            }

          });
        } else {
          empty.setVisibility(View.GONE);
        }
      }

    });

    application.setOnTimetableDataListener(new OnTimetableDataListener() {

      @Override
      public void onLoadingFinished(final List<Events> result) {
        Log.i(TAG, "End!!");

        mAdapter.setLoading(false);
        mAdapter.setItems(result);
        if (menuItemToShow != null) {
          menuItemToShow.setVisible(true);
        }

        getSherlockActivity()
            .setSupportProgressBarIndeterminateVisibility(false);
      }

      @Override
      public void onLoadingStarted() {
        Log.i(TAG, "Start!!");

        mAdapter.setLoading(true);

        if (getSherlockActivity() != null) {
          getSherlockActivity()
              .setSupportProgressBarIndeterminateVisibility(true);
          getSherlockActivity().setSupportProgress(
              Window.PROGRESS_END);
        }
      }

    });

    listView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
    listView.setAdapter(mAdapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view,
                              int position, long id) {
        if (mAdapter.getItem(position) != null) {
          if (actionMode != null) {
            mTimetableActionModeCallback.setSelected(mAdapter.getItem(position));
            mAdapter.setSelection(mAdapter.getItem(position));
            return;
          }
        }

        if (onItemClickListener != null) {
          if (mAdapter.getItem(position) != null) {
            onItemClickListener.onClick(view, mAdapter.getItem(position)
                .getSubjectId(), mAdapter.getItem(position)
                .getId());
          }

        }
      }
    });
    listView.setOnItemLongClickListener(new OnItemLongClickListener() {

      @Override
      public boolean onItemLongClick(AdapterView<?> parent, View view,
                                     int position, long id) {
        if (mAdapter.getItem(position) != null) {
          if (actionMode != null) {
            mAdapter.setSelection(mAdapter.getItem(position));
            return true;
          }
          mTimetableActionModeCallback = new TimetableActionMode();
          mTimetableActionModeCallback.setSelected(mAdapter.getItem(position));
          actionMode = getSherlockActivity().startActionMode(mTimetableActionModeCallback);
          listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
          mAdapter.setSelection(mAdapter.getItem(position));
        }

        if (onItemClickListener != null) {
          if (mAdapter.getItem(position) != null) {
            return onItemClickListener.onLongClick(view, mAdapter
                .getItem(position).getSubjectId(), mAdapter
                .getItem(position).getId());
          }
        }
        return false;
      }

    });

    fillList();
  }

  public void scrollTo(String item) {
    if (mAdapter != null) {
      if (listView != null) {
        listView.setSelection(mAdapter.getSeparatorPosition(item));
      }
    }
  }

  public void setInitalState() {
    clear();
    empty.setVisibility(View.VISIBLE);
    empty.setText(R.string.text_no_subjects);
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    this.onItemClickListener = listener;
  }

  public void showAfter(final MenuItem item) {
    this.menuItemToShow = item;
  }

  @UiThread
  private void showEditDialog(long id) {
    FragmentTransaction ft = getFragmentManager().beginTransaction();
    Fragment prev = getFragmentManager().findFragmentByTag("dialog");
    if (prev != null) {
      ft.remove(prev);
    }
    ft.addToBackStack(null);

    EventEditDialogFragment newFragment = EventEditDialogFragment.newInstance(id);
    newFragment.show(ft, "dialog");
  }

  public interface OnItemClickListener {
    public void onClick(View v, long subject_id, long event_id);

    public boolean onLongClick(View v, long subject_id, long event_id);
  }

  private class TimetableActionMode implements ActionMode.Callback {
    private Events event;

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
      getSherlockActivity().getSupportMenuInflater().inflate(R.menu.action_mode, menu);
      return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
      return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
      switch (item.getItemId()) {
        case R.id.action_mode_edit:
          showEditDialog(event.getId());
          break;
        case R.id.action_mode_delete:
          application.deleteEvent(event);
          if (getActivity() instanceof TimetableActivity) {
            ((TimetableActivity) getActivity()).updateList();
          }
          break;
      }
      mode.finish();
      return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
      actionMode = null;
      listView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
      mAdapter.setSelection(null);
    }

    public void setSelected(Events event) {
      this.event = event;
    }
  }
}