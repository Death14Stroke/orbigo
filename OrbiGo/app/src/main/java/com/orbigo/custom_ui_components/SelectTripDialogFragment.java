package com.orbigo.custom_ui_components;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.orbigo.R;
import com.orbigo.adapters.MyTripSpinnerAdapter;
import com.orbigo.constants.Constants;
import com.orbigo.models.Trip;

public class SelectTripDialogFragment  extends DialogFragment {

    public interface SelectTripDialogListener{
        void onDialogPositiveClick(DialogFragment dialog,Trip t);
    }

    private SelectTripDialogListener mListener;
    private Spinner myTripsSpinner;
    private MyTripSpinnerAdapter myTripSpinnerAdapter;
    private Context mContext;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mListener = (SelectTripDialogListener) activity;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mContext = context;
            mListener = (SelectTripDialogFragment.SelectTripDialogListener) context;

        }
        catch (ClassCastException e){
            throw new ClassCastException(context.toString()+" must implement SelectTripDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.select_trip_dialog,null);
        myTripsSpinner = view.findViewById(R.id.dialog_select_trip_spinner);
        Log.v("size", Constants.MY_TRIP_LIST.size()+"");
        myTripSpinnerAdapter = new MyTripSpinnerAdapter(getActivity(),R.layout.custom_trip_spinner, Constants.MY_TRIP_LIST);
        myTripsSpinner.setAdapter(myTripSpinnerAdapter);
        myTripsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        builder.setView(view);
        builder.setTitle("Select Trip");
        builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Trip t = (Trip) myTripsSpinner.getSelectedItem();
                mListener.onDialogPositiveClick(SelectTripDialogFragment.this,t);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return builder.create();
    }
}