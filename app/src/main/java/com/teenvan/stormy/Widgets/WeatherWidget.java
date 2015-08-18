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
    private int iconInt = R.drawable.rain;
    private CurrentWeather mCurrentWeather;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {

            int appWidgetId = appWidgetIds[i];

            Timer timer = new Timer();
            TimerTask hourTask = new TimerTask() {
                @Override
                public void run() {
                        getWeatherDetails();
                }
            };
            timer.schedule(hourTask,500*60*60);

            CharSequence widgetText = locationName;
            CharSequence tempText = temperature;
            CharSequence summaryText = summaryString;

            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
            views.setTextViewText(R.id.locationTextWidget, widgetText);
            views.setTextViewText(R.id.temperatureTextWidget,tempText);
            views.setTextViewText(R.id.summaryTextWidget,summaryText);


            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

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

    public void getWeatherDetails(){
        // Getting the parse object
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("CurrentWeather");
        query.fromLocalDatastore();
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if( e== null){
                    temperature = parseObject.getInt("Temperature")+"º";
                    summaryString = parseObject.getString("Summary");

                }else{
                    Log.e("Getting current weather object","Failure",e);
                }
            }
        });
    }



}


