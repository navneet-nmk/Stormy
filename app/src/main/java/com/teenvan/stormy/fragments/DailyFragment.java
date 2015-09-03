package com.teenvan.stormy.fragments;

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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.Window;
import android.widget.AdapterView;
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
    private ArrayList<String> temperatures,summaries,datetimes,iconList,
            precipProbs,dewPoints,pressures,humidities,winds;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_activity, container,
				false);

        //Referencing the UI elements
        mDailyForecastText = (TextView)rootView.findViewById(R.id.dailyforecastText);
        mDailyList = (ListView)rootView.findViewById(R.id.dailyList);

        // Get the data from the Parse Local datastore
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("DailyForecast");
        query.fromLocalDatastore();
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if( e== null){
                    temperatures = (ArrayList<String>) parseObject.get("Temperatures");
                    summaries = (ArrayList<String>) parseObject.get("Summaries");
                    datetimes = (ArrayList<String>) parseObject.get("DateTimes");
                    dewPoints = (ArrayList<String>) parseObject.get("DewPoints");
                    humidities = (ArrayList<String>) parseObject.get("Humidities");
                    winds = (ArrayList<String>) parseObject.get("Winds");
                    precipProbs = (ArrayList<String>) parseObject.get("PrecipProbs");
                    pressures = (ArrayList<String>) parseObject.get("Pressures");
                    iconList = (ArrayList<String>) parseObject.get("Icons");
                    CustomListAdapter adapter = new CustomListAdapter(getActivity(),
                            temperatures
                            ,datetimes,summaries,iconList);
                    mDailyList.setAdapter(adapter);
                }else{
                    Toast.makeText(getActivity(),"No data available!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


            ParseQuery<ParseObject> summquery = new ParseQuery<ParseObject>("SummaryDaily");
            summquery.fromLocalDatastore();
            summquery.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if( e== null){
                        // Success
                        mDailyForecastText.setText(parseObject.getString("Summary"));
                    }else{
                        Log.e("Summary Daily Object Retrieval","Failure",e);
                    }
                }
            });



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
                    Log.e("DailyFragment Location Object Retrieval","Failure",e);

                }
            }
        });

        // On Item click listener
        mDailyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Dialog d = new Dialog(getActivity());
                d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                d.setContentView(R.layout.dialog);
                TextView title =  (TextView)d.findViewById(R.id.titleText);
                TextView tempD = (TextView)d.findViewById(R.id.tempDText);
                TextView precipD = (TextView)d.findViewById(R.id.precipProbabilityText);
                TextView wind = (TextView)d.findViewById(R.id.windText);
                TextView humid = (TextView)d.findViewById(R.id.humidityDText);
                TextView pressure = (TextView)d.findViewById(R.id.pressureDText);
                TextView dew = (TextView)d.findViewById(R.id.dewPointDtext);
                TextView ok = (TextView)d.findViewById(R.id.okText);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Dismiss the dialog
                        d.dismiss();
                    }
                });
                // Get the data for the dialog
                if(!temperatures.isEmpty() && !datetimes.isEmpty() && !humidities.isEmpty()
                        && !dewPoints.isEmpty() && !pressures.isEmpty()
                        && !winds.isEmpty() && !precipProbs.isEmpty()){
                    tempD.setText("Temperature: "+temperatures.get(i));
                        precipD.setText("Precip. Probability: "+precipProbs.get(i));
                    title.setText(datetimes.get(i));
                    wind.setText("Wind: "+winds.get(i));
                    humid.setText("Humidity: "+humidities.get(i) +"%");
                    pressure.setText("Pressure: "+pressures.get(i));
                    dew.setText("Dew Point: "+ dewPoints.get(i));
                }
                // Show the dialog
                d.show();

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
                    Log.e(getString(R.string.forecast_service),
                            getString(R.string.error_string), e);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    // Do something with the response
                    if (response.isSuccessful()) {
                        String jsonData = response.body().string();
                        try {
                            ArrayList<CurrentWeather> mCurrentWeatherArray =
                                    getCurrentDetails(jsonData);
                            temperatures = new ArrayList<String>();
                            summaries = new ArrayList<String>();
                            datetimes = new ArrayList<String>();
                            iconList = new ArrayList<String>();
                            humidities = new ArrayList<String>();
                            pressures =new ArrayList<String>();
                            dewPoints = new ArrayList<String>();
                            winds = new ArrayList<String>();
                            precipProbs =  new ArrayList<String>();
                            for(int i=0;i<mCurrentWeatherArray.size();i++){
                                CurrentWeather mCW = mCurrentWeatherArray.get(i);
                                Double tempF = mCW.getTemperature();
                                int tempC = convertToC(tempF);
                                String temp = Integer.toString(tempC)+"º";
                                String datetime = mCW.getFormattedTime();
                                String day = mCW.getDayOfTheWeek(mCW.getTime());
                                String summary = mCW.getSummary();
                                String iconString = mCW.getIcon();
                                String humidity = Double.toString(mCW.getHumidity()*100);
                                Double dewPointF = mCW.getDewPoint();
                                int dewPointC = convertToC(dewPointF);
                                String dewPoint = Integer.toString(dewPointC)+"º";
                                String pressure = Double.toString(mCW.getPressure());
                                String wind = Double.toString(mCW.getWindSpeed());
                                String precip = Double.toString(mCW.getPrecipProbability());
                                humidities.add(humidity);
                                dewPoints.add(dewPoint);
                                pressures.add(pressure);
                                temperatures.add(temp);
                                summaries.add(summary);
                                datetimes.add(day);
                                winds.add(wind);
                                precipProbs.add(precip);
                                iconList.add(iconString);
                            }

                            // Create a parse object of Daily Forecast
                            ParseQuery<ParseObject> dailyQuery = new
                                    ParseQuery<ParseObject>("DailyForecast");
                            dailyQuery.fromLocalDatastore();
                            dailyQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    if( e== null){
                                        // Parse Object found
                                        // Update it with the new values
                                        parseObject.put("Temperatures",temperatures);
                                        parseObject.put("Humidities",humidities);
                                        parseObject.put("DewPoints",dewPoints);
                                        parseObject.put("Pressures",pressures);
                                        parseObject.put("Summaries",summaries);
                                        parseObject.put("DateTimes",datetimes);
                                        parseObject.put("Winds",winds);
                                        parseObject.put("PrecipProbs",precipProbs);
                                        parseObject.put("Icons",iconList);
                                        parseObject.pinInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if( e== null){
                                                    Log.d("Daily Object Updation","Success");
                                                }else{
                                                    Log.e("Daily Object Updation","Failure",
                                                    e);
                                                }
                                            }
                                        });
                                    }else{
                                        // Create a new Parse Object
                                        ParseObject dailyObject = new ParseObject("DailyForecast");
                                        dailyObject.put("Temperatures",temperatures);
                                        dailyObject.put("Humidities",humidities);
                                        dailyObject.put("DewPoints",dewPoints);
                                        dailyObject.put("Pressures",pressures);
                                        dailyObject.put("Summaries",summaries);
                                        dailyObject.put("DateTimes",datetimes);
                                        dailyObject.put("Winds",winds);
                                        dailyObject.put("PrecipProbs",precipProbs);
                                        dailyObject.put("Icons",iconList);
                                        dailyObject.pinInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if( e==null){
                                                    Log.d("Daily Object Saving","Success");
                                                }else{
                                                    Log.e("Daily Object Saving","Failure",e);
                                                }
                                            }
                                        });

                                    }
                                }
                            });

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    CustomListAdapter adapter = new CustomListAdapter(getActivity(),
                                            temperatures,datetimes,summaries,iconList);
                                    mDailyList.setAdapter(adapter);
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    } else {

                    }
                }
            });

        }else{

            // Get the data from the Parse Local datastore
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("DailyForecast");
            query.fromLocalDatastore();
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if( e== null){
                        temperatures = (ArrayList<String>) parseObject.get("Temperatures");
                        summaries = (ArrayList<String>) parseObject.get("Summaries");
                        datetimes = (ArrayList<String>) parseObject.get("DateTimes");
                        dewPoints = (ArrayList<String>) parseObject.get("DewPoints");
                        humidities = (ArrayList<String>) parseObject.get("Humidities");
                        winds = (ArrayList<String>) parseObject.get("Winds");
                        precipProbs = (ArrayList<String>) parseObject.get("PrecipProbs");
                        pressures = (ArrayList<String>) parseObject.get("Pressures");
                        iconList = (ArrayList<String>) parseObject.get("Icons");
                        CustomListAdapter adapter = new CustomListAdapter(getActivity(),
                                temperatures
                        ,datetimes,summaries,iconList);
                        mDailyList.setAdapter(adapter);
                    }else{
                        Toast.makeText(getActivity(),"No data available!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
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

        // Create parse object if it does'nt exist
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("SummaryDaily");
        query.fromLocalDatastore();
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if( e== null){
                    // Object found
                    // Update the object
                    parseObject.put("Summary",summary);
                    parseObject.pinInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if( e== null){
                                Log.d("Summary Daily Updation","Success");
                            }else{
                                Log.e("Summary Daily Updation","Failure",e);
                            }
                        }
                    });
                }else{
                    // Create a new Parse Object
                    ParseObject object = new ParseObject("SummaryDaily");
                    object.put("Summary",summary);
                    object.pinInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if( e== null){
                                Log.d("Summary Daily Creation","Success");
                            }else{
                                Log.e("Summary Daily Creation","Failure",e);
                            }
                        }
                    });
                }
            }
        });

        JSONArray data = hourlyForecast.getJSONArray("data");
        ArrayList<CurrentWeather> mArray = new ArrayList<CurrentWeather>();
        for( int i=0;i<6;i++){
            JSONObject currentForecast = data.getJSONObject(i);
            long time = currentForecast.getLong("time");
            String summaryHr = currentForecast.getString("summary");
            String icon = currentForecast.getString("icon");
            int precipIntensity = currentForecast.getInt("precipIntensity");
            int precipProbability = currentForecast.getInt("precipProbability");
            Double temperature = currentForecast.getDouble("temperatureMax");
            Double dewPoint = currentForecast.getDouble("dewPoint");
            Double apparentTemp = currentForecast.getDouble("apparentTemperatureMax");
            Double humidity = currentForecast.getDouble("humidity");
            Double windSpeed = currentForecast.getDouble("windSpeed");
            int windBearing = currentForecast.getInt("windBearing");
            Double cloudCover = currentForecast.getDouble("cloudCover");
            Double pressure = currentForecast.getDouble("pressure");
            Double ozone = currentForecast.getDouble("ozone");
            // Create the currentweather object
            CurrentWeather mCurrentWeather = new CurrentWeather();
            mCurrentWeather.setApparentTemperature(apparentTemp);
            mCurrentWeather.setCloudCover(cloudCover);
            mCurrentWeather.setDewPoint(dewPoint);
            mCurrentWeather.setHumidity(humidity);
            mCurrentWeather.setIcon(icon);
            mCurrentWeather.setTimeZone(timezone);
            mCurrentWeather.setOzone(ozone);
            mCurrentWeather.setPrecipIntensity(precipIntensity);
            mCurrentWeather.setPrecipProbability(precipProbability);
            mCurrentWeather.setTemperature(temperature);
            mCurrentWeather.setTime(time);
            mCurrentWeather.setPressure(pressure);
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


    public int convertToC(Double fahren){
        Double tempD = ((fahren - 32)*5)/9;
        int temp = tempD.intValue();
        return temp;
    }

    public void update(){
        setupDailyNetworkConnection(forecastURL);
    }

}
