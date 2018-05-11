package com.example.mastermind.testapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by mastermind on 2/5/2018.
 */

public class SplashActivity extends AppCompatActivity {
    private int MY_PERMISSION = 1000;
    String message = "";
    RequestQueue queue;

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    ArrayList<JobOffer> asyncOffers = new ArrayList<>();
    ArrayList<Integer> idArray = new ArrayList<>();
    SharedPreferences settingsPreferences;
    PendingIntent pendingIntentA;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        queue = Volley.newRequestQueue(this);



        if(Build.VERSION.SDK_INT>=23){
            if(!Settings.canDrawOverlays(SplashActivity.this)){
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));
                startActivityForResult(intent,MY_PERMISSION);
            }
        }
        else{
            Intent intent = new Intent(SplashActivity.this, Service.class);
            startService(intent);
        }


        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        System.out.println(settingsPreferences.getInt("numberOfCategories", 0) == 0);
        System.out.println(settingsPreferences.getInt("numberOfCheckedCategories", 0) == 0);



        System.out.println(settingsPreferences.getBoolean("checkIsChanged", false));

        if (settingsPreferences.getInt("numberOfCategories", 0) == 0 && isConn()) {
            settingsPreferences.edit().putLong("interval", 1000).apply();

            queue.add(volleySetDefault());

            /*try {
                new TaskSetDefaultCateogries().execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            System.out.println(settingsPreferences.getInt("numberOfCheckedCategories",0));

            for (int v = 0; v < (settingsPreferences.getInt("numberOfCheckedCategories", 0)); v++) {
                if (settingsPreferences.getInt("checkedCategoryId " + v, 0) != 0) {
                    System.out.println(settingsPreferences.getInt("checkedCategoryId " + v, 0) + "Before the task show for the first time");
                    System.out.println(settingsPreferences.getString("checkedCategoryTitle " + v, ""));
                    //new TaskShowOffersFromCategories().execute(String.valueOf(settingsPreferences.getInt("checkedCategoryId " + v, 0)));
                    queue.add(volleySetCheckedCategories(String.valueOf(settingsPreferences.getInt("checkedCategoryId " + v, 0))));
                }
            }*/
        } else if (settingsPreferences.getInt("numberOfCategories", 0) == 0 && !isConn()) {
            Toast.makeText(this, "You Have To Be Connected To The Internet The First Time", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
        }


    }


    public boolean isConn(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = networkInfo.isConnected();
        networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = networkInfo.isConnected();
        Log.d("connection", "Wifi connected: " + isWifiConn);
        Log.d("connection", "Mobile connected: " + isMobileConn);
        return isWifiConn || isMobileConn;
    }

    public void start() {

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(SplashActivity.this, AlarmReceiver.class);
        pendingIntentA = PendingIntent.getBroadcast(SplashActivity.this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), settingsPreferences.getLong("interval",0), pendingIntentA);

        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }

   /* public class TaskShowOffersFromCategories extends AsyncTask<String,Integer,ArrayList<JobOffer>> {
        SharedPreferences settingsPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        protected void onPreExecute() {



            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(ArrayList<JobOffer> fiveOffers) {

            for (int j = 0; j < 5; j++) {
                settingsPreferences.edit().remove("offerId " + j).apply();
                settingsPreferences.edit().remove("offerCatid " + j).apply();
                settingsPreferences.edit().remove("offerTitle " + j).apply();
                settingsPreferences.edit().remove("offerDate " + j).apply();
                settingsPreferences.edit().remove("offerDownloaded " + j).apply();
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

            System.out.println(settingsPreferences.getLong("lastSeenDate", 0));

            start();

            Intent intent = new Intent(MyApplication.getAppContext(), MainActivity.class);
            MyApplication.getAppContext().startActivity(intent);

        }

        @Override
        protected ArrayList<JobOffer> doInBackground(String... params) {

            Map<String, String> postParam = new HashMap<>();
            postParam.put("action", "showOffersFromCategory");
            postParam.put("jacat_id",params[0]);

            try {
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST("http://10.0.2.2/android/jobAds.php?", postParam);
                JSONObject jsonObjectAll = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObjectAll.getJSONArray("offers");
                int i = 0;

                while(i<jsonArray.length() && i<5) {

                    JSONObject jsonObjectCategory = jsonArray.getJSONObject(i);

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
                            if(jobOffer.getDate().getTime()-t1.getDate().getTime()<0)
                                return 1;
                            else if(jobOffer.getDate().getTime()-t1.getDate().getTime()==0)
                                return 0;
                            else
                                return -1;
                        }
                    });
                    for(int x=0;x<asyncOffers.size();x++) {
                        System.out.println(asyncOffers.get(x).getTitle());
                    }

                    i++;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return asyncOffers;
        }

    }*/

    public StringRequest volleySetDefault(){
        String url ="http://10.0.2.2/android/jobOfferCategories.php?";

    // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObjectAll = new JSONObject(response);

                            JSONArray jsonArray = jsonObjectAll.getJSONArray("joboffercategories");
                            System.out.println(jsonArray.length());
                            settingsPreferences.edit().putInt("numberOfCategories", jsonArray.length()).apply();
                            settingsPreferences.edit().putInt("numberOfCheckedCategories", jsonArray.length()).apply();
                            System.out.println(settingsPreferences.getInt("numberOfCategories", 0));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObjectCategory = jsonArray.getJSONObject(i);
                                settingsPreferences.edit().putInt("offerCategoryId " + i, Integer.valueOf(jsonObjectCategory.getString("jacat_id"))).apply();
                                settingsPreferences.edit().putInt("checkedCategoryId " + i, Integer.valueOf(jsonObjectCategory.getString("jacat_id"))).apply();
                                settingsPreferences.edit().putString("offerCategoryTitle " + i, jsonObjectCategory.getString("jacat_title")).apply();
                                settingsPreferences.edit().putString("checkedCategoryTitle " + i, jsonObjectCategory.getString("jacat_title")).apply();
                                System.out.println(jsonObjectCategory.toString());
                                System.out.println(settingsPreferences.getInt("checkedCategoryId " + i, 0) + "In The Task set Default");
                                System.out.println(settingsPreferences.getString("checkedCategoryTitle " + i, ""));

                                System.out.println(settingsPreferences.getInt("numberOfCheckedCategories",0));

                                for (int v = 0; v < (settingsPreferences.getInt("numberOfCheckedCategories", 0)); v++) {
                                    if (settingsPreferences.getInt("checkedCategoryId " + v, 0) != 0) {
                                        System.out.println(settingsPreferences.getInt("checkedCategoryId " + v, 0) + "Before the task show for the first time");
                                        System.out.println(settingsPreferences.getString("checkedCategoryTitle " + v, ""));
                                        //new TaskShowOffersFromCategories().execute(String.valueOf(settingsPreferences.getInt("checkedCategoryId " + v, 0)));
                                        queue.add(volleySetCheckedCategories(String.valueOf(settingsPreferences.getInt("checkedCategoryId " + v, 0))));
                                    }
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse (VolleyError error){

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
                System.out.println("Volley: "+ message);
                if(!message.equals("")){
                    Toast.makeText(SplashActivity.this,"There is some problem with the server ("+message+")",Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(SplashActivity.this,MainActivity.class);
                    startActivity(intentError);
                }
            }
        }
        ){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                params.put("action", "show");

                return params;
            }
        };
        return stringRequest;
    }

    public StringRequest volleySetCheckedCategories(final String param) {
        String url = "http://10.0.2.2/android/jobAds.php?";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Display the first 500 characters of the response string.
                        message = "Response is: " + response;
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

                        for (int j = 0; j < 5; j++) {
                            settingsPreferences.edit().remove("offerId " + j).apply();
                            settingsPreferences.edit().remove("offerCatid " + j).apply();
                            settingsPreferences.edit().remove("offerTitle " + j).apply();
                            settingsPreferences.edit().remove("offerDate " + j).apply();
                            settingsPreferences.edit().remove("offerDownloaded " + j).apply();
                        }

                        for (int i = 0; i < asyncOffers.size(); i++) {
                            System.out.println(asyncOffers.get(i).getTitle()+" in the Array that fills settings ");
                        }

                        if(asyncOffers.size()>0) {
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

                        start();



                        Intent intent = new Intent(MyApplication.getAppContext(), MainActivity.class);
                        MyApplication.getAppContext().startActivity(intent);
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
                    Toast.makeText(SplashActivity.this,"There is some problem with the server ("+message+")",Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(SplashActivity.this,MainActivity.class);
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
}
