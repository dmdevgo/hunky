package dmdevgo.hunky.core;

import android.app.Application;

/**
 * @author Dmitriy Gorbunov
 */
public class HunkyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        HunkyManager.getInstance().initialize(this);
    }
}
