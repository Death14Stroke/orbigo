package com.ds14.darren.orbigo.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.models.OpeningHoursModel;

import java.util.List;

public class OpeningHoursAdapter extends RecyclerView.Adapter<OpeningHoursAdapter.MyViewHolder> {
    Context mcontext;
    private List<OpeningHoursModel> openingHoursList;

    public OpeningHoursAdapter(Context mcontext, List<OpeningHoursModel> openingHoursList) {
        this.mcontext = mcontext;
        this.openingHoursList = openingHoursList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.opening_hour_cv, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        OpeningHoursModel c = openingHoursList.get(position);
        holder.from.setText(c.getFrom());
        holder.to.setText(c.getTo());
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openingHoursList.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return openingHoursList.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView from,to;
        ImageButton deleteBtn;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.from = itemView.findViewById(R.id.from);
            this.to = itemView.findViewById(R.id.to);
            this.deleteBtn = itemView.findViewById(R.id.deleteTimeBtn);
        }
    }
}
