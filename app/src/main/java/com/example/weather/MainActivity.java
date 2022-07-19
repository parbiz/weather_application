package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homerl;
    private ProgressBar PB;
    private TextView CityName, Condition, Temperatur, dat;
    private TextInputEditText cityEdt;
    private ImageView IvBack,Search,weatherIcon;
    private RecyclerView Rv;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        homerl = findViewById(R.id.rlhome);
        PB = findViewById(R.id.idPB);
        CityName = findViewById(R.id.idCityName);
        Condition = findViewById(R.id.idCondition);
        Temperatur = findViewById(R.id.idTemperatur);
        cityEdt = findViewById(R.id.idEditInputLayout);
        IvBack = findViewById(R.id.idIvBack);
        Search = findViewById(R.id.idSearch);
        weatherIcon = findViewById(R.id.idweatherIcon);
        Rv = findViewById(R.id.idRv);
        dat = findViewById(R.id.iddat);
        weatherRVModelArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModelArrayList);
        Rv.setAdapter(weatherRVAdapter);


        locationManager =  (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }

        Criteria criteria = new Criteria();

        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);

      //  Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityNames = getCityName(location.getLongitude(),location.getLatitude());
        getWeatherInfo(cityNames);

        Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityEdt.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Pleade Enter City Name", Toast.LENGTH_SHORT).show();
                }else {
                    CityName.setText(cityNames);
                    getWeatherInfo(city);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "!Permission Not Granted", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityNames = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);
            for(Address adr : addresses){
                if(adr != null){
                    String city = adr.getLocality();
                    if(city != null && !city.equals("")){
                        cityNames = city;
                    }else {
                        Log.d("TAG", "CITY  FOUND");
                        Toast.makeText(this, "user city  found..", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }catch (IOException e){
            e.printStackTrace();
        }
        return cityNames;
    }

    private void getWeatherInfo(String cityName){
        String url = "http://api.weatherapi.com/v1/forecast.json?key=fc2b93037b694cc7b6f174558221207&q="+cityName+"&days=1&aqi=no&alerts=no\n";
        CityName.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                PB.setVisibility(View.GONE);
                homerl.setVisibility(View.VISIBLE);
                weatherRVModelArrayList.clear();
                try {
                    String temprature = response.getJSONObject("current").getString("temp_c");
                    Temperatur.setText(temprature+"°c");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    //String todate = response.getJSONObject("location").getString("localtime");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(weatherIcon);
                    Condition.setText(condition);
                    if(isDay==1){
                        Picasso.get().load("https://images.unsplash.com/photo-1626166466949-3e027ee4dac0?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=360&q=80").into(IvBack);
                    }else{
                        Picasso.get().load("https://storage.googleapis.com/pod_public/1300/86838.jpg").into(IvBack);
                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecastdayObj = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    String todaydate = forecastdayObj.getString("date");
                    dat.setText(todaydate);
                    JSONArray hourArray = forecastdayObj.getJSONArray("hour");
                    for(int i=0;i<hourArray.length();i++){
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String tempr = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");
                        weatherRVModelArrayList.add(new WeatherRVModel(time,tempr,img,wind));
                    }

                    weatherRVAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please Enter Valid City Name", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);



    }

}