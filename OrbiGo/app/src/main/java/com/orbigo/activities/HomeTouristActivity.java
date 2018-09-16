package com.orbigo.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.orbigo.R;
import com.orbigo.adapters.SearchDataAdapter;
import com.orbigo.broadcast_receivers.GPSReceiver;
import com.orbigo.constants.Constants;
import com.orbigo.constants.DatabaseKeys;
import com.orbigo.constants.Urls;
import com.orbigo.custom_ui_components.DelayAutoCompleteTextView;
import com.orbigo.helpers.CacheImage;
import com.orbigo.helpers.MyApplication;
import com.orbigo.models.Poi;
import com.orbigo.models.Region;
import com.orbigo.models.SearchData;
import com.orbigo.models.State;
import com.orbigo.services.POILoader;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeTouristActivity extends AppCompatActivity implements GPSReceiver.GpsStatusListener {
    private static final String TAG = HomeTouristActivity.class.getSimpleName();
    private static final String RESPOND_CACHE_FILE = "worldSearchData.json";
    private static final String DEFAULT_COUNTRY = "Australia";
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private String myCountry = DEFAULT_COUNTRY;
    private Button discoverBtn;
    private DelayAutoCompleteTextView placesSearchView;
    private CardView localCV, countryCV, worldCV;
    private String discoverMode = Constants.MODE_LOCAL;
    private RequestQueue requestQueue;
    private ImageView imageView, thumbnail, thumbnailworld, logo_image;
    private TextView textView, local, world, loading;
    private GPSReceiver gpsReceiver = new GPSReceiver();
    private boolean mRequestingLocationUpdates;
    private List<SearchData> searchDataList = new ArrayList<>();
    private SearchDataAdapter searchDataAdapter;
    private SearchData selectedPlace;
    private ProgressBar progress_bar;
    LinearLayout linearLayout;
    private boolean iconSetted = false;
    private View drawerimage;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.activity_home_tourist);
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setIds();
        initFused();
        askLocationPermission();
        setModes(discoverMode);
        setListeners();
        setupReceivers();
        setSearchView();

        String oldRespond = CacheImage.readCacheFile(this, RESPOND_CACHE_FILE);//Get POI from Cache
        if (oldRespond != null) {
            try {
                POILoader.getPoi(new JSONObject(oldRespond));
                onGetPoi();
            } catch (Exception e) {

            }

        }
        getWorldSearchData();
    }

    private void initFused() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "Got location" + mCurrentLocation);
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                getIcon();
                Log.v("locationlog", mCurrentLocation.toString());
            }
        };
        mRequestingLocationUpdates = false;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000 * 60 * 15);
        mLocationRequest.setFastestInterval(1000 * 60 * 10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void getIcon() {
        if (iconSetted) return;

        if (Geocoder.isPresent()) {
            Log.d(TAG, "getting Icon");
            Log.d(TAG, "Call geocoder");
            final Geocoder geocoder = new Geocoder(this);


            try {
                List<Address> addressList = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
                Address address = addressList.get(0);
                myCountry = address.getCountryName();
            } catch (Exception e) {
                e.printStackTrace();
                myCountry = DEFAULT_COUNTRY;

            }
            try {
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
                                    Log.i("ImageURl", url);
                                    Log.d(TAG, "Got icon");
                                    Glide.with(getApplicationContext())
                                            .load(Uri.parse(url))
                                            .into(imageView);
                                    iconSetted = true;
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void getCountryData() {
        Constants.COUNTRY_DATA.clear();
        for (SearchData sdata : Constants.WORLD_DATA) {
            if (sdata instanceof State) {
                if (((State) sdata).getIs_in_country().equals(myCountry))
                    Constants.COUNTRY_DATA.add(sdata);
            } else if (sdata instanceof Region) {
                if (((Region) sdata).getIs_in_country().equals(myCountry))
                    Constants.COUNTRY_DATA.add(sdata);
            } else if (sdata instanceof Poi) {
                if (((Poi) sdata).getIs_in_country().equals(myCountry))
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
                        if (response.isPermanentlyDenied()) {
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
        Log.d(TAG, "Start location updates");
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.d(TAG, "Request location");
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
                                    rae.startResolutionForResult(HomeTouristActivity.this, Constants.REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i("fused", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e("fused", errorMessage);
                                Toast.makeText(HomeTouristActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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

    private void setSearchView() {
        placesSearchView.setThreshold(Constants.SEARCH_THRESHOLD);
        searchDataAdapter = new SearchDataAdapter(getApplicationContext(), searchDataList);
        placesSearchView.setAdapter(searchDataAdapter);// 'this' is Activity instance


//        placesSearchView.setLoadingIndicator(
//                (android.widget.ProgressBar) findViewById(R.id.pb_loading_indicator));
        placesSearchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                selectedPlace = (SearchData) adapterView.getItemAtPosition(position);
                placesSearchView.setText(selectedPlace.getName());
                discoverBtn.performClick();
            }
        });
    }

    private void setIds() {
        linearLayout = findViewById(R.id.linearLayout);
        discoverBtn = findViewById(R.id.discoverBtn);
        placesSearchView = findViewById(R.id.home_search_places);
        localCV = findViewById(R.id.home_local_cardview);
        countryCV = findViewById(R.id.home_country_cardview);
        worldCV = findViewById(R.id.home_world_cardview);
        imageView = findViewById(R.id.thumbnail2);
        textView = findViewById(R.id.country_name);
        local = findViewById(R.id.local);
        world = findViewById(R.id.world);
        thumbnail = findViewById(R.id.thumbnail);
        thumbnailworld = findViewById(R.id.thumbnailworld);
        loading = findViewById(R.id.loading);
        progress_bar = findViewById(R.id.progress_bar);
        logo_image = findViewById(R.id.logo_image);
        drawerimage = findViewById(R.id.drawerimage);
    }

    private void setListeners() {

//        placesSearchView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!placesSearchView.getText().toString().equals("")) {
//                    discoverBtn.performClick();
//                }
//            }
//        });

        placesSearchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (placesSearchView.getText().toString().isEmpty()) return false;
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (placesSearchView.getRight() - placesSearchView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        //selectedPlace=null;
                        placesSearchView.dismissDropDown();
                        discoverBtn.performClick();
                        return true;
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() < (placesSearchView.getLeft() + placesSearchView.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())) {
                        // your action here
                        //selectedPlace=null;
                        placesSearchView.dismissDropDown();
                        discoverBtn.performClick();
                        return true;
                    }
                }
                return false;
            }

        });

        placesSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //selectedPlace=null;
                    placesSearchView.dismissDropDown();
                    discoverBtn.performClick();
                }
                return false;
            }
        });


        discoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeTouristActivity.this, MapsActivity.class);
                intent.putExtra(Constants.SELECTED_PLACE, (selectedPlace));

                if ((discoverMode.equals(Constants.MODE_WORLD) || (discoverMode.equals(Constants.MODE_COUNTRY))) && selectedPlace == null) {
                    //if (placesSearchView.getText().toString().isEmpty() || !Geocoder.isPresent()) {
                    showSelectLocationDialog();
                    return;
                    //}


//                    Log.d(TAG, "Call geocoder");
//                    final Geocoder geocoder = new Geocoder(HomeTouristActivity.this);
//
//
//                    try {
//                        List<Address> addressList = geocoder.getFromLocationName(placesSearchView.getText().toString(), 1);
//                        if (addressList.size() <= 0 ||
//                                (discoverMode.equals(Constants.MODE_COUNTRY) && !addressList.get(0).getCountryName().equalsIgnoreCase(myCountry))) {
//                            showSelectLocationDialog();
//                            return;
//                        }
//
//                        intent.putExtra(Constants.SEARCH_STRING, placesSearchView.getText().toString());
//                        startActivity(intent);
//                        return;
//
//                    } catch (Exception e) {
//                        showSelectLocationDialog();
//                        return;
//
//                    }

                }

                if (selectedPlace == null)

                {
                    intent.putExtra(Constants.LOCATION_EXTRA, mCurrentLocation);
                }

                startActivity(intent);

            }
        });
        localCV.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                placesSearchView.setVisibility(View.INVISIBLE);
                thumbnail.setImageResource(R.drawable.locationwhitetranparent);
                thumbnailworld.setImageResource(R.drawable.world2);
                discoverMode = Constants.MODE_LOCAL;
                // placesSearchView.setVisibility(View.INVISIBLE);
                setModes(discoverMode);
            }
        });
        countryCV.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                placesSearchView.setVisibility(View.VISIBLE);
                discoverMode = Constants.MODE_COUNTRY;
                thumbnail.setImageResource(R.drawable.locationnew);
                thumbnailworld.setImageResource(R.drawable.world2);
                placesSearchView.setVisibility(View.VISIBLE);

                setModes(discoverMode);
                searchDataList = Constants.COUNTRY_DATA;
                Log.v("constantlist", "size is " + searchDataList.size());
                setSearchView();
            }
        });
        worldCV.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                placesSearchView.setVisibility(View.VISIBLE);
                thumbnail.setImageResource(R.drawable.locationnew);
                thumbnailworld.setImageResource(R.drawable.world2_white);
                discoverMode = Constants.MODE_WORLD;
                placesSearchView.setVisibility(View.VISIBLE);
                setModes(discoverMode);
                searchDataList = Constants.WORLD_DATA;
                Log.v("constantlist", "size is " + searchDataList.size());
                setSearchView();
            }
        });

        drawerimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                startActivity(new Intent(HomeTouristActivity.this, TouristProfileActivity.class));
                overridePendingTransition(R.anim.lefttoright, R.anim.righttoleft);

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
                local.setTextColor(Color.WHITE);
                textView.setTextColor(Color.GRAY);
                world.setTextColor(Color.GRAY);

                countryCV.setCardBackgroundColor(Color.WHITE);
                worldCV.setCardBackgroundColor(Color.WHITE);
                break;
            case Constants.MODE_COUNTRY:
                textView.setTextColor(Color.WHITE);
                world.setTextColor(Color.GRAY);
                local.setTextColor(Color.GRAY);
                countryCV.setCardBackgroundColor(Color.parseColor("#04c4d7"));
                localCV.setCardBackgroundColor(Color.WHITE);
                worldCV.setCardBackgroundColor(Color.WHITE);
                break;
            case Constants.MODE_WORLD:
                world.setTextColor(Color.WHITE);
                textView.setTextColor(Color.GRAY);
                local.setTextColor(Color.GRAY);
                worldCV.setCardBackgroundColor(Color.parseColor("#04c4d7"));
                localCV.setCardBackgroundColor(Color.WHITE);
                countryCV.setCardBackgroundColor(Color.WHITE);
                break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        iconSetted = false;
        Log.v("calledmethod", "onResume");
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
        Log.v("calledmethod", "onGpschanged " + enabled);
        if (enabled)
            startLocationUpdates();
    }


    private void getWorldSearchData() {
        String url = Urls.BASE_URL + Urls.GET_WORLD_POI;
        Log.v("worlddata", "before sending request");
        Log.d(TAG, "getWorldSearchData");
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Got World data");
                        CacheImage.saveCacheFile(HomeTouristActivity.this, RESPOND_CACHE_FILE, response.toString());


                        Log.v("worlddata", response.toString());
                        Log.d(TAG, "Start loader");
                        POILoader.getPoi(response);
                        onGetPoi();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(HomeTouristActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
                finish();
                Log.v("worlddata", error.toString());
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                1000 * 60 * 5,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);
    }


    private void onGetPoi() {
        Log.v("countrysearchdata", "background load finished");
        Log.v("countrysearchdata", "world: " + Constants.WORLD_DATA.size() + " country: " + Constants.COUNTRY_DATA.size());
        Log.d(TAG, "Finish loader");
        progress_bar.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        logo_image.setVisibility(View.GONE);
        linearLayout.setVisibility(View.VISIBLE);
        drawerimage.setVisibility(View.VISIBLE);
    }
}