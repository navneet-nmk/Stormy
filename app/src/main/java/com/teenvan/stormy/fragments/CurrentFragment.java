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

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.teenvan.stormy.CurrentWeather;
import com.teenvan.stormy.MainActivity;
import com.teenvan.stormy.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.crypto.spec.GCMParameterSpec;

import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Place;


public class CurrentFragment extends Fragment {
	// Declaration of member variables
	private TextView mLocation , mDateTime ,
            mTemperature , mApparentTemperature , mSummary ,
            mDewPoint ,mHumidity , mPressure;
    private ImageView mWeatherImage , mDewPointImage , mHumidityImage , mPressureImage,mSearchImage;

    private String ApiKEY = "cc360eb63a145e1a3956ebc14e34a247";
    private String forecastBaseURL = "https://api.forecast.io/forecast/";
    private double latitude = 37.8276;
    private double longitude = -122.423;
    private String forecastURL ;
    private CurrentWeather mCurrentWeather;
    private String googleApiKey = "AIzaSyA4FeAiG_BiDXP-ooY7ec3Ha-jtqQoT9vE";
    private GooglePlaces client = new GooglePlaces(googleApiKey);
    private EditText mLocationET;

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
                        //GetResults res = new GetResults();
                        //res.execute(query);

                        }else{
                            Toast.makeText(getActivity(),"Please enter a valid location",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

        // Getting the JSON data
        forecastURL = forecastBaseURL + ApiKEY + "/" + Double.toString(latitude) + "," +
                Double.toString(longitude);
        Log.d(getString(R.string.forecast_api_url),forecastURL);

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
                            Double humidity = mCurrentWeather.getHumidity()*100;
                            final int humidityLevel = humidity.intValue();
                            final Double dewPoint = mCurrentWeather.getDewPoint();
                            final Double pressure = mCurrentWeather.getPressure();
                            Double appTemp = mCurrentWeather.getApparentTemperature();
                            final String summary = mCurrentWeather.getSummary();
                            final String datetime = mCurrentWeather.getFormattedTime();
                            Double temp = mCurrentWeather.getTemperature();
                            final int tempF = temp.intValue();
                            final int appTempF = appTemp.intValue();
                            Double tempE = ((appTemp - 32)*5)/9;
                            Double tempD = ((temp - 32)*5)/9;
                            final int tempC = tempD.intValue();
                            final int appTempC = tempE.intValue();
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


		return rootView;

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 1){
            LocationManager locationManager = (LocationManager)getActivity().
                    getSystemService(Context.LOCATION_SERVICE);
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.
                        requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Log.d("Location Coordinates", Double.toString(latitude) + " " +
                                        Double.toString(longitude));
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
                locationManager.
                        requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                Log.d("Location Coordinates", Double.toString(latitude) + " " +
                                        Double.toString(longitude));
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
        }
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
    private class GetResults extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... strings) {
            List<Place> places = client.
                    getPlacesByQuery(strings[0], GooglePlaces.MAXIMUM_RESULTS);
            return places.get(0).getName();
        }
    }

    protected void onPostExecute(String result) {
        // execution of result of Long time consuming operation
        mLocationET.setVisibility(View.INVISIBLE);
        mSearchImage.setVisibility(View.INVISIBLE);
        mLocation.setVisibility(View.VISIBLE);
        mLocation.setText(result);
    }
}
