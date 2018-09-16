package com.orbigo.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import com.orbigo.R;
import com.orbigo.constants.Constants;
import com.orbigo.constants.DatabaseKeys;
import com.orbigo.constants.Urls;
import com.orbigo.models.Business;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

public class ListBusinessActivity extends AppCompatActivity {
    public static final String NEW_BUSINESS = "new_business";
    private EditText ABN;
    private FirebaseAuth mAuth;
    private EditText BusinessPhone;
    private EditText BusinessEmail, businessAddress, businessName;
    private RequestQueue requestQueue;
    private String User_id;
    private Button register;
    private boolean isNewBisiness = true;
    private Business business;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_business);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (getIntent().getExtras() != null) {
            isNewBisiness = getIntent().getBooleanExtra(NEW_BUSINESS, true);
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
        if (!isNewBisiness) {
            register.setText(R.string.business_cvv_update);
            business = Constants.SELECTED_BUSINESS;

            if (business == null) {
                if (Constants.BUSINESS_LIST.size()<=0) {
                    Toast.makeText(this, "Did not have business", Toast.LENGTH_LONG).show();
                    onBackPressed();
                    return;
                } else {
                    business=Constants.BUSINESS_LIST.get(0);
                }
            }

            BusinessEmail.setText(business.getBus_email());
            BusinessPhone.setText(business.getPhone_number());
            businessName.setText(business.getBus_name());
            businessAddress.setText(business.getAddress());
            ABN.setText(business.getAbn());
        }
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = BusinessEmail.getText().toString().trim().toLowerCase();
                String phone = BusinessPhone.getText().toString().trim();
                String name = businessName.getText().toString().trim();
                String address = businessAddress.getText().toString().trim();
                if (validate(email, phone, name, address)) {
                    if (isNewBisiness) {
                        business = new Business();
                    }
                    business.setBus_email(email);
                    business.setPhone_number(phone);
                    business.setAbn(ABN.getText().toString());
                    business.setBus_name(name);
                    business.setAddress(address);

                    if (isNewBisiness){
                        newBusiness(business);
                    } else {
                        updateBusiness(business);
                    }
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

    private boolean validate(String email, String phone, String name, String address) {
        return validateEmail(email) && validatePhone(phone) && validateName(name) && validateAddress(address);
    }

    private boolean validateName(String name) {
        if (name.isEmpty()) {
            businessName.setError("Name cannot be empty");
            return false;
        }
        return true;
    }

    private boolean validateAddress(String address) {
        if (address.isEmpty()) {
            businessAddress.setError("Address cannot be empty");
            return false;
        }
        return true;
    }

    private boolean validatePhone(String phone) {
        if (phone == null || !Patterns.PHONE.matcher(phone).matches()) {
            BusinessPhone.setError(Constants.PHONE_ERROR);
            return false;
        }
        return true;
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty())
            return true;
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            BusinessEmail.setError(Constants.EMAIL_ERROR);
            return false;
        }
        return true;
    }

    public void onCrossClicked(View view) {
        finish();
    }

    public void onBackPress(View view) {
        finish();
    }


    private void updateBusiness(Business b) {
        JSONObject data = new JSONObject();
        try {
            data.put(DatabaseKeys.AUTHORIZED_BUSINESS_NUMBER, b.getAbn());


            data.put(DatabaseKeys.BUSINESS_PHONE, b.getPhone_number());
            data.put(DatabaseKeys.BUSINESS_EMAIL, b.getBus_email());
            data.put(DatabaseKeys.BUSINESS_NAME, b.getBus_name());
            data.put(DatabaseKeys.BUSINESS_ADDRESS, b.getAddress());
            data.put(DatabaseKeys.USER_ID, User_id);

            data.put(DatabaseKeys.CAPACITY, b.getCapacity());
            data.put(DatabaseKeys.CATEGORY, b.getCategory());
            data.put(DatabaseKeys.DESCRIPTION, b.getDescription());
            data.put(DatabaseKeys.PRICE_RANGE, b.getPrice_range());
            String url = Urls.BASE_URL + Urls.UPDATE_BUSINESS;
            Log.d("updatebusiness", data.toString());
            final JsonObjectRequest updateReq = new JsonObjectRequest(Request.Method.POST, url, data,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("updatebusiness", response.toString());
                            try {
                                if (response.getString("status").compareTo("success") == 0) {
                                    Toast.makeText(ListBusinessActivity.this, "success", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
                                    Constants.onChangeBusiness();
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
            requestQueue.add(updateReq);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void newBusiness(Business b) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(DatabaseKeys.AUTHORIZED_BUSINESS_NUMBER, ABN.getText().toString());
            jsonObject.put(DatabaseKeys.BUSINESS_PHONE, b.getPhone_number());


            if (!b.getBus_email().isEmpty()) {
                jsonObject.put(DatabaseKeys.BUSINESS_EMAIL, b.getBus_email());

            } else {
                jsonObject.put(DatabaseKeys.BUSINESS_EMAIL, null);
            }
            jsonObject.put(DatabaseKeys.BUSINESS_NAME, b.getBus_name());
            jsonObject.put(DatabaseKeys.BUSINESS_ADDRESS, b.getAddress());
            jsonObject.put(DatabaseKeys.USER_ID, User_id);
            Constants.BUSINESS_LIST.add(b);
            Constants.onChangeBusiness();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Urls.BASE_URL + Urls.ADD_BUSINESS;
        Log.v("businessphp", jsonObject.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("businessphp", response.toString());
                Toast.makeText(getApplicationContext(), "Business registered", Toast.LENGTH_SHORT).show();
                Constants.onChangeBusiness();
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