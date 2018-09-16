package com.orbigo.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.orbigo.R;
import com.orbigo.constants.Constants;
import com.orbigo.models.Poi;
import com.orbigo.models.SearchData;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Observer;

import static com.orbigo.constants.Constants.CAMERA_REQUEST;

public class Utils {
    public static void openCamera(final Activity activity) {
        Dexter.withActivity(activity)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        activity.startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(activity, "Permission denied", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    public static void uploadPhoto(Activity activity) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.PICK_IMAGE_REQUEST);
    }

    public static void showPhotoMenuDialog(final Activity activity, final Observer removePhotoObserver) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.change_photo_menu_title);
        builder.setItems(activity.getResources().getStringArray(R.array.photo_menu), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Utils.openCamera(activity);
                        break;
                    case 1:
                        Utils.uploadPhoto(activity);
                        break;
                    case 2:
                        removePhotoObserver.update(null, null);
                        break;
                }
            }
        });
        builder.show();
    }

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_pin_eat);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth() , vectorDrawable.getIntrinsicHeight() );
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static boolean containsIgnoreCase(String str, String searchStr)     {
        if(str == null || searchStr == null) return false;

        final int length = searchStr.length();
        if (length == 0)
            return true;

        for (int i = str.length() - length; i >= 0; i--) {
            if (str.regionMatches(true, i, searchStr, 0, length))
                return true;
        }
        return false;
    }

    public static String nullClearing(String s){
        if (s==null) return "";
        s=s.replace("Null", "");
        s=s.replace("NULL", "");
        s=s.replace("null", "");
        return s;
    }

    private static BitmapDescriptor getBitmapMarker(Context context,String poiStatus) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_pin);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, R.drawable.ic_pin);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);


        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                Constants.getIconId(poiStatus));

        Bitmap scaleIcon = icon.createScaledBitmap(icon, (int) (0.35 * bitmap.getWidth()), (int) (0.35 * bitmap.getHeight()), false);

        int x = (bitmap.getWidth() / 2 - scaleIcon.getWidth() / 2) + 10;
        int y = (bitmap.getHeight() / 2 - scaleIcon.getHeight() / 2) - 10;

        canvas.drawBitmap(scaleIcon, x, y, null);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    private static BitmapDescriptor getBitmapTripMarker(Context context,int number) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_marker_blu);
        int width= (int) (background.getIntrinsicWidth()*1.5);
        int height= (int) (background.getIntrinsicHeight()*1.5);
        background.setBounds(0, 0, width,height);
        Drawable vectorDrawable = ContextCompat.getDrawable(context, R.drawable.ic_marker_blu);
        vectorDrawable.setBounds(0, 0, width, height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(30);
        int xPos = (canvas.getWidth() / 2);
        paint.setTextAlign(Paint.Align.CENTER);
        //int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)) ;

        canvas.drawText(String.valueOf(number),xPos,40,paint);


        //Bitmap scaleIcon = icon.createScaledBitmap(icon, (int) (0.35 * bitmap.getWidth()), (int) (0.35 * bitmap.getHeight()), false);

//        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
//                Constants.getIconId(poiStatus));
//
//        Bitmap scaleIcon = icon.createScaledBitmap(icon, (int) (0.35 * bitmap.getWidth()), (int) (0.35 * bitmap.getHeight()), false);
//
//        int x = (bitmap.getWidth() / 2 - scaleIcon.getWidth() / 2) + 10;
//        int y = (bitmap.getHeight() / 2 - scaleIcon.getHeight() / 2) - 10;
//
//        canvas.drawBitmap(scaleIcon, x, y, null);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static Marker addMarker(Context context,GoogleMap mMap,SearchData sdata){
        Marker marker=mMap.addMarker(new MarkerOptions()
                .title(sdata.getName())
                .position(((Poi) sdata).getLocation())
                .icon(getBitmapMarker(context,((Poi) sdata).getCategory())));
        marker.setTag(sdata);
        return marker;

    }


    public static Marker addTripMarker(Context context,GoogleMap mMap, LatLng latLng,String title,int number){
        Marker marker=mMap.addMarker(new MarkerOptions()
                .title(title)
                .position(latLng)
                .icon(getBitmapTripMarker(context,number)
                ));
        //marker.setTag(sdata);
        return marker;

    }

    public static LatLngBounds toBounds(LatLng center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }
}
