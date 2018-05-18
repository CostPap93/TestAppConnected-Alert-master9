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

import java.util.ArrayList;

/**
 * Created by mastermind on 18/5/2018.
 */

public class CheckBoxAreaAdapter extends BaseAdapter {
    Context context;
    ArrayList<OfferArea> categories = new ArrayList<>();
    SharedPreferences settingsPreferences;
    CheckBox checkBox;


    public CheckBoxAreaAdapter(Context context, ArrayList<OfferArea> categories) {
        this.context = context;
        this.categories = categories;
        this.settingsPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void clear(){
        categories.clear();
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int i) {
        return categories.get(i);
    }

    @Override
    public long getItemId(int i) {
        return categories.indexOf(getItem(i));
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        System.out.println("getView " + i + " " + view);
        final OfferArea offerArea = categories.get(i);
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.checkbox_list_item, null);

            checkBox = view.findViewById(R.id.chbox_category);

            checkBox.setText(offerArea.getTitle());
            for (int j = 0; j < settingsPreferences.getInt("numberOfCheckedAreas",0); j++) {
                System.out.println(checkBox.getText() + "In the checkboxadapter");
                System.out.println(settingsPreferences.getString("checkedAreaTitle " + j, ""));
                if (checkBox.getText().equals(settingsPreferences.getString("checkedAreaTitle " + j, ""))) {
                    checkBox.setChecked(true);
                }
            }

        }


        return view;
    }
}
