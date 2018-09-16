package com.orbigo.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.orbigo.R;
import com.orbigo.models.MeddsModel;

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
        drawItem(c,holder);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c.setSelected(!c.isSelected());
                meddsModelList.get(position).setSelected(c.isSelected());
                drawItem(c,holder);

            }
        });
    }

    private void drawItem(MeddsModel c,MyViewHolder holder){
        if(c.isSelected()){
            holder.cardView.setCardBackgroundColor(Color.parseColor("#04c4d7"));
            Log.i("MedssAdapter",c.getName()+"");
            switch (c.getName()){
                case "eat":
                    holder.imageView.setImageResource(R.drawable.eatcopywhite);
                    holder.textView.setTextColor(Color.WHITE);
                    break;
                case "do":
                    holder.imageView.setImageResource(R.drawable.coffeecopywhite);
                    holder.textView.setTextColor(Color.WHITE);
                    break;
                case "see":
                    holder.imageView.setImageResource(R.drawable.toseecopywhite);
                    holder.textView.setTextColor(Color.WHITE);
                    break;
                case "sleep":
                    holder.imageView.setImageResource(R.drawable.ic_outlinestar24pxwhite);
                    holder.textView.setTextColor(Color.WHITE);
                    break;
                case "move":
                    holder.imageView.setImageResource(R.drawable.walking_white);
                    holder.textView.setTextColor(Color.WHITE);
                    break;
            }

        }
        else{
            Log.i("MedssAdapter",c.getName()+"--esle");
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            switch (c.getName()){
                case "eat":
                    holder.imageView.setImageResource(R.drawable.eat_fork);
                    holder.textView.setTextColor(Color.GRAY);
                    break;
                case "do":
                    holder.imageView.setImageResource(R.drawable.coffee);
                    holder.textView.setTextColor(Color.GRAY);
                    break;
                case "see":
                    holder.imageView.setImageResource(R.drawable.tosee);
                    holder.textView.setTextColor(Color.GRAY);
                    break;
                case "sleep":
                    holder.imageView.setImageResource(R.drawable.ic_outlinestar24px);
                    holder.textView.setTextColor(Color.GRAY);
                    break;
                case "move":
                    holder.imageView.setImageResource(R.drawable.walking_grey);
                    holder.textView.setTextColor(Color.GRAY);
                    break;
            }
        }
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