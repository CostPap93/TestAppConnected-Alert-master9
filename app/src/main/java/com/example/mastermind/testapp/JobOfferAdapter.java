package com.example.mastermind.testapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by mastermind on 19/4/2018.
 */

public class JobOfferAdapter extends BaseAdapter{
    Context context;
    ArrayList<JobOffer> offers = new ArrayList<>();
    SharedPreferences settingsPreferences;



    public JobOfferAdapter(Context context, ArrayList<JobOffer> offers) {
        this.context = context;
        this.offers = offers;
        this.settingsPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void clear(){
        offers.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return offers.size();
    }

    @Override
    public Object getItem(int i) {
        return offers.get(i);
    }

    @Override
    public long getItemId(int i) {
        return offers.indexOf(getItem(i));
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.joboffer_list_item, null);
        }
        TextView txt_offertitle = view.findViewById(R.id.txtv_offertitle);
        TextView txt_offercategory = view.findViewById(R.id.txtv_offercategory);
        TextView txt_offerarea = view.findViewById(R.id.txtv_offerarea);


        txt_offertitle.setText(offers.get(i).getTitle());
        txt_offercategory.setText(offers.get(i).getCattitle());
        txt_offerarea.setText(offers.get(i).getAreatitle());



        return view;
    }


}
