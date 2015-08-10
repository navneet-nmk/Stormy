/*
 * Copyright 2013 Leon Cheng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teenvan.stormy.com.teenvan.stormy.adapters;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.teenvan.stormy.R;

import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter<String> {
	private final Activity context;
	private final ArrayList<String> temp;
    private final ArrayList<String> datetimeList;
    private final ArrayList<String> summaryList;



	public CustomListAdapter(Activity context, ArrayList<String> temp,
                             ArrayList<String> datetimeList,
                             ArrayList<String> summaryList) {
		super(context, R.layout.drawer_list_item, temp);
		this.context = context;
		this.temp = temp;
        this.datetimeList = datetimeList;
        this.summaryList = summaryList;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.drawer_list_item, null);
			holder = new ViewHolder();
			holder.imageView = (ImageView) convertView
					.findViewById(R.id.weatherTypeImage);
			holder.txtTitle = (TextView) convertView
					.findViewById(R.id.temperatureText);
            holder.summary = (TextView)convertView.findViewById(R.id.summary);
            holder.datetime = (TextView)convertView.findViewById(R.id.datetime);
			convertView.setTag(holder);
		} else {
			// convertView already created
			holder = (ViewHolder) convertView.getTag();

		}
		// Set text value
		holder.txtTitle.setText(temp.get(position));
        holder.summary.setText(summaryList.get(position));
        holder.datetime.setText(datetimeList.get(position));

        if(summaryList.get(position).equals("Drizzle")){
            holder.imageView.setImageResource(R.drawable.drizzle);
        }else if(summaryList.get(position).equals("Light Rain")){
            holder.imageView.setImageResource(R.drawable.light_rain);
        }else if(summaryList.get(position).equals("Mostly Cloudy")){
            holder.imageView.setImageResource(R.drawable.cloudy);
        }

		return convertView;
	}

	private static class ViewHolder {
		ImageView imageView;
		TextView txtTitle,summary,datetime;
	}

}