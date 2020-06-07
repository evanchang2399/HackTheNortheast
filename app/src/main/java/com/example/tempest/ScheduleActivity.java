//Java class responsible for handling

package com.example.tempest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ScheduleActivity extends AppCompatActivity {
    //Set up variables used to parse JSON objects and textview on XML
    double userTempLow ,userTempHigh;
    int userPrefTime;
    Button goBackButton;
    Double latitude;
    Double longitude;
    String jResponse;
    String forecastURL;
    JSONObject currently;
    JSONObject hourly;

    String[] hourTemps, hourHumidities, hourPrecips, hourWinds, dailyHighs, dailyLows, dailyPrecip;
    String[] dailyHumidities, dailyHighTimes, dailyLowTimes;
    int[] hourTimes, hourUVs;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;
    private ArrayList<Integer> indexes;
    JSONObject nextWeek;

    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        //Get user input from a previous screen
        try {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                userTempLow = Double.parseDouble(extras.get("tempLowKey").toString());
                userTempHigh = Double.parseDouble(extras.get("tempHighKey").toString());
                userPrefTime = Integer.parseInt(extras.get("prefTimeKey").toString());
            }
        }catch(Exception e){
            Toast.makeText(ScheduleActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
        goBackButton = (Button) findViewById(R.id.goBackBtn);
        goBackButton.setOnClickListener(new View.OnClickListener(){
            //Return back to previous page upon button press
            @Override
            public void onClick(View v){
                try {
                    moveToHomeActivity();
                }
                catch(Exception e){
                    Toast.makeText(ScheduleActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        final TextView dayOneInfo;
        final TextView dayTwoInfo;
        final TextView dayThreeInfo;
        final TextView dayFourInfo;
        final TextView dayFiveInfo;
        final TextView daySixInfo;
        final TextView daySevenInfo;
        final TextView todayInfo;

        final ListView dailyList;

        dailyList = findViewById(R.id.dailyList);

        todayInfo = findViewById(R.id.todayInfo);
        dayOneInfo = findViewById(R.id.dayOneInfo);
        dayTwoInfo = findViewById(R.id.dayTwoInfo);
        dayThreeInfo = findViewById(R.id.dayThreeInfo);
        dayFourInfo = findViewById(R.id.dayFourInfo);
        dayFiveInfo = findViewById(R.id.dayFiveInfo);
        daySixInfo = findViewById(R.id.daySixInfo);
        daySevenInfo = findViewById(R.id.daySevenInfo);


        arrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, arrayList);

        dailyList.setAdapter(adapter);

        //Upon getting users location, do this:
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } else {
                    Log.i("event", "Error getting location");
                }

                //Create URL for API call
                forecastURL = "https://api.darksky.net/forecast/45b937115ee9a714334343756da12736/" + latitude + "," + longitude;
                jResponse = "empty";

                //Build client to access data
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

                    //When data is received, act accordingly
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

                        //Access today's info
                        try {
                            JSONObject jobj = new JSONObject(jResponse);
                            currently = jobj.getJSONObject("currently");
                            String temp = currently.getString("temperature");
                            String humidity = currently.getString("humidity");
                            String windSpeed = currently.getString("windSpeed");
                            String precipitation = currently.getString("precipProbability");



                            //Get hourly
                            JSONArray hourlyData = null;
                            try {
                                hourly = jobj.getJSONObject("hourly");
                                hourlyData = hourly.getJSONArray("data");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            hourTemps = new String[24];
                            hourPrecips = new String[24];
                            hourUVs = new int[24];
                            hourWinds = new String[24];
                            hourHumidities = new String[24];
                            hourTimes = new int[24];


                            for (int i = 0; i < 24; i++) {
                                try {
                                    JSONObject curHour = hourlyData.getJSONObject(i);
                                    hourTemps[i] = curHour.getString("temperature");
                                    hourPrecips[i] = curHour.getString("precipProbability");
                                    hourUVs[i] = Integer.parseInt(curHour.getString("uvIndex"));
                                    hourWinds[i] = curHour.getString("windSpeed");
                                    hourHumidities[i] = curHour.getString("humidity");
                                    hourTimes[i] = convertToHour(curHour.getString("time"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            indexes = new ArrayList<Integer>();
                            for(int i = 0; i < 24; i ++){
                                double curHourTemp = Double.parseDouble(hourTemps[i]);
                                if(curHourTemp >= userTempLow && curHourTemp <= userTempHigh && (hourTimes[i]>5 && hourTimes[i]<21)){
                                    arrayList.add(hourTimes[i] + ":00 - " + hourTemps[i] + " degrees, UV: " + hourUVs[i]+", precip chance: " + hourPrecips[i].toString());
                                    indexes.add(i);
                                }
                            }

                            JSONArray dailyData = null;
                            try {
                                nextWeek = jobj.getJSONObject("daily");
                                dailyData = nextWeek.getJSONArray("data");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            dailyHighs = new String[7];
                            dailyHighTimes = new String[7];
                            dailyLows = new String[7];
                            dailyLowTimes = new String[7];
                            dailyHumidities = new String[7];
                            dailyPrecip = new String[7];

                            for (int i = 0; i < 7; i++) {
                                try {
                                    JSONObject curDaily = dailyData.getJSONObject(i);
                                    dailyHighs[i] = curDaily.getString("apparentTemperatureHigh");
                                    dailyHighTimes[i] = curDaily.getString("temperatureHighTime");
                                    dailyLowTimes[i] = curDaily.getString("temperatureLowTime");
                                    dailyLows[i] = curDaily.getString("apparentTemperatureLow");
                                    dailyHumidities[i] = curDaily.getString("humidity");
                                    dailyPrecip[i] = curDaily.getString("precipProbability");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            //Set all text in screen to display to user
                            ScheduleActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    adapter.addAll(arrayList);
                                    adapter.notifyDataSetChanged();
                                    String text = "High of " + dailyHighs[0] + " at " + convertToHour(dailyHighTimes[0]) + ":00, Low: " + dailyLows[0] + " at " + convertToHour(dailyLowTimes[0]) + ":00. ";
                                    text += "Precipitation chance of " + dailyPrecip[0] + " and a humidity of " + dailyHumidities[0];
                                    if(userTempLow<Double.parseDouble(dailyHighs[0]) && userTempHigh>Double.parseDouble(dailyLows[0])){
                                        text+=". A good day for the outdoors!";
                                    }
                                    else{
                                        text+=". We recommend you try an indoor workout for this day.";
                                    }
                                    dayOneInfo.setText(text);

                                    text = "High of " + dailyHighs[1] + " at " + convertToHour(dailyHighTimes[1]) + ":00, Low: " + dailyLows[1] + " at " + convertToHour(dailyLowTimes[1]) + ":00. ";
                                    text += "Precipitation chance of " + dailyPrecip[1] + " and a humidity of " + dailyHumidities[1];
                                    if(userTempLow<Double.parseDouble(dailyHighs[1]) && userTempHigh>Double.parseDouble(dailyLows[1])){
                                        text+=". A good day for the outdoors!";
                                    }
                                    else{
                                        text+=". We recommend you try an indoor workout for this day.";
                                    }
                                    dayTwoInfo.setText(text);

                                    text = "High of " + dailyHighs[2] + " at " + convertToHour(dailyHighTimes[2]) + ":00, Low: " + dailyLows[2] + " at " + convertToHour(dailyLowTimes[2]) + ":00. ";
                                    text += "Precipitation chance of " + dailyPrecip[2] + " and a humidity of " + dailyHumidities[2];
                                    if(userTempLow<Double.parseDouble(dailyHighs[2]) && userTempHigh>Double.parseDouble(dailyLows[2])){
                                        text+=". A good day for the outdoors!";
                                    }
                                    else{
                                        text+=". We recommend you try an indoor workout for this day.";
                                    }
                                    dayThreeInfo.setText(text);

                                    text = "High of " + dailyHighs[3] + " at " + convertToHour(dailyHighTimes[3]) + ":00, Low: " + dailyLows[3] + " at " + convertToHour(dailyLowTimes[3]) + ":00. ";
                                    text += "Precipitation chance of " + dailyPrecip[3] + " and a humidity of " + dailyHumidities[3];
                                    if(userTempLow<Double.parseDouble(dailyHighs[3]) && userTempHigh>Double.parseDouble(dailyLows[3])){
                                        text+=". A good day for the outdoors!";
                                    }
                                    else{
                                        text+=". We recommend you try an indoor workout for this day.";
                                    }dayFourInfo.setText(text);

                                    text = "High of " + dailyHighs[4] + " at " + convertToHour(dailyHighTimes[4]) + ":00, Low: " + dailyLows[4] + " at " + convertToHour(dailyLowTimes[4]) + ":00. ";
                                    text += "Precipitation chance of " + dailyPrecip[4] + " and a humidity of " + dailyHumidities[4];
                                    if(userTempLow<Double.parseDouble(dailyHighs[4]) && userTempHigh>Double.parseDouble(dailyLows[4])){
                                        text+=". A good day for the outdoors!";
                                    }
                                    else{
                                        text+=". We recommend you try an indoor workout for this day.";
                                    }dayFiveInfo.setText(text);

                                    text = "High of " + dailyHighs[5] + " at " + convertToHour(dailyHighTimes[5]) + ":00, Low: " + dailyLows[5] + " at " + convertToHour(dailyLowTimes[5]) + ":00. ";
                                    text += "Precipitation chance of " + dailyPrecip[5] + " and a humidity of " + dailyHumidities[5];
                                    if(userTempLow<Double.parseDouble(dailyHighs[5]) && userTempHigh>Double.parseDouble(dailyLows[5])){
                                        text+=". A good day for the outdoors!";
                                    }
                                    else{
                                        text+=". We recommend you try an indoor workout for this day.";
                                    }daySixInfo.setText(text);

                                    text = "High of " + dailyHighs[6] + " at " + convertToHour(dailyHighTimes[6]) + ":00, Low: " + dailyLows[6] + " at " + convertToHour(dailyLowTimes[6]) + ":00. ";
                                    text += "Precipitation chance of " + dailyPrecip[6] + " and a humidity of " + dailyHumidities[6];
                                    if(userTempLow<Double.parseDouble(dailyHighs[6]) && userTempHigh>Double.parseDouble(dailyLows[6])){
                                        text+=". A good day for the outdoors!";
                                    }
                                    else{
                                        text+=". We recommend you try an indoor workout today.";
                                    }daySevenInfo.setText(text);

                                    boolean set = false;
                                    for (int j = 0; j < indexes.size(); j++) {
                                        int i = indexes.get(j);
                                        if (userPrefTime == -1) {
                                            if (hourTimes[i] < 12) {
                                                todayInfo.setText("We recommend you run at " + hourTimes[i] + ":00. It'll be " + hourTemps[i] + " degrees, UV of " + hourUVs[i] + ", with precip chance of " + hourPrecips[i].toString());
                                                j = indexes.size();
                                                set = true;
                                            }
                                        } else if (userPrefTime == 1) {
                                            if (hourTimes[i] > 12 && hourTimes[i] <16) {
                                                todayInfo.setText("We recommend you run at " + hourTimes[i] + ":00. It'll be " + hourTemps[i] + " degrees, UV of " + hourUVs[i] + ", with precip chance of " + hourPrecips[i].toString());
                                                j = indexes.size();
                                                set = true;
                                            }
                                        }
                                        else if (userPrefTime == 2) {
                                            if (hourTimes[i] > 16) {
                                                todayInfo.setText("We recommend you run at " + hourTimes[i] + ":00. It'll be " + hourTemps[i] + " degrees, UV of " + hourUVs[i] + ", with precip chance of " + hourPrecips[i].toString());
                                                j = indexes.size();
                                                set = true;
                                            }
                                        }
                                        else{
                                            todayInfo.setText("We recommend you run at " + hourTimes[i] + ":00. It'll be " + hourTemps[i] + " degrees, UV of " + hourUVs[i] + ", with precip chance of " + hourPrecips[i].toString());
                                            j = indexes.size();
                                            set = true;
                                        }
                                    }
                                    if(!set){
                                        todayInfo.setText("Sorry, we can't find a time to run today to fit your preferences, maybe try an indoor workout today or change your preferences!");
                                    }
                                }
                            });} catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    //Helper to convert a Unix time stamp to a hour of the day
    private int convertToHour(String unixTime){
        long unix = Long.parseLong(unixTime);
        DateTimeFormatter formatter =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = Instant.ofEpochSecond(unix).atZone(ZoneId.of("EST")).format(formatter);
        System.out.println(formattedTime);
        Log.i("TIME", formattedTime);
        String hour = formattedTime.substring(11,13);
        return Integer.parseInt(hour);
    }

    //Move to previous activity
    private void moveToHomeActivity() {
        Intent intent = new Intent(ScheduleActivity.this, HomeActivity.class);
        startActivity(intent);
    }
}