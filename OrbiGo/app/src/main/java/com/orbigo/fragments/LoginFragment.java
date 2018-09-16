package com.orbigo.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.orbigo.R;
import com.orbigo.activities.HomeTouristActivity;
import com.orbigo.activities.SplashBusinessActivity;
import com.orbigo.constants.Constants;
import com.orbigo.constants.DatabaseKeys;
import com.orbigo.constants.Urls;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.facebook.FacebookSdk.getApplicationContext;

public class LoginFragment extends Fragment{

    private Button loginBtn;
    private EditText emailET, passwordET;
    private Button loginFbButton;
    public static CallbackManager callbackManager;
    private ImageButton showpassword;
    private boolean eyeBtnClick=true;
    private Button forgotPwd;
    private RequestQueue requestQueue;
    private RadioGroup tourist_or_visitor;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private JsonObjectRequest loginRequest;
    private SharedPreferences sharedPreferences;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        sharedPreferences = getContext().getSharedPreferences(Constants.LOGIN_SP,0);
        FacebookSdk.sdkInitialize(getContext());
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(getActivity());
        callbackManager = CallbackManager.Factory.create();
        requestQueue = Volley.newRequestQueue(getContext());
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please wait");
        progressDialog.setTitle("Loading");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        setId(v);
        setListeners();
        setFacebook();
        return v;
    }

    private void setFacebook() {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                Objects.requireNonNull(getActivity()).setResult(RESULT_OK);
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/" + AccessToken.getCurrentAccessToken().getUserId() + "?fields=id,name,gender,email,picture.type(large)",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse response) {
                                if(response!=null) {
                                    JSONObject fbdatainp = response.getJSONObject();
                                    JSONObject fbobj = new JSONObject();
                                    try {
                                        fbobj.put(DatabaseKeys.FACEBOOK_ID,fbdatainp.getString("id"));
                                        fbobj.put(DatabaseKeys.FULL_NAME,fbdatainp.getString("name"));
                                        fbobj.put(DatabaseKeys.GENDER,fbdatainp.getString("gender"));
                                        fbobj.put(DatabaseKeys.EMAIL_ADDRESS,fbdatainp.getString("email"));
                                        fbobj.put(DatabaseKeys.PROFILE_IMAGE,
                                                fbdatainp.getJSONObject("picture").getJSONObject("data").getString("url"));                                        //Log.v("formatbf",fbobj.toString());
                                        handleFacebookAccessToken(loginResult.getAccessToken(), fbobj);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                ).executeAsync();
            }

            @Override
            public void onCancel() {
                Log.v("fblogin","oncancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.v("fblogin",error.getMessage());
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token, final JSONObject fbobj) {
        Log.d("firebase", "handleFacebookAccessToken:" + token);
        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String uid = task.getResult().getUser().getUid();
                            try {
                                fbobj.put(DatabaseKeys.USER_ID,uid);
                                Log.v("uidFB",uid);
                                createLoginEmailVeriRequest(fbobj,uid);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            progressDialog.cancel();
                            Toast.makeText(getActivity(),"Failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setListeners() {
        loginFbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.CURRENT_FRAGMENT=Constants.LOGIN_TITLE;
                if(checkTVTorBTO()!=-1){
                    progressDialog.show();
                    LoginManager.getInstance().logInWithReadPermissions(getActivity(),Arrays.asList(Constants.FB_EMAIL,
                            Constants.FB_PUBLIC_PROFILE, Constants.FB_GENDER,Constants.FB_FRIENDS));
                }
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String email = emailET.getText().toString().toLowerCase().trim();
                String password = passwordET.getText().toString();
                if(checkTVTorBTO()!=1 && validateEmail(email)){
                    progressDialog.show();
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener(getActivity(), new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(final AuthResult authResult) {
                                    progressDialog.cancel();
                                    if(!authResult.getUser().isEmailVerified()){
                                        final Snackbar snackbar = Snackbar.make(v,"Please verify your email",Snackbar.LENGTH_LONG);
                                        snackbar.setActionTextColor(Color.YELLOW);
                                        snackbar.setAction("RESEND", new View.OnClickListener() {
                                            @Override
                                            public void onClick(final View v) {
                                                mAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        final Snackbar snackbar2 = Snackbar.make(v,"Verification email sent",Snackbar.LENGTH_LONG);
                                                        snackbar2.setAction("DISMISS", new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                snackbar2.dismiss();
                                                            }
                                                        });
                                                        snackbar2.setActionTextColor(Color.YELLOW);
                                                        snackbar2.show();
                                                    }
                                                });
                                                mAuth.signOut();
                                            }
                                        });
                                        snackbar.show();
                                    }
                                    else{
                                        String uid = authResult.getUser().getUid();
                                        createLoginEmailVeriRequest(null,uid);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.cancel();

                                    Log.i("NewToast",e.getMessage()+"\n");
                                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        showpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(eyeBtnClick){
                    passwordET.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    showpassword.setImageResource(R.drawable.ic_openeye);
                }
                else {
                    passwordET.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    showpassword.setImageResource(R.drawable.ic_closedeye);
                }
                eyeBtnClick = !eyeBtnClick;
            }
        });
        forgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailET.getText().toString().trim().toLowerCase();
                if(!validateEmail(email))
                    return;
                mAuth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(),"Reset password link sent",Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void setId(View v) {
        loginBtn = v.findViewById(R.id.login_login_Btn);
        emailET = v.findViewById(R.id.login_emailET);
        passwordET = v.findViewById(R.id.login_passwordET);
        loginFbButton = v.findViewById(R.id.login_fb_button);
        showpassword = v.findViewById(R.id.eyeBtn);
        forgotPwd= v.findViewById(R.id.forgotpwdBtn);
        tourist_or_visitor = v.findViewById(R.id.login_radio_group);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void createLoginEmailVeriRequest(JSONObject fbobj, String uid){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(DatabaseKeys.USER_ID,uid);
            jsonObject.put(DatabaseKeys.USER_TOKEN, FirebaseInstanceId.getInstance().getToken());
            if(checkTVTorBTO()==R.id.login_tvt_radio)
                jsonObject.put(DatabaseKeys.MODE,Constants.TVT);
            else if(checkTVTorBTO()==R.id.login_bto_radio)
                jsonObject.put(DatabaseKeys.MODE,Constants.BTO);
            if(fbobj!=null){
                jsonObject.put(DatabaseKeys.EMAIL_ADDRESS, fbobj.getString(DatabaseKeys.EMAIL_ADDRESS));
                jsonObject.put(DatabaseKeys.GENDER, fbobj.getString(DatabaseKeys.GENDER));
                jsonObject.put(DatabaseKeys.NAME, fbobj.getString(DatabaseKeys.FULL_NAME));
                jsonObject.put(DatabaseKeys.FACEBOOK_ID, fbobj.getString(DatabaseKeys.FACEBOOK_ID));
                jsonObject.put(DatabaseKeys.PROFILE_IMAGE, fbobj.getString(DatabaseKeys.PROFILE_IMAGE));
            }
            String url = Urls.BASE_URL + Urls.LOGIN;
            Log.v("response","input is "+jsonObject.toString());
            loginRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("response",response.toString());
                            progressDialog.cancel();
                            try {
                                String message = response.getString("message");
                                if(response.getString("status").equals("success")) {
                                    if(checkTVTorBTO()==R.id.login_bto_radio) {
                                        sharedPreferences.edit().putString(Constants.LOGIN_TYPE, Constants.BTO).apply();
                                        startActivity(new Intent(getActivity(),SplashBusinessActivity.class));
                                        getActivity().finish();
                                    }
                                    else{
                                        sharedPreferences.edit().putString(Constants.LOGIN_TYPE, Constants.TVT).apply();
                                        startActivity(new Intent(getActivity(),HomeTouristActivity.class));
                                        getActivity().finish();
                                    }
                                }
                                else if(response.getString("status").equals("error")){
                                    Log.v("response","error");
                                    switch (message){
                                        case "tvt_error":
                                            showDialog("tourist");
                                            break;
                                        case "bto_error":
                                            showDialog("business");
                                            break;
                                    }
                                    Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(),error.toString(),Toast.LENGTH_SHORT).show();
                        }
                    });
            loginRequest.setTag(Constants.TAG_REQUEST_QUEUE);
            requestQueue.add(loginRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showDialog(final String mode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createNewProfile(mode);
            }
        });
        builder.setTitle("Missing profile");
        builder.setMessage("You do not have a "+mode+" profile. Do you want to enable "+mode+" profile?");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void createNewProfile(final String mode) {
        String url = Urls.BASE_URL + Urls.CREATE_PROFILE;
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.USER_ID,mAuth.getCurrentUser().getUid());
            input.put(DatabaseKeys.MODE,mode);
            Log.v("createresponse",input.toString());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("createresponse", response.toString());
                            try {
                                if (response.getString("status").equals("success")) {
                                    if (mode.equals("tourist")) {
                                        startActivity(new Intent(getActivity(), HomeTouristActivity.class));
                                        sharedPreferences.edit().putString(Constants.LOGIN_TYPE, Constants.TVT).apply();
                                        getActivity().finish();
                                    } else {
                                        sharedPreferences.edit().putString(Constants.LOGIN_TYPE, Constants.BTO).apply();
                                        startActivity(new Intent(getActivity(), SplashBusinessActivity.class));
                                        getActivity().finish();
                                    }
                                }
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

    private boolean validateEmail(String email) {
        if(email==null || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailET.setError(Constants.EMAIL_ERROR);
            return false;
        }
        return true;
    }

    private int checkTVTorBTO(){
        int id;
        switch (tourist_or_visitor.getCheckedRadioButtonId()){
            case R.id.login_bto_radio:
                id = R.id.login_bto_radio;
                break;
            case R.id.login_tvt_radio:
                id = R.id.login_tvt_radio;
                break;
            default:
                id = -1;
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                builder.setMessage(Constants.RADIOGROUP_ERROR)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;
        }
        return id;
    }
}