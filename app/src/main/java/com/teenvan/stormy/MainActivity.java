package com.teenvan.stormy;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.teenvan.stormy.com.teenvan.stormy.adapters.SectionsPagerAdapter;
import com.teenvan.stormy.fragments.CurrentFragment;
import com.teenvan.stormy.fragments.DailyFragment;
import com.teenvan.stormy.fragments.HourlyFragment;
import com.teenvan.stormy.services.WeatherService;


public class MainActivity extends ActionBarActivity implements android.app.ActionBar.TabListener,
        CurrentFragment.SendLatLong {
    // Declaration of member variables

    SectionsPagerAdapter mSectionAdapter;
    Context mContext;
    ViewPager mViewPager;
    private AdView ad;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Saving a Parse installation
        ParseInstallation ints = ParseInstallation.getCurrentInstallation();
        ints.put("User", ParseUser.getCurrentUser().getUsername());
        ints.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // SUCCESS
                    Log.d("MainActivity", "Installation saved successfully");
                } else {
                    // FAILURE
                    Log.e("MainActivity", "Installation saving failed", e);
                }
            }
        });

        // Referencing the UI elements
        ad = (AdView)findViewById(R.id.ad);
        // AdRequest request = new AdRequest.Builder().addTestDevice().build();
        AdRequest request = new AdRequest.Builder().
                addTestDevice("AA967D1FB57ACA93CF35762D3CEA8762").build();
        ad.loadAd(request);

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
    public void onTabSelected(android.app.ActionBar.Tab tab,
                              FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(android.app.ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(android.app.ActionBar.Tab tab,
                                FragmentTransaction fragmentTransaction) {

    }


    @Override
    public void sendLatLong(Double lat, Double longitude) {
        HourlyFragment hourlyFragment = (HourlyFragment) mSectionAdapter.getRegisteredFragment(1);
        DailyFragment dailyFragment = (DailyFragment)mSectionAdapter.getRegisteredFragment(2);
        if(hourlyFragment !=null) {
            hourlyFragment.updateListView(lat, longitude);
        }if(dailyFragment != null){
            dailyFragment.updateListView(lat,longitude);
        }
    }

    @Override
    public void updateData() {
        HourlyFragment hourlyFragment = (HourlyFragment)mSectionAdapter.getRegisteredFragment(1);
        DailyFragment dailyFragment = (DailyFragment)mSectionAdapter.getRegisteredFragment(2);
        if(hourlyFragment != null){
         hourlyFragment.update();
        }if(dailyFragment != null){
            dailyFragment.update();
        }
    }


}
