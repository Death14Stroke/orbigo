package com.orbigo.services;

import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.orbigo.constants.DatabaseKeys;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private RequestQueue requestQueue;
    private FirebaseAuth mAuth;

    @Override
    public void onTokenRefresh() {

        //For registration of token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
            String user_id = mAuth.getCurrentUser().getUid();
            sendTokenToServer(user_id,refreshedToken);
        }
        //To displaying token on logcat
        Log.d("TOKEN: ", refreshedToken);

    }

    private void sendTokenToServer(String user_id,String refreshedToken) {
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.USER_ID,user_id);
            input.put(DatabaseKeys.TOKEN,refreshedToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
