package com.orbigo.adapters;

import android.app.DialogFragment;
import android.content.Context;
import android.location.Location;
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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.orbigo.R;
import com.orbigo.activities.AddTripPoiActivity;
import com.orbigo.activities.NearbyPoiActivity;
import com.orbigo.activities.PoiDetailsActivity;
import com.orbigo.constants.Constants;
import com.orbigo.custom_ui_components.AddTripDialogFragment;
import com.orbigo.helpers.APIHelper;
import com.orbigo.models.NearbyPoi;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class NearbyPoiAdapter extends RecyclerView.Adapter<NearbyPoiAdapter.MyViewHolder> {

    private Context context;
    private List<NearbyPoi> nearbyPoiList;
    private List<NearbyPoi> nearbyPoiListFiltered;
    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;
    RequestOptions requestOptions;
    private boolean isSaved;
    private Location currentLocation;

    public NearbyPoiAdapter(Context context, List<NearbyPoi> nearbyPoiList,boolean isSaved,Location location) {
        this.context = context;
        this.nearbyPoiList = nearbyPoiList;
        this.isSaved=isSaved;
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(context);
        requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(5));
        currentLocation=location;
    }

    public void setLocation(Location location){
        currentLocation=location;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.nearby_poi_cv, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final NearbyPoi nearbyPoi = nearbyPoiList.get(position);
        holder.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.SELECTED_NEARBY_POI = nearbyPoi;
                //AddTripPoiActivity.start(context,nearbyPoi,currentLocation);
                DialogFragment newFragment = new AddTripDialogFragment();
                newFragment.show(((NearbyPoiActivity) context).getFragmentManager(), Constants.ADD_TO_TRIP_DIALOG);
            }
        });
        holder.attractionTV.setText(nearbyPoi.getPoiDetails().getCategory());
        holder.costTV.setText(nearbyPoi.getCost());
        holder.timeTV.setText(nearbyPoi.getTime());
        holder.distanceTV.setText(nearbyPoi.getDistance());
        //  holder.areaTV.setText(nearbyPoi.getPoiDetails().getIs_in_region());
        holder.nameTV.setText(nearbyPoi.getPoiDetails().getName());
        if (nearbyPoi.isLiked()) {
            holder.likeBtn.setImageResource(R.drawable.ic_outlinefavorite);
        } else {
            holder.likeBtn.setImageResource(R.drawable.ic_outlinefavorite_border);
        }
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                APIHelper.like(!nearbyPoi.isLiked(), mAuth.getCurrentUser().getUid(), nearbyPoi.getPoiDetails().getId(), requestQueue, new Observer() {

                    @Override
                    public void update(Observable o, Object arg) {
                        try {
                            JSONObject response = (JSONObject) arg;
                            String status = response.getString("status");
                            if (status.equals("success")) {
                                nearbyPoi.setLiked(!nearbyPoi.isLiked());
                                nearbyPoiList.get(position).setLiked(nearbyPoi.isLiked());
                                if (isSaved) nearbyPoiList.remove(position);
                               notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        Log.i("URRRRL", " adapter == > " + nearbyPoi.getPoiDetails().getPicture());

        Glide.with(context)
                .load(Uri.parse(nearbyPoi.getPoiDetails().getPicture()))
                .apply(requestOptions)
                .into(holder.imageView);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.SELECTED_NEARBY_POI = nearbyPoi;
                PoiDetailsActivity.start(context, nearbyPoi);
            }
        });
    }


    @Override
    public int getItemCount() {
        return nearbyPoiList.size();
    }

   /* @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    nearbyPoiListFiltered = nearbyPoiList;
                } else {
                    List<NearbyPoi> filteredList = new ArrayList<>();
                    for (NearbyPoi row : nearbyPoiList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getPoiDetails().getName().toLowerCase().contains(charSequence)|| row.getPoiDetails().getCategory().toLowerCase().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    nearbyPoiListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = nearbyPoiListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                nearbyPoiListFiltered = (ArrayList<NearbyPoi>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }*/

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTV, areaTV, distanceTV, timeTV, costTV, attractionTV;
        private ImageButton addBtn, likeBtn;
        private ImageView imageView;
        private CardView cardView;

        public MyViewHolder(View itemView) {
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