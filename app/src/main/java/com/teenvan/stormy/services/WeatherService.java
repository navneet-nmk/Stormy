package com.teenvan.stormy.services;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
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
    private String ApiKEY = "cc360eb63a145e1a3956ebc14e34a247";
    private String forecastBaseURL = "https://api.forecast.io/forecast/";
    private double latitude = 37.8276;
    private double longitude = -122.423;


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
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

        latitude = intent.getDoubleExtra("Latitude",37.8276);
        longitude = intent.getDoubleExtra("Longitude",-122.423);


        if(!isNetworkAvailable()) {
            ParseQuery<ParseObject> locQuery = ParseQuery.getQuery("Location");
            locQuery.fromLocalDatastore();
            locQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (e == null) {
                        latitude = parseObject.getDouble("Latitude");
                        longitude = parseObject.getDouble("Longitude");
                        // Getting the JSON data
                        forecastURL = forecastBaseURL + ApiKEY + "/" +
                                Double.toString(latitude) + "," +
                                Double.toString(longitude);
                        Log.d(getString(R.string.forecast_api_url), forecastURL);
                        setupMinutelyNetworkConnection(forecastURL);
                        // Setup a timer object
                        Timer timer = new Timer();
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                setupMinutelyNetworkConnection(forecastURL);
                            }
                        };
                        timer.scheduleAtFixedRate(task, 1, 100000);
                    } else {
                        Log.e("Location HourlyFragment", "Failure", e);
                    }

                }
            });
        }else{

            LocationManager locationManager = (LocationManager)getApplicationContext().
                    getSystemService(Context.LOCATION_SERVICE);

            Location location = locationManager.getLastKnownLocation
                    (LocationManager.NETWORK_PROVIDER);
            if(location != null){
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
            Location gpsLocation = locationManager.getLastKnownLocation
                    (LocationManager.GPS_PROVIDER);
            if(location != null){
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
            forecastURL = forecastBaseURL + ApiKEY + "/" + Double.toString(latitude) + "," +
                    Double.toString(longitude);
            setupMinutelyNetworkConnection(forecastURL);
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    setupMinutelyNetworkConnection(forecastURL);
                }
            };
            timer.scheduleAtFixedRate(task,1,100000);


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
                                    getMinutelyCurrentDetails(jsonData);

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
                            // Get the current weather object
                            CurrentWeather mCurrentWeather = getCurrentDetails(jsonData);
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
                            final String iconString = mCurrentWeather.getIcon();
                            final String time = mCurrentWeather.getFormattedTime();

                            // Create a parse object
                            ParseQuery<ParseObject> query = new
                                    ParseQuery<ParseObject>("CurrentWeather");
                            query.fromLocalDatastore();
                            query.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    if( e== null){
                                        // Success finding the object
                                        parseObject.put("Temperature",tempC);
                                        parseObject.put("AppTemperature",appTempC);
                                        parseObject.put("Summary",summary);
                                        parseObject.put("DewPoint",dewPoint);
                                        parseObject.put("Pressure",pressure);
                                        parseObject.put("Humidity",humidity);
                                        parseObject.put("Icon",iconString);
                                        parseObject.put("Time",time);

                                        parseObject.pinInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if( e== null){
                                                    Log.d("Saving Service"
                                                    ,"Success");
                                                }else{
                                                    Log.e("Service Saving object",
                                                            "Failure",e);
                                                }
                                            }
                                        });
                                    }else{
                                        // Create a new Parse Object
                                        Log.e("Current Retrieval","Failure",e);
                                        // Create a new Parse Object of CurrentWeather Class
                                        ParseObject object = new ParseObject("CurrentWeather");
                                        object.put("Temperature",tempC);
                                        object.put("AppTemperature",appTempC);
                                        object.put("Summary",summary);
                                        object.put("DewPoint",dewPoint);
                                        object.put("Pressure",pressure);
                                        object.put("Humidity",humidity);
                                        object.put("Icon",iconString);
                                        object.put("Time",time);
                                        object.pinInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if(e == null){
                                                    Log.d("Current Weather Service",
                                                            "Success");
                                                }else{
                                                    Log.e("Pinning Service",
                                                            "Failure",e);
                                                }
                                            }
                                        });
                                    }
                                }
                            });


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    } else {
                        Log.e("Service Response","Failure");
                    }
                }
            });

        }else{

        }
    }

    public ArrayList<CurrentWeather> getMinutelyCurrentDetails(String jsonData)
            throws JSONException {
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


    public CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        // Create a JSONObject to handle the json data
        // Create a JSONObject to handle the json data

        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject currentForecast = forecast.getJSONObject("currently");
        final String iconString = currentForecast.getString("icon");
        String summary = currentForecast.getString("summary");


        long time = currentForecast.getLong("time");
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
