package com.ds14.darren.orbigo.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.models.MyFriend;
import com.google.android.gms.common.util.Base64Utils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.MyViewHolder> {

    private Context context;
    private List<MyFriend> myFriendList;

    public FriendsAdapter(Context context, List<MyFriend> myFriendList) {
        this.context = context;
        this.myFriendList = myFriendList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friends_detail,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MyFriend myFriend = myFriendList.get(position);
        holder.nameTV.setText(myFriend.getName());
        holder.detailTV.setText(myFriend.getDetail());
        byte[] decoded = Base64Utils.decode(myFriend.getImage());
        Glide.with(context)
                .load(decoded)
                .into(holder.circleImageView);
    }

    @Override
    public int getItemCount() {
        return myFriendList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView nameTV,detailTV;
        private CircleImageView circleImageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.friend_name);
            detailTV = itemView.findViewById(R.id.friend_detail);
            circleImageView = itemView.findViewById(R.id.friend_image);
        }
    }
}
