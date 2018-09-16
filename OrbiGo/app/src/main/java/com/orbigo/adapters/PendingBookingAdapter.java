package com.orbigo.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.orbigo.R;
import com.orbigo.constants.DatabaseKeys;
import com.orbigo.constants.Urls;
import com.orbigo.models.PendingBooking;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class PendingBookingAdapter extends RecyclerView.Adapter<PendingBookingAdapter.PBViewHolder> {

    private List<PendingBooking> pendingBookingList;
    private Context mContext;
    private RequestQueue requestQueue;

    public PendingBookingAdapter(Context mContext,List<PendingBooking> pendingBookingList) {
        this.pendingBookingList = pendingBookingList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public PBViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.booking_item,parent,false);
        requestQueue = Volley.newRequestQueue(mContext);
        return new PBViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final PBViewHolder holder, int position) {
        final PendingBooking pb = pendingBookingList.get(position);
        holder.confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = Urls.BASE_URL + Urls.CONFIRM_BOOKING;
                JSONObject object = new JSONObject();
                try {
                    object.put(DatabaseKeys.BOOKING_ID,pb.getBooking_id());
                    Log.v("confirmbooking",object.toString());
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.v("confirmbooking",response.toString());
                                    try {
                                        if(response.getString("status").compareTo("success")==0){
                                            Toast.makeText(mContext,"Confirmed",Toast.LENGTH_SHORT).show();
                                            holder.detailsLL.setVisibility(View.GONE);
                                            holder.moreBtn.setImageResource(R.drawable.ic_right);
                                            pendingBookingList.remove(pb);
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
                    requestQueue.add(jsonObjectRequest);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.detailsLL.getVisibility()==View.GONE){
                    holder.detailsLL.setVisibility(View.VISIBLE);
                    holder.moreBtn.setImageResource(R.drawable.ic_down);
                }
                else if(holder.detailsLL.getVisibility()==View.VISIBLE){
                    holder.detailsLL.setVisibility(View.GONE);
                    holder.moreBtn.setImageResource(R.drawable.ic_right);
                }
            }
        });
        holder.childrenTV.setText(pb.getNo_of_children());
        holder.adultTV.setText(pb.getNo_of_adult());
        holder.customerNameTV.setText(pb.getCustomer_name());
        holder.askPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,"ask number",Toast.LENGTH_SHORT).show();
            }
        });
        String[] s = pb.getArrival().split(",");
        holder.dateTV.setText(s[0]);
        if(s.length==2)
            holder.timeTV.setText(s[1]);
    }

    @Override
    public int getItemCount() {
        return pendingBookingList.size();
    }

    public class PBViewHolder extends RecyclerView.ViewHolder{

        TextView customerNameTV,adultTV,childrenTV,dateTV,timeTV;
        Button confirmBtn,askPhoneBtn;
        LinearLayout detailsLL;
        ImageButton moreBtn;

        public PBViewHolder(View itemView) {
            super(itemView);
            customerNameTV = itemView.findViewById(R.id.name_of_businessman);
            askPhoneBtn = itemView.findViewById(R.id.askPhoneBtn);
            adultTV = itemView.findViewById(R.id.adult_number);
            childrenTV = itemView.findViewById(R.id.children_number);
            detailsLL = itemView.findViewById(R.id.booking_details_ll);
            dateTV = itemView.findViewById(R.id.date);
            timeTV = itemView.findViewById(R.id.time);
            confirmBtn = itemView.findViewById(R.id.confirm);
            moreBtn = itemView.findViewById(R.id.moreDetailsBooking);
        }
    }
}
