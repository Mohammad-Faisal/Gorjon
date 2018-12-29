package candor.fulki;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.FirebaseDatabase;

import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class Fulki extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Fabric.with(this, new Crashlytics());


        if (BuildConfig.DEBUG) {
            Timber.uprootAll();
            Timber.plant(new Timber.DebugTree());
        }
        else Timber.plant(new FirebaseTree());


    }




    private static class FirebaseTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, @NonNull String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }
        }
    }


}
