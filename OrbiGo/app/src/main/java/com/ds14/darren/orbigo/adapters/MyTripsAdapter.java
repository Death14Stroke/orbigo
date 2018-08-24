package com.ds14.darren.orbigo.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.activities.TimelineActivity;
import com.ds14.darren.orbigo.constants.Constants;
import com.ds14.darren.orbigo.models.Trip;

import java.util.List;

public class MyTripsAdapter extends RecyclerView.Adapter<MyTripsAdapter.MyViewHolder> {

    private Context context;
    private List<Trip> myTripsList;

    public MyTripsAdapter(Context context, List<Trip> myTripsList) {
        this.context = context;
        this.myTripsList = myTripsList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.my_trip,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final Trip trip = myTripsList.get(position);
        holder.nameTV.setText(trip.getName());
        holder.dateTV.setText(trip.getCreatedDate());
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.PLANNING_TRIP = trip;
                context.startActivity(new Intent(context, TimelineActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return myTripsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView nameTV,dateTV;
        private ImageButton moreBtn;

        public MyViewHolder(View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.my_trip_name);
            dateTV = itemView.findViewById(R.id.my_trip_created);
            moreBtn = itemView.findViewById(R.id.my_trip_details);
        }
    }
}
