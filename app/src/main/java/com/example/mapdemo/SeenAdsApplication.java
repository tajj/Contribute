package com.example.mapdemo;

/**
 * Created by mayajey on 7/17/17.
 */

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;


public class SeenAdsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();


        ParseObject.registerSubclass(Message.class);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        // Can be Level.BASIC, Level.HEADERS, or Level.BODY
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.networkInterceptors().add(httpLoggingInterceptor);

        // set applicationId and server based on the values in the Heroku settings.
        // any network interceptors must be added with the Configuration Builder given this syntax
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("SeenAds") // should correspond to APP_ID env variable
                .clientBuilder(builder)
                .clientKey(null)
                .server("http://seenads.herokuapp.com/parse").build());

        // For testing purposes -- it works!
        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "TEST2");
        testObject.saveInBackground();

    //facebook
//        Parse.initialize(this);
     //   FacebookSdk.sdkInitialize(getApplicationContext());
        ParseFacebookUtils.initialize(this); //supposed to be context..what context

    }
}
