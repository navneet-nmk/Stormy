package com.teenvan.stormy;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.SaveCallback;

/**
 * Created by navneet on 11/08/15.
 */
public class StormyApplicationClass extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "AiELXemQnqA6XajYhT4bQt0SMOw0jZK1YzIvGSF5",
                "NPD5Xx3SlEezCstnY8wweJwTLP6UwEokrZJLmLQn");

        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });

    }
}
