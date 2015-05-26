package vintgug.cepnet;


import android.app.Application;

import com.parse.Parse;

public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "6YCw0iv9aqg8ylFIvcypYDiKCl1HB2ULzjec6P8k", "6xJHvrNG3Cj4gxpoV0FHiSaCuiBa5zs2Y9OYup68");
        //Parse.initialize(this, "JtaKlQ5NurWM8FbL9IHNnMmmvq849WObMPnpycZS", "qCreENjPxQSE6BVW6S3gV60ojU0lCisyGudJATqD"); GUG's thing
    }
}