package com.orbigo.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.maps.android.SphericalUtil;
import com.orbigo.R;
import com.orbigo.adapters.TimelineAdapter;
import com.orbigo.constants.Constants;
import com.orbigo.constants.DatabaseKeys;
import com.orbigo.constants.Urls;
import com.orbigo.custom_ui_components.DatePickerFragment;
import com.orbigo.custom_ui_components.TimePickerFragment;
import com.orbigo.enums.Orientation;
import com.orbigo.enums.TripStatus;
import com.orbigo.helpers.APIHelper;
import com.orbigo.helpers.SwipeAndDragHelper;
import com.orbigo.helpers.Utils;
import com.orbigo.models.TripPlanItem;
import com.orbigo.services.GeofenceTransitionsIntentService;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.orbigo.constants.Constants.PLACE_PICKER_REQUEST;

public class TimelineActivity extends AppCompatActivity implements TimelineAdapter.TripPlanChangedListener,
        DatePickerFragment.DateChangeListener, TimePickerFragment.TimeChangeListener {

    private RecyclerView recyclerView;
    private TimelineAdapter timelineAdapter;
    private List<TripPlanItem> tripPlanItemList;
    private FirebaseAuth mAuth;
    private FloatingActionButton fab;
    private RequestQueue requestQueue;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private boolean mRequestingLocationUpdates;
    private GeofencingClient mGeofencingClient;
    private List<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private Button startPickBtn, startDateBtn, startTimeBtn, start_jrny;
    private Location startPointLocation;
    private TextView startPointTV, startTimeTV, startDateTV, endTimeTV, endDateTV, endDistTV, tripTitle, changeLocation, mylocation, timendate;
    private long startMillis, endMillis;
    private SimpleDateFormat timeFormat, dateFormat, dateTimeFormat;
    private ImageView back_image;
    private TextView changeDateTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(this);
        setId();
        /*ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
           // actionBar.setTitle(Html.fromHtml("<font color='#ff0000'>ActionBartitle </font>"));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }*/
        mGeofencingClient = LocationServices.getGeofencingClient(this);
        mGeofenceList = new ArrayList<>();
        tripPlanItemList = new ArrayList<>();
        startPointLocation = new Location("");
        recyclerView.setLayoutManager(getLinearLayoutManager());
        startMillis = System.currentTimeMillis();
        endMillis = System.currentTimeMillis();
        dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        timeFormat = new SimpleDateFormat("HH:mm");
        dateTimeFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm");
        initView();
        initFused();
        setListeners();
        getTripComponents();
        askLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String enter = extras.getString("enter_poi");
            String exit = extras.getString("exit_poi");
            if (enter != null) {
                for (int i = 0; i < tripPlanItemList.size(); i++) {
                    if (tripPlanItemList.get(i).getPlaceName().equals(enter)) {
                        tripPlanItemList.get(i).setTripStatus(TripStatus.ACTIVE);
                        if (i > 0) {
                            tripPlanItemList.get(i - 1).setTripStatus(TripStatus.COMPLETED);
                        }
                        timelineAdapter.notifyDataSetChanged();
                    }
                }
            }
            if (exit != null) {
                for (int i = 0; i < tripPlanItemList.size(); i++) {
                    if (tripPlanItemList.get(i).getPlaceName().equals(exit)) {
                        tripPlanItemList.get(i).setTripStatus(TripStatus.COMPLETED);
                        if (i < tripPlanItemList.size()) {
                            tripPlanItemList.get(i + 1).setTripStatus(TripStatus.ACTIVE);
                        }
                        timelineAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
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
                                    rae.startResolutionForResult(TimelineActivity.this, Constants.REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i("fused", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e("fused", errorMessage);
                                Toast.makeText(TimelineActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private void getTripComponents() {
        Log.v("gettrip", "method called");
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.TRIP_ID, Constants.PLANNING_TRIP.getId());
            String url = Urls.BASE_URL + Urls.GET_TRIP_COMPONENTS;
            Log.v("timelineresp", input.toString());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("timelineresp", response.toString());
                            try {
                                String status = response.getString("status");
                                if (status.equals("success")) {
                                    startMillis = Long.parseLong(response.getString("start_ts"));
                                    startDateTV.setText(dateFormat.format(startMillis));
                                    startTimeTV.setText(timeFormat.format(startMillis));
                                    showTimeDate();
                                    startPointLocation.setLatitude(response.getJSONObject("start_from").getDouble("lat"));
                                    startPointLocation.setLongitude(response.getJSONObject("start_from").getDouble("lng"));
                                    try {

                                        if (Geocoder.isPresent()) {
                                            Geocoder geocoder = new Geocoder(TimelineActivity.this);
                                            List<Address> addressList= geocoder.getFromLocation(startPointLocation.getLatitude(), startPointLocation.getLongitude(), 1);
                                            if (addressList.size()>0) {
                                                mylocation.setText(addressList.get(0).getAddressLine(0));
                                                startPointTV.setText(addressList.get(0).getAddressLine(0));
                                            }


                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    JSONArray points = response.getJSONArray("list");
                                    for (int i = 0; i < points.length(); i++) {
                                        JSONObject poi = points.getJSONObject(i);
                                        TripPlanItem t = new TripPlanItem();
                                        t.setRegion(poi.getString("area"));
                                        if (i == 0)
                                            t.setTripStatus(TripStatus.ACTIVE);
                                        else
                                            t.setTripStatus(TripStatus.INACTIVE);
                                        t.setPlaceName(poi.getString("name"));
                                        t.setCategory(poi.getString("category"));
                                        t.setImageurl(poi.getString("picture"));
                                        t.setTimeSpent("-");
                                        t.setCost("0");
                                        t.setId(poi.getString("id"));
                                        t.setArrivalTime(startMillis);
                                        JSONObject loc = new JSONObject(poi.getString("location"));
                                        LatLng latLng = new LatLng(loc.getDouble("lat"), loc.getDouble("lng"));
                                        t.setLatlng(latLng);
                                        tripPlanItemList.add(t);
                                        timelineAdapter.notifyDataSetChanged();
                                        mGeofenceList.add(new Geofence.Builder()
                                                .setRequestId(t.getPlaceName())
                                                .setCircularRegion(
                                                        t.getLatlng().latitude,
                                                        t.getLatlng().longitude,
                                                        200)
                                                .setExpirationDuration(1000 * 5)
                                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                                        Geofence.GEOFENCE_TRANSITION_EXIT)
                                                .build());
                                    }
                                    mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getApplicationContext(), "added geofence", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getApplicationContext(), "add failed geofence", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    getDistanceTime(startPointLocation);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            start_jrny.setVisibility(View.VISIBLE);
                        }
                    }, null);
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getDistanceTime(Location location) {
        LatLng a = new LatLng(location.getLatitude(), location.getLongitude());
        getDistanceTimeForTwo(a, tripPlanItemList.get(0).getLatlng(), 0);
        for (int i = 1; i < tripPlanItemList.size(); i++) {
            getDistanceTimeForTwo(tripPlanItemList.get(i - 1).getLatlng(), tripPlanItemList.get(i).getLatlng(), i);
        }
        getDistanceTimeForTwo(tripPlanItemList.get(tripPlanItemList.size() - 1).getLatlng(), a, tripPlanItemList.size());
    }

    private void getDistanceTimeForTwo(LatLng a, LatLng b, final int pos) {
        StringBuilder urlBuilder = new StringBuilder();
        Log.v("distanceurl", "size is " + tripPlanItemList.size());
        urlBuilder.append("https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=");
        urlBuilder.append(a.latitude);
        urlBuilder.append(",");
        urlBuilder.append(a.longitude);
        urlBuilder.append("&destinations=");
        urlBuilder.append(b.latitude);
        urlBuilder.append(",");
        urlBuilder.append(b.longitude);
        urlBuilder.append("&key=");
        urlBuilder.append(Constants.API_KEY);
        String url = urlBuilder.toString();
        Log.v("distanceurl", url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray rows = response.getJSONArray("rows");
                            for (int j = 0; j < rows.length(); j++) {
                                JSONObject rowsElement = rows.getJSONObject(j);
                                JSONArray elements = rowsElement.getJSONArray("elements");
                                for (int k = 0; k < elements.length(); k++) {
                                    JSONObject result = elements.getJSONObject(k);
                                    Log.v("mapsresponse", result.toString());
                                    if (result.getString("status").compareToIgnoreCase("OK") == 0) {
                                        long distance = result.getJSONObject("distance").getLong("value");
                                        String distanceS = result.getJSONObject("distance").getString("text");
                                        long time = result.getJSONObject("duration").getLong("value") * 1000;
                                        String timeS = result.getJSONObject("duration").getString("text");

                                        if (pos == 0) {
                                            Log.v("timeresponse", "old time is " + startMillis);
                                            Log.v("timeresponse", "diff is " + time);
                                            tripPlanItemList.get(pos).setDistance(distance);
                                            tripPlanItemList.get(pos).setArrivalTime(startMillis + time);
                                            tripPlanItemList.get(pos).setTime(time);
                                            tripPlanItemList.get(pos).setTotalTime(time);
                                            tripPlanItemList.get(pos).setTotalDistance(distance);

                                            timelineAdapter.notifyDataSetChanged();
                                            Log.v("timeresponse", "new is " + tripPlanItemList.get(pos).getArrivalTime());
                                        } else if (pos == tripPlanItemList.size()) {
                                            endMillis = tripPlanItemList.get(tripPlanItemList.size() - 1).getArrivalTime() + time;
                                            endDateTV.setText(dateFormat.format(endMillis));
                                            endTimeTV.setText(timeFormat.format(endMillis));
                                            endDistTV.setText(distanceS);
                                        } else {
                                            tripPlanItemList.get(pos).setDistance(distance);
                                            tripPlanItemList.get(pos).setArrivalTime(tripPlanItemList.get(pos - 1).getArrivalTime() + time);
                                            tripPlanItemList.get(pos).setTime(time);
                                            timelineAdapter.notifyDataSetChanged();

                                            tripPlanItemList.get(pos).setTotalTime(time + tripPlanItemList.get(pos - 1).getTotalTime());
                                            tripPlanItemList.get(pos).setTotalDistance(distance + tripPlanItemList.get(pos - 1).getTotalDistance());
                                        }


                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_timeline, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home_logout:
                mAuth.signOut();
                finishAffinity();
                startActivity(new Intent(TimelineActivity.this, LoginActivity.class));
                break;
            case R.id.home_profile:
                Intent intent = new Intent(TimelineActivity.this, TouristProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.add_member:
                intent = new Intent(TimelineActivity.this, TripMembersActivity.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }

    public void addMember(View v) {
        Intent intent = new Intent(TimelineActivity.this, TripMembersActivity.class);
        startActivity(intent);
    }

    private void initView() {
        Log.v("timeresponse", "init view called");
        timelineAdapter = new TimelineAdapter(TimelineActivity.this, tripPlanItemList, Orientation.VERTICAL, false);
        SwipeAndDragHelper swipeAndDragHelper = new SwipeAndDragHelper(timelineAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeAndDragHelper);
        touchHelper.attachToRecyclerView(recyclerView);
        timelineAdapter.setTouchHelper(touchHelper);
        recyclerView.setAdapter(timelineAdapter);
    }

    private RecyclerView.LayoutManager getLinearLayoutManager() {
        recyclerView.setNestedScrollingEnabled(true);
        return new LinearLayoutManager(TimelineActivity.this) {
            @Override
            public boolean canScrollVertically() {
                return true;
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                startPointLocation.setLatitude(place.getLatLng().latitude);
                startPointLocation.setLongitude(place.getLatLng().longitude);
                mylocation.setText(place.getAddress());
                startPointTV.setText(place.getAddress());
                getDistanceTime(startPointLocation);
                APIHelper.update_trips(requestQueue, Constants.PLANNING_TRIP.getId(), startMillis, Constants.PLANNING_TRIP.getName(), startPointLocation);
            }
        }
    }

    @Override
    public void onItemSwapped() {
        Toast.makeText(getApplicationContext(), "Item swapped", Toast.LENGTH_SHORT).show();
        if (startPointLocation != null)
            getDistanceTime(startPointLocation);
    }

    @Override
    public void onItemRemoved() {
        Toast.makeText(getApplicationContext(), "Item removed", Toast.LENGTH_SHORT).show();
        if (startPointLocation != null)
            getDistanceTime(startPointLocation);
    }

    public void showTimePickerDialog() {
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.setDate(new Date(startMillis));
        newFragment.show(getFragmentManager(), "startTime");
    }

    public void showDatePickerDialog() {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setDate(new Date(startMillis));
        newFragment.show(getFragmentManager(), "startDate");
    }

    private void setId() {
        fab = findViewById(R.id.fab);
        changeLocation = findViewById(R.id.changeLocation);
        changeDateTime = findViewById(R.id.changeDateTime);
        mylocation = findViewById(R.id.mylocation);
        start_jrny = findViewById(R.id.start_jrny);
        startPickBtn = findViewById(R.id.changeStartBtn);
        startPointTV = findViewById(R.id.start_point_tv);
        recyclerView = findViewById(R.id.timelineRV);
        startDateBtn = findViewById(R.id.changeStartDateBtn);
        startTimeBtn = findViewById(R.id.changeStartTimeBtn);
        startDateTV = findViewById(R.id.start_date_tv);
        startTimeTV = findViewById(R.id.start_time_tv);
        timendate = findViewById(R.id.timendate);
        endDateTV = findViewById(R.id.end_date_tv);
        endTimeTV = findViewById(R.id.end_time_tv);
        endDistTV = findViewById(R.id.end_dist_tv);
        tripTitle = findViewById(R.id.tripTitle);
        tripTitle.setText(Constants.PLANNING_TRIP.getName());
        back_image = findViewById(R.id.back_image);
        start_jrny.setVisibility(View.GONE);
    }


    private void setListeners() {

        back_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        startDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        startTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=Ahmedabad");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });


        start_jrny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(TimelineActivity.this, PathGoogleMapActivity.class);
                mapIntent.putExtra("tripPlanItemList", (Serializable) tripPlanItemList);
                //  mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });


        changeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                builder.setLatLngBounds(Utils.toBounds(new LatLng(startPointLocation.getLatitude(),startPointLocation.getLongitude()),5000));

                try {
                    startActivityForResult(builder.build(TimelineActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        changeDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
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

    private void initFused() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCurrentLocation = locationResult.getLastLocation();
                if (startPointLocation == null)
                    Log.v("locationlog", mCurrentLocation.toString());
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
    public void onDateChanged(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startMillis);
        c.set(year, month, day);
        startMillis = c.getTimeInMillis();
        Toast.makeText(getApplicationContext(), startMillis + "", Toast.LENGTH_SHORT).show();
        showTimePickerDialog();
    }

    @Override
    public void onTimeChanged(int hour, int minute, String tag) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startMillis);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        startMillis = c.getTimeInMillis();
        Toast.makeText(getApplicationContext(), startMillis + "", Toast.LENGTH_SHORT).show();
        showTimeDate();
        getDistanceTime(startPointLocation);
        APIHelper.update_trips(requestQueue, Constants.PLANNING_TRIP.getId(), startMillis, Constants.PLANNING_TRIP.getName(), startPointLocation);
    }

    private void showTimeDate() {
        timendate.setText(timeFormat.format(startMillis) + "/" + dateFormat.format(startMillis));
        startDateTV.setText(dateFormat.format(startMillis));
    }
}