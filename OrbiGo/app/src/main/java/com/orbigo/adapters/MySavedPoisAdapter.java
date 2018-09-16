package com.orbigo.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
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
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.orbigo.R;
import com.orbigo.constants.DatabaseKeys;
import com.orbigo.constants.Urls;
import com.orbigo.models.Poi;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MySavedPoisAdapter extends RecyclerView.Adapter<MySavedPoisAdapter.MyViewHolder>{

    private Context context;
    private List<Poi> mySavedPoiList;
    private RequestQueue requestQueue;
    private FirebaseAuth mAuth;
    RequestOptions requestOptions;

    public MySavedPoisAdapter(Context context, List<Poi> mySavedPoiList) {
        this.context = context;
        this.mySavedPoiList = mySavedPoiList;

        requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.ic_launcher_background);
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(5));
        requestQueue = Volley.newRequestQueue(context);
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MySavedPoisAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.my_saved_places,parent,false);
        return new MySavedPoisAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MySavedPoisAdapter.MyViewHolder holder, final int position) {
        final Poi poi = mySavedPoiList.get(position);
        holder.attractionTV.setText(poi.getCategory());
        holder.areaTV.setText(poi.getIs_in_region());
        holder.nameTV.setText(poi.getName());
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFromDb(poi);
            }
        });
        Glide.with(context)
                .load(Uri.parse(poi.getPicture()))
                .apply(requestOptions)
                .into(holder.imageView);
    }

    private void removeFromDb(final Poi poi) {
        JSONObject input = new JSONObject();
        try {
            input.put(DatabaseKeys.USER_ID,mAuth.getCurrentUser().getUid());
            input.put("del_poi",poi.getId());
            String url = Urls.BASE_URL + Urls.REMOVE_SAVED_POI;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, input,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("removeFromDb",response.toString());
                            try {
                                String status = response.getString("status");
                                if(status.equals("success")) {
                                    Toast.makeText(context,response.getString("message"),Toast.LENGTH_SHORT).show();
                                    mySavedPoiList.remove(poi);
                                    notifyDataSetChanged();
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
        return mySavedPoiList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView nameTV,areaTV,attractionTV;
        private ImageButton deleteBtn;
        private ImageView imageView;

        public MyViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.saved_image);
            deleteBtn = itemView.findViewById(R.id.saved_like_btn);
            nameTV = itemView.findViewById(R.id.saved_name);
            areaTV = itemView.findViewById(R.id.saved_area);
            attractionTV = itemView.findViewById(R.id.saved_type);
        }
    }
}