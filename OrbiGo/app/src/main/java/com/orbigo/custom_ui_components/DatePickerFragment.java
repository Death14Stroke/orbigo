package com.orbigo.custom_ui_components;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    private  Date date=new Date();

    public interface DateChangeListener{
        void onDateChanged(int year, int month, int day);
    }

    public void setDate(Date date){
        this.date=date;
    }

    private DateChangeListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (DateChangeListener)context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        mListener.onDateChanged(year,month,day);
    }
}