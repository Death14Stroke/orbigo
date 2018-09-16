package com.orbigo.adapters;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.orbigo.R;
import com.orbigo.enums.Orientation;
import com.orbigo.enums.TripStatus;
import com.orbigo.helpers.SwipeAndDragHelper;
import com.orbigo.models.TripPlanItem;

import java.text.DecimalFormat;
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
    RequestOptions requestOptions;
    private DecimalFormat decimalFormat=new DecimalFormat("#.#");

    public TimelineAdapter(Context mContext,List<TripPlanItem> tripPlanItemList, Orientation mOrientation, boolean mWithLinePadding) {
        this.mContext = mContext;
        requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.ic_launcher_background);
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(5));
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
        holder.timeSpentTV.setText(decimalFormat.format(1.0*tripPlanItem.getTotalTime()/(1000*60*60)));
        holder.distanceTV.setText(decimalFormat.format(1.0*tripPlanItem.getTotalDistance()/(1609)));
        holder.placeTV.setText(tripPlanItem.getPlaceName());
        holder.regionTV.setText(tripPlanItem.getRegion());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMMM yyyy, HH:mm");
        holder.startTimeTV.setText(simpleDateFormat.format(tripPlanItem.getArrivalTime()));
        holder.categoryTV.setText(tripPlanItem.getCategory());
        holder.orderTV.setText(String.valueOf(position));
        holder.tv_num.setText(String.valueOf(position+1));

        holder.tvDistance.setText(decimalFormat.format(1.0*tripPlanItem.getDistance()/(1609)));
        holder.tvTime.setText(String.valueOf(tripPlanItem.getTime()/(1000*60)));




        Glide.with(mContext)
                .load(Uri.parse(tripPlanItem.getImageurl()))
                .apply(requestOptions)
                .into(holder.dayImageView);
        if(position==0){
            tripPlanItemList.get(0).setTripStatus(TripStatus.ACTIVE);
        }


        //holder.mTimelineView.setMarker(buildCounterDrawable(holder.getAdapterPosition(), R.drawable.ic_blank_pin));



       /* if(tripPlanItem.getTripStatus() == TripStatus.INACTIVE) {
            holder.mTimelineView.setMarker(VectorDrawableUtils.getDrawable(mContext, R.drawable.ic_marker_inactive, android.R.color.darker_gray));
        } else if(tripPlanItem.getTripStatus() == TripStatus.ACTIVE) {
            holder.mTimelineView.setMarker(VectorDrawableUtils.getDrawable(mContext, R.drawable.ic_marker_active, R.color.colorPrimary));
        } else {
            holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker), ContextCompat.getColor(mContext, R.color.colorPrimary));
        }*/

    }


    public Drawable buildCounterDrawable(int count, int backgroundImageId) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.timelinemarker, null);
        view.setBackgroundResource(backgroundImageId);
        TextView textView = (TextView) view.findViewById(R.id.count);
        textView.setText("" + count);
        /*if (count == 0) {
            View counterTextPanel = view.findViewById(R.id.counterValuePanel);
            counterTextPanel.setVisibility(View.GONE);
        } else {
            TextView textView = (TextView) view.findViewById(R.id.count);
            textView.setText("" + count);
        }*/

        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return new BitmapDrawable(mContext.getResources(), bitmap);
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
        private TextView tv_num;
        private ImageView dayImageView;
        TextView tvTime;
        TextView tvDistance;
        //private TimelineView mTimelineView;

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
            //mTimelineView = itemView.findViewById(R.id.time_marker);
            orderTV = itemView.findViewById(R.id.orderTV);
            tv_num = itemView.findViewById(R.id.tv_num);
            tvDistance = itemView.findViewById(R.id.tv_dist);
            tvTime = itemView.findViewById(R.id.tv_time);
            //mTimelineView.initLine(viewType);
        }
    }

    public interface TripPlanChangedListener{
        void onItemSwapped();
        void onItemRemoved();
    }
}