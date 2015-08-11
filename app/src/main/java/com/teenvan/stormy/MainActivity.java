package com.teenvan.stormy;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.teenvan.stormy.fragments.CurrentFragment;
import com.teenvan.stormy.fragments.HourlyFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements android.app.ActionBar.TabListener,CurrentFragment.SendLatLong {
    // Declaration of member variables

    SectionsPagerAdapter mSectionAdapter;
    Context mContext;
    ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        final ActionBar bar = getSupportActionBar();
        bar.hide();
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        FragmentManager fragmentmanager = getSupportFragmentManager();
        mSectionAdapter = new SectionsPagerAdapter(this, fragmentmanager);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.calibPager);
        mViewPager.setAdapter(mSectionAdapter);
        mViewPager.setOffscreenPageLimit(5);
        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.



    }



    @Override
    public void onTabSelected(android.app.ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(android.app.ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(android.app.ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }


    @Override
    public void sendLatLong(Double lat, Double longitude) {
//        FragmentPagerAdapter fragmentPagerAdapter = (FragmentPagerAdapter) mViewPager.getAdapter();
//        HourlyFragment hourlyFragment = (HourlyFragment) fragmentPagerAdapter.getItem(1 );
//        hourlyFragment.getLat(lat,longitude);
    }
}
