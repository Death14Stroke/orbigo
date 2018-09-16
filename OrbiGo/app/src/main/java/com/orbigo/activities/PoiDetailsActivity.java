package com.orbigo.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.orbigo.R;
import com.orbigo.adapters.SliderAdapter;
import com.orbigo.constants.Constants;
import com.orbigo.helpers.APIHelper;
import com.orbigo.helpers.Utils;
import com.orbigo.models.NearbyPoi;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

public class PoiDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
   public static final String TAG=PoiDetailsActivity.class.getSimpleName();
    private ViewPager viewPager;
    private TabLayout indicator;
    private List<String> imageUrlList;
    private SlidingUpPanelLayout mSlidePanelLayout;
    private ImageView arrowImageView;
    private Button callTV, urlTV;
    private NearbyPoi nearbyPoi;
    private SliderAdapter sliderAdapter;
    private FirebaseAuth mAuth;
    Toolbar toolbar;
    private GoogleMap mMap;
    private RequestQueue requestQueue;
    private TextView nameTV, costTV, areaTV, timeTV, descTV, categoryTV, addressTV;
    private MenuItem itemLike;


    public static void start(Context context, NearbyPoi poi) {
        Intent intent = new Intent(context, PoiDetailsActivity.class);
        intent.putExtra(Constants.SELECTED_PLACE, (Parcelable) poi);
        context.startActivity(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_details);
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(this);

        /*ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }*/
        nearbyPoi = (NearbyPoi) getIntent().getSerializableExtra(Constants.SELECTED_PLACE);
        imageUrlList = new ArrayList<>();
        setId();
        setListeners();
        sliderAdapter = new SliderAdapter(this, imageUrlList);
        viewPager.setAdapter(sliderAdapter);
        indicator.setupWithViewPager(viewPager, true);
        showUI();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new SliderTimer(), 2000, 6000);

    }

    private void showUI() {
        imageUrlList.add(nearbyPoi.getPoiDetails().getPicture());
        sliderAdapter.notifyDataSetChanged();
        callTV.setText(Utils.nullClearing(nearbyPoi.getPoiDetails().getPhone()));
        urlTV.setText(Utils.nullClearing(nearbyPoi.getPoiDetails().getUrl()));
        nameTV.setText(Utils.nullClearing(nearbyPoi.getPoiDetails().getName()));
        LatLng asd = nearbyPoi.getPoiDetails().getLocation();
        Log.i("Locatrino-->", asd + " lat" + asd.latitude + "loon" + asd.longitude);
        areaTV.setText(Utils.nullClearing(nearbyPoi.getPoiDetails().getIs_in_region()));
        timeTV.setText(Utils.nullClearing(nearbyPoi.getTime()));
        costTV.setText(Utils.nullClearing(nearbyPoi.getCost()));
        descTV.setText(Utils.nullClearing(nearbyPoi.getPoiDetails().getDescription()));
        categoryTV.setText(Utils.nullClearing(nearbyPoi.getPoiDetails().getCategory()));
        addressTV.setText(Utils.nullClearing(nearbyPoi.getPoiDetails().getAddress()));
    }

    private void setId() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.bringToFront();
        toolbar.setTitle(" ");
        setSupportActionBar(toolbar);

        // toolbar.canShowOverflowMenu();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(PoiDetailsActivity.this);

        viewPager = findViewById(R.id.image_viewPager);
        indicator = findViewById(R.id.image_indicator);
        mSlidePanelLayout = findViewById(R.id.sliding_layout2);
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

    private void setListeners() {
        mSlidePanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
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
        mSlidePanelLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSlidePanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        callTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "tel:" + callTV.getText();
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
        arrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSlidePanelLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                    mSlidePanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                } else {
                    mSlidePanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_poidetails, menu);
        itemLike=menu.findItem(R.id.item_like);
        drawIconLike();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_like:
                like();
                break;
            case R.id.item_download:

//                Intent intent = new Intent(PoiDetailsActivity.this, TouristProfileActivity.class);
//                startActivity(intent);
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        Utils.addMarker(this, mMap, nearbyPoi.getPoiDetails());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(nearbyPoi.getPoiDetails().getLocation(), 16));
    }

    private class SliderTimer extends TimerTask {

        @Override
        public void run() {
            PoiDetailsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (viewPager.getCurrentItem() < imageUrlList.size() - 1) {
                        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                    } else {
                        viewPager.setCurrentItem(0);
                    }
                }
            });
        }
    }

    private void drawIconLike(){
        if (nearbyPoi.isLiked()) {
            itemLike.setIcon(R.drawable.ic_outlinefavorite);
        } else {
            itemLike.setIcon(R.drawable.ic_outlinefavorite_border);
        }
    }
    private void like() {
        APIHelper.like(!nearbyPoi.isLiked(), mAuth.getCurrentUser().getUid(), nearbyPoi.getPoiDetails().getId(), requestQueue, new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                try {
                    JSONObject response = (JSONObject) arg;
                    String status = response.getString("status");
                    if (status.equals("success")) {
                        nearbyPoi.setLiked(!nearbyPoi.isLiked());
                        drawIconLike();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}