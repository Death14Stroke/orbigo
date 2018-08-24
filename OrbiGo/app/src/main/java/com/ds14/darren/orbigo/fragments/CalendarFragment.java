package com.ds14.darren.orbigo.fragments;

import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ds14.darren.orbigo.R;
import com.ds14.darren.orbigo.adapters.MySpinnerAdapter;
import com.ds14.darren.orbigo.adapters.OpeningHoursAdapter;
import com.ds14.darren.orbigo.constants.Constants;
import com.ds14.darren.orbigo.constants.DatabaseKeys;
import com.ds14.darren.orbigo.constants.Urls;
import com.ds14.darren.orbigo.custom_ui_components.TimePickerFragment;
import com.ds14.darren.orbigo.models.Business;
import com.ds14.darren.orbigo.models.OpeningHoursModel;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CalendarFragment extends Fragment {
    private Button fromBtn,toBtn,addWHBtn,saveChangesBtn;
    private Button[] dayButtons = new Button[7];
    private Spinner spinner;
    private List<Business> businessList = new ArrayList<>();
    private MySpinnerAdapter mySpinnerAdapter;
    private OpeningHoursAdapter openingHoursAdapter;
    private List<OpeningHoursModel> openingHoursModels = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayout linearLayout;
    private TextView showFrom,showTo;
    private boolean[] workDays = {false,false,false,false,false,false,false};
    private Business selected;
    private RequestQueue requestQueue;
    private FirebaseAuth mAuth;
    private SimpleDateFormat timeFormat;

    public CalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        requestQueue = Volley.newRequestQueue(getContext());
        mAuth = FirebaseAuth.getInstance();
        setId(view);
        setListeners();
        businessList = Constants.BUSINESS_LIST;
        timeFormat = new SimpleDateFormat("HH:mm");
        Bundle args = getArguments();
        if(args!=null){
            long time = args.getLong("time");
            String tag = args.getString("tag");
            switch (tag){
                case "from":
                    Constants.FROM = timeFormat.format(time);
                    break;
                case "to":
                    Constants.TO = timeFormat.format(time);
                    break;
            }
        }
        setSpinner();
        recyclerView.setHasFixedSize(true);
        openingHoursAdapter = new OpeningHoursAdapter(getContext(),openingHoursModels);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(openingHoursAdapter);
        showFrom.setText(Constants.FROM);
        showTo.setText(Constants.TO);
        return view;
    }

    public void setId(View view){
        spinner = view.findViewById(R.id.calendar_spinner);
        int[] dayIds = {R.id.Sun,R.id.Mon,R.id.Tue,R.id.Wed,R.id.Thu,R.id.Fri,R.id.Sat};
        for(int i=0;i<7;i++){
            dayButtons[i] = view.findViewById(dayIds[i]);
            dayButtons[i].setBackgroundColor(Color.parseColor("#C0D9D9"));
        }
        fromBtn = view.findViewById(R.id.FromBtn);
        toBtn = view.findViewById(R.id.ToBtn);
        addWHBtn = view.findViewById(R.id.addWHBtn);
        showFrom = view.findViewById(R.id.showFromTime);
        showTo = view.findViewById(R.id.showToTime);
        linearLayout = view.findViewById(R.id.calendar_parent);
        recyclerView = view.findViewById(R.id.recycler_view);
        saveChangesBtn = view.findViewById(R.id.saveChangesBtn);
    }

    private void setSpinner() {
        mySpinnerAdapter = new MySpinnerAdapter(getContext(),R.layout.custom_spinner,businessList);
        spinner.setAdapter(mySpinnerAdapter);
        if(businessList.isEmpty())
            linearLayout.setVisibility(View.GONE);
        else
            linearLayout.setVisibility(View.VISIBLE);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected = businessList.get(position);
                openingHoursModels.clear();
                for(int i=0;i<7;i++)
                    dayButtons[i].setBackgroundColor(Color.parseColor("#C0D9D9"));
                if(selected.getOpen_hours()!=null){
                    String[] hours = selected.getOpen_hours().split(",");
                    for (String hour : hours) {
                        OpeningHoursModel o = new OpeningHoursModel();
                        o.setFrom(hour.split("-")[0]);
                        o.setTo(hour.split("-")[1]);
                        openingHoursModels.add(o);
                    }
                }
                openingHoursAdapter.notifyDataSetChanged();
                if(selected.getOpen_days()!=null){
                    String days = selected.getOpen_days();
                    for(int i=0;i<7;i++){
                        dayButtons[i].setBackgroundColor(Color.parseColor("#C0D9D9"));
                    }
                    for(int i=0;i<7;i++){
                        if(days.charAt(i)=='1'){
                            workDays[i]=true;
                            dayButtons[i].setBackgroundColor(Color.parseColor("#00EEEE"));
                        }
                        else if(days.charAt(i)=='0'){
                            workDays[i]=false;
                            dayButtons[i].setBackgroundColor(Color.parseColor("#C0D9D9"));
                        }
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void setListeners(){
        for(int i=0;i<dayButtons.length;i++){
            final int finalI = i;
            dayButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    workDays[finalI]=!workDays[finalI];
                    if(workDays[finalI])
                        dayButtons[finalI].setBackgroundColor(Color.parseColor("#00EEEE"));
                    else
                        dayButtons[finalI].setBackgroundColor(Color.parseColor("#C0D9D9"));
                }
            });
        }
        addWHBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String from = showFrom.getText().toString();
                String to = showTo.getText().toString();
                if(from.isEmpty() || to.isEmpty()) {
                    Toast.makeText(getContext(), "Select proper timings", Toast.LENGTH_SHORT).show();
                    return;
                }
                OpeningHoursModel o = new OpeningHoursModel(from,to);
                openingHoursModels.add(o);
                openingHoursAdapter.notifyDataSetChanged();
            }
        });
        fromBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(v,"from");
            }
        });
        toBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(v,"to");
            }
        });
        saveChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder stringBuilder= new StringBuilder();
                for(int i=0;i<7;i++){
                    if(workDays[i])
                        stringBuilder.append("1");
                    else
                        stringBuilder.append("0");
                }
                String workDaystr = stringBuilder.toString();
                Log.v("daysdata",workDaystr);
                stringBuilder = new StringBuilder();
                for(int i=0;i<openingHoursModels.size();i++){
                    OpeningHoursModel o = openingHoursModels.get(i);
                    stringBuilder.append(o.getFrom());
                    stringBuilder.append("-");
                    stringBuilder.append(o.getTo());
                    if(i<openingHoursModels.size()-1)
                        stringBuilder.append(",");
                }
                String workHoursStr = stringBuilder.toString();
                Log.v("daysdata",workHoursStr);
                if(workHoursStr.isEmpty())
                    workHoursStr=null;
                int index = Constants.BUSINESS_LIST.indexOf(selected);
                if(index>-1){
                    Constants.BUSINESS_LIST.get(index).setOpen_hours(workHoursStr);
                    Constants.BUSINESS_LIST.get(index).setOpen_days(workDaystr);
                }
                JSONObject input = new JSONObject();
                try {
                    input.put(DatabaseKeys.AUTHORIZED_BUSINESS_NUMBER,selected.getAbn());
                    input.put(DatabaseKeys.OPENING_DAYS,workDaystr);
                    input.put(DatabaseKeys.OPENING_HOURS,workHoursStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String url = Urls.BASE_URL + Urls.UPDATE_BUSINESS_CALENDAR;
                JsonObjectRequest updateReq = new JsonObjectRequest(Request.Method.POST, url, input,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String status = response.getString("status");
                                    if(status.equals("success"))
                                        Toast.makeText(getContext(),"Updated",Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });
                requestQueue.add(updateReq);
            }
        });
    }
    public void showTimePickerDialog(View v,String s) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getActivity().getFragmentManager(), s);
    }
}