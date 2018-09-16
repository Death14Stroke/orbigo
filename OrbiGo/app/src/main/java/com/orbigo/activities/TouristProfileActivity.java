package com.orbigo.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.orbigo.R;
import com.orbigo.constants.Constants;
import com.orbigo.constants.DatabaseKeys;
import com.orbigo.constants.Urls;
import com.orbigo.helpers.APIHelper;
import com.orbigo.helpers.CacheImage;
import com.orbigo.helpers.Utils;
import com.orbigo.models.NearbyPoi;
import com.orbigo.models.Poi;
import com.orbigo.models.SearchData;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.orbigo.constants.Constants.CAMERA_REQUEST;

public class TouristProfileActivity extends AppCompatActivity {

    private Button logoutBtn,tripsBtn,savedBtn,profileBtn,switchBtn,inviteBtn;
    private ImageView close;
    private CircleImageView circleImageView;
    private TextView nameTV;
    private ImageButton uploadPhotoBtn, capturePhotoBtn;
    private RequestQueue requestQueue;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private CacheImage cacheImage;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tourist_profile);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        sharedPreferences = getSharedPreferences(Constants.LOGIN_SP,0);
        requestQueue = Volley.newRequestQueue(this);
        mAuth = FirebaseAuth.getInstance();
        setId();
        setListener();
        getProfile();
    }

    private void setListener() {
        cacheImage =new CacheImage(TouristProfileActivity.this,circleImageView,"profile");
        Bitmap b= cacheImage.getCaсhedImage();
        if (b!=null) {
            Glide.with(getApplicationContext())
                    .load(b)
                    .into(circleImageView);
        }
        /*inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_SUBJECT,"OrbiGo");
                intent.putExtra(Intent.EXTRA_TEXT,Constants.INVITE_TEXT);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                startActivity(Intent.createChooser(intent,"Invite with..."));
            }
        });*/

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  startActivity(new Intent(TouristProfileActivity.this, TouristProfileActivity.class));
                finish();
              //  overridePendingTransition(R.anim.righttoleft2,R.anim.lefttoright2);

            }
        });
        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchProfile();
            }
        });
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TouristProfileActivity.this,MyFriendsActivity.class));
            }
        });
        savedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                APIHelper.getSavedPois(mAuth.getCurrentUser().getUid(), requestQueue, new Observer() {
                    @Override
                    public void update(Observable o, Object arg) {
                        ArrayList<Poi> mySavedPoiList = (ArrayList<Poi>) arg;
                        ArrayList<NearbyPoi> regionOrState=new ArrayList<>();
                        for (Poi poi:mySavedPoiList){
                            for (SearchData poi1:Constants.WORLD_DATA){
                                if ((poi1 instanceof Poi) && poi1.getId().equalsIgnoreCase(poi.getId())){
                                    NearbyPoi nearbyPoi=new NearbyPoi();
                                    nearbyPoi.setPoiDetails((Poi) poi1);
                                    regionOrState.add(nearbyPoi);
                                }
                            }

                        }
                        NearbyPoiActivity.start(TouristProfileActivity.this,regionOrState,false,Constants.MODE_PLANE,true,null);
                        finish();

                    }
                });
                //startActivity(new Intent(TouristProfileActivity.this,SavedPlacesActivity.class));



            }
        });
        tripsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TouristProfileActivity.this,MyTripsActivity.class));
                finish();
            }
        });
       /* capturePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });*/
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cacheImage.removeCache();
                mAuth.signOut();
                finishAffinity();
                startActivity(new Intent(TouristProfileActivity.this,LoginActivity.class));
            }
        });
       /* uploadPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image*//*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.PICK_IMAGE_REQUEST);
            }
        });*/

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showPhotoMenuDialog(TouristProfileActivity.this, new Observer() {
                    @Override
                    public void update(Observable o, Object arg) {
                        uploadPicture("");
                    }
                });
            }
        });
    }

    private void switchProfile() {
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.SWITCH_TO,Constants.BTO);
            input.put(DatabaseKeys.USER_ID,mAuth.getCurrentUser().getUid());
            String url = Urls.BASE_URL + Urls.SWITCH_PROFILE;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String status = response.getString("status");
                                if(status.equals("success")){
                                    sharedPreferences.edit().putString(Constants.LOGIN_TYPE,Constants.BTO).apply();
                                    startActivity(new Intent(TouristProfileActivity.this,SplashBusinessActivity.class));
                                    finishAffinity();
                                }
                                else{
                                    showDialog("business");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },null);
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    private void openCamera() {
//        Dexter.withActivity(TouristProfileActivity.this)
//                .withPermission(Manifest.permission.CAMERA)
//                .withListener(new PermissionListener() {
//                    @Override
//                    public void onPermissionGranted(PermissionGrantedResponse response) {
//                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
//                    }
//
//                    @Override
//                    public void onPermissionDenied(PermissionDeniedResponse response) {
//                        Toast.makeText(TouristProfileActivity.this,"Permission denied",Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
//                        token.continuePermissionRequest();
//                    }
//                }).check();
//    }

    private void setId() {
        logoutBtn = findViewById(R.id.tlogout_click);
        circleImageView = findViewById(R.id.tprofile_image);
        nameTV = findViewById(R.id.tname);
        close=findViewById(R.id.close);
       // uploadPhotoBtn = findViewById(R.id.tupload_photo_btn);
        //capturePhotoBtn = findViewById(R.id.tcaptureImageBtn);
        tripsBtn = findViewById(R.id.trips);
        inviteBtn = findViewById(R.id.shareOrbiGo);
        savedBtn = findViewById(R.id.saved);
        profileBtn = findViewById(R.id.tprofile);
        switchBtn = findViewById(R.id.switchToBusiness);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==Constants.PICK_IMAGE_REQUEST && resultCode==RESULT_OK) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                Log.v("stringimage","encoded: "+encodedImage.length());
                Log.v("stringimage","bitmap: "+String.valueOf(bitmap));
                byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                uploadPicture(encodedImage);
            } catch (IOException ignored) {

            }
        }
        else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            uploadPicture(encodedImage);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadPicture(String encodedImage) {
        showPicture(encodedImage);
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.USER_ID,mAuth.getCurrentUser().getUid());
            input.put(DatabaseKeys.PROFILE_IMAGE,encodedImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Urls.BASE_URL + Urls.UPLOAD_PICTURE;
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, input,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.v("responseimage",response.toString());
                            String status = response.getString("status");
                            if(status.equals("success")){
                                Toast.makeText(getApplicationContext(),"success",Toast.LENGTH_SHORT).show();
                                getProfile();
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

    private void getProfile() {
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.USER_ID,mAuth.getCurrentUser().getUid());
            String url = Urls.BASE_URL + Urls.GET_MY_PROFILE;
            final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("t_profile",response.toString());
                            try {
                                String name = response.getString("name");
                                if(name.isEmpty())
                                    name = response.getString("email_address");

                                if (name.equals("NULL")) {
                                    name = "User";
                                }


                                nameTV.setText(name);
                                String encodedImage = response.getString("profile_image");
                                showPicture(encodedImage);
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    private void showDialog(final String mode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("createresponse",response.toString());
                            try {
                                if(response.getString("status").equals("success")){
                                    sharedPreferences.edit().putString(Constants.LOGIN_TYPE, Constants.BTO).apply();
                                    startActivity(new Intent(TouristProfileActivity.this,SplashBusinessActivity.class));
                                    finish();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },null);
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    void showPicture(String encodedImage){
        if (encodedImage == null || encodedImage.isEmpty() || encodedImage.equalsIgnoreCase("NULL")) {
            Glide.with(getApplicationContext())
                    .load(R.drawable.ic_switch_to_business)
                    .into(circleImageView);
            cacheImage.removeCaсhedImage();
        } else {
            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
            Glide.with(getApplicationContext())
                    .load(decodedString)
                    .into(circleImageView);
            cacheImage.saveCaсhedImage(decodedString);


        }
    }
}