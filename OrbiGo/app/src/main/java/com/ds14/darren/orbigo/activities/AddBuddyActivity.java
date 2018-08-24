package com.ds14.darren.orbigo.activities;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.icu.text.UnicodeSetSpanner;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.ds14.darren.orbigo.models.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONException;
import org.json.JSONObject;

public class AddBuddyActivity extends AppCompatActivity {

    private Button contactBtn,addBuddyBtn;
    private EditText nameET,emailET,phoneET;
    private RequestQueue requestQueue;
    private Trip selectedTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_buddy);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        selectedTrip = Constants.PLANNING_TRIP;
        requestQueue = Volley.newRequestQueue(this);
        setId();
        setListener();
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

    private void setListener() {
        addBuddyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTripBuddyOnDb();
            }
        });
        contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(AddBuddyActivity.this)
                        .withPermission(Manifest.permission.READ_CONTACTS)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                Intent intent = new Intent(Intent.ACTION_PICK,
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                                startActivityForResult(intent, Constants.PICK_CONTACT);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                Toast.makeText(AddBuddyActivity.this,"Permission denied",Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        });
    }

    private void addTripBuddyOnDb() {
        String email = emailET.getText().toString().toLowerCase().trim();
        String name = nameET.getText().toString().trim();
        String phone = phoneET.getText().toString().trim();
        if(validate(email,name,phone)){
            JSONObject input = new JSONObject();
            try {
                input.put(DatabaseKeys.TRIP_ID,selectedTrip.getId());
                JSONObject data = new JSONObject();
                data.put(DatabaseKeys.MEMBER_NAME,name);
                data.put(DatabaseKeys.MEMBER_PHONE,phone);
                data.put(DatabaseKeys.MEMBER_EMAIL,email);
                input.put("member",data);
                String url = Urls.BASE_URL + Urls.ADD_TRIP_MEMBER;
                Log.v("addbuddy",input.toString());
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.v("addbuddy",response.toString());
                                try {
                                    String message = response.getString("message");
                                    Toast.makeText(AddBuddyActivity.this, message, Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
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
    }

    private void setId() {
        contactBtn = findViewById(R.id.contact_btn);
        nameET = findViewById(R.id.buddy_name);
        emailET = findViewById(R.id.buddy_email);
        phoneET = findViewById(R.id.buddy_phone);
        addBuddyBtn = findViewById(R.id.buddy_add_btn);
    }

    private boolean validate(String email, String name, String phone){
        boolean isEmail = validateEmail(email);
        boolean isPhone = validatePhone(phone);
        return isEmail && !name.isEmpty() && isPhone;
    }

    private boolean validatePhone(String phone) {
        if(!Patterns.PHONE.matcher(phone).matches()) {
            phoneET.setError(Constants.PHONE_ERROR);
            return false;
        }
        return true;
    }

    private boolean validateEmail(String email) {
        if(email.isEmpty())
            return true;
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailET.setError(Constants.EMAIL_ERROR);
            return false;
        }
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Check for the request code, we might be usign multiple startActivityForReslut
            switch (requestCode) {
                case Constants.PICK_CONTACT:
                    Cursor cursor;
                    try {
                        String phoneNo;
                        String name;
                        // getData() method will have the Content Uri of the selected contact
                        Uri uri = data.getData();
                        //Query the content uri
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        cursor.moveToFirst();
                        // column index of the phone number
                        int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        // column index of the contact name
                        int  nameIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        phoneNo = cursor.getString(phoneIndex);
                        name = cursor.getString(nameIndex);
                        // Set the value to the textviews
                        nameET.setText(name);
                        phoneET.setText(phoneNo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }
    }
}