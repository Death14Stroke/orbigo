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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.orbigo.R;
import com.orbigo.adapters.MyTripSpinnerAdapter;
import com.orbigo.constants.Constants;
import com.orbigo.models.Trip;

public class AddTripDialogFragment extends DialogFragment {

    public interface AddTripDialogListener{
        void onDialogPositiveClick(DialogFragment dialog,Trip t);
        void onDialogNeutralClick(DialogFragment dialog, String name);
    }

    private AddTripDialogListener mListener;
    private EditText newTripET;
    DialogInterface dialog;
    TextView add, cancel,addNEw;
    private Spinner myTripsSpinner;
    private MyTripSpinnerAdapter myTripSpinnerAdapter;
    private Context mContext;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mListener = (AddTripDialogListener) activity;

    }

   /* @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{

            mContext = context;



        }
        catch (ClassCastException e){
            throw new ClassCastException(context.toString()+" must implement AddTripDialogListener");
        }
    }*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_trip_dialog,null);
        newTripET = view.findViewById(R.id.new_trip_name);
        /*add=view.findViewById(R.id.add);
        addNEw=view.findViewById(R.id.add_to_new);
        cancel=view.findViewById(R.id.cancel);*/

        myTripsSpinner = view.findViewById(R.id.dialog_add_trip_spinner);
//        Log.v("size",Constants.MY_TRIP_LIST.get(0).getName()+"");
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
       // builder.setTitle("Add to Trip");

       /* add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Trip t = (Trip) myTripsSpinner.getSelectedItem();
                mListener.onDialogPositiveClick(AddTripDialogFragment.this,t);
                dialog.dismiss();
            }
        });

        addNEw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDialogNeutralClick(AddTripDialogFragment.this,newTripET.getText().toString());
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });*/
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("SelectedItem",myTripsSpinner.getSelectedItem().toString()+"");
                Trip t = (Trip) myTripsSpinner.getSelectedItem();
                mListener.onDialogPositiveClick(AddTripDialogFragment.this,t);
                dialog.dismiss();
            }
        });
        builder.setNeutralButton("Add to new trip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onDialogNeutralClick(AddTripDialogFragment.this,newTripET.getText().toString());
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