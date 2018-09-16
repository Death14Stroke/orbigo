package com.orbigo.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
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
import com.orbigo.activities.BTOBottomNavActivity;
import com.orbigo.activities.ListBusinessActivity;
import com.orbigo.constants.Constants;
import com.orbigo.constants.DatabaseKeys;
import com.orbigo.constants.Urls;
import com.orbigo.helpers.Utils;
import com.orbigo.models.Business;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import de.hdodenhof.circleimageview.CircleImageView;

public class BusinessDetailsFragment extends Fragment implements Constants.ChangeBusinessListener {

    private List<Business> businessList = new ArrayList<>();
    private List<String> categoriesList = new ArrayList<>();
    private TextView countTV;
    private View newBusinessBtn;
    private TextView tv_edit;
    private TextView nameTV, addressTV, seekbar_text;
    private SeekBar PP_Player_SeekBar;
    private EditText descriptionTV, capacityTV, fromTV, toTV;
    private ImageButton moreBtn;
    private View cameraBtn, galleryBtn;
    private LinearLayout moreDetailsLL;
    private Spinner spinner;
    private Button updateBtn;
    private CircleImageView profilePicture;
    private RelativeLayout rl_camera;
    private RequestQueue requestQueue;
    private Business b;

    public BusinessDetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_business_details, container, false);
        tv_edit = v.findViewById(R.id.tv_edit);
        tv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), ListBusinessActivity.class);
                intent.putExtra(ListBusinessActivity.NEW_BUSINESS,false);
                startActivity(intent);
            }
        });
        //recyclerView = v.findViewById(R.id.businessListRV);
        countTV = v.findViewById(R.id.businessCount);
        newBusinessBtn = v.findViewById(R.id.newBusinessBtn);
        newBusinessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), ListBusinessActivity.class);
                intent.putExtra(ListBusinessActivity.NEW_BUSINESS,true);
                startActivity(intent);
            }
        });
        businessList = Constants.BUSINESS_LIST;
        countTV.setText(String.valueOf(businessList.size()));
        categoriesList = Constants.CATEGORIES_LIST;
        requestQueue = Volley.newRequestQueue(getActivity());


//        recyclerView.setAdapter(businessAdapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), VERTICAL);
//        recyclerView.addItemDecoration(decoration);
        Constants.addChangeBusinessListener(this);

        setId(v);
        updateUI();
        setListeners();
        return v;
    }

    public void setId(View itemView){
        nameTV = itemView.findViewById(R.id.NameOfBusiness);
        addressTV = itemView.findViewById(R.id.SubNameOfBusiness);
        moreBtn = itemView.findViewById(R.id.right);
        moreDetailsLL = itemView.findViewById(R.id.moreDetails);
        spinner = itemView.findViewById(R.id.spinner_b_field);
        descriptionTV = itemView.findViewById(R.id.Description);
        capacityTV = itemView.findViewById(R.id.capacity);
        updateBtn = itemView.findViewById(R.id.update);
        fromTV = itemView.findViewById(R.id.start_price);
        toTV = itemView.findViewById(R.id.end_price);
        cameraBtn = itemView.findViewById(R.id.business_camera);
        //galleryBtn = itemView.findViewById(R.id.business_gallery);
        profilePicture = itemView.findViewById(R.id.businessProfilePic);
        PP_Player_SeekBar = itemView.findViewById(R.id.PP_Player_SeekBar);
        seekbar_text = itemView.findViewById(R.id.seekbar_text);
        rl_camera = itemView.findViewById(R.id.rl_camera);

    }

    private void updateUI(){
        b = Constants.SELECTED_BUSINESS;
        final Activity a = getActivity();
        if (a==null || b==null) return;
        if (b.getImage() != null) {
            String encodedImage = b.getImage();
            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
            if (encodedImage == null || encodedImage.isEmpty() || encodedImage.equalsIgnoreCase("NULL")) {
                Glide.with(getActivity())
                        .load(R.drawable.ic_switch_to_business)
                        .into(profilePicture);
            } else {
                Glide.with(getActivity())
                        .load(decodedString)
                        .into(profilePicture);
            }


        }
        nameTV.setText(b.getBus_name());
        addressTV.setText(b.getAddress());
    }

    private void setListeners(){
        b = Constants.SELECTED_BUSINESS;
        final Activity a = getActivity();
        if (a==null || b==null) return;

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showPhotoMenuDialog(a, new Observer() {
                    @Override
                    public void update(Observable o, Object arg) {
                        if ((getActivity()) != null) {
                            ((BTOBottomNavActivity)getActivity()).uploadPicture("");
                        }
                    }
                });
            }
        });

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showPhotoMenuDialog(a, new Observer() {
                    @Override
                    public void update(Observable o, Object arg) {
                        if ((getActivity()) != null) {
                            ((BTOBottomNavActivity)getActivity()).uploadPicture("");
                        }
                    }
                });
            }
        });
//        holder.galleryBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Constants.SELECTED_BUSINESS = b;
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                a.startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.PICK_IMAGE_REQUEST);
//            }
//        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((BTOBottomNavActivity)a).loadFragment(new BusinessFragment());

//                Constants.SELECTED_BUSINESS=b;
//                if (holder.moreDetailsLL.getVisibility() == View.GONE) {
//                    holder.profilePicture.setVisibility(View.VISIBLE);
//                    holder.moreDetailsLL.setVisibility(View.VISIBLE);
//                    holder.moreBtn.setImageResource(R.drawable.ic_down);
//                    holder.rl_camera.setVisibility(View.VISIBLE);
//                } else {
//                    holder.profilePicture.setVisibility(View.GONE);
//                    holder.moreDetailsLL.setVisibility(View.GONE);
//                    holder.moreBtn.setImageResource(R.drawable.ic_right);
//                    holder.rl_camera.setVisibility(View.GONE);
//                }
            }
        });
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, categoriesList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        for (int i = 0; i < categoriesList.size(); i++) {
            if (categoriesList.get(i).equals(b.getCategory())) {
                spinner.setSelection(i);
            }
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        if (b.getPrice_range() != null) {
            String[] prices = b.getPrice_range().split(",");
            if (prices.length == 2) {
                fromTV.setText(prices[0]);
                toTV.setText(prices[1]);
            }
        }
        if (b.getDescription() != null) {
            if (b.getDescription().equals("NULL")) {
                descriptionTV.setText("Enter description");
            } else {
                descriptionTV.setText(b.getDescription());
            }

        }
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float from = 0, to = 0;
                if (!fromTV.getText().toString().isEmpty())
                    from = Float.parseFloat(fromTV.getText().toString());
                if (!toTV.getText().toString().isEmpty())
                    to = Float.parseFloat(toTV.getText().toString());
                if (from > to) {
                    Toast.makeText(getActivity(), "Invalid price range", Toast.LENGTH_SHORT).show();
                    return;
                }
                JSONObject data = new JSONObject();
                try {
                    data.put(DatabaseKeys.AUTHORIZED_BUSINESS_NUMBER, b.getAbn());
                    data.put(DatabaseKeys.CAPACITY, capacityTV.getText().toString().trim());
                    data.put(DatabaseKeys.CATEGORY, spinner.getSelectedItem().toString());
                    data.put(DatabaseKeys.DESCRIPTION, descriptionTV.getText().toString());
                    final String price_range = fromTV.getText().toString() + "," + toTV.getText().toString();
                    data.put(DatabaseKeys.PRICE_RANGE, price_range);
                    String url = Urls.BASE_URL + Urls.UPDATE_BUSINESS;
                    Log.d("updatebusiness", data.toString());
                    final JsonObjectRequest updateReq = new JsonObjectRequest(Request.Method.POST, url, data,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d("updatebusiness", response.toString());
                                    try {
                                        if (response.getString("status").compareTo("success") == 0) {
                                            Toast.makeText(getActivity(), "success", Toast.LENGTH_SHORT).show();
                                            Constants.SELECTED_BUSINESS.setCapacity(capacityTV.getText().toString().trim());
                                            Constants.SELECTED_BUSINESS.setCategory(spinner.getSelectedItem().toString());
                                            Constants.SELECTED_BUSINESS.setDescription(descriptionTV.getText().toString());
                                            Constants.SELECTED_BUSINESS.setPrice_range(price_range);
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
        });
        if (b.getCapacity() != null) {
            capacityTV.setVisibility(View.GONE);
            capacityTV.setText(b.getCapacity());
        }

        PP_Player_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                say_minutes_left(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }



    @Override
    public void onDestroy() {
        Constants.removeChangeBusinessListener(this);
        super.onDestroy();
    }

    @Override
    public void onChangeBusiness() {
        if (isDetached()) return;
        updateUI();
        setListeners();

    }

    public void say_minutes_left(int how_many) {
        String what_to_say = String.valueOf(how_many);
        seekbar_text.setText(what_to_say);
        int seek_label_pos = (((PP_Player_SeekBar.getRight() - PP_Player_SeekBar.getLeft()) * PP_Player_SeekBar.getProgress())
                / PP_Player_SeekBar.getMax()) + PP_Player_SeekBar.getLeft();
        if (how_many <= 9) {
            seekbar_text.setX(seek_label_pos - 6);
        } else {
            seekbar_text.setX(seek_label_pos - 60);
        }



       /* String what_to_say = String.valueOf(how_many);
        holder.seekbar_text.setText(what_to_say);

        int seek_label_pos = (int)((float)(holder.PP_Player_SeekBar.getMeasuredWidth()) * ((float)how_many / 60f));
        holder.seekbar_text.setX(seek_label_pos);*/
    }
}