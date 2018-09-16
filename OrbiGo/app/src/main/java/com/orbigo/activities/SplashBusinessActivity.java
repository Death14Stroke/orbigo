package com.orbigo.activities;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

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
import com.orbigo.models.Business;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SplashBusinessActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks{

    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;
    private JSONObject responseJSON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_business);
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(this);
        getCategoriesList();
        getBusinessList();
    }

    private void getCategoriesList() {
        String url = Urls.BASE_URL + Urls.GET_CATEGORIES;
        JsonObjectRequest catRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("response",response.toString());
                        try {
                            String status = response.getString("status");
                            if(status.compareTo("success")==0){
                                Constants.CATEGORIES_LIST.clear();
                                JSONArray categories = response.getJSONArray("categories");
                                for(int i=0;i<categories.length();i++){
                                    JSONObject cat = categories.getJSONObject(i);
                                    Constants.CATEGORIES_LIST.add(cat.getString("name"));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        requestQueue.add(catRequest);
    }

    private void getBusinessList() {
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.B_USER_ID,mAuth.getCurrentUser().getUid());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Urls.BASE_URL + Urls.GET_BUSINESS_LIST;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, input,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        responseJSON = response;
                        getSupportLoaderManager().initLoader(0,null,SplashBusinessActivity.this).forceLoad();
                    }
                },null);
        requestQueue.add(request);
    }

    @NonNull
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        return new BusinessLoader(this,responseJSON);
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        startActivity(new Intent(SplashBusinessActivity.this, BTOBottomNavActivity.class));
        finish();
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
    }

    public static class BusinessLoader extends AsyncTaskLoader<Boolean>{

        private JSONObject response;
        public BusinessLoader(Context context,JSONObject response) {
            super(context);
            this.response = response;
        }

        @Override
        public Boolean loadInBackground() {
            try {
                Constants.BUSINESS_LIST.clear();
                JSONArray jsonArray = response.getJSONArray("businesses");
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Business b = new Business();
                    b.setAbn(jsonObject.getString("abn"));
                    b.setUser_id(jsonObject.getString("b_user_id"));
                    b.setAddress(jsonObject.getString("business_address"));
                    if(!jsonObject.isNull("business_email"))
                        b.setBus_email(jsonObject.getString("business_email"));
                    b.setBus_name(jsonObject.getString("business_name"));
                    b.setPhone_number(jsonObject.getString("business_phone_number"));
                    if(!jsonObject.isNull("capacity"))
                        b.setCapacity(jsonObject.getString("capacity"));
                    if(!jsonObject.isNull("category"))
                        b.setCategory(jsonObject.getString("category"));
                    if(!jsonObject.isNull("description"))
                        b.setDescription(jsonObject.getString("description"));
                    b.setNo_bookings(jsonObject.getString("no_of_bookings"));
                    b.setNo_visits(jsonObject.getString("no_of_visits"));
                    if(!jsonObject.isNull("opening_days"))
                        b.setOpen_days(jsonObject.getString("opening_days"));
                    if(!jsonObject.isNull("opening_hours"))
                        b.setOpen_hours(jsonObject.getString("opening_hours"));
                    if(!jsonObject.isNull("price_range"))
                        b.setPrice_range(jsonObject.getString("price_range"));
                    if(!jsonObject.isNull("business_image"))
                        b.setImage(jsonObject.getString("business_image"));
                    Log.v("businesslist",b.toString());
                    Constants.BUSINESS_LIST.add(b);
                }
                Log.v("myresponse",jsonArray.length()+"");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }
    }
}
