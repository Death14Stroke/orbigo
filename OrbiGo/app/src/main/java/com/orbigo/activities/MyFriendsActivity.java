
package com.orbigo.activities;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.orbigo.R;
import com.orbigo.adapters.FriendsAdapter;
import com.orbigo.models.MyFriend;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyFriendsActivity extends AppCompatActivity {

    private RecyclerView fbRV,contactsRV;
    private ImageButton showFbBtn, showContactsBtn;
    private List<MyFriend> fbList,contactsList;
    private FriendsAdapter fbAdapter,contactsAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null)
            actionBar.setDisplayHomeAsUpEnabled(true);
        fbRV = findViewById(R.id.fbRV);
        fbList = new ArrayList<>();
        fbAdapter = new FriendsAdapter(this,fbList);
        fbRV.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        fbRV.setLayoutManager(new LinearLayoutManager(this));
        fbRV.setItemAnimator(new DefaultItemAnimator());
        fbRV.setAdapter(fbAdapter);
        contactsRV = findViewById(R.id.contactsRV);
        contactsList = new ArrayList<>();
        contactsAdapter = new FriendsAdapter(this,contactsList);
        contactsRV.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        contactsRV.setLayoutManager(new LinearLayoutManager(this));
        contactsRV.setItemAnimator(new DefaultItemAnimator());
        contactsRV.setAdapter(contactsAdapter);
        getFbFriends();
        getContacts();
        showContactsBtn = findViewById(R.id.show_contacts);
        showFbBtn = findViewById(R.id.show_fb);
        showFbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fbRV.getVisibility()==View.GONE) {
                    fbRV.setVisibility(View.VISIBLE);
                    showFbBtn.setImageResource(R.drawable.ic_down);
                }
                else {
                    fbRV.setVisibility(View.GONE);
                    showFbBtn.setImageResource(R.drawable.ic_right);
                }
            }
        });
        showContactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(contactsRV.getVisibility()==View.GONE) {
                    contactsRV.setVisibility(View.VISIBLE);
                    showContactsBtn.setImageResource(R.drawable.ic_down);
                }
                else {
                    contactsRV.setVisibility(View.GONE);
                    showContactsBtn.setImageResource(R.drawable.ic_right);
                }
            }
        });
    }

    private void getContacts() {
        Dexter.withActivity(MyFriendsActivity.this)
                .withPermission(android.Manifest.permission.READ_CONTACTS)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        ContentResolver cr = getContentResolver();
                        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                                null, null, null, null);
                        if (cur.getCount() > 0) {
                            while (cur.moveToNext()) {
                                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                                    Log.v("contactsdata","name : " + name + ", ID : " + id);

                                    // get the phone number
                                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                            new String[]{id}, null);
                                    while (pCur.moveToNext()) {
                                        String phone = pCur.getString(
                                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                        Log.v("contactsdata","phone" + phone);
                                    }
                                    pCur.close();
                                }
                            }
                        }
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MyFriendsActivity.this,"Permission denied",Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    private void getFbFriends() {
        Log.v("friendlist","getting list");
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
//        Log.v("friendlist","token:"+accessToken+"id:"+accessToken.getUserId());

        if (accessToken!=null)
        GraphRequest.newGraphPathRequest(
                accessToken,
                "/"+accessToken.getUserId()+"/friends",
                new GraphRequest.Callback(){
                    @Override
                    public void onCompleted(GraphResponse response) {
                        JSONObject jsonObject = response.getJSONObject();
                        try {
                            JSONArray friendsArray = jsonObject.getJSONArray("data");
                            for(int i=0;i<friendsArray.length();i++){
                                JSONObject object = friendsArray.getJSONObject(i);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }).executeAsync();
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
}