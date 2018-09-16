package com.orbigo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.orbigo.R;
import com.orbigo.adapters.TripMemberAdapter;
import com.orbigo.constants.Constants;
import com.orbigo.constants.DatabaseKeys;
import com.orbigo.constants.Urls;
import com.orbigo.helpers.SwipeAndDragHelper;
import com.orbigo.models.Trip;
import com.orbigo.models.TripMember;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TripMembersActivity extends AppCompatActivity implements TripMemberAdapter.MemberRemovedListener {

    private RecyclerView recyclerView;
    private List<TripMember> tripMemberList;
    private TripMemberAdapter tripMemberAdapter;
    private LinearLayout addMemberLL;
    private RequestQueue requestQueue;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_members);
        setId();
        tripMemberList = new ArrayList<>();
        tripMemberAdapter = new TripMemberAdapter(this,tripMemberList);
        SwipeAndDragHelper swipeAndDragHelper = new SwipeAndDragHelper(tripMemberAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeAndDragHelper);
        touchHelper.attachToRecyclerView(recyclerView);
        tripMemberAdapter.setTouchHelper(touchHelper);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(tripMemberAdapter);
        requestQueue = Volley.newRequestQueue(this);
        mAuth = FirebaseAuth.getInstance();
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setListeners();
        getTripMembers();
    }

    private void getTripMembers() {
        JSONObject input = new JSONObject();
        Trip trip = Constants.PLANNING_TRIP;
        try {
            input.put(DatabaseKeys.TRIP_ID, trip.getId());
            String url = Urls.BASE_URL + Urls.GET_TRIP_MEMBERS;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String status = response.getString("status");
                                if(status.equals("success")){
                                    JSONArray members = response.getJSONArray("members");
                                    for(int i=0;i<members.length();i++){
                                        JSONObject member = members.getJSONObject(i);
                                        TripMember t = new TripMember();
                                        t.setRole("member");
                                        t.setEncodedImage("null");
                                        t.setName(member.getString("member_name"));
                                        t.setEmail(member.getString("member_email"));
                                        t.setPhone(member.getString("member_phone"));
                                        tripMemberList.add(t);
                                        tripMemberAdapter.notifyDataSetChanged();
                                    }
                                }
                                else{
                                    String message = response.getString("message");
                                    Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            getProfile();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
                }
            });
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getProfile() {
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.USER_ID,mAuth.getCurrentUser().getUid());
            String url = Urls.BASE_URL + Urls.GET_MY_PROFILE;
            final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("t_profile",response.toString());
                            try {
                                TripMember me = new TripMember();
                                String name = response.getString("name");
                                if(name.isEmpty())
                                    name = response.getString("email_address");
                                me.setName(name);
                                String encodedImage = response.getString("profile_image");
                                me.setEncodedImage(encodedImage);
                                me.setRole("you");
                                tripMemberList.add(0,me);
                                tripMemberAdapter.notifyDataSetChanged();
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
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    private void setListeners() {
        addMemberLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TripMembersActivity.this,AddBuddyActivity.class));
            }
        });
    }

    private void setId() {
        recyclerView = findViewById(R.id.trip_members_rv);
        addMemberLL = findViewById(R.id.addPersonLL);
    }

    public void finish(View v){
        finish();
    }

    @Override
    public void onMemberRemoved(TripMember member) {
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.TRIP_ID,Constants.PLANNING_TRIP.getId());
            JSONObject memberJSON = new JSONObject();
            memberJSON.put("member_name",member.getName());
            memberJSON.put("member_email",member.getEmail());
            memberJSON.put("member_phone",member.getPhone());
            input.put("member",memberJSON);
            String url = Urls.BASE_URL + Urls.REMOVE_MEMBER;
            JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("removeresponse",response.toString());
                            try {
                                String message = response.getString("message");
                                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
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
    
    @Override
    protected void onResume() {
        super.onResume();
        getTripMembers();
    }
}
