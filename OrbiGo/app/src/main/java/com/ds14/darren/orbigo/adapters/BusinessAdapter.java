package com.ds14.darren.orbigo.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.constants.Constants;
import com.ds14.darren.orbigo.constants.DatabaseKeys;
import com.ds14.darren.orbigo.constants.Urls;
import com.ds14.darren.orbigo.models.Business;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.ds14.darren.orbigo.constants.Constants.CAMERA_REQUEST;
import static com.ds14.darren.orbigo.constants.Constants.MY_CAMERA_PERMISSION_CODE;

public class BusinessAdapter extends RecyclerView.Adapter<BusinessAdapter.MyViewHolder> {
    private Context mContext;
    private List<Business> businessList;
    private List<String> categoriesList;
    private RequestQueue requestQueue;

    public BusinessAdapter(Context mContext, List<Business> businessList, List<String> categoriesList) {
        this.mContext = mContext;
        this.businessList = businessList;
        this.categoriesList = categoriesList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.business_cv, parent, false);
        requestQueue = Volley.newRequestQueue(mContext);
        return new BusinessAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        Log.d("onBindcalled","position:"+position);
        final Business b = businessList.get(position);
        final Activity a = (Activity)mContext;
        if(b.getImage()!=null){
            String encodedImage = b.getImage();
            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
            Glide.with(mContext)
                    .load(decodedString)
                    .into(holder.profilePicture);
        }
        holder.cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.SELECTED_BUSINESS = b;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (mContext.checkSelfPermission(Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        a.requestPermissions(new String[]{Manifest.permission.CAMERA},
                                MY_CAMERA_PERMISSION_CODE);
                    } else {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        a.startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }
                }
            }
        });
        holder.galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.SELECTED_BUSINESS = b;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                a.startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.PICK_IMAGE_REQUEST);
            }
        });
        holder.nameTV.setText(b.getBus_name());
    //    holder.addressTV.setText(b.getAddress());
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.moreDetailsLL.getVisibility()==View.GONE) {
                    holder.moreDetailsLL.setVisibility(View.VISIBLE);
                    holder.moreBtn.setImageResource(R.drawable.ic_down);
                }
                else {
                    holder.moreDetailsLL.setVisibility(View.GONE);
                    holder.moreBtn.setImageResource(R.drawable.ic_right);
                }
            }
        });
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_spinner_item, categoriesList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinner.setAdapter(dataAdapter);
        for(int i=0;i<categoriesList.size();i++){
            if(categoriesList.get(i).equals(b.getCategory())){
                holder.spinner.setSelection(i);
            }
        }
        holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        if(b.getPrice_range()!=null){
            String[] prices = b.getPrice_range().split(",");
            if(prices.length==2){
                holder.fromTV.setText(prices[0]);
                holder.toTV.setText(prices[1]);
            }
        }
        if(b.getDescription()!=null){
            holder.descriptionTV.setText(b.getDescription());
        }
        holder.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float from=0,to=0;
                if(!holder.fromTV.getText().toString().isEmpty())
                    from = Float.parseFloat(holder.fromTV.getText().toString());
                if(!holder.toTV.getText().toString().isEmpty())
                    to = Float.parseFloat(holder.toTV.getText().toString());
                if(from>to){
                    Toast.makeText(mContext,"Invalid price range",Toast.LENGTH_SHORT).show();
                    return;
                }
                JSONObject data = new JSONObject();
                try {
                    data.put(DatabaseKeys.AUTHORIZED_BUSINESS_NUMBER,b.getAbn());
                    data.put(DatabaseKeys.CAPACITY,holder.capacityTV.getText().toString().trim());
                    data.put(DatabaseKeys.CATEGORY,holder.spinner.getSelectedItem().toString());
                    data.put(DatabaseKeys.DESCRIPTION,holder.descriptionTV.getText().toString());
                    final String price_range = holder.fromTV.getText().toString()+","+holder.toTV.getText().toString();
                    data.put(DatabaseKeys.PRICE_RANGE,price_range);
                    String url = Urls.BASE_URL + Urls.UPDATE_BUSINESS;
                    Log.d("updatebusiness",data.toString());
                    final JsonObjectRequest updateReq = new JsonObjectRequest(Request.Method.POST, url, data,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d("updatebusiness",response.toString());
                                    try {
                                        if(response.getString("status").compareTo("success")==0) {
                                            Toast.makeText(mContext, "success", Toast.LENGTH_SHORT).show();
                                            businessList.get(position).setCapacity(holder.capacityTV.getText().toString().trim());
                                            businessList.get(position).setCategory(holder.spinner.getSelectedItem().toString());
                                            businessList.get(position).setDescription(holder.descriptionTV.getText().toString());
                                            businessList.get(position).setPrice_range(price_range);
                                            notifyDataSetChanged();
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
        if(b.getCapacity()!=null){
            holder.capacityTV.setText(b.getCapacity());
        }
    }

    @Override
    public int getItemCount() {
        return businessList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView nameTV, addressTV;
        EditText descriptionTV, capacityTV, fromTV, toTV;
        ImageButton moreBtn, cameraBtn, galleryBtn;
        LinearLayout moreDetailsLL;
        Spinner spinner;
        Button updateBtn;
        CircleImageView profilePicture;

        public MyViewHolder(View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.NameOfBusiness);
           // addressTV = itemView.findViewById(R.id.Address);
            moreBtn = itemView.findViewById(R.id.right);
            moreDetailsLL = itemView.findViewById(R.id.moreDetails);
            spinner = itemView.findViewById(R.id.spinner_b_field);
            descriptionTV = itemView.findViewById(R.id.Description);
            capacityTV = itemView.findViewById(R.id.capacity);
            updateBtn = itemView.findViewById(R.id.update);
            fromTV = itemView.findViewById(R.id.start_price);
            toTV = itemView.findViewById(R.id.end_price);
            cameraBtn = itemView.findViewById(R.id.business_camera);
            galleryBtn = itemView.findViewById(R.id.business_gallery);
            profilePicture = itemView.findViewById(R.id.businessProfilePic);
        }
    }
}