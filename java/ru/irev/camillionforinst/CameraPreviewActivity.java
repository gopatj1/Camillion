package ru.irev.camillionforinst;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netcompss.loader.LoadJNI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.irev.camillionforinst.settings.SettingsActivity;
import ru.irev.camillionforinst.utils.DialogUtils;

import static ru.irev.camillionforinst.settings.SettingsPageFragment.FORMAT_SCALE_16X10;
import static ru.irev.camillionforinst.settings.SettingsPageFragment.FORMAT_SCALE_16X9;
import static ru.irev.camillionforinst.settings.SettingsPageFragment.FORMAT_SCALE_1X1;
import static ru.irev.camillionforinst.settings.SettingsPageFragment.FORMAT_SCALE_4X5;
import static ru.irev.camillionforinst.settings.SettingsPageFragment.FORMAT_SCALE_9X16;
import static ru.irev.camillionforinst.utils.Constants.ARMEABI_V7A;
import static ru.irev.camillionforinst.utils.Constants.CAMILLION_TAG;

public class CameraPreviewActivity extends BaseActivity implements View.OnTouchListener {

    @BindView(R.id.surfaceView) SurfaceView surfaceView; // view for camera preview
    private SurfaceHolder holder;
    private Camera camera;
    private Camera.Parameters camParams;
    private MediaRecorder mediaRecorder;
    private LoadJNI vk; // ffmpeg4android library

    private File videosDir; // main directory 'Camillion'
    private File videoFragment; // video fragment from start to pause
    private File videosDataDir; // directory of video fragments and txt file
    private File dataTextFile; // txt file, which contain paths of video fragments
    private File videoFinalFile; // final video file from concatenate video fragment, set crop and speed

    @BindView(R.id.topPanelRelativeLay) RelativeLayout topPanelRelativeLay;
    @BindView(R.id.btnFlash) ImageView btnFlash;

    @BindView(R.id.btnSpeedVideo) Button btnSpeedVideo;
    private int indexOfSpeedVideo = 0;
    private float speedScale = 1;
    private static final float NORMAL_SPEED_SCALE = (float) 1;

    @BindView(R.id.btnMic) ImageView btnMic;
    private boolean micIsOn = true;

    @BindView(R.id.btnFormatVideo) Button btnFormatVideo;
    private int indexOfFormatVideo = 0;
    private float formatScale = 1;
    private int resolutionWidth; // for set quality in ffmpeg command

    @BindView(R.id.btnTypeCam) ImageView btnTypeCam;
    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    @BindView(R.id.redLineHideCameraPreview) ImageView redLineHideCameraPreview;
    @BindView(R.id.imageHideCameraPreview) ImageView imgHideCamPreview;
    @BindView(R.id.btnAndImgVideoRecorderFrameLay) FrameLayout btnAndImgVideoRecorderFrameLay;
    @BindView(R.id.imgCamillionPauseSpeed) ImageView imgCamillionPauseSpeed;
    @BindView(R.id.btnVideoRecorder) Button btnVideoRecorder;
    private float lastTouchY = -1; // y coordinate for zoom
    private int zoom = 0; // zoom value
    private float actionDownY = 0F;

    @BindView(R.id.cancelSaveLinLay) LinearLayout cancelSaveLinLay;
    @BindView(R.id.redText15or60seconds) TextView redText15or60seconds;
    @BindView(R.id.textZoomVideo) TextView textZoomVideo;
    @BindView(R.id.textSpeedVideo) TextView textSpeedVideo;
    @BindView(R.id.btnPlayLastVideo) ImageView btnPlayLastVideo;
    @BindView(R.id.btnSettings) ImageView btnSettings;

    @BindView(R.id.pointRed) ImageView pointRed;
    @BindView(R.id.pointOrange) ImageView pointOrange;
    @BindView(R.id.chronometerTimerVideoRecord) Chronometer chronometerTimerVideoRecord;
    private float chronometerCurrentTime = 0;
    private long chronometerFullTime = 0;
    @BindView(R.id.chronometerTimerSizeOfMemory) Chronometer chronometerTimerSizeOfMemory;
    private long chronometerFullTimeSizeOfMemory = 0;

    @BindView(R.id.waitRelLay) RelativeLayout waitRelLay;
    @BindView(R.id.waitText) TextView waitText;
    @BindView(R.id.waitPercent) TextView waitPercent;
    @BindView(R.id.waitAnimPicture1) ImageView waitAnimPicture1;
    @BindView(R.id.waitAnimPicture2) ImageView waitAnimPicture2;
    @BindView(R.id.waitAnimPicture3) ImageView waitAnimPicture3;
    @BindView(R.id.waitAnimPicture4) ImageView waitAnimPicture4;

    @SuppressLint({"ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);
        ButterKnife.bind(this);
        // load ffmpeg4android library for editing videos.
        // Some devices don't support ffmpeg4android library (emulator devices), because by default supported only "armeabi-v7a" architecture
        // For using architectures "armeabi", "x86", "mips" copy jniLibs to special dirs and set property in build.gradle -> default config -> ndk.
        if (Build.CPU_ABI.equals(ARMEABI_V7A)) {
            vk = new LoadJNI();
        } else {
            vk = null;
            DialogUtils.showNotSupportAllFunctionsDialog(this, buttonId -> {
                switch (buttonId) {
                    case 1:
                        break;
                    case 2:
                        finish();
                        System.exit(0);
                        break;
                }
            });
        }

        // create directory for save final video, video fragments and create txt file
        videosDir = new File(Environment.getExternalStorageDirectory(),"/Camillion/");
        videosDataDir = new File(videosDir, "/data/");
        if (!videosDir.exists()) if (videosDir.mkdir()) Log.d(CAMILLION_TAG, "Create videos dir" + videosDir);
        if (!videosDataDir.exists()) if (videosDataDir.mkdirs()) Log.d(CAMILLION_TAG, "Create videos data dir" + videosDataDir);
        deleteAllDataFiles(); // delete old files, if app crashed
        dataTextFile = new File(videosDataDir, "myDataVideoPath.txt");

        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK); // open back camera like default
        camParams = camera.getParameters(); // camera params
        cancelSaveLinLay.setVisibility(View.GONE);
        textZoomVideo.setVisibility(View.GONE);
        textSpeedVideo.setVisibility(View.GONE);
        redText15or60seconds.setVisibility(View.GONE);
        redLineHideCameraPreview.setVisibility(View.INVISIBLE);

        // set margin for image, which hide part of camera preview. Set 1x1 video format
        btnFormatVideo.setText(Objects.requireNonNull(userSettings.getFormatScaleRealmList().first()) == FORMAT_SCALE_1X1 ? getResources().getString(R.string.format_1x1)
                : Objects.requireNonNull(userSettings.getFormatScaleRealmList().first()) == FORMAT_SCALE_4X5 ? getResources().getString(R.string.format_4x5)
                : Objects.requireNonNull(userSettings.getFormatScaleRealmList().first()) == FORMAT_SCALE_9X16 ? getResources().getString(R.string.format_9x16)
                : Objects.requireNonNull(userSettings.getFormatScaleRealmList().first()) == FORMAT_SCALE_16X10 ? getResources().getString(R.string.format_16x10)
                : getResources().getString(R.string.format_16x9));
        formatScale = Objects.requireNonNull(userSettings.getFormatScaleRealmList().first());
        resolutionWidth = userSettings.getQualityWidth();
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) imgHideCamPreview.getLayoutParams();
        FrameLayout.LayoutParams lpRedLine = (FrameLayout.LayoutParams) redLineHideCameraPreview.getLayoutParams();
        lp.setMargins(0, displayWidth + getResources().getDimensionPixelSize(R.dimen.top_panel_height_size_flash_mic_cam), 0, 0);
        lpRedLine.setMargins(0, lp.topMargin, 0, 0);
        imgHideCamPreview.setLayoutParams(lp);
        redLineHideCameraPreview.setLayoutParams(lpRedLine);
        redLineHideCameraPreview.startAnimation(AnimationUtils.loadAnimation(this, R.anim.red_line_anim));

        // silent click for correct initialize default video format
        indexOfFormatVideo = userSettings.getFormatScaleRealmListSize();
        formatScale = Objects.requireNonNull(userSettings.getFormatScaleRealmList().last());
        onClickFormatVideo(new View(this));


        // set size of video record button
        btnVideoRecorder.setOnTouchListener(this);
        FrameLayout.LayoutParams btnAndImgVideoRecordLp = (FrameLayout.LayoutParams) btnAndImgVideoRecorderFrameLay.getLayoutParams();
        btnAndImgVideoRecordLp.width = displayWidth;
        btnAndImgVideoRecordLp.height = btnAndImgVideoRecordLp.width;
        btnAndImgVideoRecorderFrameLay.setLayoutParams(btnAndImgVideoRecordLp);

        // camera preview
        holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                camera.setDisplayOrientation(90);
                try {
                    camera.setPreviewDisplay(holder);
                    camera.enableShutterSound(true);
                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });

        // video record timer and available size of memory timer
        chronometerTimerVideoRecord.setOnChronometerTickListener(t -> {
            chronometerCurrentTime = (SystemClock.elapsedRealtime() - t.getBase() + chronometerFullTime) * speedScale;
            t.setText(DateFormat.format("00:mm:ss", (long) (chronometerCurrentTime)));
            redText15or60seconds.setText(String.valueOf((int) (chronometerCurrentTime / 1000)));
            if ((chronometerCurrentTime > 12000 && chronometerCurrentTime < 16000) ||
                    (chronometerCurrentTime > 57000 && chronometerCurrentTime < 61000))
                redText15or60seconds.setVisibility(View.VISIBLE);
            else redText15or60seconds.setVisibility(View.GONE);
            // break recording by settings time limit
            if (chronometerCurrentTime > userSettings.getTimeLimit() * 1000) motionEventActionCancel(new View(this));
        });
        initChronometerFullTimeSizeOfMemory();
        chronometerTimerSizeOfMemory.setOnChronometerTickListener(t -> t.setText(DateFormat.format("hh:mm:ss",
                (long) ((chronometerFullTimeSizeOfMemory - (SystemClock.elapsedRealtime() - t.getBase())) * speedScale))));

        // animation of recording and waiting process
        pointRed.startAnimation(AnimationUtils.loadAnimation(this, R.anim.point_red_anim));
        waitAnimPicture1.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_wait_picture1_anim));
        waitAnimPicture2.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_wait_picture2_3_anim));
        waitAnimPicture3.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_wait_picture2_3_anim));
        waitAnimPicture4.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_wait_picture4_anim));

        // set image for play last video button
        if (videosDir.listFiles().length > 1)
            btnPlayLastVideo.setImageBitmap(ThumbnailUtils.createVideoThumbnail(videosDir.listFiles()[videosDir.listFiles().length - 1]
                        .getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND));
    }

    public void onClickFlashMode(View view) {
        if (camParams.getFlashMode() == null || currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            DialogUtils.alert(this, getResources().getString(R.string.flash_light_error));
            return;
        }
        if (camParams.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
            btnFlash.setImageDrawable(getResources().getDrawable(R.drawable.flash_on));
            camParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(camParams);
            return;
        }
        if (camParams.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
            btnFlash.setImageDrawable(getResources().getDrawable(R.drawable.flash_off));
            camParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(camParams);
        }
        analyticsLogEvent(camParams.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF) ?
                "Выключение вспышки" : "Включение вспышки");
    }

    @SuppressLint("SetTextI18n")
    public void onClickSpeedVideo(View view) {
        indexOfSpeedVideo++;
        if (indexOfSpeedVideo > 2) indexOfSpeedVideo = 0;

        switch (indexOfSpeedVideo) {
            case 0:
                btnSpeedVideo.setText(getResources().getString(R.string.speed_normal));
                textSpeedVideo.setText(getResources().getString(R.string.speed_normal));
                pointRed.setImageDrawable(getResources().getDrawable(R.drawable.point_red_normal));
                pointOrange.setImageDrawable(getResources().getDrawable(R.drawable.point_orange_normal));
                speedScale = NORMAL_SPEED_SCALE;
                if (!micIsOn) onClickMicMode(view); // turn on the microphone
                break;
            case 1:
                btnSpeedVideo.setText(getResources().getString(R.string.speed_fast));
                textSpeedVideo.setText(getResources().getString(R.string.speed_fast) + " " + (int) (1 / userSettings.getFastModeScale()) + "x");
                pointRed.setImageDrawable(getResources().getDrawable(R.drawable.point_red_fast));
                pointOrange.setImageDrawable(getResources().getDrawable(R.drawable.point_orange_fast));
                speedScale = userSettings.getFastModeScale();
                if (micIsOn) onClickMicMode(view); // turn of the microphone
                break;
            case 2:
                btnSpeedVideo.setText(getResources().getString(R.string.speed_slow));
                textSpeedVideo.setText(getResources().getString(R.string.speed_slow) + " " + (int) userSettings.getSlowModeScale() + "x");
                pointRed.setImageDrawable(getResources().getDrawable(R.drawable.point_red_slow));
                pointOrange.setImageDrawable(getResources().getDrawable(R.drawable.point_orange_slow));
                speedScale = userSettings.getSlowModeScale();
                if (micIsOn) onClickMicMode(view); // turn of the microphone
                break;
        }
        analyticsLogEvent("Скорость съемки " + btnSpeedVideo.getText().toString());
    }

    public void onClickMicMode(View view) {
        if (micIsOn) {
            btnMic.setImageDrawable(getResources().getDrawable(R.drawable.mic_off));
            micIsOn = false;
        } else {
            if (speedScale != NORMAL_SPEED_SCALE) {
                DialogUtils.alert(this, getResources().getString(R.string.cant_use_mic));
                return; // we can't turn on mic, if current sped type is slow or fast
            }
            btnMic.setImageDrawable(getResources().getDrawable(R.drawable.mic_on));
            micIsOn = true;
        }
        analyticsLogEvent(micIsOn ? "Включение микрофона" : "Выключение микрофона");
    }

    public void onClickFormatVideo(View view) {
        indexOfFormatVideo++;
        if (indexOfFormatVideo > userSettings.getFormatScaleRealmListSize() - 1) indexOfFormatVideo = 0;
        if (formatScale == Objects.requireNonNull(userSettings.getFormatScaleRealmList().get(indexOfFormatVideo))
                && userSettings.getFormatScaleRealmListSize() > 1) {
            onClickFormatVideo(view);
            return;
        }

        // set translation animation and duration for image, which hide part of camera preview
        int animTranslationValue = 0;
        long animTime = 0;
        Float formatVideoInDataBase = Objects.requireNonNull(userSettings.getFormatScaleRealmList().get(indexOfFormatVideo));
        if (formatVideoInDataBase == FORMAT_SCALE_1X1) {
            btnFormatVideo.setText(getResources().getString(R.string.format_1x1));
            formatScale = FORMAT_SCALE_1X1;
            animTranslationValue = 0;
            animTime = 1500;
        } else if (formatVideoInDataBase == FORMAT_SCALE_4X5) {
            btnFormatVideo.setText(getResources().getString(R.string.format_4x5));
            formatScale = FORMAT_SCALE_4X5;
            animTranslationValue = (int) (displayWidth / FORMAT_SCALE_4X5 - displayWidth);
            animTime = 1000;
        } else if (formatVideoInDataBase == FORMAT_SCALE_9X16) {
            btnFormatVideo.setText(getResources().getString(R.string.format_9x16));
            formatScale = FORMAT_SCALE_9X16;
            // for getHeight don't use getResources().getDisplayMetrics().heightPixels, because it not include system action and status bar height
            animTranslationValue = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getHeight() - displayWidth - getResources().getDimensionPixelSize(R.dimen.top_panel_height_size_flash_mic_cam) - getResources().getDimensionPixelSize(R.dimen.red_line_height);
            animTime = 1500;
        } else if (formatVideoInDataBase == FORMAT_SCALE_16X10) {
            btnFormatVideo.setText(getResources().getString(R.string.format_16x10));
            formatScale = FORMAT_SCALE_16X10;
            animTranslationValue = (int) (displayWidth / FORMAT_SCALE_16X10 - displayWidth);
            animTime = 2000;
        } else if (formatVideoInDataBase == FORMAT_SCALE_16X9) {
            btnFormatVideo.setText(getResources().getString(R.string.format_16x9));
            formatScale = FORMAT_SCALE_16X9;
            animTranslationValue = (int) (displayWidth / FORMAT_SCALE_16X9 - displayWidth);
            animTime = 1000;
        }
        imgHideCamPreview.animate().translationY(animTranslationValue).setDuration(animTime);
        redLineHideCameraPreview.animate().translationY(animTranslationValue).setDuration(animTime);
        redLineHideCameraPreview.startAnimation(AnimationUtils.loadAnimation(this, R.anim.red_line_anim));
        analyticsLogEvent("Формат съемки " + btnFormatVideo.getText().toString());
    }

    public void onClickTypeCam(View view) {
        camera.release();
        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            btnFlash.setImageDrawable(getResources().getDrawable(R.drawable.flash_off));
            camParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            btnTypeCam.setImageDrawable(getResources().getDrawable(R.drawable.cam_face));
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else {
            btnTypeCam.setImageDrawable(getResources().getDrawable(R.drawable.cam_back));
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        camera.setDisplayOrientation(90);
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        initChronometerFullTimeSizeOfMemory();
        analyticsLogEvent(currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK ? "Задняя камера" : "Фронтальная камера");
    }

    // touch on record video button and zoom by swipe
    @SuppressLint({"ClickableViewAccessibility", "DefaultLocale", "SetTextI18n"})
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        camParams = camera.getParameters();

        if (lastTouchY == -1)
            lastTouchY = event.getY();

        // touch listener
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                actionDownY = event.getY();
                chronometerTimerVideoRecord.setBase(SystemClock.elapsedRealtime()); // reset
                chronometerTimerVideoRecord.start();
                chronometerTimerSizeOfMemory.setBase(SystemClock.elapsedRealtime()); // reset
                chronometerTimerSizeOfMemory.start();
                topPanelRelativeLay.setVisibility(View.GONE);
                pointOrange.setVisibility(View.INVISIBLE);
                btnAndImgVideoRecorderFrameLay.setVisibility(View.INVISIBLE);
                cancelSaveLinLay.setVisibility(View.GONE);
                textZoomVideo.setVisibility(View.VISIBLE);
                textSpeedVideo.setVisibility(View.VISIBLE);
                btnPlayLastVideo.setVisibility(View.GONE);
                btnSettings.setVisibility(View.GONE);
                startRecord(v);
                break;
            case MotionEvent.ACTION_MOVE:
                if (camParams.isZoomSupported()) {
                    if (event.getY() < lastTouchY - 2) { // zoom up
                        zoom += 2;
                        if (zoom > camParams.getMaxZoom()) zoom -= 2;
                        if (actionDownY < zoom - 100) {
                            actionDownY = zoom;
                            analyticsLogEvent("Приближение");
                        }
                    }
                    if (event.getY() > lastTouchY + 2) { // zoom down
                        zoom -= 2;
                        if (zoom < 0) zoom += 2;
                        if (actionDownY > zoom + 100) {
                            actionDownY = zoom;
                            analyticsLogEvent("Удаление");
                        }
                    }
                    camParams.setZoom(zoom);
                    camera.setParameters(camParams);
                    // set text zoom. Get max zoom ratio + 5(cause some device return exp. 397 instead 400)
                    // this count * current zoom count and / 10000 for set decimal format
                    // then set format 1 number after the comma and replace "," to "."
                    textZoomVideo.setText(String.format("%.1f",
                            (float) (zoom * (camParams.getZoomRatios().get(camParams.getZoomRatios().size() - 1) + 5))
                                    / 10000).replaceAll(",", ".") + "x");
                    lastTouchY = event.getY();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                motionEventActionCancel(v);
                break;
        }
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void motionEventActionCancel(View v) {
        chronometerFullTime += SystemClock.elapsedRealtime() - chronometerTimerVideoRecord.getBase();
        chronometerTimerVideoRecord.stop();
        chronometerFullTimeSizeOfMemory -= SystemClock.elapsedRealtime() - chronometerTimerSizeOfMemory.getBase();
        chronometerTimerSizeOfMemory.stop();
        btnAndImgVideoRecorderFrameLay.setVisibility(View.VISIBLE);
        imgCamillionPauseSpeed.setImageDrawable(getResources().getDrawable(R.drawable.pause));
        pointOrange.setVisibility(View.VISIBLE);
        textZoomVideo.setVisibility(View.GONE);
        textSpeedVideo.setVisibility(View.GONE);
        cancelSaveLinLay.setVisibility(View.VISIBLE);
        lastTouchY = -1;
        if (chronometerCurrentTime > 60000) btnVideoRecorder.setOnTouchListener(null);
        analyticsLogEvent(chronometerCurrentTime < 16000 ? "Минимальная (15-) длительность" :
                chronometerCurrentTime > 45000 ? "Максимальная (45+) длительность" : "Средняя (15-45) длительность");
        // if user press record button small time, tell about it and release Media Recorder or delete last fragment
        if (SystemClock.elapsedRealtime() - chronometerTimerVideoRecord.getBase() < 1500) {
            DialogUtils.alert(this, getResources().getString(R.string.press_and_hold_button_for_shoot));
            releaseMediaRecorder();
            if (videosDataDir.list().length > 2)  deleteVideoFile(new File(videosDataDir, videosDataDir.list()[videosDataDir.list().length - 1]));
            else  onClickCancel(new View(this));
        } else stopRecord(v);
    }

    public void onClickCancel(View view) {
        deleteAllDataFiles();
        resetUI();
        btnPlayLastVideo.setVisibility(View.VISIBLE);
        btnSettings.setVisibility(View.VISIBLE);
    }

    // crop old video, create new and delete old videos, using ffmeg4 in new thread
    public void onClickSave(View view) {
        new Thread(cropThread).start();
        new Thread(percentThread).start();
        if (camParams.getSupportedFlashModes() != null // turn off flash
                && camParams.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) onClickFlashMode(view);
        if (videosDataDir.list().length > 2) waitText.setText(getResources().getString(R.string.save_video_fragments_wait));
        else waitText.setText(getResources().getString(R.string.save_video_wait));
        waitRelLay.setVisibility(View.VISIBLE);
        resetUI();
    }

    public void onClickWaitCancel(View view) {
        deleteAllDataFiles();
        deleteVideoFile(videoFinalFile);
        chronometerFullTime = 0;
        initChronometerFullTimeSizeOfMemory();
        waitRelLay.setVisibility(View.GONE);
        btnPlayLastVideo.setVisibility(View.VISIBLE);
        btnSettings.setVisibility(View.VISIBLE);
    }

    public void onClickPlayLastVideo(View view) {
        if (videosDir.listFiles().length > 1) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videosDir.listFiles()[videosDir.listFiles().length - 1].getAbsolutePath()));
            intent.setDataAndType(Uri.parse(videosDir.listFiles()[videosDir.listFiles().length - 1].getAbsolutePath()), "video/*");
            startActivity(intent);
        } else {
            DialogUtils.alert(this, getResources().getString(R.string.no_last_videos));
        }
        analyticsLogEvent("Последнее видео Андроид");
    }

    public void onClickSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        analyticsLogEvent("Открытие меню настроек");
    }

    @SuppressLint("ClickableViewAccessibility")
    public void resetUI() {
        topPanelRelativeLay.setVisibility(View.VISIBLE);
        imgCamillionPauseSpeed.setImageDrawable(getResources().getDrawable(R.drawable.camillion));
        cancelSaveLinLay.setVisibility(View.GONE);
        chronometerFullTime = 0;
        redText15or60seconds.setVisibility(View.GONE);
        btnVideoRecorder.setOnTouchListener(this);
        initChronometerFullTimeSizeOfMemory();
        resetZoom();
    }

    private Runnable cropThread = new Runnable() {
        @Override
        public void run() {
            try {
                videoFinalFile = new File(videosDir.getAbsoluteFile() + "/" + System.currentTimeMillis() + ".mp4");
                findVideosInGallery();
                String[] complexCommand; // command of ffmpeg4 library
                if (videosDataDir.list().length > 2) { // scan number of video fragments, if > 2, then use concatenate command
                    complexCommand = new String[]{"ffmpeg", "-f", "concat", "-safe", "0",              // concatenate video fragments
                            "-i", dataTextFile.getAbsolutePath(),                                      // txt file with video fragments paths
                            "-f", "mp4", "-vcodec", "mpeg4",                                           // file format and used video codec
                            "-threads", "0", "-qscale", "0",                                           // used all device cores and don't change the quality scale
                            "-s", (int) (resolutionWidth / formatScale) + "x" + resolutionWidth,       // set resolution for change the quality
                            "-filter_complex",                                                         // command of complex filter
                            "setpts=" + speedScale + "*PTS, crop=in_w:in_w/" + formatScale + ":0:0",   // crop video and set fast, slow or normal speed
                            videoFinalFile.getAbsolutePath()};                                         // path of new video file
                } else complexCommand = new String[]{"ffmpeg", "-i", videoFragment.getAbsolutePath(), "-f", "mp4", "-vcodec", "mpeg4", "-threads", "0",
                        "-s", (int) (resolutionWidth / formatScale) + "x" + resolutionWidth,
                        "-qscale", "0", "-filter_complex", "setpts=" + speedScale + "*PTS, crop=in_w:in_w/" + formatScale + ":0:0",
                        videoFinalFile.getAbsolutePath()};
                // execute ffmpeg4android commands for editing videos.
                if (Build.CPU_ABI.equals(ARMEABI_V7A))
                    vk.run(complexCommand, getApplicationContext().getFilesDir().getAbsolutePath(), getApplicationContext());
                else DialogUtils.alert(CameraPreviewActivity.this, getResources().getString(R.string.cant_save_video));
                deleteAllDataFiles(); // delete video fragments and txt file
                Log.i(CAMILLION_TAG, "crop command ok");
            } catch (Throwable e) {
                Log.e(CAMILLION_TAG, "vk run exception.", e);
            }
            myHandlerWait.sendMessage(myHandlerWait.obtainMessage());
        }

        // execute when crop thread finish all actions
        @SuppressLint("HandlerLeak")
        Handler myHandlerWait = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                waitText.setText(getResources().getString(R.string.save_video_finished));
                waitPercent.setText(getResources().getString(R.string.save_100_percent));
                findVideosInGallery();
                new Handler().postDelayed(() -> {
                    waitRelLay.setVisibility(View.GONE);
                    waitPercent.setText(getResources().getString(R.string.save_0_percent));
                    btnPlayLastVideo.setImageBitmap(ThumbnailUtils.createVideoThumbnail(videosDir.listFiles()[videosDir.listFiles()
                            .length - 1].getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND));
                    btnPlayLastVideo.setVisibility(View.VISIBLE);
                    btnSettings.setVisibility(View.VISIBLE);
                }, 3000);
            }
        };
    };

    private Runnable percentThread = new Runnable() {
        int percent;
        @Override
        public void run() {
            percent = 0;
            while (percent < 89 && !waitPercent.getText().equals("100%")) {
                myHandlerPercent.sendMessage(myHandlerPercent.obtainMessage());
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // increase percent count to random number
        @SuppressLint("HandlerLeak")
        Handler myHandlerPercent = new Handler() {
            @SuppressLint("SetTextI18n")
            @Override
            public void handleMessage(Message msg) {
                percent += new Random().nextInt(11);
                waitPercent.setText(percent + "%");
            }
        };
    };

    private boolean prepareVideoRecorder() {
        camera.unlock();
        mediaRecorder = new MediaRecorder();
        CamcorderProfile profile = CamcorderProfile.get(currentCameraId, CamcorderProfile.QUALITY_HIGH);
        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) mediaRecorder.setOrientationHint(90);
        else mediaRecorder.setOrientationHint(270);

        mediaRecorder.setCamera(camera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // check the audio mode
        if (micIsOn) {
            // parameters with audio
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mediaRecorder.setProfile(profile);
        } else {
            // parameters without audio
            mediaRecorder.setOutputFormat(profile.fileFormat);
            mediaRecorder.setVideoEncoder(profile.videoCodec);
            mediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
            mediaRecorder.setVideoFrameRate(profile.videoFrameRate);
            mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        }

        mediaRecorder.setOutputFile(videoFragment.getAbsolutePath());
        mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());

        try {
            mediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
        }
    }

    public void startRecord(View view) {
        videoFragment = new File(videosDataDir, System.currentTimeMillis() + ".mp4"); // create video fragment
        if (prepareVideoRecorder()) mediaRecorder.start();
        else releaseMediaRecorder();
    }

    public void stopRecord(View view) {
        addVideoPathToTXTFile(videoFragment.getAbsolutePath()); // add video fragment path to txt file
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            releaseMediaRecorder();
        }
    }

    public void addVideoPathToTXTFile(String videoPath) {
        try {
            FileWriter writer = new FileWriter(dataTextFile, true); // true - add text to end of file
            writer.append("file '").append(videoPath).append("'").append(System.lineSeparator());
            writer.flush();
            writer.close();
            Log.i(CAMILLION_TAG, "new video path add to txt file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteVideoFile(File videoPath) {
        if (videoPath.delete()) Log.d(CAMILLION_TAG, "Delete video success " + videoPath);
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(videoPath)));
    }

    public void deleteAllDataFiles() {
        String[] deletingFiles;
        deletingFiles = videosDataDir.list();
        for (String deletingFile : deletingFiles) {
            File file = new File(videosDataDir, deletingFile);
            if (file.delete()) Log.d(CAMILLION_TAG, "Delete data file success " + file);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file))); // delete reference and path
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////
    public void findVideosInGallery() {
        // scan new video file in gallery by 2 ways
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(videoFinalFile)));
//        MediaScannerConnection mediaScan = new MediaScannerConnection(getBaseContext(), (MediaScannerConnection.MediaScannerConnectionClient) this);
//        mediaScan.connect();
//        mediaScan.scanFile(getBaseContext(), new String[] { videoFinalFile.getAbsolutePath() }, null, null);
        MediaScannerConnection.scanFile(getBaseContext(), new String[] { videoFinalFile.getAbsolutePath() }, null, null);
//        mediaScan.disconnect();
    }
/////////////////////////////////////////////////////////////////////////////////////////////////
    public void initChronometerFullTimeSizeOfMemory() {
        // find available memory and / jpeg size / 30 like fps
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        chronometerFullTimeSizeOfMemory = statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong() / camParams.getJpegQuality() / 30;
    }

    public void resetZoom() {
        if (camParams.isZoomSupported()) {
            zoom = 0;
            camParams.setZoom(zoom);
            camera.setParameters(camParams);
            textZoomVideo.setText(getResources().getString(R.string.zoom_text_0_0x));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        indexOfFormatVideo = userSettings.getFormatScaleRealmListSize() - 1;
        // silent click on video format, if it was deleted in settings
        if (!userSettings.getFormatScaleRealmList().contains(formatScale)) onClickFormatVideo(new View(this));
        indexOfSpeedVideo--; onClickSpeedVideo(new View(this));
        btnFlash.setImageDrawable(getResources().getDrawable(R.drawable.flash_off));
        resolutionWidth = userSettings.getQualityWidth();
        try {
            camera = Camera.open(currentCameraId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();
        if (camera != null)
            camera.release();
        camera = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteAllDataFiles();
    }

    @Override
    public void onBackPressed() {
        try {
            onClickCancel(new View(this));
            onClickWaitCancel(new View(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getScreenName() {
        return "Превью камеры (окно видеосъемки)";
    }
}