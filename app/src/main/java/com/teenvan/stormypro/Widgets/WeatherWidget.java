package com.teenvan.stormypro.Widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.teenvan.stormypro.MainActivity;
import com.teenvan.stormy.R;

/**
 * Implementation of App Widget functionality.
 */
public class WeatherWidget extends AppWidgetProvider {
    // Declaration of member variables
    public static final String WIDGET_ID_KEY ="mywidgetproviderwidgetids";
    public static final String WIDGET_DATA_KEY ="mywidgetproviderwidgetdata";

    private String locationName = "Jaipur";

    private String temperature = "28º";
    private String summaryString = "Mostly Cloudy";
    private String appTemperature ="Feels like 26º";
    private String hourSummary = "Partly Cloudy until tomorrow morning";
    private String iconString = "rain";
    private int iconInt = R.drawable.rain;

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
            Intent intent = new Intent(context,MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);

           views.setOnClickPendingIntent(R.id.widget_layout,pendingIntent);


            setUIElements(views, appWidgetManager, appWidgetId);


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


