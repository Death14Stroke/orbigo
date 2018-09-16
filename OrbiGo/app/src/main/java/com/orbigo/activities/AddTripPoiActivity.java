package com.orbigo.activities;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.orbigo.R;
import com.orbigo.constants.Constants;
import com.orbigo.constants.DatabaseKeys;
import com.orbigo.constants.Urls;
import com.orbigo.custom_ui_components.AddTripDialogFragment;
import com.orbigo.helpers.Utils;
import com.orbigo.models.NearbyPoi;
import com.orbigo.models.Trip;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class AddTripPoiActivity extends AppCompatActivity implements OnMapReadyCallback, AddTripDialogFragment.AddTripDialogListener {
    public static final String TAG = AddTripPoiActivity.class.getSimpleName();

    private NearbyPoi nearbyPoi;

    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private GoogleMap mMap;
    private RequestQueue requestQueue;
    private Button buttonContinue;
    private Location currentLocation;
    private TextView tv_note;
    private TextView tv_cost;
    private RelativeLayout rl_selectTime;
    private TextView tv_calendar;
    private TextView tv_bottom_calendar;
    private TextView tv_bottom_time;


    public static void start(Context context, NearbyPoi poi, Location location) {
        Intent intent = new Intent(context, AddTripPoiActivity.class);
        intent.putExtra(Constants.SELECTED_PLACE, (Parcelable) poi);
        intent.putExtra(Constants.LOCATION_EXTRA, location);
        context.startActivity(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip_poi);
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(this);


        nearbyPoi = (NearbyPoi) getIntent().getSerializableExtra(Constants.SELECTED_PLACE);
        currentLocation = (Location) getIntent().getParcelableExtra(Constants.LOCATION_EXTRA);


        setId();
        setListeners();
        showUI();



    }

    private void showUI(){
        toolbar.setTitle(nearbyPoi.getPoiDetails().getName() + "/" + nearbyPoi.getPoiDetails().getCategory());
        toolbar.setTitleTextColor(getResources().getColor(R.color.warm_grey));
        tv_cost.setText(nearbyPoi.getCost());
        tv_note.setText(nearbyPoi.getPoiDetails().getDescription());
    }

    private void setId() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.bringToFront();

        setSupportActionBar(toolbar);

        // toolbar.canShowOverflowMenu();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(AddTripPoiActivity.this);

        buttonContinue = findViewById(R.id.button_continue);
        tv_note=findViewById(R.id.tv_note);
        tv_cost=findViewById(R.id.tv_cost);
        rl_selectTime=findViewById(R.id.rl_select_time);
        tv_bottom_calendar=findViewById(R.id.tv_bootom_calendar);
        tv_bottom_time=findViewById(R.id.tv_bootom_time);
        tv_calendar=findViewById(R.id.tv_calendar);


    }

    private void datePicker(){

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        String date_time = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                        tv_bottom_calendar.setText(date_time);
                        tv_calendar.setText(date_time);


                        //*************Call Time Picker Here ********************
                        tiemPicker();
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }
    private void tiemPicker(){
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        //mHour = hourOfDay;
                        //mMinute = minute;

                        tv_bottom_time.setText(hourOfDay + ":" + minute);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    private void setListeners() {
        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new AddTripDialogFragment();
                newFragment.show((AddTripPoiActivity.this).getFragmentManager(), Constants.ADD_TO_TRIP_DIALOG);

            }
        });
        rl_selectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker();
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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
        Utils.addMarker(this, mMap, nearbyPoi.getPoiDetails());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nearbyPoi.getPoiDetails().getLocation(), 16));
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog, Trip trip) {
        switch (dialog.getTag()) {
            case Constants.ADD_TO_TRIP_DIALOG:
                addToMyTrip(trip, Constants.SELECTED_NEARBY_POI);
                break;

        }
    }

    private void addToMyTrip(Trip trip, NearbyPoi p) {
        JSONObject input = new JSONObject();
        try {
            input.put("trip_id", trip.getId());
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
                            Toast.makeText(AddTripPoiActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    String message = error.getMessage();
                    Toast.makeText(getApplicationContext(), "error msg: " + message, Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(getApplicationContext(), "error in adding to trip", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onDialogNeutralClick(DialogFragment dialog, String name) {
        if (name.isEmpty())
            Toast.makeText(AddTripPoiActivity.this, "Name cannot be empty!", Toast.LENGTH_SHORT).show();
        else
            createTripWithPoi(name, Constants.SELECTED_NEARBY_POI);
    }

    private void createTripWithPoi(String name, NearbyPoi p) {
        if (currentLocation == null) {
            Toast.makeText(AddTripPoiActivity.this, "Current Location not available", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.USER_ID, mAuth.getCurrentUser().getUid());
            input.put(DatabaseKeys.TRIP_NAME, name);
            JSONObject latlng = new JSONObject();
            latlng.put("lat", currentLocation.getLatitude());
            latlng.put("lng", currentLocation.getLongitude());
            input.put("start_from", latlng);
            input.put("added_poi", p.getPoiDetails().getId());
            input.put("start_ts", String.valueOf(System.currentTimeMillis()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("addtrip", input.toString());
        String url = Urls.BASE_URL + Urls.CREATE_NEW_TRIP;
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v("tripadd", response.toString());
                        try {
                            String status = response.getString("status");
                            if (status.compareToIgnoreCase("success") == 0) {
                                Toast.makeText(AddTripPoiActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, null);
        requestQueue.add(objectRequest);
    }


}