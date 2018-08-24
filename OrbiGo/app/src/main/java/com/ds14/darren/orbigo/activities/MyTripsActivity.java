package com.ds14.darren.orbigo.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.adapters.MyTripsAdapter;
import com.ds14.darren.orbigo.constants.DatabaseKeys;
import com.ds14.darren.orbigo.constants.Urls;
import com.ds14.darren.orbigo.models.MeddsModel;
import com.ds14.darren.orbigo.models.Trip;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MyTripsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyTripsAdapter myTripsAdapter;
    private List<Trip> myTripList;
    private RequestQueue requestQueue;
    private FirebaseAuth mAuth;
    private SimpleDateFormat simpleDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trips);
        requestQueue = Volley.newRequestQueue(this);
        mAuth = FirebaseAuth.getInstance();
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        recyclerView = findViewById(R.id.myTripsRV);
        myTripList = new ArrayList<>();
        myTripsAdapter = new MyTripsAdapter(this,myTripList);
        recyclerView.setAdapter(myTripsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        getMyTrips();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getMyTrips() {
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.USER_ID,mAuth.getCurrentUser().getUid());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Urls.BASE_URL + Urls.GET_MY_TRIPS;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if(status.equals("success")){
                                JSONArray myTrips = response.getJSONArray("trips");
                                for(int i=0;i<myTrips.length();i++){
                                    JSONObject tripJSON = myTrips.getJSONObject(i);
                                    Trip t = new Trip();
                                    t.setId(tripJSON.getString("trip_id"));
                                    t.setName(tripJSON.getString("trip_name"));
                                    simpleDateFormat = new SimpleDateFormat("d MMMM yyyy");
                                    t.setCreatedDate(simpleDateFormat.format(Long.parseLong(tripJSON.getString("created_ts"))));
                                    myTripList.add(t);
                                    myTripsAdapter.notifyDataSetChanged();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },null);
        requestQueue.add(jsonObjectRequest);
    }
}