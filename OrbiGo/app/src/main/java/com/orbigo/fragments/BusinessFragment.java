package com.orbigo.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.orbigo.activities.BTOBottomNavActivity;
import com.orbigo.activities.ListBusinessActivity;
import com.orbigo.R;
import com.orbigo.adapters.BusinessAdapter;
import com.orbigo.constants.Constants;
import com.orbigo.models.Business;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static android.support.v7.widget.RecyclerView.VERTICAL;

public class BusinessFragment extends Fragment implements Constants.ChangeBusinessListener {

    private RecyclerView recyclerView;
    private BusinessAdapter businessAdapter;
    private List<Business> businessList = new ArrayList<>();
    private List<String> categoriesList = new ArrayList<>();
    private TextView countTV;
    private View newBusinessBtn;
    private TextView tv_edit;

    public BusinessFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_business, container, false);
        tv_edit = v.findViewById(R.id.tv_edit);
        tv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), ListBusinessActivity.class);
                intent.putExtra(ListBusinessActivity.NEW_BUSINESS,false);
                startActivity(intent);
            }
        });
        recyclerView = v.findViewById(R.id.businessListRV);
        countTV = v.findViewById(R.id.businessCount);
        newBusinessBtn = v.findViewById(R.id.newBusinessBtn);
        newBusinessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), ListBusinessActivity.class);
                intent.putExtra(ListBusinessActivity.NEW_BUSINESS,true);
                startActivity(intent);
            }
        });
        businessList = Constants.BUSINESS_LIST;
        updateUI();
        categoriesList = Constants.CATEGORIES_LIST;
        businessAdapter = new BusinessAdapter(getContext(), businessList, categoriesList, new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                if ((getActivity()) != null) {
                    ((BTOBottomNavActivity)getActivity()).uploadPicture("");
                }
            }
        });
        recyclerView.setAdapter(businessAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), VERTICAL);
        recyclerView.addItemDecoration(decoration);
        Constants.addChangeBusinessListener(this);
        return v;
    }

    private void updateUI(){
        businessList = Constants.BUSINESS_LIST;
        countTV.setText(String.valueOf(businessList.size()));
    }



    @Override
    public void onDestroy() {
        Constants.removeChangeBusinessListener(this);
        super.onDestroy();
    }

    @Override
    public void onChangeBusiness() {
        if (isDetached()) return;
        businessAdapter.notifyDataSetChanged();
        updateUI();

    }
}