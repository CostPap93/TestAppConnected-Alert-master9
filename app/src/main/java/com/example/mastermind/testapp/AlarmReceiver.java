package com.example.mastermind.testapp;

import android.app.Notification;
import android.app.NotificationManager;
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
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.android.volley.toolbox.JsonObjectRequest;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;


public class AlarmReceiver extends BroadcastReceiver {

    String TAG = "Tag_Alarm";

    NotificationCompat.Builder notification;
    private static final int uniqueID = 45612;
    NotificationManager nm;
    int notCount;

    ArrayList<JobOffer> asyncOffers = new ArrayList<>();
    SharedPreferences settingsPreferences;
    BubblesManager bubblesManager;

    NotificationBadge mBadge;
    ArrayList<Integer> idArray = new ArrayList<>();
    String message = "";
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    RequestQueue queue;
    String categoriesIds,areasIds;




    @Override
    public void onReceive(final Context context, final Intent intent) {
        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext());

        notCount = 0;
        categoriesIds = "";
        areasIds = "";

        if(queue == null) {
            queue = Volley.newRequestQueue(MyApplication.getAppContext());
        }else{
            queue.cancelAll(TAG);
        }

        if(isConn()) {
//            if(Build.VERSION.SDK_INT>=23) {
                initializeBubblesManager();
//            }
            for (int v = 0; v < (settingsPreferences.getInt("numberOfCheckedCategories", 0)); v++) {
                if (categoriesIds.equals("")) {
                    categoriesIds += settingsPreferences.getInt("checkedCategoryId " + v, 0);
                } else
                    categoriesIds += "," + settingsPreferences.getInt("checkedCategoryId " + v, 0);
            }
            for (int v = 0; v < (settingsPreferences.getInt("numberOfCheckedAreas", 0)); v++) {
                if (areasIds.equals("")) {
                    areasIds += settingsPreferences.getInt("checkedAreaId " + v, 0);
                } else
                    areasIds += "," + settingsPreferences.getInt("checkedAreaId " + v, 0);
            }


            queue.add(volleySetCheckedCategories(categoriesIds,areasIds));



        }else {
            settingsPreferences.edit().putBoolean("makeRequest", true).apply();
            System.out.println("Job Scheduled");
            scheduleJob();
        }


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
        queue.cancelAll(TAG);
    }






    public boolean isConn() {
        ConnectivityManager connectivityManager = (ConnectivityManager) MyApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = networkInfo.isConnected();
        networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = networkInfo.isConnected();
        Log.d("connection", "Wifi connected: " + isWifiConn);
        Log.d("connection", "Mobile connected: " + isMobileConn);
        return isWifiConn || isMobileConn;
    }

    public int checkForOffers() {
        boolean searchChecked = false;
        int notCount = 0;
        for (int i = 0; i < settingsPreferences.getInt("numberOfOffers", 0); i++) {
            System.out.println(settingsPreferences.getInt("numberOfOffers", 0));
            System.out.println(settingsPreferences.getInt("numberOfCheckedCategories", 0));
            System.out.println(settingsPreferences.getLong("offerDate " + i,0) > settingsPreferences.getLong("lastSeenDate", 0));
            if (settingsPreferences.getLong("offerDate " + i,0) > settingsPreferences.getLong("lastSeenDate", 0)) {
                for(int j = 0; j < settingsPreferences.getInt("numberOfCheckedCategories", 0); j++) {
                    for (int k = 0; k < settingsPreferences.getInt("numberOfCheckedAreas", 0); k++) {
                        System.out.println(settingsPreferences.getInt("offerCatid " + i, 0));
                        System.out.println(settingsPreferences.getInt("checkedCategoryId " + j, 0));
                        if (settingsPreferences.getInt("offerCatid " + i, 0) == settingsPreferences.getInt("checkedCategoryId " + j, 0) && settingsPreferences.getInt("offerAreaid " + i, 0) == settingsPreferences.getInt("checkedAreaId " + k, 0)) {
                            notCount++;

                        }
                    }
                }
            }

            System.out.println(settingsPreferences.getLong("lastSeenDate", 0) + " at the end of alarmreceiver ");

        }

        return notCount;
    }

    public StringRequest volleySetCheckedCategories(final String param,final String param2) {
        String url = Utils.jobAdsLink;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Display the first 500 characters of the response string.
                        System.out.println("Volley: " + message);
                        try {
                            System.out.println(response);
                            JSONObject jsonObjectAll = new JSONObject(response);
                            JSONArray jsonArray = jsonObjectAll.getJSONArray("offers");
                            int i = 0;

                            while (i < jsonArray.length() && i < 5) {


                                JSONObject jsonObjectCategory = jsonArray.getJSONObject(i);

                                JobOffer offer = new JobOffer();
                                offer.setId(Integer.valueOf(jsonObjectCategory.getString("jad_id")));
                                offer.setCatid(Integer.valueOf(jsonObjectCategory.getString("jad_catid")));
                                offer.setAreaid(Integer.valueOf(jsonObjectCategory.getString("jaarea_id")));
                                offer.setTitle(jsonObjectCategory.getString("jad_title"));
                                offer.setCattitle(jsonObjectCategory.getString("jacat_title"));
                                offer.setAreatitle(jsonObjectCategory.getString("jaarea_title"));
                                offer.setLink(jsonObjectCategory.getString("jad_link"));
                                offer.setDesc(jsonObjectCategory.getString("jad_desc"));
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
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }



                        for (int j = 0; j < 5; j++) {
                            settingsPreferences.edit().remove("offerId " + j).apply();
                            settingsPreferences.edit().remove("offerCatid " + j).apply();
                            settingsPreferences.edit().remove("offerAreaid " + j).apply();
                            settingsPreferences.edit().remove("offerTitle " + j).apply();
                            settingsPreferences.edit().remove("offerCattitle " + j).apply();
                            settingsPreferences.edit().remove("offerAreatitle " + j).apply();
                            settingsPreferences.edit().remove("offerLink " + j).apply();
                            settingsPreferences.edit().remove("offerDesc " + j).apply();
                            settingsPreferences.edit().remove("offerDate " + j).apply();
                            settingsPreferences.edit().remove("offerDownloaded " + j).apply();
                        }

                        if(asyncOffers.size()>0) {
                            for (int i = 0; i < asyncOffers.size(); i++) {
                                if (i < 5) {

                                    settingsPreferences.edit().putInt("offerId " + i, asyncOffers.get(i).getId()).apply();
                                    settingsPreferences.edit().putInt("offerCatid " + i, asyncOffers.get(i).getCatid()).apply();
                                    settingsPreferences.edit().putInt("offerAreaid " + i, asyncOffers.get(i).getAreaid()).apply();
                                    settingsPreferences.edit().putString("offerTitle " + i, asyncOffers.get(i).getTitle()).apply();
                                    settingsPreferences.edit().putString("offerCattitle " + i, asyncOffers.get(i).getCattitle()).apply();
                                    settingsPreferences.edit().putString("offerAreatitle " + i, asyncOffers.get(i).getAreatitle()).apply();
                                    settingsPreferences.edit().putString("offerLink " + i, asyncOffers.get(i).getLink()).apply();
                                    settingsPreferences.edit().putString("offerDesc " + i, asyncOffers.get(i).getDesc()).apply();
                                    settingsPreferences.edit().putLong("offerDate " + i, asyncOffers.get(i).getDate().getTime()).apply();
                                    settingsPreferences.edit().putString("offerDownloaded " + i, asyncOffers.get(i).getDownloaded()).apply();
                                    System.out.println(settingsPreferences.getLong("offerDate " + i, 0));
                                    System.out.println(settingsPreferences.getString("offerTitle " + i, ""));
                                    settingsPreferences.edit().putInt("numberOfOffers", asyncOffers.size()).apply();
                                } else
                                    settingsPreferences.edit().putInt("numberOfOffers", 5).apply();
                            }
                            System.out.println(settingsPreferences.getLong("lastSeenDate", 0));


                            if (checkForOffers() > 0 && asyncOffers.get(0).getDate().getTime() > settingsPreferences.getLong("lastNotDate", 0)) {
//                                if(Build.VERSION.SDK_INT>=23) {
                                    addNewBubble();
//                                }
                                settingsPreferences.edit().putInt("numberOfUnseenOffers", checkForOffers()).apply();
                                System.out.println(settingsPreferences.getInt("numberOfUnseenOffers", 0));


                                notification = new NotificationCompat.Builder(MyApplication.getAppContext(), "notification");
                                notification.setAutoCancel(true);

                                notification.setSmallIcon(R.drawable.ic_not);
                                notification.setTicker("This is the ticker");
                                notification.setWhen(System.currentTimeMillis());
                                notification.setCategory(NotificationCompat.CATEGORY_REMINDER);
                                notification.setDefaults(Notification.DEFAULT_ALL);
                                notification.setPriority(Notification.PRIORITY_MAX);
                                notification.setVibrate(new long[0]);
                                notification.setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL);


                                Intent intentBackToMain = new Intent(MyApplication.getAppContext(), UnseenActivity.class);
                                PendingIntent pendingIntent = PendingIntent.getActivity(MyApplication.getAppContext(), 0, intentBackToMain, PendingIntent.FLAG_UPDATE_CURRENT);
                                notification.setContentTitle("You have " + checkForOffers() + " unseen offers!!Alarm Receiver");
                                notification.setContentIntent(pendingIntent);

                                nm = (NotificationManager) MyApplication.getAppContext().getSystemService(NOTIFICATION_SERVICE);
                                nm.notify(uniqueID, notification.build());


                                settingsPreferences.edit().putBoolean("makeRequest", false).apply();
                                settingsPreferences.edit().putLong("lastNotDate", asyncOffers.get(0).getDate().getTime()).apply();

                            } else {
                                Toast.makeText(MyApplication.getAppContext(), "There is some problem with the server", Toast.LENGTH_LONG);
                            }
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
                    Toast.makeText(MyApplication.getAppContext(),"There is some problem with the server ("+message+")",Toast.LENGTH_LONG).show();
                }
            }
        }
        )
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("jacat_id",param);
                params.put("jaarea_id",param2);

                return params;
            }
        };
        stringRequest.setTag(TAG);
        return stringRequest;
    }


    private void addNewBubble() {
        BubbleLayout bubbleView = (BubbleLayout)LayoutInflater.from(MyApplication.getAppContext()).inflate(R.layout.bubble_layout, null);
        mBadge =bubbleView.findViewById(R.id.badge);
        mBadge.setNumber(checkForOffers());

        bubbleView.setOnBubbleRemoveListener(new BubbleLayout.OnBubbleRemoveListener() {
            @Override
            public void onBubbleRemoved(BubbleLayout bubble) {
            }
        });

        //The Onclick Listener for the bubble has been set below.
        bubbleView.setOnBubbleClickListener(new BubbleLayout.OnBubbleClickListener() {

            @Override
            public void onBubbleClick(BubbleLayout bubble) {

                Toast.makeText(MyApplication.getAppContext(), "Clicked", Toast.LENGTH_SHORT).show();
                Intent intentBubbleToMain = new Intent(MyApplication.getAppContext(), UnseenActivity.class);
                intentBubbleToMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentBubbleToMain.putExtra("source", "alarm");
                intentBubbleToMain.putExtra("asyncOffers",asyncOffers);
                MyApplication.getAppContext().startActivity(intentBubbleToMain);
                bubblesManager.removeBubble(bubble);

                nm.cancel(uniqueID);
            }
        });
        bubbleView.setShouldStickToWall(true);
        bubblesManager.addBubble(bubbleView, 60, 20);
    }

    private void initializeBubblesManager() {
        bubblesManager = new BubblesManager.Builder(MyApplication.getAppContext())
                .setTrashLayout(R.layout.bubble_remove)
                .setInitializationCallback(new OnInitializedCallback() {
                    @Override
                    public void onInitialized() {

                    }
                })
                .build();
        bubblesManager.initialize();
    }





}


