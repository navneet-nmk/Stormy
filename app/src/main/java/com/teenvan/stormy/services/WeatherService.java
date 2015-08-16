package com.teenvan.stormy.services;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.SendCallback;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.teenvan.stormy.CurrentWeather;
import com.teenvan.stormy.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherService extends Service {

	private String forecastURL = "";


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
        forecastURL = intent.getStringExtra("ForecastURL");
		// Toast.makeText(this, username, Toast.LENGTH_LONG).show();
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d("Service", "Created");



	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("Service", "Started");
        if(intent.getStringExtra("ForecastURL") !=null) {
            forecastURL = intent.getStringExtra("ForecastURL");
            if (forecastURL.isEmpty() || forecastURL == null) {

            } else {
                setupMinutelyNetworkConnection(forecastURL);
            }
        }
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private String getCurrentDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		String date = sdf.format(cal.getTime());
		return date;
	}

	private int getCurrentHour() {
		Calendar cal = Calendar.getInstance();
		int hours = cal.getTime().getHours();
		Log.d("Current Hours", Integer.toString(hours));
		return hours;
	}

    public void setupMinutelyNetworkConnection(String forecastURL){
        if(isNetworkAvailable()) {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(forecastURL).build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.e(getString(R.string.forecast_service), getString(R.string.error_string), e);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    // Do something with the response
                    if (response.isSuccessful()) {
                        String jsonData = response.body().string();
                        try {
                            ArrayList<CurrentWeather> mCurrentWeatherArray =
                                    getHourlyCurrentDetails(jsonData);

                            for(int i=0;i<mCurrentWeatherArray.size();i++){
                                CurrentWeather mCW = mCurrentWeatherArray.get(i);
                                int precipProb = mCW.getPrecipProbability();
                                if(precipProb == 1){
                                    String time = mCW.getFormattedTime();
                                    ParsePush push = new ParsePush();
                                    push.setMessage("Rain will come at "+time);
                                    push.sendInBackground(new SendCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if( e== null){
                                                Log.d("Parse Push Sent","Success");
                                            }else{
                                                Log.e("Parse Push Sent","Failure",e);
                                            }
                                        }
                                    });
                                    break;
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    } else {

                    }
                }
            });

        }else{

        }
    }

    public ArrayList<CurrentWeather> getHourlyCurrentDetails(String jsonData) throws JSONException {
        // Create a JSONObject to handle the json data

        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourlyForecast = forecast.getJSONObject("minutely");
        String iconString = hourlyForecast.getString("icon");
        final String summary = hourlyForecast.getString("summary");
        if(hourlyForecast != null) {
            JSONArray data = hourlyForecast.getJSONArray("data");
            ArrayList<CurrentWeather> mArray = new ArrayList<CurrentWeather>();

            for (int i = 0; i < 12; i++) {
                JSONObject currentForecast = data.getJSONObject(i);
                long time = currentForecast.getLong("time");
                int precipIntensity = currentForecast.getInt("precipIntensity");
                int precipProbability = currentForecast.getInt("precipProbability");

                // Create the currentweather object
                CurrentWeather mCurrentWeather = new CurrentWeather();
                mCurrentWeather.setPrecipIntensity(precipIntensity);
                mCurrentWeather.setPrecipProbability(precipProbability);
                mArray.add(mCurrentWeather);
            }
            return mArray;
        }else{
            ArrayList<CurrentWeather> mArray = new ArrayList<>();
            return mArray;
        }

    }
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)getApplicationContext().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }


}
