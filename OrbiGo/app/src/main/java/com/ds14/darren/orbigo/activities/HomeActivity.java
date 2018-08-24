package com.ds14.darren.orbigo.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.LocationManager;
import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.graphics.Color;
import android.app.AlertDialog;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.adapters.SearchDataAdapter;
import com.ds14.darren.orbigo.broadcast_receivers.GPSReceiver;
import com.ds14.darren.orbigo.constants.Constants;
import com.ds14.darren.orbigo.constants.DatabaseKeys;
import com.ds14.darren.orbigo.constants.Urls;
import com.ds14.darren.orbigo.custom_ui_components.DelayAutoCompleteTextView;
import com.ds14.darren.orbigo.helpers.MyApplication;
import com.ds14.darren.orbigo.models.Country;
import com.ds14.darren.orbigo.models.Poi;
import com.ds14.darren.orbigo.models.Region;
import com.ds14.darren.orbigo.models.SearchData;
import com.ds14.darren.orbigo.models.State;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements GPSReceiver.GpsStatusListener{

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private String myCountry;
    private Button discoverBtn;
    private DelayAutoCompleteTextView placesSearchView;
    private CardView localCV, countryCV, worldCV;
    private String discoverMode = Constants.MODE_LOCAL;
    private RequestQueue requestQueue;
    private ImageView imageView;
    private TextView textView;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private GPSReceiver gpsReceiver = new GPSReceiver();
    private boolean mRequestingLocationUpdates;
    private List<SearchData> searchDataList = new ArrayList<>();
    private SearchDataAdapter searchDataAdapter;
    private SearchData selectedPlace;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        setIds();
        initFused();
        askLocationPermission();
        setModes(discoverMode);
        setListeners();
        setupReceivers();
        setSearchView();
    }

    private void initFused() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                getIcon();
                Log.v("locationlog",mCurrentLocation.toString());
            }
        };
        mRequestingLocationUpdates = false;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000*60*15);
        mLocationRequest.setFastestInterval(1000*60*10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void getIcon() {
        if (Geocoder.isPresent()) {
            final Geocoder geocoder = new Geocoder(this);
            try {
                List<Address> addressList = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
                Address address = addressList.get(0);
                myCountry = address.getCountryName();
                getCountryData();
                textView.setText(myCountry);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(DatabaseKeys.COUNTRY_NAME, myCountry);
                Log.v("iconresponse", jsonObject.toString());
                String url = Urls.BASE_URL + Urls.GET_ICON;
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.v("iconresponse", response.toString());
                                try {
                                    String url = response.getString("image");
                                    Glide.with(getApplicationContext())
                                            .load(Uri.parse(url))
                                            .into(imageView);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.v("iconresponse", "error");
                            }
                        });
                requestQueue.add(jsonObjectRequest);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getCountryData() {
        Constants.COUNTRY_DATA.clear();
        for (SearchData sdata : Constants.WORLD_DATA) {
            if(sdata instanceof State) {
                if (((State) sdata).getIs_in_country().equals(myCountry))
                    Constants.COUNTRY_DATA.add(sdata);
            }
            else if(sdata instanceof Region) {
                if (((Region) sdata).getIs_in_country().equals(myCountry))
                    Constants.COUNTRY_DATA.add(sdata);
            }
            else if(sdata instanceof Poi){
                if(((Poi) sdata).getIs_in_country().equals(myCountry))
                    Constants.COUNTRY_DATA.add(sdata);
            }
        }
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
                                    rae.startResolutionForResult(HomeActivity.this, Constants.REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i("fused", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e("fused", errorMessage);
                                Toast.makeText(HomeActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void setupReceivers() {
        gpsReceiver = new GPSReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.location.PROVIDERS_CHANGED");
        registerReceiver(gpsReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(gpsReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home_logout:
                logout();
                break;
            case R.id.home_profile:

                Intent intent = new Intent(HomeActivity.this, TouristProfileActivity.class);
                startActivity(intent);
        }
        return true;
    }

    private void logout() {
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.USER_ID,mAuth.getCurrentUser().getUid());
            String url = Urls.BASE_URL + Urls.LOGOUT;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String status = response.getString("status");
                                if(status.equals("success")){
                                    mAuth.signOut();
                                    finishAffinity();
                                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },null);
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setSearchView() {
        placesSearchView.setThreshold(Constants.SEARCH_THRESHOLD);
        searchDataAdapter = new SearchDataAdapter(getApplicationContext(), searchDataList);
        placesSearchView.setAdapter(searchDataAdapter); // 'this' is Activity instance
        placesSearchView.setLoadingIndicator(
                (android.widget.ProgressBar) findViewById(R.id.pb_loading_indicator));
        placesSearchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                selectedPlace = (SearchData) adapterView.getItemAtPosition(position);
                placesSearchView.setText(selectedPlace.getName());
            }
        });
    }

    private void setIds() {
        discoverBtn = findViewById(R.id.discoverBtn);
        placesSearchView = findViewById(R.id.home_search_places);
        localCV = findViewById(R.id.home_local_cardview);
        countryCV = findViewById(R.id.home_country_cardview);
        worldCV = findViewById(R.id.home_world_cardview);
        imageView = findViewById(R.id.thumbnail2);
        textView = findViewById(R.id.country_name);
    }

    private void setListeners() {
        discoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (discoverMode.equals(Constants.MODE_WORLD) && selectedPlace == null)
                    showSelectLocationDialog();
                else {
                    Intent intent = new Intent(HomeActivity.this, MapsActivity.class);
                    intent.putExtra(Constants.SELECTED_PLACE,(selectedPlace));
                    startActivity(intent);
                }
            }
        });
        localCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverMode = Constants.MODE_LOCAL;
                placesSearchView.setVisibility(View.INVISIBLE);
                setModes(discoverMode);
            }
        });
        countryCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverMode = Constants.MODE_COUNTRY;
                placesSearchView.setVisibility(View.VISIBLE);
                setModes(discoverMode);
                searchDataList = Constants.COUNTRY_DATA;
                Log.v("constantlist","size is "+ searchDataList.size());
                setSearchView();
            }
        });
        worldCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverMode = Constants.MODE_WORLD;
                placesSearchView.setVisibility(View.VISIBLE);
                setModes(discoverMode);
                searchDataList = Constants.WORLD_DATA;
                Log.v("constantlist","size is "+ searchDataList.size());
                setSearchView();
            }
        });
    }

    private void showSelectLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please select a location to proceed")
                .setCancelable(false)
                .setTitle("Missing Location")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setModes(String mode) {
        switch (mode) {
            case Constants.MODE_LOCAL:
                localCV.setCardBackgroundColor(Color.parseColor("#04c4d7"));
                countryCV.setCardBackgroundColor(Color.WHITE);
                worldCV.setCardBackgroundColor(Color.WHITE);
                break;
            case Constants.MODE_COUNTRY:
                countryCV.setCardBackgroundColor(Color.parseColor("#04c4d7"));
                localCV.setCardBackgroundColor(Color.WHITE);
                worldCV.setCardBackgroundColor(Color.WHITE);
                break;
            case Constants.MODE_WORLD:
                worldCV.setCardBackgroundColor(Color.parseColor("#04c4d7"));
                localCV.setCardBackgroundColor(Color.WHITE);
                countryCV.setCardBackgroundColor(Color.WHITE);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v("calledmethod","onResume");
        MyApplication.getInstance().setGpsStatusListener(this);
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        onGpsStatusChanged(lm.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e("gpsdialog", "User agreed to make required location settings changes.");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e("gpsdialog", "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        break;
                }
                break;
        }
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

    @Override
    public void onGpsStatusChanged(boolean enabled) {
        Log.v("calledmethod","onGpschanged "+enabled);
        if(enabled)
            startLocationUpdates();
    }
}