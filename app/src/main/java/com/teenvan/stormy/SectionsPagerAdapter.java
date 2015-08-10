package com.teenvan.stormy;

import java.util.Locale;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import com.teenvan.stormy.fragments.DailyFragment;
import com.teenvan.stormy.fragments.HourlyFragment;
import com.teenvan.stormy.fragments.CurrentFragment;

/**
 * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
// fragment code
public class SectionsPagerAdapter extends FragmentPagerAdapter {

	protected Context mContext;

	public SectionsPagerAdapter(MainActivity context,
			android.support.v4.app.FragmentManager fragmentmanager) {
		// TODO Auto-generated constructor stub
		super(fragmentmanager);
		mContext = context;

	}

	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		// Return a PlaceholderFragment (defined as a static inner class
		// below).
		// Create a switch to switch between the various tabs
		switch (position) {
		case 0:
			return new CurrentFragment();
		case 1:
			return new HourlyFragment();

		case 2:
			return new DailyFragment();

		}
		return null;
	}

	@Override
	public int getCount() {
		// Show 2 total pages.
		return 3;
	}

	// Setting the text on the various tabs
	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
		case 0:
			return mContext.getString(R.string.title_section_1).toUpperCase(l);
		case 1:
			return mContext.getString(R.string.title_section_2).toUpperCase(l);

		}
		return null;
	}



}
