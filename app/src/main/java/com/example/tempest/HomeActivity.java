package com.example.tempest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.util.*;

import java.util.*;

import android.os.Bundle;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
    int prefTime;
    String spinnerInput;
    EditText inputTempLow, inputTempHigh;
    Button submitButton;
    Spinner mySpinner;
    ArrayList<String> jsonString;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Drop Down user input
        mySpinner = (Spinner) findViewById(R.id.dropDownTime);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.prefered_times, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(adapter);



        //Temp user input
        inputTempHigh = (EditText) findViewById(R.id.inputTempHigh);
        inputTempLow = (EditText) findViewById(R.id.inputTempLow);

        //Submit button
        submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                try {
                    userTempLow = Integer.valueOf(inputTempLow.getText().toString());
                    userTempHigh = Integer.valueOf(inputTempHigh.getText().toString());
                    spinnerInput = mySpinner.getSelectedItem().toString();
                    convertToInt(); //Converts spinner value to -1,0, or 1
                    moveToScheduleActivity(); //Method at the bottom of all the Evan stuff

                }catch (NumberFormatException en){ //No numbers filled out
                    Toast.makeText(HomeActivity.this, "Please fill out your preferences", Toast.LENGTH_SHORT).show();
                }
                catch(Exception e){ //Something else went wrong... (:o)
                    Toast.makeText(HomeActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //Next Activity
    private void moveToScheduleActivity() {
        Intent intent = new Intent(HomeActivity.this, ScheduleActivity.class);
        intent.putExtra("tempLowKey", userTempLow);
        intent.putExtra("tempHighKey", userTempHigh);
        intent.putExtra("prefTimeKey", prefTime);
        startActivity(intent);
    }
    //Converts input, morning, evening or anytime into -1, 0 or 1... just for funsies
    private void convertToInt(){
        if (spinnerInput.equals("Morning")) {
            prefTime = -1;
        }else if(spinnerInput.equals("Evening")){
            prefTime = 1;
        }else { //Both/anytime
            prefTime = 0;
        }
    }
}
