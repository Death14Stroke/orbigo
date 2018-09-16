package com.orbigo.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.orbigo.R;
import com.orbigo.constants.Constants;
import com.google.firebase.auth.FirebaseAuth;

public class BusinessPlusActivity extends AppCompatActivity {
    private RequestQueue requestQueue;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_plus);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        sharedPreferences = getSharedPreferences(Constants.LOGIN_SP, 0);
        requestQueue = Volley.newRequestQueue(this);
        mAuth = FirebaseAuth.getInstance();
        setId();
        setListener();

    }

    private void setId() {

    }

    private void setListener() {

    }

    public void onBackPress(View view) {
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
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

}
