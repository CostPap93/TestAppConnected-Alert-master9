package com.example.mastermind.testapp;

import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nex3z.notificationbadge.NotificationBadge;
import com.txusballesteros.bubbles.BubblesManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mastermind on 9/5/2018.
 */

public class UnseenActivity  extends AppCompatActivity {
    SharedPreferences settingsPreferences;
    ArrayList<JobOffer> offers;
    ArrayList<JobOffer> asyncOffers;


    ListView lv;
    DateFormat format;
    Button btn_back;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Datalabs");
        lv = findViewById(R.id.listView);
        asyncOffers = new ArrayList<>();
        offers = new ArrayList<>();
        btn_back = findViewById(R.id.btn_back);
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        System.out.println(settingsPreferences.getInt("numberOfCategories", 0) == 0);
        System.out.println(settingsPreferences.getInt("numberOfCheckedCategories", 0) == 0);


        System.out.println(settingsPreferences.getBoolean("checkIsChanged", false));

            for (int i = 0; i < settingsPreferences.getInt("numberOfUnseenOffers", 0); i++) {

                JobOffer jobOffer = new JobOffer();
                jobOffer.setId(settingsPreferences.getInt("offerId " + i, 0));
                jobOffer.setCatid(settingsPreferences.getInt("offerCatid " + i, 0));
                jobOffer.setTitle(settingsPreferences.getString("offerTitle " + i, ""));
                jobOffer.setDate(new Date(settingsPreferences.getLong("offerDate " + i, 0)));
                jobOffer.setDownloaded(settingsPreferences.getString("offerDownloaded " + i, ""));

                if(jobOffer.getDate().getTime()>settingsPreferences.getLong("lastSeenDate",jobOffer.getDate().getTime()))
                    settingsPreferences.edit().putLong("lastSeenDate",jobOffer.getDate().getTime()).apply();

                offers.add(jobOffer);

                btn_back.setVisibility(View.VISIBLE);

            }


        System.out.println(offers.toString());
        JobOfferAdapter jobOfferAdapter = new JobOfferAdapter(getApplicationContext(), offers);
        lv.setAdapter(jobOfferAdapter);
        System.out.println(settingsPreferences.getLong("interval",0));


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intentToDetail = new Intent(UnseenActivity.this,DetailActivity.class);
                intentToDetail.putExtra("jobOffer", (Serializable) adapterView.getItemAtPosition(i));
                startActivity(intentToDetail);

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_unseen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(UnseenActivity.this,SettingActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(UnseenActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
