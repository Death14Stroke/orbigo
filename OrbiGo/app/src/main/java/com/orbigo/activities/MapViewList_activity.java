
package com.orbigo.activities;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.orbigo.R;
import com.orbigo.constants.Constants;
import com.orbigo.models.NearbyPoi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapViewList_activity extends AppCompatActivity {

    Context context;
    ArrayList<NearbyPoi> searchData;
    private Location mCurrentLocation;
    RecyclerView listView;
    MapToListAdapter mapToListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
      //  mCurrentLocation=new Location();
        setContentView(R.layout.activity_map_view_list_activity);
        searchData = new ArrayList<>();
        searchData = (ArrayList<NearbyPoi>) getIntent().getSerializableExtra("regionOrState");
        Log.i("searchData", searchData.size() + "");
         listView = findViewById(R.id.list_to_map);
        MapToListAdapter mapToListAdapter = new MapToListAdapter(context, searchData);
        listView.setLayoutManager(new LinearLayoutManager(context));
        listView.setAdapter(mapToListAdapter);
        /*listView.setItemAnimator(new DefaultItemAnimator());
        listView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));*/



    }

    private void getDistanceTime(){
        for(int i=0;i<searchData.size();i++){
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=" +
                    mCurrentLocation.getLatitude() +
                    "," +
                    mCurrentLocation.getLongitude() +
                    "&destinations=" +
                    searchData.get(i).getPoiDetails().getLocation().latitude +
                    "," +
                    searchData.get(i).getPoiDetails().getLocation().longitude +
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
                                            searchData.get(i).setDistance(distance);
                                            searchData.get(i).setTime(time);
                                            mapToListAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },null);
            //requestQueue.add(timeRequest);
        }
       /* for(int i=0;i<nearbyPoiList.size();i++){
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
        }*/
    }


}