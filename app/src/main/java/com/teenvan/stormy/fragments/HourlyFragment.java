package com.teenvan.stormy.fragments;



import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.teenvan.stormy.CurrentWeather;
import com.teenvan.stormy.R;
import com.teenvan.stormy.com.teenvan.stormy.adapters.CustomListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HourlyFragment extends Fragment {
	// Declaration of member variables
	private TextView mForecastText;
    private ListView mHoursList;
    private String ApiKEY = "cc360eb63a145e1a3956ebc14e34a247";
    private String forecastBaseURL = "https://api.forecast.io/forecast/";
    private double latitude = 37.8276;
    private double longitude = -122.423;
    private String forecastURL;
    private ArrayList<String> temperatures,summaries,datetimes;


    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_heart_rate,
				container, false);
        // Referencing the UI elements
        mForecastText = (TextView)rootView.findViewById(R.id.forecastText);
        mHoursList = (ListView)rootView.findViewById(R.id.hourlyList);

        ParseQuery<ParseObject> locQuery = ParseQuery.getQuery("Location");
        locQuery.fromLocalDatastore();
        locQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if( e== null){
                    latitude = parseObject.getDouble("Latitude");
                    longitude = parseObject.getDouble("Longitude");
                    // Getting the JSON data
                    forecastURL = forecastBaseURL + ApiKEY + "/" + Double.toString(latitude) + "," +
                            Double.toString(longitude);
                    Log.d(getString(R.string.forecast_api_url),forecastURL);
                    setupHourlyNetworkConnection(forecastURL);
                }else{
                    Log.e("Location Object retrieval HourlyFragment","failure",e);
                }
            }
        });



        return rootView;
	}
    public void setupHourlyNetworkConnection(String forecastURL){
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
                            temperatures = new ArrayList<String>();
                            summaries = new ArrayList<String>();
                            datetimes = new ArrayList<String>();
                            // Creating parse objects
                            ParseObject tempObject = new ParseObject("Temperature");
                            final ParseObject summObject = new ParseObject("Summaries");
                            final ParseObject dateObject = new ParseObject("DateTime");
                            for(int i=0;i<mCurrentWeatherArray.size();i++){
                                CurrentWeather mCW = mCurrentWeatherArray.get(i);
                                String temp = Double.toString(mCW.getTemperature());
                                String datetime = mCW.getFormattedTime();
                                String summary = mCW.getSummary();
                                temperatures.add(temp);
                                summaries.add(summary);
                                datetimes.add(datetime);
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    CustomListAdapter adapter = new CustomListAdapter(getActivity(),
                                            temperatures,datetimes,summaries);
                                    mHoursList.setAdapter(adapter);
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

    public ArrayList<CurrentWeather> getHourlyCurrentDetails(String jsonData) throws JSONException {
        // Create a JSONObject to handle the json data

        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourlyForecast = forecast.getJSONObject("hourly");
        String iconString = hourlyForecast.getString("icon");
        final String summary = hourlyForecast.getString("summary");

        JSONArray data = hourlyForecast.getJSONArray("data");
        ArrayList<CurrentWeather> mArray = new ArrayList<CurrentWeather>();
        for( int i=0;i<12;i++){
            JSONObject currentForecast = data.getJSONObject(i);
            long time = currentForecast.getLong("time");
            final String summaryHr = currentForecast.getString("summary");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mForecastText.setText(summaryHr);
                }
            });

            // int nearestSD = currentForecast.getInt("nearestStormDistance");
            // int nearestSB = currentForecast.getInt("nearestStormBearing");
            int precipIntensity = currentForecast.getInt("precipIntensity");
            int precipProbability = currentForecast.getInt("precipProbability");
            Double temperature = currentForecast.getDouble("temperature");
            Double dewPoint = currentForecast.getDouble("dewPoint");
            Double apparentTemp = currentForecast.getDouble("apparentTemperature");
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
    private String getLocationName(Double latitude,Double longitude) throws IOException {
        // Getting the city name
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> addresses = null;

        addresses = geocoder.getFromLocation(latitude, longitude, 1);
        String cityName = addresses.get(0).getAddressLine(0);
        String stateName = addresses.get(0).getAddressLine(1);
        String countryName = addresses.get(0).getAddressLine(2);
        Log.d("Location Name",cityName);
        return cityName;

    }

    private String getLocationFromQuery(String query) throws IOException {
        Geocoder geocoder = new Geocoder(getActivity(),Locale.getDefault());
        List<Address> addresses = null;
        addresses = geocoder.getFromLocationName(query,1);
        String cityName = addresses.get(0).getAddressLine(0);

        return cityName;
    }

    private Double getLat(String query) throws IOException {
        Geocoder geocoder = new Geocoder(getActivity(),Locale.getDefault());
        List<Address> addresses = null;
        addresses = geocoder.getFromLocationName(query,1);
        String cityName = addresses.get(0).getAddressLine(0);
        Double lat = addresses.get(0).getLatitude();
        return lat;
    }
    private Double getLong(String query) throws IOException {
        Geocoder geocoder = new Geocoder(getActivity(),Locale.getDefault());
        List<Address> addresses = null;
        addresses = geocoder.getFromLocationName(query,1);
        String cityName = addresses.get(0).getAddressLine(0);
        Double longi = addresses.get(0).getLongitude();
        return longi;
    }

    public void updateListView(Double lat,Double longi){
        forecastURL = forecastBaseURL + ApiKEY + "/" + Double.toString(lat) + "," +
                Double.toString(longi);
        Log.d(getString(R.string.forecast_api_url),forecastURL);
        setupHourlyNetworkConnection(forecastURL);
    }

}
