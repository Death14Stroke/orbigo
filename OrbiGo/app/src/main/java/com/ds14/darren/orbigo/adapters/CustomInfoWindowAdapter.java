package com.ds14.darren.orbigo.adapters;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.models.Poi;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public CustomInfoWindowAdapter(Context context) {
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        View view = ((Activity)context).getLayoutInflater().inflate(R.layout.info_window_layout,null);
        TextView nameTV = view.findViewById(R.id.info_name);
        TextView urlTV = view.findViewById(R.id.info_description);

        Poi poi = (Poi) marker.getTag();
        nameTV.setText(poi.getName());
        urlTV.setText(poi.getDescription());
        return view;
    }
}