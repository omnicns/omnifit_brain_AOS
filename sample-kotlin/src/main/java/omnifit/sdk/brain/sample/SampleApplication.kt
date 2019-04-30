package omnifit.sdk.brain.sample

import android.app.Application
import omnifit.sdk.brain.OmnifitBrain
import timber.log.Timber

class SampleApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        OmnifitBrain.init(this)
    }
}