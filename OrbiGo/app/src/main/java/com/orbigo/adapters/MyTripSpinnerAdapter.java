package com.orbigo.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.orbigo.R;
import com.orbigo.models.Trip;

import java.util.List;

public class MyTripSpinnerAdapter extends ArrayAdapter {

    private List<Trip> tripList;
    private Context mContext;

    public MyTripSpinnerAdapter(@NonNull Context mContext, int resource, List<Trip> tripList) {
        super(mContext, resource, tripList);
        this.tripList = tripList;
        this.mContext = mContext;
        Log.v("tripadapter","constructor size :"+this.tripList.size());
    }

    private View getCustomView(int position, View convertview, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.custom_trip_spinner,parent,false);
        TextView nameTV = row.findViewById(R.id.spinner_tripName);
        TextView dateTV = row.findViewById(R.id.spinner_tripCreated);
        Log.v("tripadapter",tripList.size()+"");
        Trip t = tripList.get(position);
        nameTV.setText(t.getName());
        dateTV.setText(t.getCreatedDate());
        return row;
    }

    // It gets a View that displays in the drop down popup the data at the specified position
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    // It gets a View that displays the data at the specified position
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }
}