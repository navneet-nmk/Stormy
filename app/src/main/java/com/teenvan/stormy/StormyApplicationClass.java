package com.teenvan.stormy;

import android.app.Application;

import com.parse.Parse;

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
    }
}
