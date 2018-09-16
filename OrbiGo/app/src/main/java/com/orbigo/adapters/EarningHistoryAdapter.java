package com.orbigo.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.orbigo.R;
import com.orbigo.models.EarningHistoryModel;

import java.util.List;

public class EarningHistoryAdapter extends RecyclerView.Adapter<EarningHistoryAdapter.MyViewHolder>{
    Context mcontext;
    private List<EarningHistoryModel> historyList;

    public EarningHistoryAdapter(Context mcontext, List<EarningHistoryModel> historyList) {
        this.mcontext = mcontext;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.earnings_item, parent, false);
        return new MyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final EarningHistoryModel c = historyList.get(position);
        holder.date.setText(c.getDate());
        holder.abn.setText(c.getAbn());
        holder.booking_id.setText(c.getBooking_id());
        holder.amount.setText(c.getAmount());
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView date,abn,booking_id,amount;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.date = itemView.findViewById(R.id.date);
            this.abn = itemView.findViewById(R.id.abn);
            this.booking_id = itemView.findViewById(R.id.booking_id);
            this.amount = itemView.findViewById(R.id.amount);
        }
    }
}