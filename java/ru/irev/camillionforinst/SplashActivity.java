package ru.irev.camillionforinst;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import ru.irev.camillionforinst.controller.UserSettingsController;
import ru.irev.camillionforinst.utils.DialogUtils;


public class SplashActivity extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int MY_MULTIPLE_PERMISSIONS_REQUEST = 1234;

    @BindView(R.id.main_layout)
    View mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        // Check is permissions allows, enable it and start camera
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestMultiplePermissions();
        else mLayout.animate().alpha(100).setDuration(300).withEndAction(this::startCamera);

        // reset purchase status if debug for testing application
        if (BuildConfig.DEBUG && userSettings.isPaid()) {
            UserSettingsController.setUserPaid(Realm.getDefaultInstance(), false);
            DialogUtils.alert(this, "\"Тестовая\" подписка отменена!");
        }
    }

    public void requestMultiplePermissions() {
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                },
                MY_MULTIPLE_PERMISSIONS_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_MULTIPLE_PERMISSIONS_REQUEST && grantResults.length == 4) {
            if ((grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[3] == PackageManager.PERMISSION_GRANTED)) {
                mLayout.animate().alpha(100).setDuration(300).withEndAction(this::startCamera);
            } else
                DialogUtils.alert(this, getResources().getString(R.string.permission_error), dialogInterface -> finish());
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startCamera() {
        Intent intent = new Intent(this, CameraPreviewActivity.class);
        startActivity(intent);
    }

    @Override
    public String getScreenName() {
        return "Сплеш экран";
    }
}