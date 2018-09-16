package com.orbigo.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.orbigo.R;
import com.orbigo.adapters.MySavedPoisAdapter;
import com.orbigo.constants.DatabaseKeys;
import com.orbigo.constants.Urls;
import com.orbigo.helpers.APIHelper;
import com.orbigo.models.Poi;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class SavedPlacesActivity extends AppCompatActivity {

    private RequestQueue requestQueue;
    private FirebaseAuth mAuth;
    private List<Poi> mySavedPoiList;
    private MySavedPoisAdapter myPoisAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_places);
        requestQueue = Volley.newRequestQueue(this);
        mAuth = FirebaseAuth.getInstance();
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mySavedPoiList = new ArrayList<>();
        myPoisAdapter = new MySavedPoisAdapter(this,mySavedPoiList);
        recyclerView = findViewById(R.id.saved_places_rv);
        recyclerView.setAdapter(myPoisAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        getSavedPois();
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





    public void finish(View v){
        finish();
    }

    private void getSavedPois() {
        APIHelper.getSavedPois(mAuth.getCurrentUser().getUid(), requestQueue, new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                mySavedPoiList = (ArrayList<Poi>) arg;
                myPoisAdapter.notifyDataSetChanged();

            }
        });

        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.USER_ID,mAuth.getCurrentUser().getUid());
            String url = Urls.BASE_URL + Urls.GET_SAVED_POIS;
            Log.v("loadsaved",input.toString());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("loadsaved",response.toString());
                            try {
                                String status = response.getString("status");
                                if(status.equals("success")){
                                    JSONArray savedPois = response.getJSONArray("poi_details");
                                    for(int i=0;i<savedPois.length();i++){
                                        JSONObject item = savedPois.getJSONObject(i);
                                        Poi savedPoi = new Poi();
                                        savedPoi.setId(item.getString("id"));
                                        savedPoi.setCategory(item.getString("category"));
                                        savedPoi.setPicture(item.getString("picture"));
                                        savedPoi.setName(item.getString("name"));
                                        savedPoi.setIs_in_region(item.getString("is_in_region"));
                                        savedPoi.setIs_in_state(item.getString("is_in_state"));
                                        savedPoi.setIs_in_country(item.getString("is_in_country"));
                                        mySavedPoiList.add(savedPoi);
                                        myPoisAdapter.notifyDataSetChanged();
                                    }
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
}