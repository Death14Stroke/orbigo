package com.orbigo.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.orbigo.R;
import com.orbigo.adapters.ViewPagerAdapter;
import com.orbigo.broadcast_receivers.ConnectivityReceiver;
import com.orbigo.constants.Constants;
import com.orbigo.fragments.LoginFragment;
import com.orbigo.fragments.SignUpFragment;
import com.orbigo.helpers.MyApplication;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener{

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FirebaseAuth mAuth;
    private ConnectivityReceiver receiver;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(Constants.LOGIN_SP,0);
        checkIfLoggedIn();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_login);
        setupReceivers();
        defId();
        setupTabs();
        getSHAkey();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void checkIfLoggedIn() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            String mode = sharedPreferences.getString(Constants.LOGIN_TYPE,Constants.TVT);
            if(mode.equals(Constants.TVT)) {
                startActivity(new Intent(LoginActivity.this, HomeTouristActivity.class));
                finish();
            }
            else if(mode.equals(Constants.BTO)) {
                startActivity(new Intent(LoginActivity.this, SplashBusinessActivity.class));
                finish();
            }
        }
    }

    private void getSHAkey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.ds14.darren.orbigo",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("NewSHA",md+"");
                Log.d("YourKeyHash :", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            Log.d("YourKeyHash :", e.getMessage());
        }
    }

    private void setupReceivers() {
        receiver = new ConnectivityReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(receiver,intentFilter);
    }

    private void defId(){
        viewPager = findViewById(R.id.login_viewpager);
        tabLayout = findViewById(R.id.login_tabs);
    }

    private void setupTabs(){
        addTabs(viewPager);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void addTabs(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Fragment signupFragment = new SignUpFragment();
        Log.v("fragid",signupFragment.getId()+"");
        Fragment loginFragment = new LoginFragment();
        Log.v("fragid",loginFragment.getId()+"");
        adapter.addFrag(signupFragment, Constants.SIGNUP_TITLE);
        adapter.addFrag(loginFragment, Constants.LOGIN_TITLE);
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register connection status listener
        MyApplication.getInstance().setConnectivityListener(this);
      //  checkIfLoggedIn();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    private void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = "Welcome to OrbiGo!";
            color = Color.WHITE;
        } else {
            message = "Please connect to internet";
            color = Color.RED;
        }

        final Snackbar snackbar = Snackbar
                .make(findViewById(R.id.login_ll), message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("DISMISS", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                })
                .setActionTextColor(Color.YELLOW);

        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("actresult","called in login activity");
        if(Constants.CURRENT_FRAGMENT!=null){
            if(Constants.CURRENT_FRAGMENT.equals(Constants.SIGNUP_TITLE))
                SignUpFragment.callbackManager.onActivityResult(requestCode,resultCode,data);
            else if(Constants.CURRENT_FRAGMENT.equals(Constants.LOGIN_TITLE))
                LoginFragment.callbackManager.onActivityResult(requestCode,resultCode,data);
        }

    }
}