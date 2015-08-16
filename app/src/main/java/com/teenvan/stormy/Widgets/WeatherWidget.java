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

                ParseQuery<ParseObject> cwQuery = ParseQuery.getQuery("CurrentWeather");
                cwQuery.fromLocalDatastore();
                cwQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (e == null) {
                            temperature = parseObject.getString("Temperature");
                            // Log.d("Temp widget Parse",temperature);
                            summaryString = parseObject.getString("Summary");
                           // Log.d("Summary widget Parse",summaryString);
                        } else {
                            Log.e("Current Weather Object Retrieval Widget", "Failure", e);
                        }
                    }
                });

            if(isNetworkAvailable(context)) {
                // Get the location name
                try {
                    locationName = getLocation(context);
                    Log.d("Location Name Widget", locationName);
                } catch (IOException e) {
                    Log.e("IO Exception", "Getting the location Name", e);
                }

                forecastURL = forecastBaseURL + ApiKEY + "/" + Double.toString(latitude) + "," +
                        Double.toString(longitude);
                if (!forecastURL.isEmpty()) {
                    setupNetworkConnection(forecastURL, context);
                }
            }

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

    // Get the weather data
    public void setupNetworkConnection(String forecastURL,Context context){
        if(isNetworkAvailable(context)) {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(forecastURL).build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

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
                            summaryString = mCurrentWeather.getSummary();
                            final String datetime = mCurrentWeather.getFormattedTime();
                            Double temp = mCurrentWeather.getTemperature();
                            final int tempF = temp.intValue();
                            final int appTempF = appTemp.intValue();
                            Double tempE = ((appTemp - 32)*5)/9;
                            Double tempD = ((temp - 32)*5)/9;
                            final int tempC = tempD.intValue();
                            final int appTempC = tempE.intValue();
                            final String iconString = mCurrentWeather.getIcon();

                            temperature = tempC +"º";
                            Log.d("Weather widget",temperature);

                            Log.d("Summary Widget",summaryString);

                        } catch (JSONException e) {
                            Log.e("JSON Error","Error",e);
                        }


                    } else {

                    }
                }
            });

        }else{

        }
    }

    public CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
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


        // Create the currentweather object
        CurrentWeather mCurrentWeather = new CurrentWeather();
        mCurrentWeather.setApparentTemperature(apparentTemp);
        mCurrentWeather.setCloudCover(cloudCover);
        mCurrentWeather.setDewPoint(dewPoint);
        mCurrentWeather.setHumidity(humidity);
        mCurrentWeather.setIcon(iconString);
        mCurrentWeather.setTimeZone(timezone);
          mCurrentWeather.setOzone(ozone);
        mCurrentWeather.setPrecipIntensity(precipIntensity);
        mCurrentWeather.setPrecipProbability(precipProbability);
        mCurrentWeather.setTemperature(temperature);
        mCurrentWeather.setTime(time);
        mCurrentWeather.setPressure(pressure);
            mCurrentWeather.setWindBearing(windBearing);
        mCurrentWeather.setWindSpeed(windSpeed);
        mCurrentWeather.setSummary(summary);


        return mCurrentWeather;
    }



    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

}


