package com.teenvan.stormy.Widgets;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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
import com.teenvan.stormy.services.WeatherService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class WidgetLocationActivity extends ActionBarActivity {

    // Declaration of member variables
    private EditText mLocationEditText;
    private ImageView mCurrentLoc , mSearchImage;
    private AdView mAdView;
    private Double longitude , latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_location);

        // Hide the action bar
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        if(bar != null){
            // Hide the bar
            bar.hide();
        }

        // Referencing the UI elements
        mAdView = (AdView)findViewById(R.id.widgetAd);
        mLocationEditText = (EditText)findViewById(R.id.locationEdiTextWidget);
        mCurrentLoc = (ImageView)findViewById(R.id.currentLocationImageWidget);
        mSearchImage = (ImageView)findViewById(R.id.searchImageWidget);

        // Setup the advertisement
        AdRequest request = new AdRequest.Builder().
                addTestDevice("AA967D1FB57ACA93CF35762D3CEA8762").build();
        mAdView.loadAd(request);


        // Getting the required data
        mSearchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String location = mLocationEditText.getText().toString();

                try {
                    Double lat = getLat(location);
                    Double longi = getLong(location);
                    // Send this data to the service
                    Intent intent = new Intent(WidgetLocationActivity.this, WeatherService.class);
                    intent.putExtra("Latitude",lat);
                    intent.putExtra("Longitude",longi);
                    startService(intent);
                    Log.d("WidgetLocation",lat+" "+longi);
                } catch (IOException e) {
                    Log.e("Widget Location","Error",e);
                }

            }
        });

        // Getting the current location
        mCurrentLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current location
                LocationManager locationManager = (LocationManager)
                        getSystemService(Context.LOCATION_SERVICE);
                Location location = locationManager.
                        getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if(location != null){
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
                Location gpsLocation = locationManager.
                        getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(gpsLocation != null){
                    latitude = gpsLocation.getLatitude();
                    longitude = gpsLocation.getLongitude();
                }

                Intent intent = new Intent(WidgetLocationActivity.this,WeatherService.class);
                intent.putExtra("Latitude",latitude);
                intent.putExtra("Longitude",longitude);
                startService(intent);
                finish();
            }
        });

    }



    private String getLocationFromQuery(String query) throws IOException {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        addresses = geocoder.getFromLocationName(query,1);
        String cityName = addresses.get(0).getAddressLine(0);

        return cityName;
    }

    private Double getLat(String query) throws IOException {
        Geocoder geocoder = new Geocoder(this,Locale.getDefault());
        List<Address> addresses = null;
        addresses = geocoder.getFromLocationName(query,1);
        String cityName = addresses.get(0).getAddressLine(0);
        Double lat = addresses.get(0).getLatitude();
        return lat;
    }
    private Double getLong(String query) throws IOException {
        Geocoder geocoder = new Geocoder(this,Locale.getDefault());
        List<Address> addresses = null;
        addresses = geocoder.getFromLocationName(query,1);
        String cityName = addresses.get(0).getAddressLine(0);
        Double longi = addresses.get(0).getLongitude();
        return longi;
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)this.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }
}
