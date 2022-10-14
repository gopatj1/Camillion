package ru.irev.camillionforinst.controller;

import java.util.ArrayList;

import io.realm.Realm;
import ru.irev.camillionforinst.model.UserSettings;
import ru.irev.camillionforinst.settings.SettingsPageFragment;

public class UserSettingsController {
    public static UserSettings loadUserSettings(Realm realm) {
        UserSettings settings = realm.where(UserSettings.class).equalTo("id", 0).findFirst();
        if (settings == null) {
            realm.beginTransaction();
            UserSettings userSettings = realm.createObject(UserSettings.class, 0);
            userSettings.setFastModeScale(SettingsPageFragment.FAST_MODE_SCALE_3X);
            userSettings.setSlowModeScale(SettingsPageFragment.SLOW_MODE_SCALE_1_4X);
            userSettings.setQualityWidth(SettingsPageFragment.QUALITY_SD_WIDTH);
            userSettings.setTimeLimit(SettingsPageFragment.TIME_LIMIT_INFINITY);
            userSettings.addFormatsScale(new ArrayList<Float>() {{
                add(SettingsPageFragment.FORMAT_SCALE_1X1);
                add(SettingsPageFragment.FORMAT_SCALE_4X5);
                add(SettingsPageFragment.FORMAT_SCALE_9X16);
                add(SettingsPageFragment.FORMAT_SCALE_16X10);
                add(SettingsPageFragment.FORMAT_SCALE_16X9); }});
            userSettings.setPreviewAndEditPossibility(false);
            realm.copyToRealmOrUpdate(userSettings);
            realm.commitTransaction();
            settings = realm.where(UserSettings.class).equalTo("id", 0).findFirst();
        }
        return settings;
    }

    public static void setUserPaid(Realm realm, boolean isPaid) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setPaid(isPaid);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
        realm.close();
    }

    public static void setFastModeScale(Realm realm, float fastModeScale) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setFastModeScale(fastModeScale);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }

    public static void setSlowModeScale(Realm realm, float slowModeScale) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setSlowModeScale(slowModeScale);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }

    public static void setQualityHeightAndWidth(Realm realm, int qualityWidth) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setQualityWidth(qualityWidth);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }

    public static void setTimeLimit(Realm realm, int timeConstraint) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setTimeLimit(timeConstraint);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }

    public static void addOrDeleteFormatScale(Realm realm, int index, float formatScale) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        if (!settings.getFormatScaleRealmList().contains(formatScale))
            settings.addFormatScale(settings.getFormatScaleRealmListSize() < index ? settings.getFormatScaleRealmListSize() : index , formatScale);
        else settings.removeFormatScale(formatScale);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }

    public static void setPreviewAndEditPossibility(Realm realm, boolean previewAndEditPossibility) {
        UserSettings settings = loadUserSettings(realm);
        realm.beginTransaction();
        settings.setPreviewAndEditPossibility(previewAndEditPossibility);
        realm.copyToRealmOrUpdate(settings);
        realm.commitTransaction();
    }
}
