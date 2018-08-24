package com.ds14.darren.orbigo.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.activities.PoiDetailsActivity;
import com.ds14.darren.orbigo.constants.Constants;
import com.ds14.darren.orbigo.constants.DatabaseKeys;
import com.ds14.darren.orbigo.constants.Urls;
import com.ds14.darren.orbigo.custom_ui_components.AddTripDialogFragment;
import com.ds14.darren.orbigo.models.NearbyPoi;
import com.ds14.darren.orbigo.models.Poi;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class NearbyPoiAdapter extends RecyclerView.Adapter<NearbyPoiAdapter.MyViewHolder> {

    private Context context;
    private List<NearbyPoi> nearbyPoiList;
    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;

    public NearbyPoiAdapter(Context context, List<NearbyPoi> nearbyPoiList) {
        this.context = context;
        this.nearbyPoiList = nearbyPoiList;
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.nearby_poi_cv,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        final NearbyPoi nearbyPoi = nearbyPoiList.get(position);
        holder.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.SELECTED_NEARBY_POI = nearbyPoi;
                DialogFragment newFragment = new AddTripDialogFragment();
                newFragment.show(((Activity)context).getFragmentManager(), Constants.ADD_TO_TRIP_DIALOG);
            }
        });
        holder.attractionTV.setText(nearbyPoi.getPoiDetails().getCategory());
        holder.costTV.setText(nearbyPoi.getCost());
        holder.timeTV.setText(nearbyPoi.getTime());
        holder.distanceTV.setText(nearbyPoi.getDistance());
        holder.areaTV.setText(nearbyPoi.getPoiDetails().getIs_in_region());
        holder.nameTV.setText(nearbyPoi.getPoiDetails().getName());
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nearbyPoi.setLiked(!nearbyPoi.isLiked());
                nearbyPoiList.get(position).setLiked(nearbyPoi.isLiked());
                if(nearbyPoi.isLiked()) {
                    addToDb(nearbyPoi.getPoiDetails(),v);
                }
                else {
                    removeFromDb(nearbyPoi.getPoiDetails(),v);
                }
            }
        });
        Glide.with(context)
                .load(Uri.parse(nearbyPoi.getPoiDetails().getPicture()))
                .into(holder.imageView);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.SELECTED_NEARBY_POI = nearbyPoi;
                context.startActivity(new Intent(context, PoiDetailsActivity.class));
            }
        });
    }

    private void addToDb(Poi poi, final View v) {
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.USER_ID,mAuth.getCurrentUser().getUid());
            input.put("add_poi",poi.getId());
            String url = Urls.BASE_URL + Urls.ADD_SAVED_POI;
            Log.v("addpoi",input.toString());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("addpoi",response.toString());
                            try {
                                String status = response.getString("status");
                                if(status.equals("success")) {
                                    ((ImageButton)v).setImageResource(R.drawable.ic_heart_selected);
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

    private void removeFromDb(Poi poi, final View v) {
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.USER_ID,mAuth.getCurrentUser().getUid());
            input.put("del_poi",poi.getId());
            String url = Urls.BASE_URL + Urls.REMOVE_SAVED_POI;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("removeFromdb",response.toString());
                            try {
                                String status = response.getString("status");
                                if(status.equals("success")) {
                                    ((ImageButton)v).setImageResource(R.drawable.ic_heart_not_selected);
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

    @Override
    public int getItemCount() {
        return nearbyPoiList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView nameTV,areaTV,distanceTV,timeTV,costTV,attractionTV;
        private ImageButton addBtn,likeBtn;
        private ImageView imageView;
        private CardView cardView;

        public MyViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.poi_image);
            likeBtn = itemView.findViewById(R.id.poi_like_btn);
            nameTV = itemView.findViewById(R.id.poi_name);
            areaTV = itemView.findViewById(R.id.poi_area);
            distanceTV = itemView.findViewById(R.id.poi_distance);
            timeTV = itemView.findViewById(R.id.poi_time);
            costTV = itemView.findViewById(R.id.poi_cost);
            attractionTV = itemView.findViewById(R.id.poi_type);
            addBtn = itemView.findViewById(R.id.poi_add_btn);
            cardView = itemView.findViewById(R.id.nearby_poi_card);
        }
    }
}