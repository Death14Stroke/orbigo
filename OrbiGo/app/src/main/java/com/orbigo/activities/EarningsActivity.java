package com.orbigo.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.orbigo.R;
import com.orbigo.adapters.EarningHistoryAdapter;
import com.orbigo.constants.DatabaseKeys;
import com.orbigo.constants.Urls;
import com.orbigo.models.EarningHistoryModel;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EarningsActivity extends AppCompatActivity {
    private EarningHistoryAdapter adapter;
    private List<EarningHistoryModel> earningHistoryModels = new ArrayList<EarningHistoryModel>();
    private RequestQueue requestQueue;
    private ProgressDialog progressDialog;
    RecyclerView recyclerView;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earnings);
        init();
        progressDialog.show();
        getEarningsData();
    }

    private void init() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        auth = FirebaseAuth.getInstance();
        recyclerView =findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        adapter = new EarningHistoryAdapter(getApplicationContext(),earningHistoryModels);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(adapter);
        requestQueue = Volley.newRequestQueue(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading your history...");
        progressDialog.setTitle("Earnings");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    private void getEarningsData() {
        JSONObject jsonObject =new JSONObject();
        try {
            jsonObject.put(DatabaseKeys.B_USER_ID,auth.getCurrentUser().getUid());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Urls.BASE_URL + Urls.GET_EARNINGS_HISTORY;
        Log.d("earnings",jsonObject.toString());
        JsonObjectRequest jsonObjectRequest =new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("earnings",response.toString());
                try {
                    if(response.getString("status").compareTo("success")==0){
                        JSONArray jsonArray = response.getJSONArray("earnings");
                        for (int i=0;i<jsonArray.length();i++){
                            JSONObject j = jsonArray.getJSONObject(i);
                            EarningHistoryModel earningHistoryModel = new EarningHistoryModel();
                            earningHistoryModel.setAbn(j.getString("abn"));
                            earningHistoryModel.setAmount(j.getString("amount"));
                            earningHistoryModel.setDate(j.getString("create_ts"));
                            earningHistoryModel.setBusiness_user_id(j.getString("b_user_id"));
                            earningHistoryModel.setBooking_id(j.getString("booking_id"));
                            earningHistoryModels.add(earningHistoryModel);
                            adapter.notifyDataSetChanged();
                        }
                        progressDialog.cancel();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);
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
}