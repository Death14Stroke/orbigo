package com.ds14.darren.orbigo.activities;

import android.Manifest;
import android.app.DialogFragment;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.custom_ui_components.DatePickerFragment;
import com.ds14.darren.orbigo.custom_ui_components.WorkaroundMapFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

public class RestWithMapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private ScrollView mScrollView;
    private GoogleMap mMap;
    private Button readMoreBtn;
    private TextView noteTV;
    private ImageButton showDatePickerBtn;
    private boolean readMoreEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_with_map);
        setId();
        ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map)).getMapAsync(this);
        ((WorkaroundMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map)).setListener(new WorkaroundMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                mScrollView.requestDisallowInterceptTouchEvent(true);
            }
        });
        setListeners();
    }

    private void setListeners() {
        readMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readMoreEnabled = !readMoreEnabled;
                if (readMoreEnabled) {
                    noteTV.setMaxLines(Integer.MAX_VALUE);
                    readMoreBtn.setText("Read less");
                    readMoreBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_up, 0);
                } else {
                    noteTV.setMaxLines(2);
                    readMoreBtn.setText("Read more");
                    readMoreBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right, 0);
                }
            }
        });
        showDatePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });
    }

    private void setId() {
        mScrollView = findViewById(R.id.scrollView_map);
        readMoreBtn = findViewById(R.id.read_more_btn);
        noteTV = findViewById(R.id.noteTV);
        showDatePickerBtn = findViewById(R.id.date_picker_btn);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "reserveDate");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }
}
