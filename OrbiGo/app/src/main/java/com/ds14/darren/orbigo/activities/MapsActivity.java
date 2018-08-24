package com.ds14.darren.orbigo.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.adapters.MedssAdapter;
import com.ds14.darren.orbigo.adapters.CustomInfoWindowAdapter;
import com.ds14.darren.orbigo.adapters.SearchDataAdapter;
import com.ds14.darren.orbigo.constants.Constants;
import com.ds14.darren.orbigo.constants.DatabaseKeys;
import com.ds14.darren.orbigo.constants.Urls;
import com.ds14.darren.orbigo.custom_ui_components.DelayAutoCompleteTextView;
import com.ds14.darren.orbigo.models.Country;
import com.ds14.darren.orbigo.models.MeddsModel;
import com.ds14.darren.orbigo.models.Poi;
import com.ds14.darren.orbigo.models.Region;
import com.ds14.darren.orbigo.models.SearchData;
import com.ds14.darren.orbigo.models.State;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnPolygonClickListener,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMapLoadedCallback {

    private GoogleMap mMap;
    private SlidingUpPanelLayout mLayout;
    private RecyclerView activityRV;
    private ImageView arrowImageView;
    private MedssAdapter activityAdapter;
    private List<MeddsModel> activityList = new ArrayList<>();
    private DelayAutoCompleteTextView placesSearchView;
    private RequestQueue requestQueue;
    private TextView nameTV, addressTV,distanceTV,timeTV,seekBarText;
    private CardView carCV,planeCV,trainCV;
    private String travel_mode,search_mode;
    private SeekBar seekBar;
    private boolean mRequestingLocationUpdates;
    private Button distanceBtn,timeBtn;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private SearchData selectedPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        requestQueue = Volley.newRequestQueue(MapsActivity.this);
        setId();
        initFused();
        askLocationPermission();
        setRecyclerViews();
        addActivities();
        setListeners();
    }

    private void initFused() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                if(Geocoder.isPresent()){
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try {
                        List<Address> addressList = geocoder.getFromLocation(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude(),1);
                        Address address = addressList.get(0);
                        nameTV.setText(address.getSubLocality()+", "+address.getSubAdminArea());
                        addressTV.setText(address.getLocality()+", "+address.getAdminArea()+", "+address.getCountryName());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.v("locationlog",mCurrentLocation.toString());
                getDistanceTime();
            }
        };
        mRequestingLocationUpdates = false;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(Constants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void askLocationPermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        startLocationUpdates();
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if(response.isPermanentlyDenied()){
                            showSettingsDialog();
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Need Permissions");
        builder.setMessage("OrbiGo needs location permission to show you best trip plans. You can grant them in app settings.");
        builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        builder.show();
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, Constants.LOCATION_PERMISSION_CODE);
    }

    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MapsActivity.this, Constants.REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i("fused", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e("fused", errorMessage);
                                Toast.makeText(MapsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void getDistanceTime(){
        StringBuilder urlBuilder = new StringBuilder();
        if(mCurrentLocation!=null && selectedPlace!=null) {
            urlBuilder.append("https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=");
            urlBuilder.append(mCurrentLocation.getLatitude());
            urlBuilder.append(",");
            urlBuilder.append(mCurrentLocation.getLongitude());
            urlBuilder.append("&destinations=");
            urlBuilder.append(selectedPlace.getName());
            switch (travel_mode){
                case Constants.MODE_CAR:
                    urlBuilder.append("&mode=driving");
                    break;
                case Constants.MODE_TRAIN:
                    urlBuilder.append("&mode=transit&transit_mode=rail");
                    break;
            }
            urlBuilder.append("&key=");
            urlBuilder.append(Constants.API_KEY);
            String url = urlBuilder.toString();
            Log.v("distanceurl",url);
            JsonObjectRequest timeRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray rows = response.getJSONArray("rows");
                                for(int i=0;i<rows.length();i++){
                                    JSONObject rowsElement = rows.getJSONObject(i);
                                    JSONArray elements = rowsElement.getJSONArray("elements");
                                    for(int j=0;j<elements.length();j++){
                                        JSONObject result = elements.getJSONObject(j);
                                        Log.v("mapsresponse",result.toString());
                                        if(result.getString("status").compareToIgnoreCase("OK")==0){
                                            String distance = result.getJSONObject("distance").getString("text");
                                            String time = result.getJSONObject("duration").getString("text");
                                            distanceTV.setText(distance);
                                            timeTV.setText(time);
                                        }
                                        else{
                                            distanceTV.setText("Not available");
                                            timeTV.setText("Not available");
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },null);
            requestQueue.add(timeRequest);
        }
    }

    private void setListeners() {
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState.equals(SlidingUpPanelLayout.PanelState.EXPANDED))
                    arrowImageView.setImageDrawable(getResources().getDrawable(android.R.drawable.arrow_down_float));
                else if (newState.equals(SlidingUpPanelLayout.PanelState.COLLAPSED))
                    arrowImageView.setImageDrawable(getResources().getDrawable(android.R.drawable.arrow_up_float));
            }
        });
        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        planeCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                travel_mode = Constants.MODE_PLANE;
                planeCV.setCardBackgroundColor(Color.parseColor("#04c4d7"));
                trainCV.setCardBackgroundColor(Color.WHITE);
                carCV.setCardBackgroundColor(Color.WHITE);
                getDistanceTime();
            }
        });
        trainCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                travel_mode = Constants.MODE_TRAIN;
                trainCV.setCardBackgroundColor(Color.parseColor("#04c4d7"));
                planeCV.setCardBackgroundColor(Color.WHITE);
                carCV.setCardBackgroundColor(Color.WHITE);
                getDistanceTime();
            }
        });
        carCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                travel_mode = Constants.MODE_CAR;
                carCV.setCardBackgroundColor(Color.parseColor("#04c4d7"));
                trainCV.setCardBackgroundColor(Color.WHITE);
                planeCV.setCardBackgroundColor(Color.WHITE);
                getDistanceTime();
            }
        });
        distanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_mode = Constants.MODE_DISTANCE;
                seekBar.setMax(Constants.DISTANCE_RANGE_KM);
                distanceBtn.setTextColor(Color.BLACK);
                seekBar.setProgress(0);
                seekBarText.setText(seekBar.getProgress()+" miles");
                timeBtn.setTextColor(Color.parseColor("#b7b7b7"));
            }
        });
        timeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setMax(Constants.TIME_RANGE_MIN);
                search_mode = Constants.MODE_TIME;
                timeBtn.setTextColor(Color.BLACK);
                seekBar.setProgress(0);
                seekBarText.setText(seekBar.getProgress()+" min");
                distanceBtn.setTextColor(Color.parseColor("#b7b7b7"));
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                StringBuilder builder = new StringBuilder();
                builder.append(progress);
                if(search_mode.equals(Constants.MODE_DISTANCE)){
                    builder.append(" miles");
                }
                else if(search_mode.equals(Constants.MODE_TIME)){
                    builder.append(" min");
                }
                seekBarText.setText(builder.toString());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int value = seekBar.getProgress();
                getSearchPolygon(value);
            }
        });
    }

    private void getSearchPolygon(int value) {
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.MODE,search_mode);
            if(search_mode.equals(Constants.MODE_DISTANCE))
                input.put(Constants.MODE_DISTANCE,value);
            else if(search_mode.equals(Constants.MODE_TIME))
                input.put(Constants.MODE_TIME,value);
            JSONObject loc = new JSONObject();
            loc.put("lat",mCurrentLocation.getLatitude());
            loc.put("lng",mCurrentLocation.getLongitude());
            input.put("location",loc);
            String url = Urls.BASE_URL + Urls.GET_REGIONS_IN_POLYGON;
            Log.v("polyresponse",input.toString());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("polyresponse", response.toString());
                            try {
                                JSONArray regions = response.getJSONArray("regions");
                                mMap.clear();
                                for (int i = 0; i < regions.length(); i++) {
                                    plotPolygon(regions.getJSONObject(i));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MapsActivity.this,"An error occured",Toast.LENGTH_SHORT).show();
                    Log.v("polyresponse",error.toString());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    1000*60*5,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void plotPolygon(JSONObject jsonObject){
        try {
            String string = jsonObject.getString("coordinates");
            JSONArray jsonArray = new JSONArray(string);
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            int fillColor = Color.argb(90,0,255,0);
            if(!jsonObject.isNull("is_current")){
                fillColor = Color.argb(90,250,0,0);
            }
            for(int i=0;i<jsonArray.length();i++) {
                JSONArray jsonArray1 = jsonArray.getJSONArray(i);
                for (int j = 0; j < jsonArray1.length(); j++) {
                    JSONArray jsonArray2 = jsonArray1.getJSONArray(j);
                    List<LatLng> latLngList = new ArrayList<>();
                    for (int k = 0; k < jsonArray2.length(); k++) {
                        JSONArray coords = jsonArray2.getJSONArray(k);
                        LatLng l = new LatLng(coords.getDouble(1), coords.getDouble(0));
                        latLngList.add(l);
                        builder.include(l);
                    }
                    Region r = new Region();
                    r.setId(jsonObject.getString(DatabaseKeys.REGION_ID));
                    r.setName(jsonObject.getString("region_name"));
                    r.setIs_in_country(jsonObject.getString("is_in_country"));
                    r.setIs_in_state(jsonObject.getString("is_in_state"));
                    r.setLatLngBounds(builder.build());
                    Polygon polygon = mMap.addPolygon(new PolygonOptions()
                    .addAll(latLngList)
                    .fillColor(fillColor)
                    .strokeColor(Color.BLUE));
                    polygon.setClickable(true);
                    polygon.setTag(r);
                }
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    private void setRecyclerViews() {
        activityAdapter = new MedssAdapter(getApplicationContext(), activityList);
        activityRV.setAdapter(activityAdapter);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 5);
        activityRV.setLayoutManager(mLayoutManager);
      }

    private void setId() {
        addressTV = findViewById(R.id.maps_address);
        arrowImageView = findViewById(R.id.arrow_image);
        mLayout = findViewById(R.id.sliding_layout);
        activityRV = findViewById(R.id.activity_rv);
        placesSearchView = findViewById(R.id.maps_search_places);
        nameTV = findViewById(R.id.maps_name);
        distanceTV = findViewById(R.id.distanceTV);
        timeTV = findViewById(R.id.timeTV);
        carCV = findViewById(R.id.carCV);
        trainCV = findViewById(R.id.trainCV);
        planeCV = findViewById(R.id.planeCV);
        distanceBtn = findViewById(R.id.distance_select);
        timeBtn = findViewById(R.id.time_select);
        seekBar = findViewById(R.id.seekbar);
        seekBarText = findViewById(R.id.seekbar_text);
    }

    private void setSearchView() {
        placesSearchView.setThreshold(Constants.SEARCH_THRESHOLD);
        SearchDataAdapter placesAdapter = new SearchDataAdapter(this,Constants.WORLD_DATA);
        placesSearchView.setAdapter(placesAdapter); // 'this' is Activity instance
        placesSearchView.setLoadingIndicator(
                (android.widget.ProgressBar) findViewById(R.id.pb_loading_indicator));
        placesSearchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                selectedPlace = (SearchData) adapterView.getItemAtPosition(position);
                mMap.clear();
                placesSearchView.setText(selectedPlace.getName());
                handleSelection();
            }
        });
    }

    private void handleSelection() {
        nameTV.setText(selectedPlace.getName());
        if(selectedPlace instanceof Country) {
            Country c = (Country) selectedPlace;
            getCountryFocus(c);
            addressTV.setText(c.getName());
        }
        else if(selectedPlace instanceof State){
            State s = (State) selectedPlace;
            getStateBoundary();
            addressTV.setText(s.getIs_in_country());
        }
        else if(selectedPlace instanceof Region){
            Region r = (Region) selectedPlace;
            getRegionBoundary();
            StringBuilder builder = new StringBuilder();
            builder.append(r.getIs_in_state());
            builder.append(", ");
            builder.append(r.getIs_in_country());
            addressTV.setText(builder.toString());
        }
        else if(selectedPlace instanceof Poi){
            Poi p = (Poi) selectedPlace;
            getPoiDetails();
            StringBuilder builder = new StringBuilder();
            builder.append(p.getIs_in_region());
            builder.append(", ");
            builder.append(p.getIs_in_state());
            builder.append(", ");
            builder.append(p.getIs_in_country());
            addressTV.setText(builder.toString());
        }
    }

    private void addActivities() {
        MeddsModel c1 = new MeddsModel("move", R.drawable.eat_ic,false);
        activityList.add(c1);
        activityAdapter.notifyDataSetChanged();
        MeddsModel c2 = new MeddsModel("eat", R.drawable.eat_ic,false);
        activityList.add(c2);
        activityAdapter.notifyDataSetChanged();
        MeddsModel c3 = new MeddsModel("do", R.drawable.eat_ic,false);
        activityList.add(c3);
        activityAdapter.notifyDataSetChanged();
        MeddsModel c4 = new MeddsModel("see", R.drawable.eat_ic,false);
        activityList.add(c4);
        activityAdapter.notifyDataSetChanged();
        MeddsModel c5 = new MeddsModel("sleep", R.drawable.eat_ic,false);
        activityList.add(c5);
        activityAdapter.notifyDataSetChanged();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        distanceBtn.callOnClick();
        carCV.callOnClick();
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLoadedCallback(this);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnPolygonClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
    }

    private void getPoiDetails() {
        Poi p = (Poi) selectedPlace;
        CustomInfoWindowAdapter customInfoWindowAdapter = new CustomInfoWindowAdapter(MapsActivity.this);
        mMap.setInfoWindowAdapter(customInfoWindowAdapter);
        mMap.addMarker(new MarkerOptions()
                .title(selectedPlace.getName())
                .position(p.getLocation())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.eat_ic))).
                setTag(selectedPlace);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(p.getLocation(),16));
        getDistanceTime();
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        selectedPlace = (SearchData) marker.getTag();
        handleSelection();
        marker.showInfoWindow();
        return true;
    }

    private void getRegionBoundary(){
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.REGION_ID,selectedPlace.getId());
            String url = Urls.BASE_URL + Urls.GET_REGION_BOUNDARY;
            final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            plotSinglePolygon(response);
                        }
                    },null);
            requestQueue.add(request);
            getDistanceTime();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void plotSinglePolygon(JSONObject response) {
        try {
            String string = response.getString("coordinates");
            JSONArray jsonArray = new JSONArray(string);
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for(int i=0;i<jsonArray.length();i++){
                JSONArray jsonArray1 = jsonArray.getJSONArray(i);
                for(int j=0;j<jsonArray1.length();j++){
                    JSONArray jsonArray2 = jsonArray1.getJSONArray(j);
                    List<LatLng> latLngList = new ArrayList<>();
                    for(int k=0;k<jsonArray2.length();k++){
                        JSONArray coords = jsonArray2.getJSONArray(k);
                        LatLng l = new LatLng(coords.getDouble(1),coords.getDouble(0));
                        latLngList.add(l);
                        builder.include(l);
                    }
                    Polygon polygon = mMap.addPolygon(new PolygonOptions()
                            .addAll(latLngList)
                            .fillColor(Color.argb(100,2,184,208))
                            .strokeColor(Color.parseColor("#02b8d0")));
                    polygon.setClickable(true);
                    polygon.setTag(selectedPlace);
                }
            }
            if(selectedPlace instanceof State) {
                ((State) selectedPlace).setLatLngBounds(builder.build());
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(((State) selectedPlace).getLatLngBounds(), 0));
            }
            else if(selectedPlace instanceof Region) {
                ((Region) selectedPlace).setLatLngBounds(builder.build());
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(((Region) selectedPlace).getLatLngBounds(), 0));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getStateBoundary(){
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.STATE_CODE,selectedPlace.getId());
            Log.v("stateboundaryresponse","input is "+input.toString());
            String url = Urls.BASE_URL + Urls.GET_STATE_BOUNDARY;
            final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            plotSinglePolygon(response);
                        }
                    },null);
            requestQueue.add(request);
            getDistanceTime();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getCountryFocus(Country country){
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(country.getLatLngBounds(),0));
    }

    @Override
    public void onPolygonClick(Polygon polygon) {
        SearchData searchData = (SearchData) polygon.getTag();
        nameTV.setText(searchData.getName());
        if(searchData instanceof Region){
            Toast.makeText(this,searchData.getName(),Toast.LENGTH_SHORT).show();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(((Region) searchData).getLatLngBounds(),0));
            getRegionPois(searchData.getName());
            addressTV.setText(((Region) searchData).getIs_in_state()+", "+((Region) searchData).getIs_in_country());
        }
        else if(searchData instanceof State){
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(((State) searchData).getLatLngBounds(),0));
            Toast.makeText(this,searchData.getName(),Toast.LENGTH_SHORT).show();
            getStatePois(searchData.getName());
            addressTV.setText(((State) searchData).getIs_in_country());
        }
    }

    private void getStatePois(String stateName) {
        int count=0;
        for (SearchData sdata : Constants.WORLD_DATA) {
            if(sdata instanceof Poi){
                if(((Poi) sdata).getIs_in_state().compareToIgnoreCase(stateName)==0){
                    count++;
                    CustomInfoWindowAdapter customInfoWindowAdapter = new CustomInfoWindowAdapter(MapsActivity.this);
                    mMap.setInfoWindowAdapter(customInfoWindowAdapter);
                    mMap.addMarker(new MarkerOptions()
                            .title(sdata.getName())
                            .position(((Poi) sdata).getLocation())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.eat_ic))).
                            setTag(sdata);
                }
            }
        }
        Toast.makeText(MapsActivity.this,count+" pois in "+stateName,Toast.LENGTH_SHORT).show();
    }

    private void getRegionPois(String regionName) {
        int count = 0;
        for (SearchData sdata : Constants.WORLD_DATA) {
            if(sdata instanceof Poi){
                if(((Poi) sdata).getIs_in_region().compareToIgnoreCase(regionName)==0){
                    count++;
                    CustomInfoWindowAdapter customInfoWindowAdapter = new CustomInfoWindowAdapter(MapsActivity.this);
                    mMap.setInfoWindowAdapter(customInfoWindowAdapter);
                    mMap.addMarker(new MarkerOptions()
                            .title(sdata.getName())
                            .position(((Poi) sdata).getLocation())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.eat_ic))).
                            setTag(sdata);
                }
            }
        }
        Toast.makeText(MapsActivity.this,count+" pois in "+regionName,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        selectedPlace = (SearchData) marker.getTag();
        Constants.SELECTED_DATA = selectedPlace;
        Intent i = new Intent(MapsActivity.this,NearbyPoiActivity.class);
        i.putExtra("travel_mode",travel_mode);
        startActivity(i);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(MapsActivity.this,"Taking to your location...",Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMapLoaded() {
        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            selectedPlace = extras.getParcelable(Constants.SELECTED_PLACE);
            if (selectedPlace != null) {
                placesSearchView.setText(selectedPlace.getName());
                handleSelection();
            }
            setSearchView();
        }
    }
}