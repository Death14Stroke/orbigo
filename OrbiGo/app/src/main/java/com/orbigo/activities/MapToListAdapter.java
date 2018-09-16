package com.orbigo.activities;

import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.orbigo.R;
import com.orbigo.constants.Constants;
import com.orbigo.constants.DatabaseKeys;
import com.orbigo.constants.Urls;
import com.orbigo.custom_ui_components.AddTripDialogFragment;
import com.orbigo.models.NearbyPoi;
import com.orbigo.models.Poi;
import com.orbigo.models.SearchData;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by infiny on 11/8/18.
 */

class MapToListAdapter extends RecyclerView.Adapter<MapToListAdapter.MyClassHolder> {

    Context context;
    ArrayList<NearbyPoi> searchData;
    RequestOptions requestOptions;
    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;

    public MapToListAdapter(Context context,ArrayList<NearbyPoi> searchData) {
        this.context=context;
        this.searchData=searchData;
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(context);
        requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(5));
    }

    @NonNull
    @Override
    public MyClassHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.maplist_adapter,null);
        return new MyClassHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyClassHolder holder, final int position) {

        holder.nameTV.setText(((SearchData)(searchData.get(position).getPoiDetails())).getName());
        holder.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Constants.SELECTED_NEARBY_POI = nearbyPoi;
                DialogFragment newFragment = new AddTripDialogFragment();
                newFragment.show(((NearbyPoiActivity)context).getFragmentManager(), Constants.ADD_TO_TRIP_DIALOG);
            }
        });
        holder.attractionTV.setText(searchData.get(position).getPoiDetails().getCategory());
         holder.costTV.setText(searchData.get(position).getCost());
        holder.timeTV.setText(searchData.get(position).getTime());
        holder.distanceTV.setText(searchData.get(position).getDistance());
     //   holder.areaTV.setText(searchData.get(position).getPoiDetails().getIs_in_region());
      //  holder.nameTV.setText(searchData.get(position).getPoiDetails().getName());

         holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                searchData.get(position).setLiked(!searchData.get(position).isLiked());
                searchData.get(position).setLiked(searchData.get(position).isLiked());
                if(searchData.get(position).isLiked()) {
                    addToDb(searchData.get(position).getPoiDetails(),v);
                }
                else {
                    removeFromDb(searchData.get(position).getPoiDetails(),v);
                }
            }
        });

       // Log.i("URRRRL", " adapter == > "+nearbyPoi.getPoiDetails().getPicture());

        Glide.with(context)
                .load(Uri.parse(searchData.get(position).getPoiDetails().getPicture()))
                .apply(requestOptions)
                .into(holder.imageView);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             /*   Constants.SELECTED_NEARBY_POI = nearbyPoi;*/
             PoiDetailsActivity.start(context,searchData.get(position));
                //context.startActivity(new Intent(context, PoiDetailsActivity.class));
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
                                    ((ImageButton)v).setImageResource(R.drawable.ic_outlinefavorite);
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
                                    ((ImageButton)v).setImageResource(R.drawable.ic_outlinefavorite_border);
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
        return searchData.size();
    }

    class MyClassHolder extends RecyclerView.ViewHolder {

        private TextView nameTV,areaTV,distanceTV,timeTV,costTV,attractionTV;
        private ImageButton addBtn,likeBtn;
        private ImageView imageView;
        private CardView cardView;


        public MyClassHolder(View itemView) {
          super(itemView);
          imageView = itemView.findViewById(R.id.poi_image);
          likeBtn = itemView.findViewById(R.id.poi_like_btn);
          nameTV = itemView.findViewById(R.id.poi_name);
          //  areaTV = itemView.findViewById(R.id.poi_area);
          distanceTV = itemView.findViewById(R.id.poi_distance);
          timeTV = itemView.findViewById(R.id.poi_time);
          costTV = itemView.findViewById(R.id.poi_cost);
          attractionTV = itemView.findViewById(R.id.poi_type);
          addBtn = itemView.findViewById(R.id.poi_add_btn);
          cardView = itemView.findViewById(R.id.nearby_poi_card);
      }
  }

}
