package com.orbigo.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.orbigo.R;
import com.orbigo.models.Business;

import java.util.List;

public class MySpinnerAdapter extends ArrayAdapter {

    private List<Business> businessList;
    private Context mContext;

    public MySpinnerAdapter(@NonNull Context context, int resource, List<Business> businessList) {
        super(context, resource, businessList);
        this.businessList = businessList;
        this.mContext = context;
    }

    private View getCustomView(int position, View convertview, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.custom_spinner,parent,false);
        ImageView imageView = row.findViewById(R.id.business_img);
        TextView nameTV = row.findViewById(R.id.spinner_bname);
        TextView addressTV = row.findViewById(R.id.spinner_address);
        Business b = businessList.get(position);
        nameTV.setText(b.getBus_name());
        addressTV.setText(b.getAddress());
        if(b.getImage()!=null){
            byte[] decodedString = Base64.decode(b.getImage(), Base64.DEFAULT);
            Glide.with(mContext)
                    .load(decodedString)
                    .into(imageView);
        }
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
