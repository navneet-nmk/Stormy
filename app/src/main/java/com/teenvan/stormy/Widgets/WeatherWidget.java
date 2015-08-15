package com.teenvan.stormy.Widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.teenvan.stormy.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 */
public class WeatherWidget extends AppWidgetProvider {
    // Declaration of member variables
    private double latitude = 37.8276;
    private double longitude = -122.423;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);

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

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {



        CharSequence widgetText = "Jaipur";
        CharSequence tempText = "26º";
        CharSequence summaryText = "Mostly Cloudy";


        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
        views.setTextViewText(R.id.locationTextWidget, widgetText);
        views.setTextViewText(R.id.temperatureTextWidget,tempText);
        views.setTextViewText(R.id.summaryTextWidget,summaryText);
        views.setImageViewResource(R.id.weatherImageWidget,R.drawable.light_rain);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    // Get the location data
    public void getLocation(Context context){
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
        return stateName;

    }

    // Get the weather data


}


