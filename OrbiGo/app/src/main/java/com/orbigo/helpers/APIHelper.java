package com.orbigo.helpers;


import android.location.Location;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.orbigo.constants.DatabaseKeys;
import com.orbigo.constants.Urls;
import com.orbigo.models.Poi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

public class APIHelper {


    public static void like(boolean value, String uid, String poiId, RequestQueue requestQueue, final Observer observer) {

        JSONObject input = new JSONObject();
        try {
            String url;
            input.put(DatabaseKeys.USER_ID, uid);
            if (value) {
                url = Urls.BASE_URL + Urls.ADD_SAVED_POI;
                input.put("add_poi", poiId);
                Log.v("addpoi", input.toString());
            } else {
                input.put("del_poi", poiId);
                url = Urls.BASE_URL + Urls.REMOVE_SAVED_POI;
                Log.v("delpoi", input.toString());
            }


            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("addpoi", response.toString());
                            observer.update(null, response);
                        }
                    }, null);
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void getSavedPois(String uid, RequestQueue requestQueue, final Observer observer) {
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.USER_ID, uid);
            String url = Urls.BASE_URL + Urls.GET_SAVED_POIS;
            Log.v("loadsaved", input.toString());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("loadsaved", response.toString());
                            List<Poi> mySavedPoiList = new ArrayList<>();
                            try {
                                String status = response.getString("status");
                                if (status.equals("success")) {
                                    JSONArray savedPois = response.getJSONArray("poi_details");
                                    for (int i = 0; i < savedPois.length(); i++) {
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
                                    }

                                    observer.update(null, mySavedPoiList);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("error load saved poi", error.toString());
                    observer.update(null, new ArrayList<>());
                }
            });
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public static void update_trips(RequestQueue requestQueue, String trip_id, long start_ts, String trip_name, Location location){
        try {
        JSONObject input = new JSONObject();
        input.put(DatabaseKeys.TRIP_ID, trip_id);
            input.put(DatabaseKeys.TRIP_NAME, trip_name);
            input.put(DatabaseKeys.TRIP_START_TS,String.valueOf(start_ts));


            JSONObject latlng = new JSONObject();
            latlng.put("lat", location.getLatitude());
            latlng.put("lng", location.getLongitude());
            input.put(DatabaseKeys.TRIP_START_FROM, latlng);

            String url = Urls.BASE_URL + Urls.UPDATE_TRIPS;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("loadsaved", response.toString());


                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("error load saved poi", error.toString());

                }
            });
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
