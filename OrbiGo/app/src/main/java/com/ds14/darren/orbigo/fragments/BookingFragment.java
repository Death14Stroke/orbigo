package com.ds14.darren.orbigo.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.adapters.MySpinnerAdapter;
import com.ds14.darren.orbigo.adapters.PendingBookingAdapter;
import com.ds14.darren.orbigo.constants.Constants;
import com.ds14.darren.orbigo.constants.DatabaseKeys;
import com.ds14.darren.orbigo.constants.Urls;
import com.ds14.darren.orbigo.models.Business;
import com.ds14.darren.orbigo.models.PendingBooking;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.VERTICAL;

public class BookingFragment extends Fragment {

    private Spinner spinner;
    private List<Business> businessList;
    private MySpinnerAdapter mySpinnerAdapter;
    private RequestQueue requestQueue;
    private RecyclerView recyclerView;
    private List<PendingBooking> pendingBookingList = new ArrayList<>();
    private PendingBookingAdapter adapter;
    private FirebaseAuth mAuth;

    public BookingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_booking, container, false);
        requestQueue = Volley.newRequestQueue(getContext());
        mAuth = FirebaseAuth.getInstance();
        spinner = v.findViewById(R.id.booking_spinner);
        recyclerView = v.findViewById(R.id.booking_details_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new PendingBookingAdapter(getContext(),pendingBookingList);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), VERTICAL);
        recyclerView.addItemDecoration(decoration);
        businessList = Constants.BUSINESS_LIST;
        mySpinnerAdapter = new MySpinnerAdapter(getContext(),R.layout.custom_spinner,businessList);
        spinner.setAdapter(mySpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final JSONObject reqJSON = new JSONObject();
                try {
                    reqJSON.put(DatabaseKeys.AUTHORIZED_BUSINESS_NUMBER,businessList.get(position).getAbn());
                    String url = Urls.BASE_URL + Urls.GET_PENDING_BOOKINGS;
                    Log.v("bookingsrequest",reqJSON.toString());
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, reqJSON,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        Log.v("bookingsrequest",response.toString());
                                        if(response.getString("status").compareTo("success")==0){
                                            JSONArray jsonArray = response.getJSONArray("requests");
                                            pendingBookingList.clear();
                                            for(int i=0;i<jsonArray.length();i++){
                                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                                PendingBooking pb = new PendingBooking();
                                                pb.setBooking_id(jsonObject.getString("booking_id"));
                                                pb.setCustomer_name(jsonObject.getString("customer_name"));
                                                pb.setNo_of_adult(jsonObject.getString("no_of_adult"));
                                                pb.setNo_of_children(jsonObject.getString("no_of_children"));
                                                pb.setArrival(jsonObject.getString("for_ts"));
                                                pendingBookingList.add(pb);
                                            }
                                            adapter.notifyDataSetChanged();
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
                    requestQueue.add(jsonObjectRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return v;
    }
}