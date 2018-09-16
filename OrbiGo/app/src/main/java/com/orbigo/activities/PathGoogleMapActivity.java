package com.orbigo.activities;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.orbigo.R;
import com.orbigo.constants.Constants;
import com.orbigo.helpers.HttpConnection;
import com.orbigo.helpers.PathJSONParser;
import com.orbigo.helpers.Utils;
import com.orbigo.models.TripPlanItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PathGoogleMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final LatLng LOWER_MANHATTAN = new LatLng(40.722543,
            -73.998585);
    private static final LatLng BROOKLYN_BRIDGE = new LatLng(40.7057, -73.9964);
    private static final LatLng WALL_STREET = new LatLng(40.7064, -74.0094);
    private ImageView back_image;

    private GoogleMap googleMap;
    private final String TAG = "PathGoogleMapActivity";
    private ArrayList<TripPlanItem> tripPlanItemList;
    private TextView tripTitle;
    private View list_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_google_map);
        tripPlanItemList = (ArrayList<TripPlanItem>) getIntent().getSerializableExtra("tripPlanItemList");


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        setId();
        setListeners();
        showUI();
    }

    private void setId() {
        back_image=findViewById(R.id.back_image);
        tripTitle=findViewById(R.id.tripTitle);
        list_view=findViewById(R.id.list_view);

    }

    private void showUI(){
        tripTitle.setText(Constants.PLANNING_TRIP.getName());
    }

    private void setListeners() {
        back_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        list_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private String getMapsApiDirectionsUrl() {
        String latlngOfPlaces = "";
        for (TripPlanItem trip : tripPlanItemList) {
            latlngOfPlaces = latlngOfPlaces + "|" + trip.getLatlng().latitude + "," + trip.getLatlng().longitude;
            Log.i("latlog_logg", "==>" + latlngOfPlaces);
        }

        String waypoints = "origin=" + tripPlanItemList.get(0).getLatlng().latitude + "," + tripPlanItemList.get(0).getLatlng().longitude
                + "&destination="
                + tripPlanItemList.get(tripPlanItemList.size() - 1).getLatlng().latitude + "," + tripPlanItemList.get(tripPlanItemList.size() - 1).getLatlng().longitude
                + "&waypoints=optimize:true|" +
                +tripPlanItemList.get(0).getLatlng().latitude + "," + tripPlanItemList.get(0).getLatlng().longitude
                + "|" + latlngOfPlaces;

        String sensor = "sensor=false";
        String params = waypoints + "&" + sensor + "&key=" + Constants.API_KEY;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
        Log.i("logging_url", " printing the url " + url);
        return url;
    }

    private void addMarkers() {
        if (googleMap != null) {

            int index = 1;

            //    List<Marker> markers=new ArrayList<>();
            for (TripPlanItem trip : tripPlanItemList) {
                Log.i("tripPlanItemList", "add marker" + trip.getLatlng().toString());

                Utils.addTripMarker(this, googleMap, trip.getLatlng(), trip.getPlaceName(), index++);

//                googleMap.addMarker(new MarkerOptions().position(trip.getLatlng())
//                        .title(index++ +" Place"));
//                Marker mark=
//                markers.add(mark);
            }
            /*for(int i=0;i<markers.size();i++){
                markers.get(i).showInfoWindow();
            }*/
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        MarkerOptions options = new MarkerOptions();
//        for(TripPlanItem trip:tripPlanItemList){
//            Log.i("tripPlanItemList"," on map ready marker" + trip.getLatlng().toString());
//            options.position(trip.getLatlng());
//        }
//        googleMap.addMarker(options);
        String url = getMapsApiDirectionsUrl();
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tripPlanItemList.get(0).getLatlng(),
                13));
        addMarkers();

    }

    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = null;
            try {
                HttpConnection http = new HttpConnection();
                Log.i("priting_route", "printing the url[0] " + url[0]);
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            Log.i("priting_route", "printing the data " + data);
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i("priting_route", "printing the Route" + routes);
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            //     if(routes!=null){
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                polyLineOptions.addAll(points);
                polyLineOptions.width(10);
//                List<PatternItem> dashedPattern = Arrays.asList(new Dash(60), new Gap(60));
//                polyLineOptions.pattern(dashedPattern);
                polyLineOptions.color(getResources().getColor(R.color.aqua_blue));
                googleMap.addPolyline(polyLineOptions);
            }

            //   }
        }
    }
}
