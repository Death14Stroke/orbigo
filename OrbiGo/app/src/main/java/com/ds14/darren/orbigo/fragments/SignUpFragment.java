package com.ds14.darren.orbigo.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.activities.SplashTouristActivity;
import com.ds14.darren.orbigo.constants.Constants;
import com.ds14.darren.orbigo.constants.DatabaseKeys;
import com.ds14.darren.orbigo.constants.Urls;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class SignUpFragment extends Fragment {

    private Button createAccountBtn;
    private EditText emailET, passwordET,phoneET;
    private Button loginFbButton;
    public static CallbackManager callbackManager;
    private ImageButton showPasswordBtn;
    private boolean eyeBtnClick=true;
    private RequestQueue requestQueue;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private JsonObjectRequest signupRequest;
    private List<String> phoneCodes;
    private Spinner codeSpinner;
    private CheckBox enableTVT,enableBTO;

    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_up, container, false);
        sharedPreferences = getContext().getSharedPreferences(Constants.LOGIN_SP,0);
        FacebookSdk.sdkInitialize(getContext());
        mAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();
        requestQueue = Volley.newRequestQueue(getContext());
        progressDialog = new ProgressDialog(getContext());
        phoneCodes = new ArrayList<>();
        setId(v);
        getCountryCodes();
        setListeners();
        setFacebook();
        return v;
    }

    private void setId(View v) {
        createAccountBtn = v.findViewById(R.id.signup_createAccountBtn);
        emailET = v.findViewById(R.id.signup_emailET);
        passwordET = v.findViewById(R.id.signup_passwordET);
        phoneET = v.findViewById(R.id.signup_phoneET);
        enableBTO = v.findViewById(R.id.enable_bto);
        enableTVT = v.findViewById(R.id.enable_tvt);
        loginFbButton = v.findViewById(R.id.signup_fb_button);
        showPasswordBtn = v.findViewById(R.id.eyeBtn);
        codeSpinner = v.findViewById(R.id.phone_codes_spinner);
    }

    private void getCountryCodes() {
        String url = Urls.BASE_URL + Urls.GET_COUNTRY_CODES;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v("mobileresponse",response.toString());
                        try {
                            String status = response.getString("status");
                            if(status.equals("success")){
                                JSONArray codes = response.getJSONArray("codes");
                                for(int i=0;i<codes.length();i++){
                                    String code = codes.getString(i);
                                    phoneCodes.add(code);
                                }
                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, phoneCodes);
                                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                codeSpinner.setAdapter(arrayAdapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },null);
        requestQueue.add(jsonObjectRequest);
    }

    private void setListeners() {
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Creating account");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String email = emailET.getText().toString().trim().toLowerCase();
                String password = passwordET.getText().toString();
                String phone = codeSpinner.getSelectedItem()+phoneET.getText().toString().trim();
                if(validate(email,password,phone) && checkTVTorBTO()){
                    progressDialog.show();
                    mAuth.createUserWithEmailAndPassword(email,password)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    FirebaseUser user = authResult.getUser();
                                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            final Snackbar snackbar = Snackbar.make(v,"Verification email sent",Snackbar.LENGTH_LONG);
                                            snackbar.setAction("DISMISS", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    snackbar.dismiss();
                                                }
                                            });
                                            snackbar.setActionTextColor(Color.YELLOW);
                                            snackbar.show();
                                        }
                                    });
                                    createSignUpRequest(null, user.getUid());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.cancel();
                                    Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        showPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(eyeBtnClick){
                    passwordET.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    showPasswordBtn.setImageResource(R.drawable.ic_openeye);
                }
                else {
                    passwordET.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    showPasswordBtn.setImageResource(R.drawable.ic_closedeye);
                }
                eyeBtnClick = !eyeBtnClick;
            }
        });
        loginFbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.CURRENT_FRAGMENT=Constants.SIGNUP_TITLE;
                if(checkTVTorBTO()){
                    progressDialog.show();
                    LoginManager.getInstance().logInWithReadPermissions(getActivity(),Arrays.asList(Constants.FB_EMAIL,
                            Constants.FB_PUBLIC_PROFILE, Constants.FB_GENDER,Constants.FB_FRIENDS));
                }
            }
        });
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
                                    final JSONObject fbdatainp = response.getJSONObject();
                                    final JSONObject fbobj = new JSONObject();
                                    try {
                                        fbobj.put(DatabaseKeys.FACEBOOK_ID,fbdatainp.getString("id"));
                                        fbobj.put(DatabaseKeys.FULL_NAME,fbdatainp.getString("name"));
                                        fbobj.put(DatabaseKeys.GENDER,fbdatainp.getString("gender"));
                                        fbobj.put(DatabaseKeys.EMAIL_ADDRESS,fbdatainp.getString("email"));
                                        final String url = fbdatainp.getJSONObject("picture").getJSONObject("data").getString("url");
                                        ImageRequest ir = new ImageRequest(url, new Response.Listener<Bitmap>() {
                                            @Override
                                            public void onResponse(Bitmap response) {
                                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                response.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                                byte[] imageBytes = baos.toByteArray();
                                                String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                                                try {
                                                    fbobj.put(DatabaseKeys.PROFILE_IMAGE,encodedImage);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                Log.v("fbdatainp",fbdatainp.toString());
                                                Log.v("fbobj",fbobj.toString());
                                                handleFacebookAccessToken(loginResult.getAccessToken(), fbobj);
                                            }
                                        }, 100, 100, null, null);
                                        requestQueue.add(ir);

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
            }
            @Override
            public void onError(FacebookException error) {
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
                                Log.v("fbobj",fbobj.toString());
                                createSignUpRequest(fbobj,uid);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private boolean validate(String email, String password, String phone){
        boolean isEmail = validateEmail(email);
        boolean isPassword = validatePassword(password);
        boolean isPhone = validatePhone(phone);
        return isEmail && isPassword && isPhone;
    }

    private boolean validatePhone(String phone) {
        if(phone==null || !Patterns.PHONE.matcher(phone).matches()) {
            phoneET.setError(Constants.PHONE_ERROR);
            return false;
        }
        return true;
    }

    private boolean validatePassword(String password) {
        if(password.length()<6){
            passwordET.setError(Constants.PASSWORD_ERROR);
            return false;
        }
        return true;
    }

    private boolean validateEmail(String email) {
        if(email==null || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailET.setError(Constants.EMAIL_ERROR);
            return false;
        }
        return true;
    }

    private void createSignUpRequest(JSONObject fbobj, String uid){
        final JSONObject jsonObject = new JSONObject();
        boolean isFb = false;
        try {
            jsonObject.put(DatabaseKeys.USER_ID,uid);
            jsonObject.put(DatabaseKeys.IS_BTO,String.valueOf(enableBTO.isChecked()));
            jsonObject.put(DatabaseKeys.IS_TVT,String.valueOf(enableTVT.isChecked()));
            if(fbobj!=null){
                isFb = true;
                jsonObject.put(DatabaseKeys.ACTIVE_MODE,Constants.TVT);
                jsonObject.put(DatabaseKeys.EMAIL_ADDRESS, fbobj.getString(DatabaseKeys.EMAIL_ADDRESS));
                jsonObject.put(DatabaseKeys.GENDER, fbobj.getString(DatabaseKeys.GENDER));
                jsonObject.put(DatabaseKeys.NAME, fbobj.getString(DatabaseKeys.FULL_NAME));
                jsonObject.put(DatabaseKeys.FACEBOOK_ID, fbobj.getString(DatabaseKeys.FACEBOOK_ID));
                jsonObject.put(DatabaseKeys.PROFILE_IMAGE, fbobj.getString(DatabaseKeys.PROFILE_IMAGE));
            }
            else{
                jsonObject.put(DatabaseKeys.EMAIL_ADDRESS, emailET.getText().toString().trim().toLowerCase());
                jsonObject.put(DatabaseKeys.PHONE_NUMBER, codeSpinner.getSelectedItem()+phoneET.getText().toString().trim());
            }
            String url=Urls.BASE_URL + Urls.SIGNUP;
            jsonObject.put(DatabaseKeys.USER_TOKEN, FirebaseInstanceId.getInstance().getToken());
            Log.v("signupreq",jsonObject.toString());
            final boolean finalIsFb = isFb;
            signupRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("jsonresponse", response.toString());
                            try {
                                String status = response.getString("status");
                                String msg = response.getString("message");
                                progressDialog.cancel();
                                if (status.equals("success")) {
                                    if (finalIsFb) {
                                        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                                        if (enableBTO.isChecked() && !enableTVT.isChecked())
                                            sharedPreferences.edit().putString(Constants.LOGIN_TYPE, Constants.BTO).apply();
                                        else
                                            sharedPreferences.edit().putString(Constants.LOGIN_TYPE, Constants.TVT).apply();
                                        startActivity(new Intent(getActivity(), SplashTouristActivity.class));
                                        getActivity().finish();
                                    }
                                } else
                                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getContext(),error.toString(),Toast.LENGTH_SHORT).show();
                }
            });
            requestQueue.add(signupRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean checkTVTorBTO(){
        if(!enableTVT.isChecked() && !enableBTO.isChecked()){
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
            return false;
        }
        return true;
    }
}