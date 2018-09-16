package com.orbigo.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.orbigo.R;
import com.orbigo.activities.BTOBottomNavActivity;
import com.orbigo.constants.Constants;
import com.orbigo.fragments.BusinessDetailsFragment;
import com.orbigo.helpers.Utils;
import com.orbigo.models.Business;

import java.util.List;
import java.util.Observer;

import de.hdodenhof.circleimageview.CircleImageView;

public class BusinessAdapter extends RecyclerView.Adapter<BusinessAdapter.MyViewHolder> {
    private Context mContext;
    private List<Business> businessList;
    private List<String> categoriesList;
    private RequestQueue requestQueue;
    private Observer removePhotoObserver;

    public BusinessAdapter(Context mContext, List<Business> businessList, List<String> categoriesList, Observer removePhotoObserver) {
        this.mContext = mContext;
        this.businessList = businessList;
        this.categoriesList = categoriesList;
        this.removePhotoObserver = removePhotoObserver;
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
        Log.d("onBindcalled", "position:" + position);
        final Business b = businessList.get(position);
        final Activity a = (Activity) mContext;
        if (b.getImage() != null) {
            String encodedImage = b.getImage();
            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
            if (encodedImage == null || encodedImage.isEmpty() || encodedImage.equalsIgnoreCase("NULL")) {
                Glide.with(mContext)
                        .load(R.drawable.ic_switch_to_business)
                        .into(holder.profilePicture);
            } else {
                Glide.with(mContext)
                        .load(decodedString)
                        .into(holder.profilePicture);
            }


        }


        holder.profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showPhotoMenuDialog(a, removePhotoObserver);
            }
        });

        holder.nameTV.setText(b.getBus_name());
        holder.addressTV.setText(b.getAddress());
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.SELECTED_BUSINESS = b;
                ((BTOBottomNavActivity) a).loadFragment(new BusinessDetailsFragment());



            }
        });



    }





    @Override
    public int getItemCount() {
        return businessList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nameTV, addressTV;//, seekbar_text;
        //SeekBar PP_Player_SeekBar;
        //EditText descriptionTV, capacityTV, fromTV, toTV;
        ImageButton moreBtn;
        //View cameraBtn, galleryBtn;
        //LinearLayout moreDetailsLL;
        //Spinner spinner;
        //Button updateBtn;
        CircleImageView profilePicture;
        //RelativeLayout rl_camera;

        public MyViewHolder(View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.NameOfBusiness);
            addressTV = itemView.findViewById(R.id.SubNameOfBusiness);
            moreBtn = itemView.findViewById(R.id.right);
            profilePicture = itemView.findViewById(R.id.businessProfilePic);

        }
    }
}