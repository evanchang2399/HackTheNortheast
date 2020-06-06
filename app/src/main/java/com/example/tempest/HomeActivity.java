package com.example.tempest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.util.*;

import java.util.*;

import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class HomeActivity extends AppCompatActivity {
    double userTempLow, userTempHigh;
    EditText inputTempLow, inputTempHigh;
    Button submitButton;

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
    String[] hourTemps;
    String[] hourUVs;
    String[] hourHumidities;
    String[] hourPrecips;
    String[] hourWinds;
    String[] dailyHighs;
    String[] dailyLows;
    String[] dailyPrecip;
    String[] dailyHumidities;
    String[] dailyHighTimes;
    String[] dailyLowTimes;
    JSONObject nextWeek;

    ArrayList<String> jsonString;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputTempHigh = (EditText) findViewById(R.id.inputTempHigh);
        inputTempLow = (EditText) findViewById(R.id.inputTempLow);

        submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                try {
                    userTempLow = Integer.valueOf(inputTempLow.getText().toString());
                    userTempHigh = Integer.valueOf(inputTempHigh.getText().toString());
                    moveToScheduleActivity(); //Method at the bottom of all the Evan stuff
                }catch (NumberFormatException en){
                    Toast.makeText(HomeActivity.this, "Please fill out your preferences", Toast.LENGTH_SHORT).show();
                }
                catch(Exception e){
                    Toast.makeText(HomeActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Evan stuff \/
        forecastURL = "https://api.darksky.net/forecast/45b937115ee9a714334343756da12736/37,-122";
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        final TextView jsonTextView;
        final TextView hourlyTemps;
        final TextView averages;
        final TextView weeklyAve;
        final TextView oldDate;
        final TextView testBox;


    }

    private void moveToScheduleActivity() {
        Intent intent = new Intent(HomeActivity.this, ScheduleActivity.class);
        intent.putExtra("tempLowKey", userTempLow);
        intent.putExtra("tempHighKey", userTempHigh);
        startActivity(intent);
    }
}
