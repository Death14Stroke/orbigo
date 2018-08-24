package com.ds14.darren.orbigo.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.helpers.SwipeAndDragHelper;
import com.ds14.darren.orbigo.models.TripMember;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TripMemberAdapter extends RecyclerView.Adapter<TripMemberAdapter.MyViewHolder> implements SwipeAndDragHelper.ActionCompletionContract{

    private Context context;
    private List<TripMember> tripMemberList;
    private ItemTouchHelper touchHelper;
    private MemberRemovedListener mListener;

    public TripMemberAdapter(Context context, List<TripMember> tripMemberList) {
        this.context = context;
        this.tripMemberList = tripMemberList;
        this.mListener = (MemberRemovedListener)context;
    }

    @NonNull
    @Override
    public TripMemberAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.trip_member,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripMemberAdapter.MyViewHolder holder, int position) {
        TripMember tripMember = tripMemberList.get(position);
        holder.nameTV.setText(tripMember.getName());
        holder.roleTV.setText(tripMember.getRole());
     //   byte[] decodedImage = Base64.decode(tripMember.getEncodedImage(),Base64.DEFAULT);
        Glide.with(context)
       //         .load(decodedImage)
                .load(Uri.parse(tripMember.getEncodedImage()))
                .into(holder.imageView);
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {
        this.touchHelper = touchHelper;
    }

    @Override
    public int getItemCount() {
        return tripMemberList.size();
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {

    }

    @Override
    public void onViewSwiped(int position) {
        mListener.onMemberRemoved(tripMemberList.get(position));
        tripMemberList.remove(position);
        notifyItemRemoved(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private TextView nameTV,roleTV;
        private CircleImageView imageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.member_name);
            roleTV = itemView.findViewById(R.id.member_role);
            imageView = itemView.findViewById(R.id.member_image);
        }
    }

    public interface MemberRemovedListener{
        void onMemberRemoved(TripMember member);
    }
}
