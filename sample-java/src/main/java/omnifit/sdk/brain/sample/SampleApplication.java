package omnifit.sdk.brain.sample;

import android.app.Application;
import android.content.Context;
import omnifit.sdk.brain.OmnifitBrain;
import timber.log.Timber;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        OmnifitBrain.init(this);
    }
}
