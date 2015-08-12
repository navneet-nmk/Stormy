package com.teenvan.stormy.fragments;

import android.app.Activity;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
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
import com.teenvan.stormy.MainActivity;
import com.teenvan.stormy.R;
import com.teenvan.stormy.com.teenvan.stormy.adapters.CustomListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.crypto.spec.GCMParameterSpec;

import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Place;


public class CurrentFragment extends Fragment {
	// Declaration of member variables
	public TextView mLocation , mDateTime ,
            mTemperature , mApparentTemperature , mSummary ,
            mDewPoint ,mHumidity , mPressure;
    private ImageView mWeatherImage , mDewPointImage , mHumidityImage , mPressureImage,mSearchImage;

    private String ApiKEY = "cc360eb63a145e1a3956ebc14e34a247";
    private String forecastBaseURL = "https://api.forecast.io/forecast/";
    private double latitude = 37.8276;
    private double longitude = -122.423;
    private String forecastURL ;
    private CurrentWeather mCurrentWeather;
    private String googleApiKey = "AIzaSyDAm2MBA09M2bLATgj2rvLP8bj68-y7cwc";
    private GooglePlaces client = new GooglePlaces(googleApiKey);
    private EditText mLocationET;
    SendLatLong mSendLatLong;
    private ArrayList<String> temperatures,summaries,datetimes;


    @SuppressWarnings("deprecation")
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_time, container,
				false);

        // Referencing the UI elements
        mLocation = (TextView)rootView.findViewById(R.id.locationText);
        mDateTime = (TextView)rootView.findViewById(R.id.datetimetext);
        mTemperature = (TextView)rootView.findViewById(R.id.temperatureText);
        mApparentTemperature = (TextView)rootView.findViewById(R.id.apparentTempText);
        mSummary = (TextView)rootView.findViewById(R.id.summaryText);
        mDewPoint = (TextView)rootView.findViewById(R.id.dewPointText);
        mHumidity = (TextView)rootView.findViewById(R.id.humidityText);
        mPressure = (TextView)rootView.findViewById(R.id.pressureText);
        mLocationET = (EditText)rootView.findViewById(R.id.locationEditText);
        mSearchImage = (ImageView)rootView.findViewById(R.id.searchImage);

        ParseQuery<ParseObject> cwQuery = ParseQuery.getQuery("CurrentWeather");
        cwQuery.fromLocalDatastore();
        cwQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if( e== null){
                    mTemperature.setText(parseObject.getInt("Temperature")+"º");
                    mApparentTemperature.setText("Feels like "+
                            parseObject.getInt("AppTemperature")+"º");
                    mSummary.setText(parseObject.getString("Summary"));
                    mDewPoint.setText("Dew Point: "+parseObject.getDouble("DewPoint"));
                    mHumidity.setText("Humidity: "+parseObject.getDouble("Humidity")+"%");
                    mPressure.setText("Pressure: "+parseObject.getDouble("Pressure"));
                }else{
                    Log.e("Current Weather Object Retrieval","Failure",e);
                }
            }
        });

        LocationManager locationManager = (LocationManager)getActivity().
                getSystemService(Context.LOCATION_SERVICE);
        Log.d("Location Values GPS", String.valueOf(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)));
        Log.d("Location Values Network", String.valueOf(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)));

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });
        }else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });
        }else if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)&&
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            Toast.makeText(getActivity(),"Enable location services",Toast.LENGTH_LONG).show();
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setMessage("GPS not enabled");
            dialog.setPositiveButton("Open Location Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                   getActivity().startActivityForResult(myIntent,0);
                    //get gps
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub


                }
            });
            dialog.show();
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(location != null){
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null){
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        // Save the location coordinates in a ParseObject on the local data store
        ParseQuery<ParseObject> locQuery = ParseQuery.getQuery("Location");
        locQuery.fromLocalDatastore();
        locQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if( e == null){
                    if(parseObject == null){
                        ParseObject locationObject = new ParseObject("Location");
                        locationObject.put("Latitude",latitude);
                        locationObject.put("Longitude",longitude);
                        locationObject.pinInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Log.d("Location Object Saving", "Success");
                                } else {
                                    Log.e("Parse Error", "Location Object saving", e);
                                }
                            }
                        });
                    }else{
                        parseObject.put("Latitude",latitude);
                        parseObject.put("Longitude",longitude);
                        parseObject.pinInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e == null){
                                    Log.d("Location Object Updation","Success");
                                }else{
                                    Log.e("Location Object Updation","Failure",e);
                                }
                            }
                        });
                    }
                }else{
                    Log.e("Location Object Retrieval","Failure",e);
                }
            }
        });


        // Getting the JSON data
        forecastURL = forecastBaseURL + ApiKEY + "/" + Double.toString(latitude) + "," +
                Double.toString(longitude);
        Log.d(getString(R.string.forecast_api_url),forecastURL);


        try {
            mLocation.setText(getLocationName(latitude,longitude));
        } catch (IOException e) {
            Log.e("Location Error","Error",e);
        }
        mLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Make the edit text visible
                mLocation.setVisibility(View.INVISIBLE);
                mLocationET.setVisibility(View.VISIBLE);
                mSearchImage.setVisibility(View.VISIBLE);
                mSearchImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String query = mLocationET.getText().toString();
                        if(!query.isEmpty()){
                            try {
                                if(isNetworkAvailable()) {
                                    final String cityName = getLocationFromQuery(query);
                                    latitude = getLat(cityName);
                                    longitude = getLong(cityName);
                                    mSendLatLong.sendLatLong(latitude, longitude);
                                    Log.d("Locations", latitude + " " + longitude);
                                    forecastURL = forecastBaseURL + ApiKEY + "/" +
                                            Double.toString(latitude) + "," +
                                            Double.toString(longitude);
                                    Log.d(getString(R.string.forecast_api_url), forecastURL);
                                    mLocationET.setVisibility(View.INVISIBLE);
                                    mSearchImage.setVisibility(View.INVISIBLE);
                                    mLocation.setVisibility(View.VISIBLE);
                                    mLocation.setText(cityName);
                                    setupNetworkConnection(forecastURL);
                               }else{
                                    Toast.makeText(getActivity(),
                                            "Please check your internet connection",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                Log.e("Location Exception","Error",e);
                            }

                        }else{
                            Toast.makeText(getActivity(),"Please enter a valid location",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });


        // Setting everything up
        setupNetworkConnection(forecastURL);


		return rootView;

	}

    public void setupNetworkConnection(String forecastURL){
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
                            mCurrentWeather = getCurrentDetails(jsonData);
                            final Double humidity = mCurrentWeather.getHumidity()*100;
                            final int humidityLevel = humidity.intValue();
                            final Double dewPoint = mCurrentWeather.getDewPoint();
                            final Double pressure = mCurrentWeather.getPressure();
                            final Double appTemp = mCurrentWeather.getApparentTemperature();
                            final String summary = mCurrentWeather.getSummary();
                            final String datetime = mCurrentWeather.getFormattedTime();
                            Double temp = mCurrentWeather.getTemperature();
                            final int tempF = temp.intValue();
                            final int appTempF = appTemp.intValue();
                            Double tempE = ((appTemp - 32)*5)/9;
                            Double tempD = ((temp - 32)*5)/9;
                            final int tempC = tempD.intValue();
                            final int appTempC = tempE.intValue();

                            // Save the current weather data in parse local data store
                            ParseQuery<ParseObject> cQuery = ParseQuery.getQuery("CurrentWeather");
                            cQuery.fromLocalDatastore();
                            cQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    if( e == null){
                                        if(parseObject == null){
                                           ParseObject object = new ParseObject("CurrentWeather");
                                            object.put("Temperature",tempC);
                                            object.put("AppTemperature",appTempC);
                                            object.put("Summary",summary);
                                            object.put("DewPoint",dewPoint);
                                            object.put("Pressure",pressure);
                                            object.put("Humidity",humidity);
                                            object.pinInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if(e == null){
                                                        Log.d("Parse Object Pinning","Success");
                                                    }else{
                                                        Log.e("Parse Object Pinning","Failure",e);
                                                    }
                                                }
                                            });
                                        }else{
                                            parseObject.put("Temperature",tempC);
                                            parseObject.put("AppTemperature",appTempC);
                                            parseObject.put("Summary",summary);
                                            parseObject.put("DewPoint",dewPoint);
                                            parseObject.put("Pressure",pressure);
                                            parseObject.put("Humidity",humidity);
                                        }
                                    }else{
                                        Log.e("Current Weather Object Retrieval","Failure",e);
                                    }
                                }
                            });

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTemperature.setText(Integer.toString(tempC)+"º");
                                    mApparentTemperature.setText("Feels like "+Integer.toString(appTempC)+"º");
                                    mSummary.setText(summary);
                                    mDateTime.setText(datetime);
                                    mDewPoint.setText("Dew Point: "+Double.toString(dewPoint));
                                    mPressure.setText("Pressure: "+Double.toString(pressure));
                                    mHumidity.setText("Humidity: "+Integer.toString(humidityLevel) +"%");
                                }
                            });

                          getParseObject();

                        } catch (JSONException e) {
                            Log.e("JSON Error","Error",e);
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

    private void getParseObject() {
       ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
        query.fromLocalDatastore();
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if( e==null){
                    Log.d("Getting Parse Object","Success");
                    Log.d("Location Coordinates Local",
                            parseObject.getDouble("Latitude")+" "+
                                    parseObject.getDouble("Longitude")+"");
                    parseObject.put("Latitude",latitude);
                    parseObject.put("Longitude",longitude);
                    parseObject.pinInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                Log.d("Location Object Update","Success");
                            }else{
                                Log.e("Location Object Update","Failure",e);
                            }
                        }
                    });
                }else{
                    Log.d("Getting Parse Object",e.getMessage());
                }
            }
        });
    }


    public CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        // Create a JSONObject to handle the json data

        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject currentForecast = forecast.getJSONObject("currently");
        String iconString = currentForecast.getString("icon");
        String summary = currentForecast.getString("summary");
        long time = currentForecast.getLong("time");
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

        Log.d("MainActivity",Double.toString(currentForecast.getDouble("temperature")));

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
        mCurrentWeather.setSummary(summary);


        return mCurrentWeather;
    }



    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
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

    public interface SendLatLong{
        public void sendLatLong(Double lat,Double longitude);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mSendLatLong = (SendLatLong)activity;
        }catch (ClassCastException e){
            Log.e("Class Cast","You need to implement interface in Activity",e);
        }

    }

}
