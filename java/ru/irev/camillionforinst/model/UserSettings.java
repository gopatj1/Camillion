package ru.irev.camillionforinst.model;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UserSettings extends RealmObject{

    @PrimaryKey
    private Integer id;
    private boolean isPaid;
    private float fastModeScale;
    private float slowModeScale;
    private int qualityWidth;
    private int timeLimit;
    private RealmList<Float> formatScaleRealmList = new RealmList<>();
    private boolean previewAndEditPossibility;

    public UserSettings() {
    }

    public UserSettings(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public UserSettings setId(Integer id) {
        this.id = id;
        return this;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public UserSettings setPaid(boolean isPaid) {
        this.isPaid = isPaid;
        return this;
    }

    public float getFastModeScale() {
        return fastModeScale;
    }

    public UserSettings setFastModeScale(float fastModeScale) {
        this.fastModeScale = fastModeScale;
        return this;
    }

    public float getSlowModeScale() {
        return slowModeScale;
    }

    public UserSettings setSlowModeScale(float slowModeScale) {
        this.slowModeScale = slowModeScale;
        return this;
    }

    public int getQualityWidth() {
        return qualityWidth;
    }

    public UserSettings setQualityWidth(int qualityWidth) {
        this.qualityWidth = qualityWidth;
        return this;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public UserSettings setTimeLimit(int timeConstraint) {
        this.timeLimit = timeConstraint;
        return this;
    }

    public RealmList<Float> getFormatScaleRealmList() {
        return formatScaleRealmList;
    }

    public int getFormatScaleRealmListSize() {
        return formatScaleRealmList.size();
    }

    public UserSettings addFormatScale(int index, float formatScale) {
        this.formatScaleRealmList.add(index, formatScale);
        return this;
    }

    public UserSettings addFormatsScale(ArrayList<Float> formats) {
        this.formatScaleRealmList.addAll(formats);
        return this;
    }

    public UserSettings removeFormatScale(float formatScale) {
        if (getFormatScaleRealmListSize() > 1)
            this.formatScaleRealmList.remove(formatScale);
        return this;
    }

    public boolean getPreviewAndEditPossibility() {
        return previewAndEditPossibility;
    }

    public UserSettings setPreviewAndEditPossibility(boolean previewAndEditPossibility) {
        this.previewAndEditPossibility = previewAndEditPossibility;
        return this;
    }
}