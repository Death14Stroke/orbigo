package com.ds14.darren.orbigo.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.ds14.darren.orbigo.models.Business;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

public class ListBusinessActivity extends AppCompatActivity {
    private EditText ABN;
    private FirebaseAuth mAuth;
    private EditText BusinessPhone;
    private EditText BusinessEmail, businessAddress, businessName;
    private RequestQueue requestQueue;
    private String User_id;
    private Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_business);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ABN = findViewById(R.id.ABN);
        BusinessPhone = findViewById(R.id.Business_ph);
        BusinessEmail = findViewById(R.id.Business_email);
        businessAddress = findViewById(R.id.business_address);
        businessName = findViewById(R.id.business_name);
        register = findViewById(R.id.register_business);
        mAuth = FirebaseAuth.getInstance();
        User_id = mAuth.getCurrentUser().getUid();
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = BusinessEmail.getText().toString().trim().toLowerCase();
                String phone = BusinessPhone.getText().toString().trim();
                String name = businessName.getText().toString().trim();
                String address = businessAddress.getText().toString().trim();
                if(validate(email,phone,name,address)){
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(DatabaseKeys.AUTHORIZED_BUSINESS_NUMBER,ABN.getText().toString());
                        jsonObject.put(DatabaseKeys.BUSINESS_PHONE,phone);
                        Business b = new Business();
                        b.setAbn(ABN.getText().toString());
                        b.setPhone_number(phone);
                        if(!email.isEmpty()) {
                            jsonObject.put(DatabaseKeys.BUSINESS_EMAIL, email);
                            b.setBus_email(email);
                        }
                        else
                            jsonObject.put(DatabaseKeys.BUSINESS_EMAIL,null);
                        jsonObject.put(DatabaseKeys.BUSINESS_NAME,name);
                        jsonObject.put(DatabaseKeys.BUSINESS_ADDRESS,address);
                        jsonObject.put(DatabaseKeys.USER_ID,User_id);
                        b.setBus_name(name);
                        b.setAddress(address);
                        Constants.BUSINESS_LIST.add(b);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String url = Urls.BASE_URL + Urls.ADD_BUSINESS;
                    Log.v("businessphp",jsonObject.toString());
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("businessphp",response.toString());
                            Toast.makeText(getApplicationContext(),"Business registered",Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });
                    requestQueue.add(jsonObjectRequest);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean validate(String email, String phone, String name, String address){
        return validateEmail(email) && validatePhone(phone) && validateName(name) && validateAddress(address);
    }

    private boolean validateName(String name){
        if(name.isEmpty()){
            businessName.setError("Name cannot be empty");
            return false;
        }
        return true;
    }

    private boolean validateAddress(String address){
        if(address.isEmpty()){
            businessAddress.setError("Address cannot be empty");
            return false;
        }
        return true;
    }

    private boolean validatePhone(String phone) {
        if(phone==null || !Patterns.PHONE.matcher(phone).matches()) {
            BusinessPhone.setError(Constants.PHONE_ERROR);
            return false;
        }
        return true;
    }

    private boolean validateEmail(String email) {
        if(email.isEmpty())
            return true;
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            BusinessEmail.setError(Constants.EMAIL_ERROR);
            return false;
        }
        return true;
    }
}