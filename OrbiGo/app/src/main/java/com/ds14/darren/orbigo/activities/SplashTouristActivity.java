package com.ds14.darren.orbigo.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.constants.Constants;
import com.ds14.darren.orbigo.constants.Urls;
import com.ds14.darren.orbigo.models.Country;
import com.ds14.darren.orbigo.models.Poi;
import com.ds14.darren.orbigo.models.Region;
import com.ds14.darren.orbigo.models.State;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SplashTouristActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks{

    private RequestQueue requestQueue;
    private JSONObject responseJSON;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_tourist);
        requestQueue = Volley.newRequestQueue(SplashTouristActivity.this);
        getWorldSearchData();
    }

    private void getWorldSearchData() {
        String url = Urls.BASE_URL + Urls.GET_WORLD_POI;
        Log.v("worlddata","before sending request");
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        responseJSON = response;
                        Log.v("worlddata", response.toString());
                        getSupportLoaderManager().initLoader(0, null, SplashTouristActivity.this).forceLoad();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SplashTouristActivity.this,"An error occured",Toast.LENGTH_SHORT).show();
                finish();
                Log.v("worlddata",error.toString());
            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                1000*60*5,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.v("loader","loader created");
        return new POILoader(SplashTouristActivity.this,responseJSON);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        Log.v("countrysearchdata","background load finished");
        Log.v("countrysearchdata","world: "+Constants.WORLD_DATA.size()+" country: "+Constants.COUNTRY_DATA.size());
        startActivity(new Intent(this,HomeActivity.class));
        finish();
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

    public static class POILoader extends AsyncTaskLoader<Boolean> {

        private JSONObject response;

        public POILoader(Context context, JSONObject response) {
            super(context);
            this.response = response;
        }

        @Override
        public Boolean loadInBackground() {
            try {
                String status = response.getString("status");
                Log.v("countrysearchdata",response.toString());
                if(status.equals("success")){
                    Constants.WORLD_DATA.clear();
                    JSONArray countries = response.getJSONArray("countries");
                    for(int i=0;i<countries.length();i++){
                        JSONObject country = countries.getJSONObject(i);
                        Country c = getCountryFromJSON(country);
                        Constants.WORLD_DATA.add(c);
                    }
                    JSONArray states = response.getJSONArray("states");
                    for(int i=0;i<states.length();i++){
                        JSONObject state = states.getJSONObject(i);
                        State s = getStateFromJSON(state);
                        Constants.WORLD_DATA.add(s);
                    }
                    JSONArray regions = response.getJSONArray("regions");
                    for(int i=0;i<regions.length();i++){
                        JSONObject region = regions.getJSONObject(i);
                        Region r = getRegionFromJSON(region);
                        Constants.WORLD_DATA.add(r);
                    }
                    JSONArray pois = response.getJSONArray("pois");
                    Log.v("searchcheck",pois.length()+"");
                    for(int i=0;i<pois.length();i++){
                        JSONObject poi = pois.getJSONObject(i);
                        Poi p = getPoiFromJSON(poi);
                        if(p!=null) {
                            Log.v("poicheck","added ");
                            Constants.WORLD_DATA.add(p);
                        }
                        else{
                            Log.v("poicheck","null poi");
                        }
                    }
                    Log.v("searchcheck",Constants.WORLD_DATA.size()+"");
                    Log.v("searchcheck",Constants.COUNTRY_DATA.size()+"");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        private Poi getPoiFromJSON(JSONObject poi) throws JSONException {
            if(poi.isNull("location"))
                return null;
            Poi p = new Poi();
            p.setId(poi.getString("id"));
            p.setName(poi.getString("name"));
            p.setIs_in_region(poi.getString("is_in_region"));
            p.setIs_in_country(poi.getString("is_in_country"));
            p.setIs_in_state(poi.getString("is_in_state"));
            //   Log.v("deletepoiid",p.getId());
            JSONObject loc = new JSONObject(poi.getString("location"));
            p.setLocation(new LatLng(loc.getDouble("lat"),loc.getDouble("lng")));
            p.setAddress(poi.getString("address"));
            p.setCategory(poi.getString("category"));
            p.setDescription(poi.getString("description"));
            p.setEventdate(poi.getString("eventdate"));
            p.setPhone(poi.getString("phone"));
            p.setUrl(poi.getString("url"));
            p.setPicture(poi.getString("picture"));
            return p;
        }

        private Region getRegionFromJSON(JSONObject region) throws JSONException {
            Region r = new Region();
            r.setId(region.getString("id"));
            r.setName(region.getString("name"));
            r.setIs_in_state(region.getString("is_in_state"));
            r.setIs_in_country(region.getString("is_in_country"));
            return r;
        }

        private State getStateFromJSON(JSONObject state) throws JSONException {
            State s = new State();
            s.setId(state.getString("id"));
            s.setName(state.getString("name"));
            s.setIs_in_country(state.getString("is_in_country"));
            return s;
        }

        private Country getCountryFromJSON(JSONObject country) throws JSONException {
            Country c = new Country();
            c.setId(country.getString("id"));
            c.setName(country.getString("name"));
            c.setFlagUrl(country.getString("flagUrl"));
            c.setIconUrl(country.getString("iconUrl"));
            JSONObject bounds = new JSONObject(country.getString("latLngBounds"));
            LatLng ne = new LatLng(bounds.getJSONObject("northeast").getDouble("lat"),bounds.getJSONObject("northeast").getDouble("lng"));
            LatLng sw = new LatLng(bounds.getJSONObject("southwest").getDouble("lat"),bounds.getJSONObject("northeast").getDouble("lng"));
            c.setLatLngBounds(new LatLngBounds(sw,ne));
            return c;
        }
    }
}