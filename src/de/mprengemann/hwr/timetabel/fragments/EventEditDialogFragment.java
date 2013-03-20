package de.mprengemann.hwr.timetabel.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import com.actionbarsherlock.app.SherlockDialogFragment;
import de.mprengemann.hwr.timetabel.Events;
import de.mprengemann.hwr.timetabel.R;
import de.mprengemann.hwr.timetabel.TimetableActivity;
import de.mprengemann.hwr.timetabel.TimetableApplication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * User: marcprengemann
 * Date: 18.03.13
 * Time: 16:15
 */
public class EventEditDialogFragment extends SherlockDialogFragment {

  private static final String KEY_EDIT_DIALOG_ID = "keyEditDialogId";
  private EditText typeEdit;
  private EditText lecturerEdit;
  private EditText roomEdit;
  private EditText dateEdit;
  private EditText startEdit;
  private EditText endEdit;
  private Events event;
  private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
  private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.GERMANY);
  private DialogInterface.OnDismissListener onDialogDismissListener;

  public static EventEditDialogFragment newInstance(long id) {
    Bundle b = new Bundle();
    b.putLong(KEY_EDIT_DIALOG_ID, id);

    EventEditDialogFragment f = new EventEditDialogFragment();
    f.setArguments(b);

    return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      long id = getArguments().getLong(KEY_EDIT_DIALOG_ID);
      event = ((TimetableApplication) getActivity().getApplication()).getEventById(id);
    }
  }

  private void initViews(View v) {
    typeEdit = (EditText) v.findViewById(R.id.text_edit_type);
    lecturerEdit = (EditText) v.findViewById(R.id.text_edit_lecturer);
    roomEdit = (EditText) v.findViewById(R.id.text_edit_room);
    dateEdit = (EditText) v.findViewById(R.id.text_edit_date);
    startEdit = (EditText) v.findViewById(R.id.text_edit_start);
    endEdit = (EditText) v.findViewById(R.id.text_edit_end);

    typeEdit.setText(event.getType());
    lecturerEdit.setText(event.getLecturer());
    roomEdit.setText(event.getRoom());
    dateEdit.setText(dateFormat.format(event.getEnd()));
    startEdit.setText(timeFormat.format(event.getStart()));
    endEdit.setText(timeFormat.format(event.getEnd()));

    dateEdit.setFocusable(false);
    dateEdit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        DatePickerFragment newFragment = (DatePickerFragment) DatePickerFragment.newInstance(event.getEnd().getTime());
        newFragment.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
          @Override
          public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar c = Calendar.getInstance(Locale.GERMANY);
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            dateEdit.setText(dateFormat.format(c.getTime()));

          }
        });
        newFragment.show(getSherlockActivity().getSupportFragmentManager(), "timePicker");
      }
    });
    startEdit.setFocusable(false);
    startEdit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final long dateDiff = event.getEnd().getTime() - event.getStart().getTime();

        TimePickerFragment newFragment = (TimePickerFragment) TimePickerFragment.newInstance(event.getStart().getTime());
        newFragment.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
          @Override
          public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar c = Calendar.getInstance(Locale.GERMANY);
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);

            startEdit.setText(timeFormat.format(c.getTime()));
            endEdit.setText(timeFormat.format(new Date(c.getTimeInMillis() + dateDiff)));
          }
        });
        newFragment.show(getSherlockActivity().getSupportFragmentManager(), "startPicker");
      }
    });
    endEdit.setFocusable(false);
    endEdit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        TimePickerFragment newFragment = (TimePickerFragment) TimePickerFragment.newInstance(event.getEnd().getTime());
        newFragment.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
          @Override
          public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar c = Calendar.getInstance(Locale.GERMANY);
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);

            Date start;
            Date end;
            try {
              end = c.getTime();
              start = timeFormat.parse(startEdit.getText().toString());
              if (start.after(end)) {
                end = start;
              }
              endEdit.setText(timeFormat.format(end));
            } catch (ParseException e) {
            }
          }
        });
        newFragment.show(getSherlockActivity().getSupportFragmentManager(), "endPicker");
      }
    });
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    View layout = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_dialog_edit, null, false);
    if (event != null) {
      initViews(layout);
    }
    builder.setTitle(event == null ? getString(R.string.dialog_event_change_title) : event.getSubjects().getTitle())
        .setCancelable(false)
        .setView(layout)
        .setPositiveButton(R.string.dialog_event_change_save, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            event.setType(typeEdit.getText().toString());
            event.setLecturer(lecturerEdit.getText().toString());
            event.setRoom(roomEdit.getText().toString());
            try {
              Calendar date = Calendar.getInstance();
              date.setTime(dateFormat.parse(dateEdit.getText().toString()));

              int month = date.get(Calendar.MONTH);
              int day = date.get(Calendar.DAY_OF_MONTH);
              int year = date.get(Calendar.YEAR);

              Calendar start = Calendar.getInstance();
              start.setTime(timeFormat.parse(startEdit.getText().toString()));
              start.set(year, month, day);
              Calendar end = Calendar.getInstance();
              end.setTime(timeFormat.parse(endEdit.getText().toString()));
              end.set(year, month, day);

              event.setStart(start.getTime());
              event.setEnd(end.getTime());
            } catch (Exception e) {
              Log.e(getClass().getSimpleName(), e.toString());
            } finally {
              ((TimetableApplication) getActivity().getApplication()).updateEvent(event);
              if (getActivity() instanceof TimetableActivity) {
                ((TimetableActivity) getActivity()).updateList();
              }
              dialog.dismiss();
            }
          }
        })
        .setNegativeButton(R.string.dialog_event_change_discard, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
          }
        });
    AlertDialog alertDialog = builder.create();
    return alertDialog;
  }

  public static class DatePickerFragment extends DialogFragment
      implements DatePickerDialog.OnDateSetListener {

    private static final String KEY_EDIT_DATE = "keyEditDate";
    private DatePickerDialog.OnDateSetListener dateSetListener;

    static DialogFragment newInstance(long dateInMillis) {
      DatePickerFragment f = new DatePickerFragment();

      Bundle b = new Bundle();
      b.putLong(KEY_EDIT_DATE, dateInMillis);

      f.setArguments(b);
      return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final Calendar c = Calendar.getInstance();
      c.setTimeInMillis(getArguments().getLong(KEY_EDIT_DATE));
      int year = c.get(Calendar.YEAR);
      int month = c.get(Calendar.MONTH);
      int day = c.get(Calendar.DAY_OF_MONTH);

      return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void setOnDateSetListener(DatePickerDialog.OnDateSetListener onDateSetListener) {
      this.dateSetListener = onDateSetListener;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
      if (dateSetListener != null) {
        dateSetListener.onDateSet(view, year, monthOfYear, dayOfMonth);
      }
    }
  }

  public static class TimePickerFragment extends DialogFragment
      implements TimePickerDialog.OnTimeSetListener {

    private static final String KEY_EDIT_TIME = "keyEditTime";
    private TimePickerDialog.OnTimeSetListener onTimeSetListener;

    static DialogFragment newInstance(long dateInMillis) {
      TimePickerFragment f = new TimePickerFragment();

      Bundle b = new Bundle();
      b.putLong(KEY_EDIT_TIME, dateInMillis);

      f.setArguments(b);
      return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final Calendar c = Calendar.getInstance();
      c.setTimeInMillis(getArguments().getLong(KEY_EDIT_TIME));
      int hour = c.get(Calendar.HOUR_OF_DAY);
      int minute = c.get(Calendar.MINUTE);

      return new TimePickerDialog(getActivity(), this, hour, minute,
          DateFormat.is24HourFormat(getActivity()));
    }

    public void setOnTimeSetListener(TimePickerDialog.OnTimeSetListener onTimeSetListener) {
      this.onTimeSetListener = onTimeSetListener;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
      if (onTimeSetListener != null) {
        onTimeSetListener.onTimeSet(view, hourOfDay, minute);
      }
    }
  }
}
