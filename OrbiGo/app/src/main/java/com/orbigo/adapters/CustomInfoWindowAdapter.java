package com.orbigo.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.orbigo.R;
import com.orbigo.activities.MapsActivity;
import com.orbigo.models.Poi;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.io.InputStream;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    Context context;
    RequestOptions requestOptions;

    public CustomInfoWindowAdapter(Context context) {
        this.context = context;
        requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(5));
       // requestOptions.placeholder(R.drawable.statis);
        requestOptions.override(250,250);

    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bmp = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bmp;
        }
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }



    @Override
    public View getInfoContents(final Marker marker) {
        View view = ((MapsActivity)context).getLayoutInflater().inflate(R.layout.info_window_layout,null);
        TextView nameTV = view.findViewById(R.id.info_name);
        TextView urlTV = view.findViewById(R.id.info_description);
        ImageView info_image=view.findViewById(R.id.info_image);

       // info_image.setImageResource(R.drawable.world2);
        Poi poi = (Poi) marker.getTag();
      //  new DownloadImageTask(info_image).execute(poi.getPicture());
        nameTV.setText(poi.getName());
        urlTV.setText(poi.getDescription());
        Log.i("URRRRL",poi.getPicture());

        Glide.with(context)
                .load(Uri.parse(poi.getPicture()))
                .apply(requestOptions)
                .into(info_image);
        return view;
    }
}