package com.ds14.darren.orbigo.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ds14.darren.orbigo.activities.EarningsActivity;
import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.constants.DatabaseKeys;
import com.ds14.darren.orbigo.constants.Urls;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;


public class StatsFragment extends Fragment {
    private TextView Business_listed,last_earn,Visit_last,Booking_last,Active_campaign,Campaign_last;
    private Button viewHistoryBtn;
    private RequestQueue requestQueue;
    private FirebaseAuth mauth;
    private ProgressDialog progressDialog;
    public StatsFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        init(view);
        setListeners();
        progressDialog.show();
        getStats();
        return view;
    }

    private void setListeners() {
        viewHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),EarningsActivity.class));
            }
        });
    }

    private void init(View view) {
        Business_listed = view.findViewById(R.id.business_listed);
        mauth = FirebaseAuth.getInstance();
        last_earn = view.findViewById(R.id.earnLast);
        Visit_last = view.findViewById(R.id.visitLast);
        Booking_last = view.findViewById(R.id.bookingLast);
        Active_campaign = view.findViewById(R.id.activeCampaign);
        Campaign_last = view.findViewById(R.id.campaignLast);
        viewHistoryBtn = view.findViewById(R.id.view_history_btn);
        requestQueue = Volley.newRequestQueue(getActivity());
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading your business stats...");
        progressDialog.setTitle("Statistics");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    private void getStats() {
        String user_id = mauth.getCurrentUser().getUid();
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(DatabaseKeys.B_USER_ID,user_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Urls.BASE_URL + Urls.GET_STATS;
        Log.v("myresponse",jsonObject.toString());
        JsonObjectRequest jsonObjectRequest =new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Business_listed.setText(String.valueOf(response.getInt("listed_businesses")));
                    last_earn.setText(String.valueOf(response.getDouble("earnings_30_days")));
                    Visit_last.setText(String.valueOf(response.getInt("visits_30_days")));
                    Booking_last.setText(String.valueOf(response.getInt("no_of_bookings_30_days")));
                    Active_campaign.setText(String.valueOf(response.getInt("active_campaigns")));
                    Campaign_last.setText(String.valueOf(response.getInt("campaigns_30_days")));
                    progressDialog.cancel();
                    Log.v("StatsResponse",response.toString());
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
}