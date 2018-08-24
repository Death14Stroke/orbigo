package com.ds14.darren.orbigo.adapters;


import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.activities.TimelineActivity;
import com.ds14.darren.orbigo.helpers.SwipeAndDragHelper;
import com.ds14.darren.orbigo.enums.Orientation;
import com.ds14.darren.orbigo.models.TripPlanItem;
import com.ds14.darren.orbigo.enums.TripStatus;
import com.ds14.darren.orbigo.helpers.VectorDrawableUtils;
import com.github.vipulasri.timelineview.TimelineView;

import java.text.SimpleDateFormat;
import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> implements SwipeAndDragHelper.ActionCompletionContract{

    private List<TripPlanItem> tripPlanItemList;
    private Context mContext;
    private Orientation mOrientation;
    private boolean mWithLinePadding;
    private LayoutInflater mLayoutInflater;
    private ItemTouchHelper touchHelper;
    private TripPlanChangedListener mListener;

    public TimelineAdapter(Context mContext,List<TripPlanItem> tripPlanItemList, Orientation mOrientation, boolean mWithLinePadding) {
        this.mContext = mContext;
        this.tripPlanItemList = tripPlanItemList;
        this.mOrientation = mOrientation;
        this.mWithLinePadding = mWithLinePadding;
        this.mListener = (TripPlanChangedListener) mContext;
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {
        this.touchHelper = touchHelper;
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mLayoutInflater = LayoutInflater.from(mContext);
        View view = mLayoutInflater.inflate(R.layout.item_timeline, parent, false);
        return new TimelineViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        TripPlanItem tripPlanItem = tripPlanItemList.get(position);
        Log.v("timelineadapter","position: "+position+ "item.getPlace: "+ tripPlanItem.getPlaceName()+" arrival: "+tripPlanItem.getArrivalTime());
        holder.costTV.setText(tripPlanItem.getCost());
        holder.timeSpentTV.setText(tripPlanItem.getTimeSpent());
        holder.distanceTV.setText(tripPlanItem.getDistance());
        holder.placeTV.setText(tripPlanItem.getPlaceName());
        holder.regionTV.setText(tripPlanItem.getRegion());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMMM yyyy, HH:mm");
        holder.startTimeTV.setText(simpleDateFormat.format(tripPlanItem.getArrivalTime()));
        holder.categoryTV.setText(tripPlanItem.getCategory());
        holder.orderTV.setText(String.valueOf(position));
        Glide.with(mContext)
                .load(Uri.parse(tripPlanItem.getImageurl()))
                .into(holder.dayImageView);
        if(position==0){
            tripPlanItemList.get(0).setTripStatus(TripStatus.ACTIVE);
        }
        if(tripPlanItem.getTripStatus() == TripStatus.INACTIVE) {
            holder.mTimelineView.setMarker(VectorDrawableUtils.getDrawable(mContext, R.drawable.ic_marker_inactive, android.R.color.darker_gray));
        } else if(tripPlanItem.getTripStatus() == TripStatus.ACTIVE) {
            holder.mTimelineView.setMarker(VectorDrawableUtils.getDrawable(mContext, R.drawable.ic_marker_active, R.color.colorPrimary));
        } else {
            holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker), ContextCompat.getColor(mContext, R.color.colorPrimary));
        }

    }

    @Override
    public int getItemCount() {
        return tripPlanItemList.size();
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        TripPlanItem tripPlanItem = tripPlanItemList.get(oldPosition);
        tripPlanItemList.remove(oldPosition);
        tripPlanItemList.add(newPosition,tripPlanItem);
        if(oldPosition!=0)
            tripPlanItem.setTripStatus(TripStatus.INACTIVE);
        notifyItemMoved(oldPosition,newPosition);
        mListener.onItemSwapped();
    }

    @Override
    public void onViewSwiped(int position) {
        tripPlanItemList.remove(position);
        notifyItemRemoved(position);
        mListener.onItemRemoved();
    }

    public class TimelineViewHolder extends RecyclerView.ViewHolder{

        private TextView startTimeTV, placeTV, regionTV, distanceTV, timeSpentTV, costTV, categoryTV, orderTV;
        private ImageView dayImageView;
        private TimelineView mTimelineView;

        public TimelineViewHolder(View itemView, int viewType) {
            super(itemView);
            dayImageView = itemView.findViewById(R.id.day_image);
            startTimeTV = itemView.findViewById(R.id.day_time);
            placeTV = itemView.findViewById(R.id.day_place_name);
            regionTV = itemView.findViewById(R.id.day_place_region);
            distanceTV = itemView.findViewById(R.id.day_miles);
            timeSpentTV = itemView.findViewById(R.id.day_hours);
            costTV = itemView.findViewById(R.id.day_per_person);
            categoryTV = itemView.findViewById(R.id.day_cat_type);
            mTimelineView = itemView.findViewById(R.id.time_marker);
            orderTV = itemView.findViewById(R.id.orderTV);
            mTimelineView.initLine(viewType);
        }
    }

    public interface TripPlanChangedListener{
        void onItemSwapped();
        void onItemRemoved();
    }
}