package com.ds14.darren.orbigo.constants;

import android.location.Location;

import com.ds14.darren.orbigo.models.Business;
import com.ds14.darren.orbigo.models.NearbyPoi;
import com.ds14.darren.orbigo.models.SearchData;
import com.ds14.darren.orbigo.models.Trip;

import java.util.ArrayList;
import java.util.List;

public class Constants {

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000*60*10;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 1000*60*5;
    public static final int REQUEST_CHECK_SETTINGS = 100;

    public static final String LOGIN_TITLE = "Login";
    public static final String SIGNUP_TITLE = "Sign up";

    public static final String EMAIL_ERROR = "Invalid Email format";
    public static final String PASSWORD_ERROR = "Password must contain at least 6 characters";
    public static final String PHONE_ERROR = "Invalid phone number";
    public static final String RADIOGROUP_ERROR = "Please specify if you wish to travel or trade";

    public static final String MODE_LOCAL = "local";
    public static final String MODE_COUNTRY = "country";
    public static final String MODE_WORLD = "world";
    public static final int LOCATION_PERMISSION_CODE = 4;
    public static final int PICK_CONTACT = 13;
    public static final int SEARCH_THRESHOLD = 3;
    public static final String LOGIN_TYPE = "login_type";
    public static final String LOGIN_SP = "login_type_sp";
    public static final String TVT = "tvt";
    public static final String BTO = "bto";
    public static final int MY_CAMERA_PERMISSION_CODE = 99;
    public static final int CAMERA_REQUEST = 56;
    public static final String API_KEY = "AIzaSyCqEAC29qr3So5VTo7Gwzy2oPktyk4Bcw0";
    public static final String ADD_TO_TRIP_DIALOG = "addToTripDialog";
    public static final String SELECT_TRIP_DIALOG = "selectTripDialog";
    public static final String SELECTED_PLACE = "selected_place_parcel";
    public static final int PLACE_PICKER_REQUEST = 56;
    public static final String INVITE_TEXT = "Go to this link : https://stackoverflow.com/questions/8638475/intent-action-send-whatsapp";
    public static Trip PLANNING_TRIP;
    public static String CURRENT_FRAGMENT;

    public static String FB_EMAIL = "email";
    public static String FB_PUBLIC_PROFILE = "public_profile";
    public static String FB_GENDER = "user_gender";
    public static String FB_FRIENDS = "user_friends";

    public static String TAG_REQUEST_QUEUE = "volley";

    public static int PICK_IMAGE_REQUEST = 10;

    public static List<Business> BUSINESS_LIST = new ArrayList<>();
    public static Business SELECTED_BUSINESS;
    public static List<String> CATEGORIES_LIST = new ArrayList<>();
    public static Location MY_LOCATION;

    public static final String MODE_CAR = "car";
    public static final String MODE_PLANE = "plane";
    public static final String MODE_TRAIN = "train";

    public static final int DISTANCE_RANGE_KM = 30;
    public static final int TIME_RANGE_MIN = 90;
    public static final String MODE_DISTANCE = "distance";
    public static final String MODE_TIME = "time";

    public static List<SearchData> WORLD_DATA = new ArrayList<>();
    public static List<SearchData> COUNTRY_DATA = new ArrayList<>();
    public static SearchData SELECTED_DATA;
    public static NearbyPoi SELECTED_NEARBY_POI;
    public static List<Trip> MY_TRIP_LIST = new ArrayList<>();
    public static String FROM = "";
    public static String TO = "";
}