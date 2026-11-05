package cat.cims.tracker

import android.app.Application
import cat.cims.tracker.avisos.Avisos

class TrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Avisos.creaCanal(this)
        Avisos.programa(this)
    }
}
