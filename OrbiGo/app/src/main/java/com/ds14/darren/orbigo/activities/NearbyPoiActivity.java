package com.ds14.darren.orbigo.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.app.DialogFragment;
import android.content.IntentSender;
import android.location.Location;
import android.net.Uri;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.adapters.NearbyPoiAdapter;
import com.ds14.darren.orbigo.constants.Constants;
import com.ds14.darren.orbigo.constants.DatabaseKeys;
import com.ds14.darren.orbigo.constants.Urls;
import com.ds14.darren.orbigo.custom_ui_components.AddTripDialogFragment;
import com.ds14.darren.orbigo.custom_ui_components.SelectTripDialogFragment;
import com.ds14.darren.orbigo.models.NearbyPoi;
import com.ds14.darren.orbigo.models.Poi;
import com.ds14.darren.orbigo.models.SearchData;
import com.ds14.darren.orbigo.models.Trip;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class NearbyPoiActivity extends AppCompatActivity implements AddTripDialogFragment.AddTripDialogListener,
    SelectTripDialogFragment.SelectTripDialogListener{

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private RecyclerView selectedRV,nearbyRV;
    private String travel_mode;
    private Button planTripBtn;
    private List<NearbyPoi> selectedList,nearbyPoiList;
    private NearbyPoiAdapter selectedAdapter,nearbyPoiAdapter;
    private RequestQueue requestQueue;
    private FirebaseAuth mAuth;
    private boolean mRequestingLocationUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_poi);
        mAuth = FirebaseAuth.getInstance();
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setRecyclerViews();
        initFused();
        askLocationPermission();
        planTripBtn = findViewById(R.id.plan_trip_btn);
        requestQueue = Volley.newRequestQueue(this);
        getMyTrips();
        planTripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new SelectTripDialogFragment();
                newFragment.show(getFragmentManager(), Constants.SELECT_TRIP_DIALOG);
            }
        });
        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            travel_mode = extras.getString("travel_mode");
        }
        addSelectedPoi();
        addNearbyPois();
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

    @Override
    protected void onPause() {
        super.onPause();
        if (mRequestingLocationUpdates) {
            stopLocationUpdates();
        }
    }

    public void stopLocationUpdates() {
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();
                    }
                });
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
                                    rae.startResolutionForResult(NearbyPoiActivity.this, Constants.REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i("fused", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e("fused", errorMessage);
                                Toast.makeText(NearbyPoiActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void initFused() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                getDistanceTime();
                Log.v("locationlog",mCurrentLocation.toString());
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


    private void addNearbyPois() {
        for (SearchData sdata : Constants.WORLD_DATA) {
            if(sdata instanceof Poi && Constants.SELECTED_DATA!=sdata){
                if(((Poi) sdata).getIs_in_region().compareToIgnoreCase(((Poi)Constants.SELECTED_DATA).getIs_in_region())==0){
                    NearbyPoi nearbyPoi = new NearbyPoi();
                    nearbyPoi.setLiked(false);
                    nearbyPoi.setPoiDetails((Poi) sdata);
                    nearbyPoiList.add(nearbyPoi);
                    nearbyPoiAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void addSelectedPoi() {
        NearbyPoi myPoi = new NearbyPoi();
        myPoi.setPoiDetails((Poi) Constants.SELECTED_DATA);
        myPoi.setLiked(false);
        selectedList.add(myPoi);
        selectedAdapter.notifyDataSetChanged();
    }

    private void getDistanceTime(){
        for(int i=0;i<selectedList.size();i++){
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=" +
                    mCurrentLocation.getLatitude() +
                    "," +
                    mCurrentLocation.getLongitude() +
                    "&destinations=" +
                    selectedList.get(i).getPoiDetails().getLocation().latitude +
                    "," +
                    selectedList.get(i).getPoiDetails().getLocation().longitude +
                    "&key=" +
                    Constants.API_KEY;
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
                                            selectedList.get(i).setDistance(distance);
                                            selectedList.get(i).setTime(time);
                                            selectedAdapter.notifyDataSetChanged();
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
        for(int i=0;i<nearbyPoiList.size();i++){
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=" +
                    mCurrentLocation.getLatitude() +
                    "," +
                    mCurrentLocation.getLongitude() +
                    "&destinations=" +
                    nearbyPoiList.get(i).getPoiDetails().getLocation().latitude +
                    "," +
                    nearbyPoiList.get(i).getPoiDetails().getLocation().longitude +
                    "&key=" +
                    Constants.API_KEY;
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
                                            nearbyPoiList.get(i).setDistance(distance);
                                            nearbyPoiList.get(i).setTime(time);
                                            nearbyPoiAdapter.notifyDataSetChanged();
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

    private void setRecyclerViews() {
        selectedRV = findViewById(R.id.selected_poi_rv);
        selectedList = new ArrayList<>();
        selectedAdapter = new NearbyPoiAdapter(this,selectedList);
        selectedRV.setLayoutManager(new LinearLayoutManager(this));
        selectedRV.setAdapter(selectedAdapter);
        selectedRV.setItemAnimator(new DefaultItemAnimator());
        selectedRV.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        
        nearbyRV = findViewById(R.id.nearby_poi_rv);
        nearbyPoiList = new ArrayList<>();
        nearbyPoiAdapter = new NearbyPoiAdapter(this,nearbyPoiList);
        nearbyRV.setLayoutManager(new LinearLayoutManager(this));
        nearbyRV.setAdapter(nearbyPoiAdapter);
        nearbyRV.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        nearbyRV.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, Trip trip) {
        switch (dialog.getTag()){
            case Constants.ADD_TO_TRIP_DIALOG:
                addToMyTrip(trip,Constants.SELECTED_NEARBY_POI);
                break;
            case Constants.SELECT_TRIP_DIALOG:
                planTrip(trip);
        }
    }

    private void planTrip(Trip trip) {
        Constants.PLANNING_TRIP = trip;
        startActivity(new Intent(this,TimelineActivity.class));
    }

    private void addToMyTrip(Trip trip, NearbyPoi p) {
        JSONObject input = new JSONObject();
        try {
            input.put("trip_id",trip.getId());
            input.put("added_poi", p.getPoiDetails().getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Urls.BASE_URL + Urls.ADD_TO_TRIP;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v("addresponse", response.toString());
                        try {
                            Toast.makeText(NearbyPoiActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error!=null) {
                    String message = error.getMessage();
                    Toast.makeText(getApplicationContext(), "error msg: " + message, Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(getApplicationContext(),"error in adding to trip",Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onDialogNeutralClick(DialogFragment dialog, String name) {
        if(name.isEmpty())
            Toast.makeText(NearbyPoiActivity.this,"Name cannot be empty!",Toast.LENGTH_SHORT).show();
        else
            createTripWithPoi(name,Constants.SELECTED_NEARBY_POI);
    }

    private void getMyTrips(){
        Constants.MY_TRIP_LIST.clear();
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.USER_ID,mAuth.getCurrentUser().getUid());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Urls.BASE_URL + Urls.GET_MY_TRIPS;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, input,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v("mytrips",response.toString());
                        try {
                            String status = response.getString("status");
                            if(status.compareToIgnoreCase("success")==0){
                                JSONArray trips = response.getJSONArray("trips");
                                for(int i=0;i<trips.length();i++){
                                    JSONObject tripJSON = trips.getJSONObject(i);
                                    Trip t = new Trip();
                                    t.setId(tripJSON.getString("trip_id"));
                                    t.setName(tripJSON.getString("trip_name"));
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMMM yyyy");
                                    t.setCreatedDate(simpleDateFormat.format(Long.parseLong(tripJSON.getString("created_ts"))));
                                    Constants.MY_TRIP_LIST.add(t);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },null);
        requestQueue.add(request);
    }

    private void createTripWithPoi(String name, NearbyPoi p) {
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.USER_ID,mAuth.getCurrentUser().getUid());
            input.put(DatabaseKeys.TRIP_NAME,name);
            JSONObject latlng = new JSONObject();
            latlng.put("lat",mCurrentLocation.getLatitude());
            latlng.put("lng",mCurrentLocation.getLongitude());
            input.put("start_from",latlng);
            input.put("added_poi", p.getPoiDetails().getId());
            input.put("start_ts",String.valueOf(System.currentTimeMillis()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("addtrip",input.toString());
        String url = Urls.BASE_URL + Urls.CREATE_NEW_TRIP;
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v("tripadd",response.toString());
                        try {
                            String status = response.getString("status");
                            if(status.compareToIgnoreCase("success")==0) {
                                Toast.makeText(NearbyPoiActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                                getMyTrips();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },null);
        requestQueue.add(objectRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home_logout:
                mAuth.signOut();
                finishAffinity();
                startActivity(new Intent(NearbyPoiActivity.this, LoginActivity.class));
                break;
            case R.id.home_profile:
                Intent intent = new Intent(NearbyPoiActivity.this, TouristProfileActivity.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }
}