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
    double userTempLow ,userTempHigh;
    Button goBackButton;
    //Evan stuff \/
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
    String[] hourTemps, hourHumidities, hourPrecips, hourWinds, dailyHighs, dailyLows, dailyPrecip;
    String[] dailyHumidities, dailyHighTimes, dailyLowTimes;
    int[] hourTimes, hourUVs;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;

    JSONObject nextWeek;

    ArrayList<String> jsonString;
    private FusedLocationProviderClient fusedLocationProviderClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        try {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                userTempLow = Double.parseDouble(extras.get("tempLowKey").toString());
                userTempHigh = Double.parseDouble(extras.get("tempHighKey").toString());
                //Test
            }
        }catch(Exception e){
            Toast.makeText(ScheduleActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
        goBackButton = (Button) findViewById(R.id.goBackBtn);
        goBackButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                try {
                    moveToHomeActivity(); //Method at the bottom of all the Evan stuff
                }
                catch(Exception e){
                    Toast.makeText(ScheduleActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        final TextView jsonTextView;
        final TextView hourlyTemps;
        final TextView averages;
        final TextView weeklyAve;
        final TextView oldDate;
        final TextView testBox;
        final ListView dailyList;

        testBox = findViewById(R.id.message1);
        dailyList = findViewById(R.id.dailyList);

        arrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, arrayList);

        dailyList.setAdapter(adapter);

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


                //forecastURL = "https://api.darksky.net/forecast/45b937115ee9a714334343756da12736/39.0840,-77.1528";

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

                            for(int i = 0; i < 24; i ++){
                                double curHourTemp = Double.parseDouble(hourTemps[i]);
                                if(curHourTemp >= userTempLow && curHourTemp <= userTempHigh){
                                    arrayList.add(hourTimes[i] + ":00 - " + hourTemps[i] + " degrees, UV index of " + hourUVs+", with a " + hourPrecips.toString()+ " chance of precipitation");

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
                            convertToHour(dailyHighTimes[0]);
                            ScheduleActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // next thing you have to do is check if your adapter has changed
                                    adapter.addAll(arrayList);
                                    adapter.notifyDataSetChanged();
                                    //adapter.notifyDataSetChanged();

                                }
                            });} catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private int convertToHour(String unixTime){
        long unix = Long.parseLong(unixTime);
        DateTimeFormatter formatter =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = Instant.ofEpochSecond(unix).atZone(ZoneId.of("EST")).format(formatter);
        System.out.println(formattedTime);
        Log.i("TIME", formattedTime);
        String hour = formattedTime.substring(11,13);
        return Integer.parseInt(hour);
    }

    private void moveToHomeActivity() {
        Intent intent = new Intent(ScheduleActivity.this, HomeActivity.class);
        startActivity(intent);
    }
}