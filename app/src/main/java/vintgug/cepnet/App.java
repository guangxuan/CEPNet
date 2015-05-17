package vintgug.cepnet;

/**
 * Created by GuangXuan on 17/5/2015.
 */

import android.app.Application;

import com.parse.Parse;

public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "JtaKlQ5NurWM8FbL9IHNnMmmvq849WObMPnpycZS", "qCreENjPxQSE6BVW6S3gV60ojU0lCisyGudJATqD");
    }
}