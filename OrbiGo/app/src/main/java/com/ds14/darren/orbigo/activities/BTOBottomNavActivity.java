package com.ds14.darren.orbigo.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.constants.Constants;
import com.ds14.darren.orbigo.constants.DatabaseKeys;
import com.ds14.darren.orbigo.constants.Urls;
import com.ds14.darren.orbigo.custom_ui_components.TimePickerFragment;
import com.ds14.darren.orbigo.fragments.BookingFragment;
import com.ds14.darren.orbigo.fragments.BusinessFragment;
import com.ds14.darren.orbigo.fragments.CalendarFragment;
import com.ds14.darren.orbigo.fragments.StatsFragment;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.ds14.darren.orbigo.constants.Constants.CAMERA_REQUEST;

public class BTOBottomNavActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
        TimePickerFragment.TimeChangeListener{

    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;
    private SimpleDateFormat timeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btobottom_nav);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        loadFragment(new StatsFragment());
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().popBackStackImmediate();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_stats:
                loadFragment(new StatsFragment());
                return true;
            case R.id.navigation_calender:
                loadFragment(new CalendarFragment());
                return true;
            case R.id.navigation_business:
                loadFragment(new BusinessFragment());
                return true;
            case R.id.navigation_booking:
                loadFragment(new BookingFragment());
                return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.home_logout:
                mAuth.signOut();
                finish();
                startActivity(new Intent(BTOBottomNavActivity.this,LoginActivity.class));
                break;
            case R.id.home_profile:

                Intent intent = new Intent(BTOBottomNavActivity.this,BusinessProfileActivity.class);
                startActivity(intent);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode== Constants.PICK_IMAGE_REQUEST && resultCode==RESULT_OK) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                Log.v("stringimage","encoded: "+encodedImage.length());
                uploadPicture(encodedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            Log.v("stringimage","encoded: "+encodedImage.length());
            uploadPicture(encodedImage);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadPicture(String encodedImage) {
        Log.v("stringimage","inside upload picture function in activity");
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.AUTHORIZED_BUSINESS_NUMBER,Constants.SELECTED_BUSINESS.getAbn());
            input.put(DatabaseKeys.PROFILE_IMAGE,encodedImage);
            int index = Constants.BUSINESS_LIST.indexOf(Constants.SELECTED_BUSINESS);
            Constants.BUSINESS_LIST.get(index).setImage(encodedImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Urls.BASE_URL + Urls.UPLOAD_PICTURE_BUSINESS;
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, input,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.v("responseimage",response.toString());
                            String status = response.getString("status");
                            if(status.equals("success")){
                                Toast.makeText(getApplicationContext(),"success",Toast.LENGTH_SHORT).show();
                            }
                            else if(status.equals("error")){
                                Toast.makeText(getApplicationContext(),"error",Toast.LENGTH_SHORT).show();
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
        requestQueue.add(request);
    }

    @Override
    public void onTimeChanged(int hour, int minute, String tag) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(Calendar.HOUR_OF_DAY,hour);
        c.set(Calendar.MINUTE,minute);
        CalendarFragment calendarFragment = new CalendarFragment();
        long time = c.getTimeInMillis();
        timeFormat = new SimpleDateFormat("HH:mm");
        switch (tag){
            case "from":
                Constants.FROM=timeFormat.format(time);
                break;
            case "to":
                Constants.TO=timeFormat.format(time);
                break;
        }
        loadFragment(calendarFragment);
    }
}