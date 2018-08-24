package com.ds14.darren.orbigo.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.models.MeddsModel;

import java.util.List;

public class MedssAdapter extends RecyclerView.Adapter<MedssAdapter.MyViewHolder>{

    private Context mContext;
    private List<MeddsModel> meddsModelList;
    public MedssAdapter(Context mContext, List<MeddsModel> meddsModelList){
        this.mContext=mContext;
        this.meddsModelList = meddsModelList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.modes_cv, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final MeddsModel c = meddsModelList.get(position);
        holder.imageView.setImageResource(c.getImage_id());
        holder.textView.setText(c.getName());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c.setSelected(!c.isSelected());
                meddsModelList.get(position).setSelected(c.isSelected());
                if(c.isSelected()){
                    holder.cardView.setCardBackgroundColor(Color.parseColor("#04c4d7"));
                }
                else{
                    holder.cardView.setCardBackgroundColor(Color.WHITE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return meddsModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;
        private TextView textView;
        private CardView cardView;
        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.card_imageview);
            textView = itemView.findViewById(R.id.card_textview);
            cardView = itemView.findViewById(R.id.cardview);
        }
    }
}