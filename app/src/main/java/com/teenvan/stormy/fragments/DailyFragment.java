package com.teenvan.stormy.fragments;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.teenvan.stormy.CurrentWeather;
import com.teenvan.stormy.R;
import com.teenvan.stormy.com.teenvan.stormy.adapters.CustomListAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class DailyFragment extends Fragment {
	// Declaration of member variables
    private TextView mDailyForecastText;
    private ListView mDailyList;
    private String ApiKEY = "cc360eb63a145e1a3956ebc14e34a247";
    private String forecastBaseURL = "https://api.forecast.io/forecast/";
    private double latitude = 37.8276;
    private double longitude = -122.423;
    private String forecastURL;
    private ArrayList<String> temperatures,summaries,datetimes;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_activity, container,
				false);

        //Referencing the UI elements
        mDailyForecastText = (TextView)rootView.findViewById(R.id.dailyforecastText);
        mDailyList = (ListView)rootView.findViewById(R.id.dailyList);

        // Get the parse object
        ParseQuery<ParseObject> locQuery = ParseQuery.getQuery("Location");
        locQuery.fromLocalDatastore();
        locQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if( e == null){
                    latitude = parseObject.getDouble("Latitude");
                    longitude = parseObject.getDouble("Longitude");
                    // Getting the JSON data
                    forecastURL = forecastBaseURL + ApiKEY + "/" +
                            Double.toString(latitude) + "," + Double.toString(longitude);
                    Log.d(getString(R.string.forecast_api_url),forecastURL);
                    setupDailyNetworkConnection(forecastURL);
                }else{

                }
            }
        });







        return rootView;
	}

    public void setupDailyNetworkConnection(String forecastURL){
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
                            ArrayList<CurrentWeather> mCurrentWeatherArray = getCurrentDetails(jsonData);
                            temperatures = new ArrayList<String>();
                            summaries = new ArrayList<String>();
                            datetimes = new ArrayList<String>();
                            for(int i=0;i<mCurrentWeatherArray.size();i++){
                                CurrentWeather mCW = mCurrentWeatherArray.get(i);
                                String temp = Double.toString(mCW.getTemperature());
                                String datetime = mCW.getFormattedTime();
                                String day = mCW.getDayOfTheWeek(mCW.getTime());
                                String summary = mCW.getSummary();
                                temperatures.add(temp);
                                summaries.add(summary);
                                datetimes.add(day);
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    CustomListAdapter adapter = new CustomListAdapter(getActivity(),temperatures,datetimes,summaries);
                                    mDailyList.setAdapter(adapter);
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    } else {
                        Toast.makeText(getActivity(), getString(R.string.response_error), Toast.LENGTH_LONG).show();
                    }
                }
            });

        }else{
            Toast.makeText(getActivity(),getString(R.string.network_not_available),Toast.LENGTH_LONG).show();
        }

    }

    public ArrayList<CurrentWeather> getCurrentDetails(String jsonData) throws JSONException {
        // Create a JSONObject to handle the json data

        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourlyForecast = forecast.getJSONObject("daily");
        String iconString = hourlyForecast.getString("icon");
        final String summary = hourlyForecast.getString("summary");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDailyForecastText.setText(summary);
            }
        });
        JSONArray data = hourlyForecast.getJSONArray("data");
        ArrayList<CurrentWeather> mArray = new ArrayList<CurrentWeather>();
        for( int i=0;i<6;i++){
            JSONObject currentForecast = data.getJSONObject(i);
            long time = currentForecast.getLong("time");
            String summaryHr = currentForecast.getString("summary");
            // int nearestSD = currentForecast.getInt("nearestStormDistance");
            // int nearestSB = currentForecast.getInt("nearestStormBearing");
            int precipIntensity = currentForecast.getInt("precipIntensity");
            int precipProbability = currentForecast.getInt("precipProbability");
            Double temperature = currentForecast.getDouble("temperatureMax");
            Double dewPoint = currentForecast.getDouble("dewPoint");
            Double apparentTemp = currentForecast.getDouble("apparentTemperatureMax");
            Double humidity = currentForecast.getDouble("humidity");
            Double windSpeed = currentForecast.getDouble("windSpeed");
            int windBearing = currentForecast.getInt("windBearing");
            //Double visibility = currentForecast.getDouble("visibility");
            Double cloudCover = currentForecast.getDouble("cloudCover");
            Double pressure = currentForecast.getDouble("pressure");
            Double ozone = currentForecast.getDouble("ozone");
            // Create the currentweather object
            CurrentWeather mCurrentWeather = new CurrentWeather();
            mCurrentWeather.setApparentTemperature(apparentTemp);
            mCurrentWeather.setCloudCover(cloudCover);
            mCurrentWeather.setDewPoint(dewPoint);
            mCurrentWeather.setHumidity(humidity);
            mCurrentWeather.setIcon(iconString);
            mCurrentWeather.setTimeZone(timezone);
            // mCurrentWeather.setNearestStormBearing(nearestSB);
            // mCurrentWeather.setNearestStormDistance(nearestSD);
            mCurrentWeather.setOzone(ozone);
            mCurrentWeather.setPrecipIntensity(precipIntensity);
            mCurrentWeather.setPrecipProbability(precipProbability);
            mCurrentWeather.setTemperature(temperature);
            mCurrentWeather.setTime(time);
            mCurrentWeather.setPressure(pressure);
            //mCurrentWeather.setVisibility(visibility);
            mCurrentWeather.setWindBearing(windBearing);
            mCurrentWeather.setWindSpeed(windSpeed);
            mCurrentWeather.setSummary(summaryHr);
            mArray.add(mCurrentWeather);
        }
        return mArray;
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)getActivity().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }


    public void updateListView(Double lat,Double longi) {
        forecastURL = forecastBaseURL + ApiKEY + "/" + Double.toString(lat) + "," +
                Double.toString(longi);
        Log.d(getString(R.string.forecast_api_url), forecastURL);
        setupDailyNetworkConnection(forecastURL);
    }

}
