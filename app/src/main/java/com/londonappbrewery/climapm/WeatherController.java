package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {

    // Constants:
    final int REQUEST_CODE = 123;
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "e39ade7631419098c5a67dcdb57db50f";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION_PROVIDER here:
    String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager;
    LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


        // TODO: Add an OnClickListener to the changeCityButton here:
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(WeatherController.this,ChangeCityController.class);
                startActivity(myIntent);
            }
        });

    }


    // TODO: Add onResume() here:
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Clima", "O onResume executou");
        Intent myIntent = getIntent();
        String city = myIntent.getStringExtra("City");

        if(city != null){
            getWeatherForNewCity(city);
        }else{
            getWeatherFromCurrentLocation();
        }
    }


    // TODO: Add getWeatherForNewCity(String city) here:
    private void getWeatherForNewCity(String city){
        RequestParams params = new RequestParams();
        params.put("q",city);
        params.put("appid",APP_ID);
        letsDoSomeNetworking(params);
    }
    // TODO: Add getWeatherForCurrentLocation() here:

    private void getWeatherFromCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Clima","Pronto vitu, peguei o local, é isso âe cuzão");
                String latidude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());
                Log.d("Clima","Latitude: " + latidude + "Longitude: " + longitude);

                RequestParams params = new RequestParams();
                params.put("lat",latidude);
                params.put("lon",longitude);
                params.put("appid",APP_ID);

                letsDoSomeNetworking(params);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }

        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if(requestCode == REQUEST_CODE){

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("Clima","Permissão aprovada");
                getWeatherFromCurrentLocation();
            }else{
                Log.d("Clima","Permissão negada");
            }

        }

    }

    // TODO: Add letsDoSomeNetworking(RequestParams params) here:
    void letsDoSomeNetworking(RequestParams params){

        AsyncHttpClient client = new AsyncHttpClient();

        client.get(WEATHER_URL,params,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                Log.d("Clima","Sucesso viado1");
                Log.d("Clima",response.toString());
                WeatherDataModel weatherD = WeatherDataModel.fromJson(response);
                updateUI(weatherD);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e ,JSONObject response){
                Log.e("Clima",e.toString());
                Log.d("Clima","Satus code: " + statusCode);
                Toast.makeText(WeatherController.this,"Não vai dar não",Toast.LENGTH_SHORT).show();
            }
        });
    }

    // TODO: Add updateUI() here:

    protected  void updateUI(WeatherDataModel weaterData){
        mTemperatureLabel.setText(weaterData.getTemperature());
        mCityLabel.setText(weaterData.getCity());
        int resourceId = getResources().getIdentifier(weaterData.getIconName(),"drawable",getPackageName());
        mWeatherImage.setImageResource(resourceId);
    }


    // TODO: Add onPause() here:
    @Override
    protected void onPause() {
        super.onPause();
        if(mLocationManager != null){
            mLocationManager.removeUpdates(mLocationListener);
        }
    }
}
