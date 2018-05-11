package com.example.mastermind.testapp;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import com.txusballesteros.bubbles.BubbleLayout;
import com.txusballesteros.bubbles.BubblesManager;
import com.txusballesteros.bubbles.OnInitializedCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity  {
    SharedPreferences settingsPreferences;
    ArrayList<JobOffer> offers;
    ArrayList<JobOffer> asyncOffers;
    private int MY_PERMISSION = 1000;
    static BubblesManager bubblesManager;
    private NotificationBadge mBadge;
    private int count;

    NotificationCompat.Builder notification;
    private static final int uniqueID = 45612;

    private PendingIntent pendingIntentA;


    ListView lv;
    DateFormat format;
    Button btn_back;
    String message = "";
    RequestQueue queue;
    SwipeRefreshLayout swipeLayout;
    ArrayList<Integer> idArray = new ArrayList<>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Datalabs");
        setSupportActionBar(toolbar);
        lv = findViewById(R.id.listView);
        asyncOffers = new ArrayList<>();
        offers = new ArrayList<>();
        btn_back = findViewById(R.id.btn_back);
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        queue = Volley.newRequestQueue(this);


        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        System.out.println(settingsPreferences.getInt("numberOfCategories", 0) == 0);
        System.out.println(settingsPreferences.getInt("numberOfCheckedCategories", 0) == 0);


        System.out.println(settingsPreferences.getBoolean("checkIsChanged", false));


            for (int i = 0; i < settingsPreferences.getInt("numberOfOffers", 0); i++) {

                JobOffer jobOffer = new JobOffer();
                jobOffer.setId(settingsPreferences.getInt("offerId " + i, 0));
                jobOffer.setCatid(settingsPreferences.getInt("offerCatid " + i, 0));
                jobOffer.setTitle(settingsPreferences.getString("offerTitle " + i, ""));
                jobOffer.setDate(new Date(settingsPreferences.getLong("offerDate " + i, 0)));
                jobOffer.setDownloaded(settingsPreferences.getString("offerDownloaded " + i, ""));
                offers.add(jobOffer);

            }

        System.out.println(offers.toString());
        JobOfferAdapter jobOfferAdapter = new JobOfferAdapter(getApplicationContext(), offers);
        lv.setAdapter(jobOfferAdapter);
        System.out.println(settingsPreferences.getLong("interval",0));


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intentToDetail = new Intent(MainActivity.this,DetailActivity.class);
                intentToDetail.putExtra("jobOffer", (Serializable) adapterView.getItemAtPosition(i));
                startActivity(intentToDetail);

            }
        });

        swipeLayout = findViewById(R.id.swipeContainer);
        // Adding Listener
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code here
                Toast.makeText(getApplicationContext(), "Works!", Toast.LENGTH_LONG).show();
                // To keep animation for 4 seconds
                RefreshOperation();

            }
        });

        scheduleJob();


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void scheduleJob() {
        JobInfo myJob = new JobInfo.Builder(0, new ComponentName(MyApplication.getAppContext(), NetworkSchedulerService.class))

                .setMinimumLatency(1000)
                .setOverrideDeadline(2000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();

        JobScheduler jobScheduler = (JobScheduler) MyApplication.getAppContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(myJob);
    }

    @Override
    protected void onStop() {
        // A service can be "started" and/or "bound". In this case, it's "started" by this Activity
        // and "bound" to the JobScheduler (also called "Scheduled" by the JobScheduler). This call
        // to stopService() won't prevent scheduled jobs to be processed. However, failing
        // to call stopService() would keep it alive indefinitely.
        stopService(new Intent(this, NetworkSchedulerService.class));
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start service and provide it a way to communicate with this class.
        Intent startServiceIntent = new Intent(this, NetworkSchedulerService.class);
        startService(startServiceIntent);
    }

    public void RefreshOperation() {

        for (int v = 0; v < (settingsPreferences.getInt("numberOfCheckedCategories", 0)); v++) {
            if (settingsPreferences.getInt("checkedCategoryId " + v, 0) != 0) {
                System.out.println(settingsPreferences.getInt("checkedCategoryId " + v, 0) + "Before the task show for the first time");
                System.out.println(settingsPreferences.getString("checkedCategoryTitle " + v, ""));
                //new TaskShowOffersFromCategories().execute(String.valueOf(settingsPreferences.getInt("checkedCategoryId " + v, 0)));
                queue.add(volleySetCheckedCategories(String.valueOf(settingsPreferences.getInt("checkedCategoryId " + v, 0))));
            }
        }

        swipeLayout.setRefreshing(false);
    }





    public void btnBackClicked(View view){
        Intent intent = new Intent(MainActivity.this,MainActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            Intent intent = new Intent(MainActivity.this,SettingActivity.class);
            startActivity(intent);
            return true;
        }else{
            RefreshOperation();

        }

        return super.onOptionsItemSelected(item);
    }

    public StringRequest volleySetCheckedCategories(final String param) {
        String url = "http://10.0.2.2/android/jobAds.php?";


        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ArrayList<JobOffer> offersRefresh = new ArrayList<>();
                        JobOfferAdapter jobOfferAdapter= new JobOfferAdapter(getApplicationContext(), offersRefresh);

                        // Display the first 500 characters of the response string.
                        System.out.println("Volley: " + message);
                        try {
                            JSONObject jsonObjectAll = new JSONObject(response);
                            JSONArray jsonArray = jsonObjectAll.getJSONArray("offers");
                            int i = 0;

                            while (i < jsonArray.length() && i < 5) {


                                JSONObject jsonObjectCategory = jsonArray.getJSONObject(i);

                                if(!idArray.contains(Integer.valueOf(jsonObjectCategory.getString("jad_id")))) {
                                    idArray.add(Integer.valueOf(jsonObjectCategory.getString("jad_id")));


                                    JobOffer offer = new JobOffer();
                                    offer.setId(Integer.valueOf(jsonObjectCategory.getString("jad_id")));
                                    offer.setCatid(Integer.valueOf(jsonObjectCategory.getString("jad_catid")));
                                    offer.setTitle(jsonObjectCategory.getString("jad_title"));
                                    offer.setDate(format.parse(jsonObjectCategory.getString("jad_date")));
                                    offer.setDownloaded(jsonObjectCategory.getString("jad_downloaded"));
                                    System.out.println(offer.getTitle() + " first time");

                                    asyncOffers.add(offer);

                                    Collections.sort(asyncOffers, new Comparator<JobOffer>() {
                                        @Override
                                        public int compare(JobOffer jobOffer, JobOffer t1) {
                                            if (jobOffer.getDate().getTime() - t1.getDate().getTime() < 0)
                                                return 1;
                                            else if (jobOffer.getDate().getTime() - t1.getDate().getTime() == 0)
                                                return 0;
                                            else
                                                return -1;
                                        }
                                    });
                                    for (int x = 0; x < asyncOffers.size(); x++) {
                                        System.out.println(asyncOffers.get(x).getTitle());
                                    }
                                }

                                i++;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if(asyncOffers.size()>0) {
                            for (int j = 0; j < 5; j++) {
                                settingsPreferences.edit().remove("offerId " + j).apply();
                                settingsPreferences.edit().remove("offerCatid " + j).apply();
                                settingsPreferences.edit().remove("offerTitle " + j).apply();
                                settingsPreferences.edit().remove("offerDate " + j).apply();
                                settingsPreferences.edit().remove("offerDownloaded " + j).apply();
                            }

                            for (int i = 0; i < asyncOffers.size(); i++) {
                                System.out.println(asyncOffers.get(i).getTitle() + " in the Array that fills settings ");
                            }

                            for (int i = 0; i < asyncOffers.size(); i++) {
                                if (i < 5) {

                                    settingsPreferences.edit().putInt("offerId " + i, asyncOffers.get(i).getId()).apply();
                                    settingsPreferences.edit().putInt("offerCatid " + i, asyncOffers.get(i).getCatid()).apply();
                                    settingsPreferences.edit().putString("offerTitle " + i, asyncOffers.get(i).getTitle()).apply();
                                    settingsPreferences.edit().putLong("offerDate " + i, asyncOffers.get(i).getDate().getTime()).apply();
                                    settingsPreferences.edit().putString("offerDownloaded " + i, asyncOffers.get(i).getDownloaded()).apply();
                                    System.out.println(settingsPreferences.getLong("offerDate " + i, 0));
                                    System.out.println(settingsPreferences.getString("offerTitle " + i, ""));
                                    settingsPreferences.edit().putInt("numberOfOffers", asyncOffers.size()).apply();
                                } else
                                    settingsPreferences.edit().putInt("numberOfOffers", 5).apply();
                            }

                            settingsPreferences.edit().putLong("lastSeenDate", asyncOffers.get(0).getDate().getTime()).apply();
                            settingsPreferences.edit().putLong("lastNotDate", asyncOffers.get(0).getDate().getTime()).apply();

                            System.out.println(settingsPreferences.getLong("lastSeenDate", 0));

                        }




                        for (int i = 0; i < settingsPreferences.getInt("numberOfOffers", 0); i++) {

                            JobOffer jobOffer = new JobOffer();
                            jobOffer.setId(settingsPreferences.getInt("offerId " + i, 0));
                            jobOffer.setCatid(settingsPreferences.getInt("offerCatid " + i, 0));
                            jobOffer.setTitle(settingsPreferences.getString("offerTitle " + i, ""));
                            jobOffer.setDate(new Date(settingsPreferences.getLong("offerDate " + i, 0)));
                            jobOffer.setDownloaded(settingsPreferences.getString("offerDownloaded " + i, ""));
                            offersRefresh.add(jobOffer);

                        }





                        System.out.println(offers.toString());

                        lv.setAdapter(jobOfferAdapter);
                        System.out.println(settingsPreferences.getLong("interval",0));


                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Intent intentToDetail = new Intent(MainActivity.this,DetailActivity.class);
                            intentToDetail.putExtra("jobOffer", (Serializable) adapterView.getItemAtPosition(i));
                            startActivity(intentToDetail);

                            }
                        });


                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    message = "TimeOutError";
                    //This indicates that the reuest has either time out or there is no connection

                } else if (error instanceof AuthFailureError) {
                    message = "AuthFailureError";
                    // Error indicating that there was an Authentication Failure while performing the request

                } else if (error instanceof ServerError) {
                    message = "ServerError";
                    //Indicates that the server responded with a error response

                } else if (error instanceof NetworkError) {
                    message = "NetworkError";
                    //Indicates that there was network error while performing the request

                } else if (error instanceof ParseError) {
                    message = "ParseError";
                    // Indicates that the server response could not be parsed

                }
                System.out.println("Volley: " + message);
                if(!message.equals("")){
                    Toast.makeText(MainActivity.this,"There is some problem with the server ("+message+")",Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(MainActivity.this,MainActivity.class);
                    startActivity(intentError);
                }
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "showOffersFromCategory");
                params.put("jacat_id",param);

                return params;
            }
        };
        return stringRequest;
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);

    }


}
