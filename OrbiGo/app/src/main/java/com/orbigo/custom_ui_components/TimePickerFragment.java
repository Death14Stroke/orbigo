package com.orbigo.custom_ui_components;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private TimeChangeListener mListener;
    private Date date=new Date();

    public interface TimeChangeListener {
        void onTimeChanged(int hour, int minute, String tag);
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mListener = (TimeChangeListener) activity;

    }

    public void setDate(Date date){
        this.date=date;
    }

   /* @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (TimeChangeListener) context;
    }*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mListener.onTimeChanged(hourOfDay,minute,getTag());
    }
}