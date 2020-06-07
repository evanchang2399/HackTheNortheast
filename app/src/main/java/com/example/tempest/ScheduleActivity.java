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

        final TextView dayOneInfo;
        final TextView dayTwoInfo;
        final TextView dayThreeInfo;
        final TextView dayFourInfo;
        final TextView dayFiveInfo;
        final TextView daySixInfo;
        final TextView daySevenInfo;

        final ListView dailyList;

        dailyList = findViewById(R.id.dailyList);

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
                                if(curHourTemp >= userTempLow && curHourTemp <= userTempHigh && (hourTimes[i]>5 && hourTimes[i]<21)){
                                    arrayList.add(hourTimes[i] + ":00 - " + hourTemps[i] + " degrees, UV: " + hourUVs[i]+", precip chance: " + hourPrecips[i].toString());

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
                                    String text ="High of "+ dailyHighs[0] + " at "+convertToHour(dailyHighTimes[0])+":00, Low: " + dailyLows[0]+" at " +convertToHour(dailyLowTimes[0])+":00. ";
                                    text += "Precipitation chance of " + dailyPrecip[0] +" and a humidity of " + dailyHumidities[0];
                                    dayOneInfo.setText(text);

                                    text ="High of "+ dailyHighs[1] + " at "+convertToHour(dailyHighTimes[1])+":00, Low: " + dailyLows[1]+" at " +convertToHour(dailyLowTimes[1])+":00. ";
                                    text += "Precipitation chance of " + dailyPrecip[1] +" and a humidity of " + dailyHumidities[1];
                                    dayTwoInfo.setText(text);

                                    text ="High of "+ dailyHighs[2] + " at "+convertToHour(dailyHighTimes[2])+":00, Low: " + dailyLows[2]+" at " +convertToHour(dailyLowTimes[2])+":00. ";
                                    text += "Precipitation chance of " + dailyPrecip[2] +" and a humidity of " + dailyHumidities[2];
                                    dayThreeInfo.setText(text);

                                    text ="High of "+ dailyHighs[3] + " at "+convertToHour(dailyHighTimes[3])+":00, Low: " + dailyLows[3]+" at " +convertToHour(dailyLowTimes[3])+":00. ";
                                    text += "Precipitation chance of " + dailyPrecip[3] +" and a humidity of " + dailyHumidities[3];
                                    dayFourInfo.setText(text);

                                    text ="High of "+ dailyHighs[4] + " at "+convertToHour(dailyHighTimes[4])+":00, Low: " + dailyLows[4]+" at " +convertToHour(dailyLowTimes[4])+":00. ";
                                    text += "Precipitation chance of " + dailyPrecip[4] +" and a humidity of " + dailyHumidities[4];
                                    dayFiveInfo.setText(text);

                                    text ="High of "+ dailyHighs[5] + " at "+convertToHour(dailyHighTimes[5])+":00, Low: " + dailyLows[5]+" at " +convertToHour(dailyLowTimes[5])+":00. ";
                                    text += "Precipitation chance of " + dailyPrecip[5] +" and a humidity of " + dailyHumidities[5];
                                    daySixInfo.setText(text);

                                    text ="High of "+ dailyHighs[6] + " at "+convertToHour(dailyHighTimes[6])+":00, Low: " + dailyLows[6]+" at " +convertToHour(dailyLowTimes[6])+":00. ";
                                    text += "Precipitation chance of " + dailyPrecip[6] +" and a humidity of " + dailyHumidities[6];
                                    daySevenInfo.setText(text);

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