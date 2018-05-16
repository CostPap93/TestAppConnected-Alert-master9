package com.example.mastermind.testapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by mastermind on 18/4/2018.
 */

public class SettingActivity  extends AppCompatActivity {
    SharedPreferences settingsPreferences;

    CheckBox checkBox;
    ListView lv;
    Button btnSave, btnCancel;
    RadioButton radioButton, radioButton1, radioButton2;
    ArrayList<Boolean> checkIsChanged;
    ArrayList<JobOffer> offers;
    ArrayList<JobOffer> asyncOffers;
    ArrayList<OfferCategory> categories;
    String message = "";
    ArrayList<OfferCategory> offerCategories;
    boolean addNewChecked = true;

    ArrayList<Integer> idArray = new ArrayList<>();
    SimpleDateFormat format;
    PendingIntent pendingIntentA;

    RequestQueue queue;
    SwipeRefreshLayout swipeLayoutSettings;
    int t=0,s=0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setTitle("Datalabs");
        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        checkBox = findViewById(R.id.chbox_category);
        lv = findViewById(R.id.lv_categories);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        radioButton = findViewById(R.id.rb_day);
        radioButton1 = findViewById(R.id.rb_once);
        radioButton2 = findViewById(R.id.rb_twice);
        checkIsChanged = new ArrayList<>();
        asyncOffers = new ArrayList<>();
        offers = new ArrayList<>();
        categories = new ArrayList<>();
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        queue = Volley.newRequestQueue(this);


        System.out.println(settingsPreferences.getInt("numberOfCategories", 0));
        settingsPreferences.edit().putBoolean("checkIsChanged", false).apply();

        if (settingsPreferences.getInt("numberOfCategories", 0) != 0) {
            for (int i = 0; i < settingsPreferences.getInt("numberOfCategories", 0); i++) {
                OfferCategory category = new OfferCategory();
                category.setCatid(settingsPreferences.getInt("offerCategoryId " + i, 0));
                category.setTitle(settingsPreferences.getString("offerCategoryTitle " + i, ""));
                categories.add(category);
                System.out.println(categories.get(i).getTitle()+"checkBoxAdapter");
            }
            CheckBoxAdapter checkBoxAdapter = new CheckBoxAdapter(getApplicationContext(), categories);

            lv.setAdapter(checkBoxAdapter);


            checkBoxAdapter.notifyDataSetChanged();

    }

        if (settingsPreferences.getLong("interval", 0) == 6000) {
            radioButton.setChecked(true);
        } else if (settingsPreferences.getLong("interval", 0) == 18000) {
            radioButton1.setChecked(true);
        } else {
            radioButton2.setChecked(true);
        }



    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_refresh) {
            RefreshOperation();

        }

        return super.onOptionsItemSelected(item);
    }

    public void RefreshOperation() {

        queue.add(volleyUpdateDefault());
    }


    public void btnSaveClicked(View view) throws ExecutionException, InterruptedException {
        CheckBox checkBox;
        offerCategories = new ArrayList<>();
        OfferCategory offerCategory;

        if(isConn()) {

            for (int i = 0; i < lv.getChildCount(); i++) {
                checkBox = lv.getChildAt(i).findViewById(R.id.chbox_category);
                offerCategory = (OfferCategory) lv.getAdapter().getItem(i);
                if (checkBox.isChecked() && !offerCategories.contains(offerCategory)) {

                    offerCategories.add(offerCategory);
                }
            }
            System.out.println(offerCategories.size());
            for (OfferCategory oc : offerCategories) {
                System.out.println(oc.getCatid()+"Save");
                //new TaskShowOffersFromCategories().execute(String.valueOf(settingsPreferences.getInt("checkedCategoryId " + v, 0))).get();
                queue.add(volleySaveOffers(String.valueOf(oc.getCatid())));
            }



            if (radioButton.isChecked()) {
                if (!(settingsPreferences.getLong("interval", 0) == 6000)) {
                    settingsPreferences.edit().putLong("interval", 6000).apply();

                }
            } else if (radioButton1.isChecked()) {
                if (!(settingsPreferences.getLong("interval", 0) == 18000)) {
                    settingsPreferences.edit().putLong("interval", 18000).apply();

                }
            } else {
                if (!(settingsPreferences.getLong("interval", 0) == 30000)) {
                    settingsPreferences.edit().putLong("interval", 30000).apply();

                }
            }

            System.out.println(settingsPreferences.getLong("interval", 0));

            start();

            Intent intent = new Intent(SettingActivity.this, MainActivity.class);
            startActivity(intent);

        }else  {
            Toast.makeText(SettingActivity.this, "You Have To Be Connected To Reset", Toast.LENGTH_LONG).show();
        }

    }

    public void btnCancelClicked(View view) {
        Intent intent = new Intent(SettingActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void btnResetClicked(View view) throws ExecutionException, InterruptedException {

        if(isConn()) {
            cancel();

            queue.add(volleySetDefault());
            //new TaskSetDefaultCateogries().execute().get();

            /*for (int v = 0; v < (settingsPreferences.getInt("numberOfCheckedCategories", 0)); v++) {
                if (settingsPreferences.getInt("checkedCategoryId " + v, 0) != 0) {
                    System.out.println(settingsPreferences.getInt("checkedCategoryId " + v, 0) + "Before the task show for the first time");
                    System.out.println(settingsPreferences.getString("checkedCategoryTitle " + v, ""));
                    try {
                        new TaskShowOffersFromCategories().execute(String.valueOf(settingsPreferences.getInt("checkedCategoryId " + v, 0))).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }*/
            settingsPreferences.edit().putLong("interval", 6000).apply();
            start();
        }else {
            Toast.makeText(SettingActivity.this, "You Have To Be Connected To Reset", Toast.LENGTH_LONG).show();
        }
    }

  /*  public class TaskShowOffersFromCategories extends AsyncTask<String, Integer, ArrayList<JobOffer>> {

        ArrayList<JobOffer> fiveOffers = new ArrayList<>();


        protected void onPreExecute() {


            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(ArrayList<JobOffer> fiveOffers) {
            super.onPostExecute(fiveOffers);

            if(fiveOffers!= null) {

                for (int j = 0; j < 5; j++) {
                    settingsPreferences.edit().remove("offerId " + j).apply();
                    settingsPreferences.edit().remove("offerCatid " + j).apply();
                    settingsPreferences.edit().remove("offerTitle " + j).apply();
                    settingsPreferences.edit().remove("offerDate " + j).apply();
                    settingsPreferences.edit().remove("offerDownloaded " + j).apply();
                }

                for (int i = 0; i < fiveOffers.size(); i++) {
                    if (i < 5) {

                        settingsPreferences.edit().putInt("offerId " + i, fiveOffers.get(i).getId()).apply();
                        settingsPreferences.edit().putInt("offerCatid " + i, fiveOffers.get(i).getCatid()).apply();
                        settingsPreferences.edit().putString("offerTitle " + i, fiveOffers.get(i).getTitle()).apply();
                        settingsPreferences.edit().putLong("offerDate " + i, fiveOffers.get(i).getDate().getTime()).apply();
                        settingsPreferences.edit().putString("offerDownloaded " + i, fiveOffers.get(i).getDownloaded()).apply();

                        System.out.println(settingsPreferences.getLong("offerDate " + i, 0));
                        System.out.println(settingsPreferences.getString("offerTitle " + i, ""));
                        settingsPreferences.edit().putInt("numberOfOffers", fiveOffers.size()).apply();
                    } else
                        settingsPreferences.edit().putInt("numberOfOffers", 5).apply();

                }
                settingsPreferences.edit().putLong("lastSeenDate", fiveOffers.get(0).getDate().getTime()).apply();
                System.out.println(settingsPreferences.getLong("lastSeenDate", 0));
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

            }else{
                Toast.makeText(MyApplication.getAppContext(),"There is some problem with the server",Toast.LENGTH_LONG);
            }


        }

        @Override
        protected ArrayList<JobOffer> doInBackground(String... params) {

            Map<String, String> postParam = new HashMap<>();
            postParam.put("action", "showOffersFromCategory");
            postParam.put("jacat_id", params[0]);

            try {
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST("http://10.0.2.2/android/jobAds.php?", postParam);
                if(jsonString.equals("")){
                    return null;
                }
                JSONObject jsonObjectAll = new JSONObject(jsonString);
                JSONArray jsonArray = jsonObjectAll.getJSONArray("offers");
                int i = 0;

                while (i < jsonArray.length() && i < 5) {


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

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }*/


    public boolean isConn() {
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

        Intent alarmIntent = new Intent(SettingActivity.this, AlarmReceiver.class);
        pendingIntentA = PendingIntent.getBroadcast(SettingActivity.this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), settingsPreferences.getLong("interval",0), pendingIntentA);

        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }

    public void cancel() {
        Intent alarmIntent = new Intent(SettingActivity.this, AlarmReceiver.class);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(PendingIntent.getBroadcast(SettingActivity.this,0,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT));
        Toast.makeText(this, "Alarm Canceled", Toast.LENGTH_SHORT).show();
    }

    public StringRequest volleySetDefault(){
        String url ="http://10.0.2.2/android/jobOfferCategories.php?";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        System.out.println("Volley: " + message);
                        System.out.println(response);
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
                    Toast.makeText(SettingActivity.this,"There is some problem with the server ("+message+")",Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(SettingActivity.this,SettingActivity.class);
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
                        System.out.println("Volley: " + message);
                        System.out.println(response);
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
                            t++;

                        if(t==settingsPreferences.getInt("numberOfCheckedCategories",0)){
                            Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
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
                    Toast.makeText(SettingActivity.this,"There is some problem with the server ("+message+")",Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(SettingActivity.this,SettingActivity.class);
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
        super.onBackPressed();
        Intent i = new Intent(SettingActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    public StringRequest volleyUpdateDefault(){
        String url ="http://10.0.2.2/android/jobOfferCategories.php?";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        ArrayList<OfferCategory> categoriesRefresh = new ArrayList<>();


                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonObjectAll = new JSONObject(response);

                            JSONArray jsonArray = jsonObjectAll.getJSONArray("joboffercategories");
                            System.out.println(jsonArray.length());
                            settingsPreferences.edit().putInt("numberOfCategories", jsonArray.length()).apply();
                            System.out.println(settingsPreferences.getInt("numberOfCategories", 0));
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObjectCategory = jsonArray.getJSONObject(i);
                                settingsPreferences.edit().putInt("offerCategoryId " + i, Integer.valueOf(jsonObjectCategory.getString("jacat_id"))).apply();
                                settingsPreferences.edit().putString("offerCategoryTitle " + i, jsonObjectCategory.getString("jacat_title")).apply();
                                System.out.println(jsonObjectCategory.toString());

                            }

                            if (settingsPreferences.getInt("numberOfCategories", 0) != 0) {
                                for (int i = 0; i < settingsPreferences.getInt("numberOfCategories", 0); i++) {
                                    OfferCategory category = new OfferCategory();
                                    category.setCatid(settingsPreferences.getInt("offerCategoryId " + i, 0));
                                    category.setTitle(settingsPreferences.getString("offerCategoryTitle " + i, ""));
                                    categoriesRefresh.add(category);
                                    System.out.println(categoriesRefresh.get(i).getTitle()+"checkBoxAdapter");
                                }

                                CheckBoxAdapter checkBoxAdapter= new CheckBoxAdapter(getApplicationContext(), categoriesRefresh);
                                lv.setAdapter(checkBoxAdapter);


                                checkBoxAdapter.notifyDataSetChanged();

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
                    Toast.makeText(SettingActivity.this,"There is some problem with the server ("+message+")",Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(SettingActivity.this,SettingActivity.class);
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

    public boolean isConnectedToServer(String url, int timeout) {
        try{
            URL myUrl = new URL(url);
            URLConnection connection = myUrl.openConnection();
            connection.setConnectTimeout(timeout);
            connection.connect();
            return true;
        } catch (Exception e) {
            // Handle your exceptions
            return false;
        }
    }

    public StringRequest volleySaveOffers(final String param) {

        String url = "http://10.0.2.2/android/jobAds.php?";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Display the first 500 characters of the response string.
                        System.out.println("Volley: " + message);
                        System.out.println(response);

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

                        if(addNewChecked) {

                            for (int j = 0; j < settingsPreferences.getInt("numberOfCategories", 0); j++) {
                                System.out.println(settingsPreferences.getString("checkedCategoryTitle " + j, "") + "Removed from checked categories");
                                settingsPreferences.edit().remove("checkedCatergoryId " + j).apply();
                                settingsPreferences.edit().remove("checkedCatergoryTitle " + j).apply();
                            }

                            for (OfferCategory oc : offerCategories) {

                                System.out.println(settingsPreferences.getString("checkedCategoryTitle " + offerCategories.indexOf(oc), "") + "Previously in checked categories");
                                settingsPreferences.edit().putInt("checkedCategoryId " + offerCategories.indexOf(oc), oc.getCatid()).apply();
                                settingsPreferences.edit().putString("checkedCategoryTitle " + offerCategories.indexOf(oc), oc.getTitle()).apply();
                                System.out.println(settingsPreferences.getString("checkedCategoryTitle " + offerCategories.indexOf(oc), "") + "Added to checked categories");

                            }
                            settingsPreferences.edit().putInt("numberOfCheckedCategories", offerCategories.size()).apply();
                            addNewChecked = false;

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
                        }else{
                            settingsPreferences.edit().putInt("numberOfOffers", 0).apply();
                        }

                        s++;

                        if(s==settingsPreferences.getInt("numberOfCheckedCategories",0)){
                            Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
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
                    Toast.makeText(SettingActivity.this,"There is some problem with the server ("+message+")",Toast.LENGTH_LONG).show();
                    Intent intentError = new Intent(SettingActivity.this,SettingActivity.class);
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
    protected void onDestroy() {
        super.onDestroy();
    }
}





