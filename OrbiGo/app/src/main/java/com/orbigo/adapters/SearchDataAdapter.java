package com.orbigo.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.orbigo.R;
import com.orbigo.models.Country;
import com.orbigo.models.Poi;
import com.orbigo.models.Region;
import com.orbigo.models.SearchData;
import com.orbigo.models.State;

import java.util.ArrayList;
import java.util.List;

public class SearchDataAdapter extends BaseAdapter implements Filterable {

    private static final int MAX_RESULTS = 10;
    private Context mContext;
    private List<SearchData> searchDataList = new ArrayList<>(),searchDataList2;

    public SearchDataAdapter(Context mContext, List<SearchData> searchDataList) {
        this.mContext = mContext;
        this.searchDataList2 = searchDataList;
    }

    @Override
    public int getCount() {
        return searchDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return searchDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            convertView = inflater.inflate(R.layout.dropdown_2line, parent, false);
        }
        Log.v("methodCheck","getView length:"+this.searchDataList2.size());
        TextView t1 = convertView.findViewById(R.id.text1);
        TextView t2 = convertView.findViewById(R.id.text2);
        SearchData s = (SearchData) getItem(position);
        t1.setText(s.getName());
        if(s instanceof Country)
            t2.setText(s.getName());
        else if(s instanceof State)
            t2.setText(((State) s).getIs_in_country());
        else if(s instanceof Region)
            t2.setText(((Region) s).getIs_in_state()+", "+((Region) s).getIs_in_country());
        else if(s instanceof Poi)
            t2.setText(((Poi) s).getIs_in_region()+", "+((Poi) s).getIs_in_state()+", "+((Poi) s).getIs_in_country());
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                Log.v("methodCheck","performfiltering");
                FilterResults filterResults = new FilterResults();
                if(constraint!=null){
                    List<SearchData> searchDatas = findSearchResults(mContext, constraint.toString());
                    filterResults.values = searchDatas;
                    filterResults.count = searchDatas.size();
                    Log.v("logauto",""+filterResults.count);
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                Log.v("methodCheck","publishresults");
                if(results.values!=null)
                    searchDataList = (List<SearchData>) results.values;
                else
                    searchDataList = new ArrayList<>();
                notifyDataSetChanged();
            }
        };
    }

    private List<SearchData> findSearchResults(Context mContext, String s) {
        Log.v("methodCheck","findplace size:"+this.searchDataList2.size());
        List<SearchData> q = new ArrayList<>();
        for(int i=0;i<searchDataList2.size();i++){
            if(searchDataList2.get(i).getName().toLowerCase().contains(s.toLowerCase())) {
                q.add(searchDataList2.get(i));
                Log.v("logauto",searchDataList2.get(i).getName());
            }
        }
        return q;
    }
}
