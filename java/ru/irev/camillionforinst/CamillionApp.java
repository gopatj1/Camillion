package ru.irev.camillionforinst;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import static ru.irev.camillionforinst.utils.Constants.CAMILLION_TAG;

public class CamillionApp extends MultiDexApplication {

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    public static Context getAppContext() {
        return sContext;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        MultiDex.install(newBase);
        super.attachBaseContext(newBase);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        deleteCash(); // clean cash for correct work ffmpeg4 without license

        // initialize realm database. Do migration if needed
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .schemaVersion(0)
//                .migration(new Migration())
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    private void deleteCash() {
        File cacheDirectory = getCacheDir();
        File applicationDirectory = new File(cacheDirectory.getParent());
        if (applicationDirectory.exists()) {
            String[] fileNames = applicationDirectory.list();
            for (String fileName : fileNames) {
                File file = new File(applicationDirectory, fileName);
                if (fileName.equals("ffmpeglicense.lic")) {
                    if (file.delete())
                        Log.d(CAMILLION_TAG, "Licence file " + file.getAbsolutePath() + "/" + fileName + " is successful delete");
                    else Log.d(CAMILLION_TAG, "Licence file " + file.getAbsolutePath() + "/" + fileName + " deleting error!!!");
                }
                if (file.isDirectory()) {
                    for (String childrenFile : file.list()) {
                        if (childrenFile.equals("ffmpeglicense.lic")) {
                            if (new File(file, childrenFile).delete())
                                Log.d(CAMILLION_TAG, "Licence file " + file.getAbsolutePath() + "/" + childrenFile + " is successful delete");
                            else Log.d(CAMILLION_TAG, "Licence file " + file.getAbsolutePath() + "/" + childrenFile + " deleting error!!!");
                        }
                    }
                }
            }
        }
    }

}
