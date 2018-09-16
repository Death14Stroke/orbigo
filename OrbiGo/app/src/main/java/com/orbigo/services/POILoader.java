package com.orbigo.services;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.orbigo.constants.Constants;
import com.orbigo.models.Country;
import com.orbigo.models.Poi;
import com.orbigo.models.Region;
import com.orbigo.models.State;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class POILoader extends AsyncTaskLoader<Boolean> {


    private JSONObject response;

    public POILoader(Context context, JSONObject response) {
        super(context);
        this.response = response;
    }


    public static void getPoi(JSONObject response){
        try {
            String status = response.getString("status");
            Log.v("countrysearchdata", response.toString());
            if (status.equals("success")) {
                Constants.WORLD_DATA.clear();
                JSONArray countries = response.getJSONArray("countries");
                for (int i = 0; i < countries.length(); i++) {
                    JSONObject country = countries.getJSONObject(i);
                    Country c = getCountryFromJSON(country);
                    Constants.WORLD_DATA.add(c);
                }
                JSONArray states = response.getJSONArray("states");
                for (int i = 0; i < states.length(); i++) {
                    JSONObject state = states.getJSONObject(i);
                    State s = getStateFromJSON(state);
                    Constants.WORLD_DATA.add(s);
                }
                JSONArray regions = response.getJSONArray("regions");
                for (int i = 0; i < regions.length(); i++) {
                    JSONObject region = regions.getJSONObject(i);
                    Region r = getRegionFromJSON(region);
                    Constants.WORLD_DATA.add(r);
                }

                JSONArray pois = response.getJSONArray("pois");
                Log.v("searchcheck", pois.length() + "");
                for (int i = 0; i < pois.length(); i++) {
                    JSONObject poi = pois.getJSONObject(i);
                    Poi p = getPoiFromJSON(poi);
                    if (p != null) {
                        Log.v("poicheck", "added ");
                        Constants.WORLD_DATA.add(p);
                    } else {
                        Log.v("poicheck", "null poi");
                    }
                }
                Log.v("searchcheck", Constants.WORLD_DATA.size() + "");
                Log.v("searchcheck", Constants.COUNTRY_DATA.size() + "");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean loadInBackground() {
        getPoi(response);
        return true;
    }

    private static Poi getPoiFromJSON(JSONObject poi) throws JSONException {
        if (poi.isNull("location"))
            return null;
        Poi p = new Poi();
        p.setId(poi.getString("id"));
        p.setName(poi.getString("name"));
        p.setIs_in_region(poi.getString("is_in_region"));
        p.setIs_in_country(poi.getString("is_in_country"));
        p.setIs_in_state(poi.getString("is_in_state"));
        //   Log.v("deletepoiid",p.getId());
        JSONObject loc = new JSONObject(poi.getString("location"));
        p.setLocation(new LatLng(loc.getDouble("lat"), loc.getDouble("lng")));
        p.setAddress(poi.getString("address"));
        p.setCategory(poi.getString("category"));
        p.setDescription(poi.getString("description"));
        p.setEventdate(poi.getString("eventdate"));
        p.setPhone(poi.getString("phone"));
        p.setUrl(poi.getString("url"));
        p.setPicture(poi.getString("picture"));
        return p;
    }

    private static Region getRegionFromJSON(JSONObject region) throws JSONException {
        Region r = new Region();
        r.setId(region.getString("id"));
        r.setName(region.getString("name"));
        r.setIs_in_state(region.getString("is_in_state"));
        r.setIs_in_country(region.getString("is_in_country"));
        return r;
    }

    private static State getStateFromJSON(JSONObject state) throws JSONException {
        State s = new State();
        s.setId(state.getString("id"));
        s.setName(state.getString("name"));
        s.setIs_in_country(state.getString("is_in_country"));
        return s;
    }

    private static Country getCountryFromJSON(JSONObject country) throws JSONException {
        Country c = new Country();
        c.setId(country.getString("id"));
        c.setName(country.getString("name"));
        c.setFlagUrl(country.getString("flagUrl"));
        c.setIconUrl(country.getString("iconUrl"));
        JSONObject bounds = new JSONObject(country.getString("latLngBounds"));
        LatLng ne = new LatLng(bounds.getJSONObject("northeast").getDouble("lat"), bounds.getJSONObject("northeast").getDouble("lng"));
        LatLng sw = new LatLng(bounds.getJSONObject("southwest").getDouble("lat"), bounds.getJSONObject("northeast").getDouble("lng"));
        c.setLatLngBounds(new LatLngBounds(sw, ne));
        return c;
    }
}

