package com.teenvan.stormy.Widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.RemoteViews;
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
import com.teenvan.stormy.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Implementation of App Widget functionality.
 */
public class WeatherWidget extends AppWidgetProvider {
    // Declaration of member variables
    private double latitude = 37.8276;
    private double longitude = -122.423;
    private String locationName = "Jaipur";
    private String forecastURL ;
    private String ApiKEY = "cc360eb63a145e1a3956ebc14e34a247";
    private String forecastBaseURL = "https://api.forecast.io/forecast/";
    private String temperature = "28º";
    private String summaryString = "Mostly Cloudy";
    private String appTemperature ="Feels like 26º";
    private String hourSummary = "Partly Cloudy until tomorrow morning";
    private String iconString = "rain";
    private int iconInt = R.drawable.rain;
    private CurrentWeather mCurrentWeather;
    private String time = "4:30pm";

    @Override
    public void onUpdate(final Context context,final AppWidgetManager appWidgetManager,
                         final int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {

            final int appWidgetId = appWidgetIds[i];

            final RemoteViews views =
                    new RemoteViews(context.getPackageName(), R.layout.weather_widget);

            // Set on click listener
            // Open a new activity





            setUIElements(views,appWidgetManager,appWidgetId);

            // Refresh UI every 10 minutes
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    setUIElements(views,appWidgetManager,appWidgetId);
                }
            };
            timer.scheduleAtFixedRate(task,1,10000);





        }

    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
    // Get the location data
    public String getLocation(Context context) throws IOException {
        LocationManager locationManager = (LocationManager)context.
                getSystemService(Context.LOCATION_SERVICE);
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

        return  getLocationName(context,latitude,longitude);
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

    public void getWeatherDetails(final Context context){
        // Getting the parse object
        ParseQuery<ParseObject> query = ParseQuery.getQuery("CurrentWeather");
        query.fromLocalDatastore();
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    Log.d("Widget", "Success");
                    temperature = parseObject.getInt("Temperature") + "º";
                    summaryString = parseObject.getString("Summary");
                    RemoteViews views = new RemoteViews(context.getPackageName(),
                            R.layout.weather_widget);
                    views.setTextViewText(R.id.temperatureTextWidget, temperature);
                    views.setTextViewText(R.id.summaryTextWidget, summaryString);
                } else {
                    Log.e("Widget", "Failure", e);
                }
            }
        });
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

    public void setUIElements(final RemoteViews views,
                              final AppWidgetManager appWidgetManager,final int appWidgetId){

        // Getting the parse object for weather
        ParseQuery<ParseObject> query = ParseQuery.getQuery("CurrentWeather");
        query.fromLocalDatastore();
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    Log.d("Widget", "Success");
                    temperature = parseObject.getInt("Temperature") + "º";
                    summaryString = parseObject.getString("Summary");
                    locationName = parseObject.getString("Location");
                    appTemperature = "Feels like "+parseObject.getInt("AppTemperature")+"º";
                    iconString = parseObject.getString("Icon");
                    iconInt = getImageDrawable(iconString);
                    time = parseObject.getString("Time");
                    views.setTextViewText(R.id.timeTextWidget,time);
                    views.setImageViewResource(R.id.weatherImageWidget,iconInt);
                    views.setTextViewText(R.id.apparentTempTextWidget,appTemperature);
                    views.setTextViewText(R.id.locationTextWidget,locationName);
                    views.setTextViewText(R.id.temperatureTextWidget, temperature);
                    views.setTextViewText(R.id.summaryTextWidget, summaryString);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                } else {
                    Log.e("Widget", "Failure", e);

                    CharSequence tempText = temperature;
                    CharSequence summaryText = summaryString;
                    CharSequence locText = locationName;
                    CharSequence appText = appTemperature;
                    CharSequence timeText = time;

                    // Construct the RemoteViews object
                    views.setTextViewText(R.id.timeTextWidget,timeText);
                    views.setTextViewText(R.id.apparentTempTextWidget,appText);
                    views.setTextViewText(R.id.locationTextWidget,locText);
                    views.setTextViewText(R.id.temperatureTextWidget, tempText);
                    views.setTextViewText(R.id.summaryTextWidget, summaryText);
                    views.setImageViewResource(R.id.weatherImageWidget,iconInt);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            }
        });

        // Getting the parse object for hourly summary
        ParseQuery<ParseObject> hourlyQuery = ParseQuery.getQuery("HourlySummary");
        hourlyQuery.fromLocalDatastore();
        hourlyQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if( e==null){
                    hourSummary = parseObject.getString("Summary");
                    CharSequence hourSummaryText = hourSummary;
                    views.setTextViewText(R.id.nextHourForecastWidget,hourSummaryText);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }else{
                    Log.e("Widget Summary","Failure",e);
                    CharSequence hourSummaryText = hourSummary;
                    views.setTextViewText(R.id.nextHourForecastWidget,hourSummaryText);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            }
        });
    }



}


