package com.ds14.darren.orbigo.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ds14.darren.orbigo.activities.ListBusinessActivity;
import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.adapters.BusinessAdapter;
import com.ds14.darren.orbigo.constants.Constants;
import com.ds14.darren.orbigo.constants.DatabaseKeys;
import com.ds14.darren.orbigo.constants.Urls;
import com.ds14.darren.orbigo.models.Business;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.VERTICAL;

public class BusinessFragment extends Fragment {

    private RecyclerView recyclerView;
    private BusinessAdapter businessAdapter;
    private List<Business> businessList = new ArrayList<>();
    private List<String> categoriesList = new ArrayList<>();
    private TextView countTV;
    private Button newBusinessBtn;

    public BusinessFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_business, container, false);
        recyclerView = v.findViewById(R.id.businessListRV);
        countTV = v.findViewById(R.id.businessCount);
        newBusinessBtn = v.findViewById(R.id.newBusinessBtn);
        newBusinessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),ListBusinessActivity.class));
            }
        });
        businessList = Constants.BUSINESS_LIST;
        countTV.setText(String.valueOf(businessList.size()));
        categoriesList = Constants.CATEGORIES_LIST;
        businessAdapter = new BusinessAdapter(getContext(),businessList, categoriesList);
        recyclerView.setAdapter(businessAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), VERTICAL);
        recyclerView.addItemDecoration(decoration);
        return v;
    }
}