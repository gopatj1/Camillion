package ru.irev.camillionforinst.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;

import java.util.Objects;

import ru.irev.camillionforinst.CamillionApp;
import ru.irev.camillionforinst.R;

public class DialogUtils {

    private static void showImmersiveDialog(AlertDialog dialog) {
        Objects.requireNonNull(dialog.getWindow()).setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.show();
        //Set the dialog to immersive
        dialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        //Clear the not focusable flag from the window
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    public static void showNoInternetInShopDialog(Activity activity, OnDialogClose listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder .setTitle(CamillionApp.getAppContext().getResources().getString(R.string.attention))
                .setMessage(CamillionApp.getAppContext().getResources().getString(R.string.payment_no_internet))
                .setPositiveButton(CamillionApp.getAppContext().getResources().getString(R.string.payment_no_internet_try_again), (dialogInterface, i) -> listener.onClose(1))
                .setNegativeButton(CamillionApp.getAppContext().getResources().getString(R.string.payment_no_internet_exit), (dialogInterface, i) -> listener.onClose(2))
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        showImmersiveDialog(dialog);
    }

    public static void alert(Activity activity, String message) {
        alert(activity, message, null);
    }

    public static void alert(Activity activity, String message, DialogInterface.OnDismissListener listener) {
        AlertDialog.Builder bld = new AlertDialog.Builder(activity);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        AlertDialog alert = bld.create();
        if (listener != null) alert.setOnDismissListener(listener);
        showImmersiveDialog(alert);
    }

    public interface OnDialogClose {
        void onClose(int buttonId);
    }

    public static void showNotSupportAllFunctionsDialog(Activity activity, OnDialogClose listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder .setTitle(CamillionApp.getAppContext().getResources().getString(R.string.attention))
                .setMessage(CamillionApp.getAppContext().getResources().getString(R.string.not_support_ffmpeg))
                .setPositiveButton(CamillionApp.getAppContext().getResources().getString(R.string.not_support_ffmpeg_positive_btn), (dialogInterface, i) -> listener.onClose(1))
                .setNegativeButton(CamillionApp.getAppContext().getResources().getString(R.string.not_support_ffmpeg_negative_btn), (dialogInterface, i) -> listener.onClose(2))
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        showImmersiveDialog(dialog);
    }
}
