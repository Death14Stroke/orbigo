package com.ds14.darren.orbigo.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.adapters.SliderAdapter;
import com.ds14.darren.orbigo.constants.Constants;
import com.ds14.darren.orbigo.models.NearbyPoi;
import com.ds14.darren.orbigo.models.Poi;
import com.google.firebase.auth.FirebaseAuth;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PoiDetailsActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout indicator;
    private List<String> imageUrlList;
    private SlidingUpPanelLayout mLayout;
    private ImageView arrowImageView;
    private Button callTV, urlTV;
    private NearbyPoi nearbyPoi;
    private SliderAdapter sliderAdapter;
    private FirebaseAuth mAuth;
    private TextView nameTV,costTV,areaTV,timeTV,descTV,categoryTV,addressTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_details);
        mAuth = FirebaseAuth.getInstance();
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        nearbyPoi = Constants.SELECTED_NEARBY_POI;
        imageUrlList = new ArrayList<>();
        setId();
        setListeners();
        sliderAdapter = new SliderAdapter(this,imageUrlList);
        viewPager.setAdapter(sliderAdapter);
        indicator.setupWithViewPager(viewPager,true);
        setupData();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new SliderTimer(),2000,6000);
    }

    private void setupData() {
        imageUrlList.add(nearbyPoi.getPoiDetails().getPicture());
        sliderAdapter.notifyDataSetChanged();
        callTV.setText(nearbyPoi.getPoiDetails().getPhone());
        urlTV.setText(nearbyPoi.getPoiDetails().getUrl());
        nameTV.setText(nearbyPoi.getPoiDetails().getName());
        areaTV.setText(nearbyPoi.getPoiDetails().getIs_in_region());
        timeTV.setText(nearbyPoi.getTime());
        costTV.setText(nearbyPoi.getCost());
        descTV.setText(nearbyPoi.getPoiDetails().getDescription());
        categoryTV.setText(nearbyPoi.getPoiDetails().getCategory());
        addressTV.setText(nearbyPoi.getPoiDetails().getAddress());
    }

    private void setId(){
        viewPager = findViewById(R.id.image_viewPager);
        indicator = findViewById(R.id.image_indicator);
        mLayout = findViewById(R.id.sliding_layout2);
        arrowImageView = findViewById(R.id.arrow_image2);
        callTV = findViewById(R.id.poi_call);
        urlTV = findViewById(R.id.poi_url);
        nameTV = findViewById(R.id.poi_detail_name);
        costTV = findViewById(R.id.poi_details_cost);
        areaTV = findViewById(R.id.poi_detail_area);
        timeTV = findViewById(R.id.poi_detail_time);
        descTV = findViewById(R.id.poi_detail_desc);
        categoryTV = findViewById(R.id.poi_detail_cat);
        addressTV = findViewById(R.id.poi_detail_address);
    }

    private void setListeners(){
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState.equals(SlidingUpPanelLayout.PanelState.EXPANDED))
                    arrowImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_down));
                else if (newState.equals(SlidingUpPanelLayout.PanelState.COLLAPSED))
                    arrowImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_up));
            }
        });
        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        callTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "tel:"+callTV.getText();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });
        urlTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = urlTV.getText().toString().trim();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home_logout:
                mAuth.signOut();
                finishAffinity();
                startActivity(new Intent(PoiDetailsActivity.this, LoginActivity.class));
                break;
            case R.id.home_profile:
                Intent intent = new Intent(PoiDetailsActivity.this, TouristProfileActivity.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }

    private class SliderTimer extends TimerTask{

        @Override
        public void run() {
            PoiDetailsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(viewPager.getCurrentItem()<imageUrlList.size()-1){
                        viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
                    }
                    else{
                        viewPager.setCurrentItem(0);
                    }
                }
            });
        }
    }
}