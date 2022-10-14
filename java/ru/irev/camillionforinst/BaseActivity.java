package ru.irev.camillionforinst;

import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.google.firebase.analytics.FirebaseAnalytics;

import io.realm.Realm;
import ru.irev.camillionforinst.controller.UserSettingsController;
import ru.irev.camillionforinst.model.UserSettings;

public abstract class BaseActivity extends AppCompatActivity {

    public Realm realm;
    public UserSettings userSettings;
    public FirebaseAnalytics mFirebaseAnalytics;
    public int displayHeight;
    public int displayWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        userSettings = UserSettingsController.loadUserSettings(realm);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        displayHeight = getResources().getDisplayMetrics().heightPixels;
        displayWidth = getResources().getDisplayMetrics().widthPixels;

        setVolumeControlStream(AudioManager.STREAM_MUSIC); // change volume in app by system buttons
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // wake lock alternative
    }

    @Override
    protected void onResume() {
        super.onResume();
        setImmersiveMode(); // hide system buttons of device, which locate on screen area
        analyticsOpenScreen(getScreenName());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setImmersiveMode(); // hide system buttons of device, which locate on screen area
        }
    }

    protected void setImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public void analyticsOpenScreen(String screenName) {
        if (screenName == null) return;
        mFirebaseAnalytics.setCurrentScreen(this, screenName, null);
    }

    public void analyticsLogEvent(String action) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, action);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userSettings.removeAllChangeListeners();
        realm.removeAllChangeListeners();
        realm.close();
    }

    public abstract String getScreenName();
}