package com.example.tempest;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.util.*;

import java.util.*;

import android.os.Bundle;

import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    Double latitude;
    Double longitude;
    String jResponse;
    String forecastURL;
    String pastForecastURL;
    JSONObject currently;
    JSONObject hourly;
    JSONObject daily;
    Double temp;
    String humidity;
    String currentInfo;
    String[] hourTemps;
    String[] dailyTemps;
    JSONObject nextWeek;

    ArrayList<String> jsonString;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        forecastURL = "https://api.darksky.net/forecast/45b937115ee9a714334343756da12736/37,-122";
        pastForecastURL = "https://api.darksky.net/forecast/45b937115ee9a714334343756da12736/37,-122,1554264000?exclude=alerts";
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        final TextView jsonTextView;
        final TextView hourlyTemps;
        final TextView averages;
        final TextView weeklyAve;
        final TextView oldDate;
        /*oldDate = findViewById(R.id.oldTemp);
        weeklyAve = findViewById(R.id.dailyAve);
        averages = findViewById(R.id.averages);
        jsonTextView = findViewById(R.id.text_result);
        hourlyTemps = findViewById(R.id.hourly);
        */
        Log.i("TEST", "ABOUT TO GET LOCATION");
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                Log.i("SUCCESS", "WE made it boys");
                if (location != null) {
                    Log.i("event", "" + location.getLatitude());
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.i("location", "latitude is " + latitude + " long is " + longitude);
                } else {
                    Log.i("event", "Error getting location");
                }

                forecastURL = "https://api.darksky.net/forecast/45b937115ee9a714334343756da12736/" + latitude + "," + longitude;
                //forecastURL = "https://api.myjson.com/bins/kp9wz";
                Log.i("URL", forecastURL);
            }
        });
/*
        jResponse = "empty";

        OkHttpClient client = new OkHttpClient.Builder().build();
        Log.i("event", forecastURL);
        Request request = new Request.Builder()
                .url(forecastURL)
                .build();


        Call call = client.newCall(request);
        call.enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("event", "Failure");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("event", "on response");
                if (response.isSuccessful()) {
                    //obj = response.getJsonObject("currently");
                    jResponse = response.body().string();
                    Log.i("event", "Received JSON");
                } else {
                    Log.i("event", "Something went wrong");
                }


                try {
                    JSONObject jobj = new JSONObject(jResponse);
                    currently = jobj.getJSONObject("currently");
                    String temp = currently.getString("temperature");
                    String humidity = currently.getString("humidity");
                    String windSpeed = currently.getString("windSpeed");
                    String precipitation = currently.getString("precipProbability");


                    currentInfo = "Current Temp: " + temp + ", current humidty: " + humidity + ", current wind speed: " + windSpeed;
                    currentInfo += ", current precipation chance: " + precipitation;

                    //Get hourly
                    JSONArray hourlyData = null;
                    try {
                        hourly = jobj.getJSONObject("hourly");
                        hourlyData = hourly.getJSONArray("data");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    hourTemps = new String[48];
                    for (int i = 0; i < 48; i++) {
                        try {
                            JSONObject curHour = hourlyData.getJSONObject(i);
                            hourTemps[i] = curHour.getString("temperature");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    double total = 0;
                    for (int i = 0; i < 48; i++) {
                        total += (Double.parseDouble(hourTemps[i]));
                    }
                    total = total / 48;

                    averages.setText("The average temperature over the next 48 hours is " + total);


                    //Start getting averages over next week
                    JSONArray dailyData = null;
                    try {
                        nextWeek = jobj.getJSONObject("daily");
                        dailyData = nextWeek.getJSONArray("data");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    dailyTemps = new String[7];
                    for (int i = 0; i < 7; i++) {
                        try {
                            JSONObject curDaily = dailyData.getJSONObject(i);
                            dailyTemps[i] = curDaily.getString("apparentTemperatureHigh");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            jsonTextView.setText(currentInfo);
                            hourlyTemps.setText("The temperatures for the next 5 hours are: " + hourTemps[0] + ", " + hourTemps[1]
                                    + ", " + hourTemps[2] + ", " + hourTemps[3] + ", " + hourTemps[4]);

                            weeklyAve.setText("The high temperatures for the next 7 days are: " + dailyTemps[0] + " " + dailyTemps[1] + " "
                                    + dailyTemps[2] + " " + dailyTemps[3] + " " + dailyTemps[4]);
                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("Error", "nope didnt work");
                }
            }
        });


        OkHttpClient client2 = new OkHttpClient.Builder().build();

        Request request2 = new Request.Builder()
                .url(pastForecastURL)
                .build();

        client2.newCall(request2).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("event", "Failure");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try {
                    String JSONrep = "no";
                    if (response.isSuccessful()) {
                        //obj = response.getJsonObject("currently");
                        JSONrep = response.body().string();
                        Log.i("event", "Received JSON");
                    } else {
                        Log.i("event", "Something went wrong");
                    }
                    Log.i("hello", JSONrep);
                    JSONObject jobj = new JSONObject(JSONrep);
                    Log.i("Eelhk", JSONrep);
                    JSONObject cur = jobj.getJSONObject("currently");
                    final String oldTemp = cur.getString("temperature");

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            oldDate.setText("The temperature on 4/3/19 in Austin was " + oldTemp);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("J", "JSON failed");
                }


            }
        });
        */
    }


}
