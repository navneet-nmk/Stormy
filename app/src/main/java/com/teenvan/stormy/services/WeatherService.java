package com.teenvan.stormy.services;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.widget.RemoteViews;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.teenvan.stormy.CurrentWeather;
import com.teenvan.stormy.R;
import com.teenvan.stormy.Widgets.WeatherWidget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherService extends Service {

	private String forecastURL = "";
    private String ApiKEY = "cc360eb63a145e1a3956ebc14e34a247";
    private String forecastBaseURL = "https://api.forecast.io/forecast/";
    private double latitude;
    private double longitude;
    private String hourlySummaryString = "Clear for the day";
    private Double lat=0.0 , longi=0.0;


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

            if(intent!=null){
                lat= intent.getDoubleExtra("Latitude",0.0);
                longi= intent.getDoubleExtra("Longitude",0.0);
                Log.d("WeatherService",lat+" "+longi);
            }
            if(lat==0.0 && longi == 0.0) {
                if (!isNetworkAvailable()) {
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
                                timer.scheduleAtFixedRate(task, 30*60*1000, 40*60*1000);
                            } else {
                                Log.e("Location HourlyFragment", "Failure", e);
                            }

                        }
                    });
                } else {

                    LocationManager locationManager = (LocationManager) getApplicationContext().
                            getSystemService(Context.LOCATION_SERVICE);

                    Location location = locationManager.getLastKnownLocation
                            (LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                    Location gpsLocation = locationManager.getLastKnownLocation
                            (LocationManager.GPS_PROVIDER);
                    if (gpsLocation != null) {
                        latitude = gpsLocation.getLatitude();
                        longitude = gpsLocation.getLongitude();
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
                    timer.scheduleAtFixedRate(task, 30*60*1000,  40*60*1000);



                }
            }else{
                latitude = lat;
                longitude = longi;
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
                timer.scheduleAtFixedRate(task, 30*60*1000, 40*60*1000);

            }



		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
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
                            final int iconInt = getImageDrawable(iconString);

                            final String locationName = getLocationName(WeatherService.this,
                                    latitude, longitude);


                            // Get HourlySummary
                            ParseQuery<ParseObject> hourlyQuery = ParseQuery.
                                    getQuery("HourlySummary");
                            hourlyQuery.fromLocalDatastore();
                            hourlyQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    if( e==null){
                                        // SUCCESS
                                        hourlySummaryString = parseObject.getString("Summary");

                                    }else{
                                        // FAILURE
                                        Log.e("WeatherService","Failed to get object",e);
                                    }
                                }
                            });


                            // Update the widget

                            AppWidgetManager manager = AppWidgetManager
                                    .getInstance(WeatherService.this);
                            // Get a list of widgets to update
                            int[] widgetIds = manager.getAppWidgetIds(new ComponentName
                                    (WeatherService.this, WeatherWidget.class));
                            //Update the widgets
                            RemoteViews views = new RemoteViews(
                                    WeatherService.this.getPackageName(),R.layout.weather_widget);
                            views.setTextViewText(R.id.timeTextWidget,time);
                            views.setImageViewResource(R.id.weatherImageWidget, iconInt);
                            views.setTextViewText(R.id.apparentTempTextWidget, appTempC + "º");
                            views.setTextViewText(R.id.locationTextWidget,locationName);
                            views.setTextViewText(R.id.temperatureTextWidget, tempC+"º");
                            views.setTextViewText(R.id.summaryTextWidget, summary);
                            views.setTextViewText(R.id.nextHourForecastWidget,hourlySummaryString);
                            manager.updateAppWidget(widgetIds,views);

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


    // Get the appropriate icon
    public int getImageDrawable(String icon){
        switch (icon){
            case "clear-day":
                return R.drawable.sunny;
            case "clear-night":
                return R.drawable.clear_night;
            case "rain":
                return R.drawable.rain;
            case "snow":
                return R.drawable.snow;
            case "sleet":
                return R.drawable.sleet;
            case "windy":
                return R.drawable.windy;
            case "cloudy":
                return R.drawable.cloudy;
            case "partly-cloudy-day":
                return R.drawable.partly_cloudy_day;
            case "partly-cloudy-night":
                return R.drawable.partly_cloudy_night;
            default:
                return R.drawable.sunny;
        }
    }
    // Get the location Name
    private String getLocationName(Context context,
                                   Double latitude,Double longitude) throws IOException {
        // Getting the city name

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;

        addresses = geocoder.getFromLocation(latitude, longitude, 1);
        String cityName = addresses.get(0).getAddressLine(0);
        String stateName = addresses.get(0).getAddressLine(1);
        String countryName = addresses.get(0).getAddressLine(2);
        Log.d("Location Name",cityName);
        return cityName;

    }




}
