package com.orbigo.activities;
import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.VectorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.orbigo.R;
import com.orbigo.adapters.CustomInfoWindowAdapter;
import com.orbigo.adapters.MedssAdapter;
import com.orbigo.adapters.SearchDataAdapter;
import com.orbigo.constants.Constants;
import com.orbigo.constants.DatabaseKeys;
import com.orbigo.constants.Urls;
import com.orbigo.custom_ui_components.DelayAutoCompleteTextView;
import com.orbigo.helpers.BadgeDrawerToggle;
import com.orbigo.helpers.Utils;
import com.orbigo.models.Country;
import com.orbigo.models.MeddsModel;
import com.orbigo.models.NearbyPoi;
import com.orbigo.models.Poi;
import com.orbigo.models.Region;
import com.orbigo.models.SearchData;
import com.orbigo.models.State;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnPolygonClickListener,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMapLoadedCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private SlidingUpPanelLayout mSlidePanelLayout;
    private RecyclerView activityRV;
    private ImageView arrowImageView, drawerimage, car_image, plane_image, tram_image, list_view;
    private MedssAdapter activityAdapter;
    private List<MeddsModel> activityList = new ArrayList<>();
    private DelayAutoCompleteTextView placesSearchView;
    private RequestQueue requestQueue;
    private TextView nameTV, addressTV, distanceTV, timeTV, seekBarText, car_text, plane_text, tram_text;
    private CardView carCV, planeCV, trainCV;
    private String travel_mode, search_mode;
    private SeekBar seekBar;
    private boolean mRequestingLocationUpdates;
    private Button distanceBtn, timeBtn, explore;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private SearchData selectedPlace;
    private ArrayList<Marker> markers = new ArrayList<Marker>();
    ProgressBar progress_bar;
    Polygon polygon;
    Boolean mapReady = false, setProgress = false;
    Context context;
    Marker markernew;
    boolean isMapLoaded = false;
    ArrayList<NearbyPoi> regionOrState;

    DrawerLayout drawer;
    private BadgeDrawerToggle badgeToggle;
    private String searchText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        context = this;
        regionOrState = new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            selectedPlace = extras.getParcelable(Constants.SELECTED_PLACE);
            //searchText = extras.getString(Constants.SEARCH_STRING, "");
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
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

//        if (selectedPlace==null && !searchText.isEmpty()){
//            if (Geocoder.isPresent()) {
//                Geocoder geocoder = new Geocoder(MapsActivity.this);
//                List<Address> addressList = geocoder.getFromLocation(, 1);
//                Address address = addressList.get(0);
//                nameTV.setText(Utils.nullClearing(address.getSubLocality() + ", " + address.getSubAdminArea());
//                addressTV.setText(Utils.nullClearing(address.getLocality() + ", " + address.getAdminArea() + ", " + address.getCountryName());
//            }
//        }

        if (!searchText.isEmpty() && selectedPlace == null) {
            placesSearchView.setText(searchText);
        }
    }


    private void initFused() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);


                mCurrentLocation = locationResult.getLastLocation();

                try {
                    if (selectedPlace == null /*&& placesSearchView.getText().toString().isEmpty()*/) {
                        Log.d(TAG, "Call geocoder");
                        if (Geocoder.isPresent()) {
                            Geocoder geocoder = new Geocoder(MapsActivity.this);
                            List<Address> addressList = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
                            Address address = addressList.get(0);
                            nameTV.setText(Utils.nullClearing(address.getSubLocality() + ", " + address.getSubAdminArea()));
                            addressTV.setText(Utils.nullClearing(address.getLocality() + ", " + address.getAdminArea() + ", " + address.getCountryName()));
                            findMYRegion(address.getSubAdminArea());
                            if (selectedPlace == null) {
                                findMYRegion(address.getAdminArea());
                            }
                            if (isMapLoaded) {
                                handleSelection();
                            }

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.v("locationlog", mCurrentLocation.toString());
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

    private void getDistanceTime() {
        StringBuilder urlBuilder = new StringBuilder();
        if (mCurrentLocation != null && selectedPlace != null) {
            urlBuilder.append("https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=");
            urlBuilder.append(mCurrentLocation.getLatitude());
            urlBuilder.append(",");
            urlBuilder.append(mCurrentLocation.getLongitude());
            urlBuilder.append("&destinations=");
            urlBuilder.append(selectedPlace.getName());
            switch (travel_mode) {
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
            Log.v("distanceurl", url);
            JsonObjectRequest timeRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray rows = response.getJSONArray("rows");
                                for (int i = 0; i < rows.length(); i++) {
                                    JSONObject rowsElement = rows.getJSONObject(i);
                                    JSONArray elements = rowsElement.getJSONArray("elements");
                                    for (int j = 0; j < elements.length(); j++) {
                                        JSONObject result = elements.getJSONObject(j);
                                        Log.v("mapsresponse", result.toString());
                                        if (result.getString("status").compareToIgnoreCase("OK") == 0) {
                                            String distance = result.getJSONObject("distance").getString("text");
                                            String time = result.getJSONObject("duration").getString("text");
                                            distanceTV.setText(Utils.nullClearing(distance));
                                            timeTV.setText(Utils.nullClearing(time));
                                        } else {
                                            distanceTV.setText("Not available");
                                            timeTV.setText("Not available");
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, null);
            requestQueue.add(timeRequest);
        }
    }

    private void setListeners() {


        list_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (regionOrState != null) {
//                    Intent intent = new Intent(MapsActivity.this, NearbyPoiActivity.class);
//                    intent.putExtra("regionOrState", regionOrState);
//                    intent.putExtra("is_has_selected", false);
//                    startActivity(intent);
                    NearbyPoiActivity.start(MapsActivity.this,regionOrState,false,travel_mode,false,mCurrentLocation);
                } else {
                    Toast.makeText(context, "Loading data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        explore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("MapsActivity", "onPolygonClickIF-----" + mapReady + "ploygon-->" + polygon + "");
                if (mapReady) {
                    if (polygon != null) {
                        Log.i("MapsActivity", "onPolygonClickIF");
                        onPolygonClick(polygon);
                        mSlidePanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);


                    }
                }


            }
        });


        drawerimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                startActivity(new Intent(MapsActivity.this, TouristProfileActivity.class));
                overridePendingTransition(R.anim.lefttoright, R.anim.righttoleft);

            }
        });


        mSlidePanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState.equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {


                    arrowImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_drop_down_black_24dp));
                } else if (newState.equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                    if (setProgress) {
                        //progress_bar.setVisibility(View.VISIBLE);
                    }
                    arrowImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_drop_up_black_24dp));
                }

            }
        });
        mSlidePanelLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSlidePanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        planeCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                travel_mode = Constants.MODE_PLANE;
                planeCV.setCardBackgroundColor(Color.parseColor("#04c4d7"));
                plane_text.setTextColor(Color.WHITE);
                plane_image.setImageResource(R.drawable.ic_airport_departures_white);
                car_text.setTextColor(Color.GRAY);
                car_image.setImageResource(R.drawable.ic_car_front_view);
                tram_image.setImageResource(R.drawable.ic_tram_front_view);
                tram_text.setTextColor(Color.GRAY);
                trainCV.setCardBackgroundColor(Color.WHITE);
                carCV.setCardBackgroundColor(Color.WHITE);
                setProgress = true;
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

                plane_text.setTextColor(Color.GRAY);
                plane_image.setImageResource(R.drawable.ic_airport_departures);
                car_text.setTextColor(Color.GRAY);
                car_image.setImageResource(R.drawable.ic_car_front_view);
                tram_image.setImageResource(R.drawable.ic_tram_front_view_white);
                tram_text.setTextColor(Color.WHITE);
                setProgress = true;

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
                plane_text.setTextColor(Color.GRAY);
                plane_image.setImageResource(R.drawable.ic_airport_departures);
                car_text.setTextColor(Color.WHITE);
                car_image.setImageResource(R.drawable.ic_car_front_view_white);
                tram_image.setImageResource(R.drawable.ic_tram_front_view);
                tram_text.setTextColor(Color.GRAY);
                //  setProgress=true;
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
                seekBarText.setText(Utils.nullClearing(seekBar.getProgress() + " miles"));
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

                seekBarText.setText(Utils.nullClearing(seekBar.getProgress() + " min"));
                distanceBtn.setTextColor(Color.parseColor("#b7b7b7"));
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                StringBuilder builder = new StringBuilder();
                builder.append(progress);
                if (progress > 0) {
                    setProgress = true;
                }
                if (search_mode.equals(Constants.MODE_DISTANCE)) {
                    builder.append(" miles");
                } else if (search_mode.equals(Constants.MODE_TIME)) {
                    builder.append(" min");
                }
                seekBarText.setText(Utils.nullClearing(builder.toString()));

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
            input.put(DatabaseKeys.MODE, search_mode);
            if (search_mode.equals(Constants.MODE_DISTANCE))
                input.put(Constants.MODE_DISTANCE, value);
            else if (search_mode.equals(Constants.MODE_TIME))
                input.put(Constants.MODE_TIME, value);
            JSONObject loc = new JSONObject();
            LatLng latLng = getCurrentCooridinates();
            loc.put("lat", latLng.latitude);
            loc.put("lng", latLng.longitude);
            input.put("location", loc);
            String url = Urls.BASE_URL + Urls.GET_REGIONS_IN_POLYGON;
            Log.v("polyresponse", input.toString());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("polyresponse", response.toString());
                            try {
                                JSONArray regions = response.getJSONArray("regions");
                                mMap.clear();
                                polygon=null;
                                cleanMarkers();
                                for (int i = 0; i < regions.length(); i++) {
                                    plotPolygon(regions.getJSONObject(i));
                                }
                                explore.setVisibility(View.VISIBLE);
                                progress_bar.setVisibility(View.GONE);
                                mapReady = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MapsActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
                    Log.v("polyresponse", error.toString());
                }
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    1000 * 60 * 5,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void plotPolygon(JSONObject jsonObject) {
        try {
            String string = jsonObject.getString("coordinates");
            JSONArray jsonArray = new JSONArray(string);
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            int fillColor = Color.argb(30, 0, 255, 0);
            if (!jsonObject.isNull("is_current")) {
                fillColor = Color.argb(30, 250, 0, 0);
            }
            for (int i = 0; i < jsonArray.length(); i++) {
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
                    polygon = mMap.addPolygon(new PolygonOptions()
                            .addAll(latLngList)
                            .fillColor(fillColor)
                            .strokeWidth((float)4)
                            .strokeColor(Color.BLUE));
                    polygon.setClickable(true);

                    polygon.setTag(r);
                    fillContentPolygon(polygon, false, false);

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
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        badgeToggle = new BadgeDrawerToggle(
                this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(badgeToggle);
        badgeToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.aqua_blue));
        badgeToggle.setBadgeEnabled(false);
        badgeToggle.syncState();
        drawerimage = findViewById(R.id.drawerimage);
        list_view = findViewById(R.id.list_view);
        addressTV = findViewById(R.id.maps_address);
        arrowImageView = findViewById(R.id.arrow_image);
        mSlidePanelLayout = findViewById(R.id.sliding_layout);
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
        car_image = findViewById(R.id.car_image);
        tram_image = findViewById(R.id.tram_image);
        plane_image = findViewById(R.id.plane_image);
        tram_text = findViewById(R.id.tram_text);
        plane_text = findViewById(R.id.plane_text);
        car_text = findViewById(R.id.car_text);
        explore = findViewById(R.id.explore);
        progress_bar = findViewById(R.id.progress_bar);


        /*SVG svg = SVGParser.getSVGFromResource(getResources(), R.raw.android);
        PictureDrawable pictureDrawable = svg.createPictureDrawable();
        Bitmap bitmap = Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth(), pictureDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);*/


    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        //  Log.e(TAG, "getBitmap: 1");
        return bitmap;
    }


    private void setSearchView() {
        placesSearchView.setThreshold(Constants.SEARCH_THRESHOLD);
        SearchDataAdapter placesAdapter = new SearchDataAdapter(this, Constants.WORLD_DATA);
        placesSearchView.setAdapter(placesAdapter); // 'this' is Activity instance
//        placesSearchView.setLoadingIndicator(
//                (android.widget.ProgressBar) findViewById(R.id.pb_loading_indicator));
        placesSearchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                selectedPlace = (SearchData) adapterView.getItemAtPosition(position);
                mMap.clear();
                polygon=null;
                placesSearchView.setText(selectedPlace.getName());
                handleSelection();
            }
        });

//        placesSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    selectedPlace = null;
//                    placesSearchView.dismissDropDown();
//                    handleSelection();
//                }
//                return false;
//            }
//        });
//
//        placesSearchView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                final int DRAWABLE_LEFT = 0;
//                final int DRAWABLE_TOP = 1;
//                final int DRAWABLE_RIGHT = 2;
//                final int DRAWABLE_BOTTOM = 3;
//
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    if (event.getRawX() >= (placesSearchView.getRight() - placesSearchView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
//                        // your action here
//                        selectedPlace = null;
//                        placesSearchView.dismissDropDown();
//                        handleSelection();
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });


    }

    private void handleSelection() {
        View view = this.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        if (selectedPlace == null) {
//            if (!placesSearchView.getText().toString().isEmpty()) {
//                try {
//                    if (Geocoder.isPresent()) {
//                        Geocoder geocoder = new Geocoder(MapsActivity.this);
//                        List<Address> addressList = null;
//
//                        addressList = geocoder.getFromLocationName(placesSearchView.getText().toString(), 1);
//                        if (addressList.size() > 0) {
//                            findSearchRegions(placesSearchView.getText().toString());
//                            if (selectedPlace == null) {
//                                Address address = addressList.get(0);
//                                nameTV.setText(Utils.nullClearing(placesSearchView.getText().toString()));
//                                addressTV.setText(Utils.nullClearing(address.getLocality() + ", " + address.getAdminArea() + ", " + address.getCountryName()));
//
//                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                                        new LatLng(address.getLatitude(),
//                                                address.getLongitude()), 14));
//                            }
//
//
//                        }
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            } else
//                {
                moveToLocation();
//            }
            return;
        }
        nameTV.setText(Utils.nullClearing(selectedPlace.getName()));
        if (selectedPlace instanceof Country) {
            Country c = (Country) selectedPlace;
            getCountryFocus(c);
            addressTV.setText(Utils.nullClearing(c.getName()));
        } else if (selectedPlace instanceof State) {
            State s = (State) selectedPlace;
            getStateBoundary(selectedPlace, true);
            addressTV.setText(Utils.nullClearing(s.getIs_in_country()));
        } else if (selectedPlace instanceof Region) {
            Region r = (Region) selectedPlace;
            getRegionBoundary(selectedPlace, true);
            StringBuilder builder = new StringBuilder();
            builder.append(r.getIs_in_state());
            builder.append(", ");
            builder.append(r.getIs_in_country());
            addressTV.setText(Utils.nullClearing(builder.toString()));
        } else if (selectedPlace instanceof Poi) {
            Poi p = (Poi) selectedPlace;
            getPoiDetails();
            StringBuilder builder = new StringBuilder();
            builder.append(p.getIs_in_region());
            builder.append(", ");
            builder.append(p.getIs_in_state());
            builder.append(", ");
            builder.append(p.getIs_in_country());
            addressTV.setText(Utils.nullClearing(builder.toString()));
        } else {
            moveToLocation();
        }

    }

//    void findSearchRegions(String searchText) {
//        for (SearchData sdata : Constants.WORLD_DATA) {
//
//            if ((sdata.getName().equalsIgnoreCase(searchText))) {
//                if (sdata instanceof Region) {
//                    selectedPlace = sdata;
//                    handleSelection();
//                    return;
//                }
//            }
//
//
//        }
//
//        int count=0;
//        mMap.clear();
//        polygon=null;
//        cleanMarkers();
//        for (SearchData sdata : Constants.WORLD_DATA) {
//            if (sdata instanceof Region ) {
//                if (Utils.containsIgnoreCase(sdata.getName(),searchText)) {
//                    getRegionBoundary(sdata,false);
//                    count++;
//                    if (count>10) return;
//                }
//            }
//        }
//
//        if (count<=0){
//            for (SearchData sdata : Constants.WORLD_DATA) {
//                if (sdata instanceof State ) {
//                    if (Utils.containsIgnoreCase(sdata.getName(),searchText)) {
//                        getStateBoundary(sdata,false);
//                        count++;
//                        if (count>10) return;
//                    }
//                }
//            }
//        }
//
//
//        //getRegionBoundary(selectedPlace.getId(),true);
//    }



    private LatLng getCurrentCooridinates() {
        LatLng loc;
        if (mCurrentLocation == null) {
            loc = new LatLng(-33.953988, 151.179457);
        } else {
            loc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }


        if (selectedPlace == null) return loc;

        LatLngBounds latLngBounds = null;
        if (selectedPlace instanceof Country) {
            latLngBounds = ((Country) selectedPlace).getLatLngBounds();
        } else if (selectedPlace instanceof State) {
            State s = (State) selectedPlace;
            latLngBounds = ((State) selectedPlace).getLatLngBounds();
        } else if (selectedPlace instanceof Region) {
            latLngBounds = ((Region) selectedPlace).getLatLngBounds();
        } else if (selectedPlace instanceof Poi) {
            Poi p = (Poi) selectedPlace;
            return p.getLocation();
        }

        if (latLngBounds != null) {
            if (latLngBounds.contains(loc)) {
                return loc;
            } else {
                return latLngBounds.getCenter();
            }
        }


        return loc;

    }

    private void moveToLocation() {
        if (mCurrentLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude()), 16));
        }
    }

    private void addActivities() {
        MeddsModel c1 = new MeddsModel("move", R.drawable.walking_grey, true);
        activityList.add(c1);
        activityAdapter.notifyDataSetChanged();
        MeddsModel c2 = new MeddsModel("eat", R.drawable.eat_fork, true);
        activityList.add(c2);
        activityAdapter.notifyDataSetChanged();
        MeddsModel c3 = new MeddsModel("do", R.drawable.coffee, true);
        activityList.add(c3);
        activityAdapter.notifyDataSetChanged();
        MeddsModel c4 = new MeddsModel("see", R.drawable.tosee, true);
        activityList.add(c4);
        activityAdapter.notifyDataSetChanged();
        MeddsModel c5 = new MeddsModel("sleep", R.drawable.ic_outlinestar24px, true);
        activityList.add(c5);
        activityAdapter.notifyDataSetChanged();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
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

        distanceBtn.callOnClick();
        carCV.callOnClick();
        explore.callOnClick();
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
        CustomInfoWindowAdapter customInfoWindowAdapter = new CustomInfoWindowAdapter(context);
        mMap.setInfoWindowAdapter(customInfoWindowAdapter);
        Utils.addMarker(this,mMap,selectedPlace);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(p.getLocation(), 16));
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
        markernew = marker;
        selectedPlace = (SearchData) marker.getTag();
        handleSelection();
        markernew.showInfoWindow();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                markernew.showInfoWindow();
            }
        }, 2000);


        return true;
    }

    private void getRegionBoundary(final SearchData place, final boolean cleanMarkers) {
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.REGION_ID, place.getId());
            String url = Urls.BASE_URL + Urls.GET_REGION_BOUNDARY;
            final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.i("JOSONOBJ", response.toString());

                            plotSinglePolygon(response, cleanMarkers,place);


                        }
                    }, null);
            requestQueue.add(request);
            getDistanceTime();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void plotSinglePolygon(JSONObject response, boolean cleanMarkers,SearchData place) {
        try {
            String string = response.getString("coordinates");
            JSONArray jsonArray = new JSONArray(string);
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (int i = 0; i < jsonArray.length(); i++) {
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
                    polygon = mMap.addPolygon(new PolygonOptions()
                            .addAll(latLngList)
                            .fillColor(Color.argb(30, 2, 184, 208))
                            .strokeWidth((float)4)
                            .strokeColor(Color.parseColor("#02b8d0")));
                    polygon.setClickable(true);
                    polygon.setTag(place);
                }
            }
            if (place instanceof State) {
                ((State) place).setLatLngBounds(builder.build());
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(((State) place).getLatLngBounds(), 0));
            } else if (place instanceof Region) {
                ((Region) place).setLatLngBounds(builder.build());
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(((Region) place).getLatLngBounds(), 0));
            }
            fillContentPolygon(polygon, false, cleanMarkers);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        explore.setVisibility(View.VISIBLE);
        mapReady = true;

    }

    private void getStateBoundary(final SearchData place, final boolean cleanMarkers) {
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.STATE_CODE, place.getId());
            Log.v("stateboundaryresponse", "input is " + input.toString());
            String url = Urls.BASE_URL + Urls.GET_STATE_BOUNDARY;
            final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Log.v("stateboundaryresponse", "response " + response);
                            plotSinglePolygon(response, cleanMarkers,place);
                        }
                    }, null);
            requestQueue.add(request);
            getDistanceTime();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getCountryFocus(Country country) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(country.getLatLngBounds(), 0));
    }

    @Override
    public void onPolygonClick(Polygon polygon) {
        fillContentPolygon(polygon, true, true);
    }

    private void fillContentPolygon(Polygon polygon, boolean moveCamera, boolean cleanMarkers) {
        Log.i("MapsActivity", "onPolygonClick");
        SearchData searchData = (SearchData) polygon.getTag();
        nameTV.setText(Utils.nullClearing(searchData.getName()));
        if (searchData instanceof Region) {
            Toast.makeText(this, searchData.getName(), Toast.LENGTH_SHORT).show();
            if (moveCamera)
                if (((Region) searchData).getLatLngBounds()!=null)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(((Region) searchData).getLatLngBounds(), 0));
            getRegionPois(searchData.getName(), cleanMarkers);
            addressTV.setText(Utils.nullClearing(((Region) searchData).getIs_in_state() + ", " + ((Region) searchData).getIs_in_country()));
        } else if (searchData instanceof State) {
            if (moveCamera)
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(((State) searchData).getLatLngBounds(), 0));
            Toast.makeText(this, searchData.getName(), Toast.LENGTH_SHORT).show();
            getStatePois(searchData.getName(), cleanMarkers);
            addressTV.setText(Utils.nullClearing(((State) searchData).getIs_in_country()));
        }

    }

    private void cleanMarkers() {
        for (Marker marker : markers)
            marker.remove();
        markers.clear();
        regionOrState.clear();

    }

    private void getStatePois(String stateName, boolean cleanMarkers) {
        int count = 0;

        if (cleanMarkers) {
            cleanMarkers();
        }

        for (SearchData sdata : Constants.WORLD_DATA) {
            if (sdata instanceof Poi) {
                if (((Poi) sdata).getIs_in_state().compareToIgnoreCase(stateName) == 0
                        && isFilter(((Poi) sdata).getCategory())) {

                    NearbyPoi nearbyPoi = new NearbyPoi();
                    nearbyPoi.setPoiDetails((Poi) sdata);
                    regionOrState.add(nearbyPoi);

                    count++;
                    CustomInfoWindowAdapter customInfoWindowAdapter = new CustomInfoWindowAdapter(MapsActivity.this);
                    mMap.setInfoWindowAdapter(customInfoWindowAdapter);
                    Marker marker = Utils.addMarker(this,mMap,sdata);
                    markers.add(marker);
                }
            }
        }
        Toast.makeText(MapsActivity.this, count + " pois in " + stateName, Toast.LENGTH_SHORT).show();
    }



    private void getRegionPois(String regionName, boolean cleanMarkers) {
        int count = 0;

        if (cleanMarkers) {
            cleanMarkers();
        }
        for (SearchData sdata : Constants.WORLD_DATA) {
            if (sdata instanceof Poi) {
                if (((Poi) sdata).getIs_in_region().compareToIgnoreCase(regionName) == 0 && isFilter(((Poi) sdata).getCategory())) {
                    NearbyPoi nearbyPoi = new NearbyPoi();
                    nearbyPoi.setPoiDetails((Poi) sdata);
                    regionOrState.add(nearbyPoi);
                    count++;
                    CustomInfoWindowAdapter customInfoWindowAdapter = new CustomInfoWindowAdapter(MapsActivity.this);
                    mMap.setInfoWindowAdapter(customInfoWindowAdapter);

                    Marker marker = Utils.addMarker(this,mMap,sdata);
                    markers.add(marker);
                }
            }
        }
        Toast.makeText(MapsActivity.this, count + " pois in " + regionName, Toast.LENGTH_SHORT).show();
    }

    private void findMYRegion(String regionName) {


        for (SearchData sdata : Constants.WORLD_DATA) {
            if (sdata instanceof Region || sdata instanceof State || sdata instanceof Country) {
                if ((sdata.getName().equalsIgnoreCase(regionName))) {
                    selectedPlace = sdata;
                    return;
                }
            }
        }

    }


    private boolean isFilter(String category) {

        int poiFilterId = Constants.getFilterId(category);

        if (activityList.get(poiFilterId).isSelected()) {
            return true;
        } else {
            return false;
        }


    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        selectedPlace = (SearchData) marker.getTag();
        Constants.SELECTED_DATA = selectedPlace;
//        Intent i = new Intent(MapsActivity.this, NearbyPoiActivity.class);
//        i.putExtra("travel_mode", travel_mode);
//        i.putExtra("is_has_selected", true);
//        i.putExtra("regionOrState", regionOrState);
//        startActivity(i);

        NearbyPoiActivity.start(MapsActivity.this,regionOrState,true,travel_mode,false,mCurrentLocation);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(MapsActivity.this, "Taking to your location...", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMapLoaded() {
        isMapLoaded = true;
        if (selectedPlace != null) {
            placesSearchView.setText(selectedPlace.getName());
        }
        handleSelection();

        setSearchView();
    }
}