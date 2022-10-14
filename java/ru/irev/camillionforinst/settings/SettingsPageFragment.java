package ru.irev.camillionforinst.settings;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

import ru.irev.camillionforinst.R;
import ru.irev.camillionforinst.controller.UserSettingsController;
import ru.irev.camillionforinst.subscribe.PaymentActivity;
import ru.irev.camillionforinst.utils.DialogUtils;

public class SettingsPageFragment extends Fragment {

    private static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    public static final float FAST_MODE_SCALE_3X = (float) 0.33;
    public static final float FAST_MODE_SCALE_4X = (float) 0.25;
    public static final float FAST_MODE_SCALE_5X = (float) 0.2;
    public static final float SLOW_MODE_SCALE_1_2X = (float) 2;
    public static final float SLOW_MODE_SCALE_1_3X = (float) 3;
    public static final float SLOW_MODE_SCALE_1_4X = (float) 4;
    public static final int QUALITY_HD_WIDTH = 1080;
    public static final int QUALITY_SD_WIDTH = 720;
    public static final int TIME_LIMIT_15 = 15;
    public static final int TIME_LIMIT_60 = 60;
    public static final int TIME_LIMIT_INFINITY = 9999;
    public static final float FORMAT_SCALE_1X1 = (float) 1;
    public static final float FORMAT_SCALE_4X5 = (float) 0.8;
    public static final float FORMAT_SCALE_9X16 = (float) 0.5625;
    public static final float FORMAT_SCALE_16X10 = (float) 1.6;
    public static final float FORMAT_SCALE_16X9 = (float) 1.777;

    private int pageNumber;
    private SettingsActivity parentActivity;

    static SettingsPageFragment newInstance(int page) {
        SettingsPageFragment settingsPageFragment = new SettingsPageFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        settingsPageFragment.setArguments(arguments);
        return settingsPageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = Objects.requireNonNull(getArguments()).getInt(ARGUMENT_PAGE_NUMBER);
        parentActivity = (SettingsActivity) getActivity();
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = null;
        switch (pageNumber) {
            case 0:
                view = inflater.inflate(R.layout.fragment_settings_menu_1, null);
                TextView btnSpeedFast3x = view.findViewById(R.id.btnSpeedFast3x);
                TextView btnSpeedFast4x = view.findViewById(R.id.btnSpeedFast4x);
                TextView btnSpeedFast5x = view.findViewById(R.id.btnSpeedFast5x);
                TextView btnSpeedSlow2x = view.findViewById(R.id.btnSpeedSlow2x);
                TextView btnSpeedSlow3x = view.findViewById(R.id.btnSpeedSlow3x);
                TextView btnSpeedSlow4x = view.findViewById(R.id.btnSpeedSlow4x);
                TextView btnVideoQualityHD = view.findViewById(R.id.btnVideoQualityHD);
                TextView btnVideoQualitySD = view.findViewById(R.id.btnVideoQualitySD);
                TextView btnTimeLimit15 = view.findViewById(R.id.btnTimeLimit15);
                TextView btnTimeLimit60 = view.findViewById(R.id.btnTimeLimit60);
                ImageButton btnTimeNoLimit = view.findViewById(R.id.btnTimeNoLimit);

                // turn on default or saved button from realm
                if (parentActivity.userSettings.getFastModeScale() == FAST_MODE_SCALE_3X) {
                    buttonSelectTextView(btnSpeedFast3x);
                } else if (parentActivity.userSettings.getFastModeScale() == FAST_MODE_SCALE_4X) {
                        buttonSelectTextView(btnSpeedFast4x);
                } else buttonSelectTextView(btnSpeedFast5x);

                if (parentActivity.userSettings.getSlowModeScale() == SLOW_MODE_SCALE_1_2X) {
                    buttonSelectTextView(btnSpeedSlow2x);
                } else if (parentActivity.userSettings.getSlowModeScale() == SLOW_MODE_SCALE_1_3X) {
                        buttonSelectTextView(btnSpeedSlow3x);
                } else buttonSelectTextView(btnSpeedSlow4x);

                if (parentActivity.userSettings.getQualityWidth() == QUALITY_HD_WIDTH) {
                    buttonSelectTextView(btnVideoQualityHD);
                } else buttonSelectTextView(btnVideoQualitySD);

                if (parentActivity.userSettings.getTimeLimit() == TIME_LIMIT_15) {
                    buttonSelectTextView(btnTimeLimit15);
                } else if (parentActivity.userSettings.getTimeLimit() == TIME_LIMIT_60) {
                    buttonSelectTextView(btnTimeLimit60);
                } else infinityTimeLimitChanges(btnTimeNoLimit, getResources().getDrawable(R.drawable.infinity_icon_blue), getResources().getDimension(R.dimen.selected_settings_infinity_time_size));

                // set click trigger to settings button
                btnSpeedFast3x.setOnClickListener(v -> {
                    if (buttonSelectAndDeselectOthers(new ArrayList<TextView>() {{ add(btnSpeedFast3x); add(btnSpeedFast4x);
                        add(btnSpeedFast5x); }},btnSpeedFast3x, "Коэффициент ускорения 3х"))
                        UserSettingsController.setFastModeScale(parentActivity.realm, FAST_MODE_SCALE_3X);
                });
                btnSpeedFast4x.setOnClickListener(v -> {
                    if (buttonSelectAndDeselectOthers(new ArrayList<TextView>() {{ add(btnSpeedFast3x); add(btnSpeedFast4x);
                        add(btnSpeedFast5x); }}, btnSpeedFast4x, "Коэффициент ускорения 4х"))
                        UserSettingsController.setFastModeScale(parentActivity.realm, FAST_MODE_SCALE_4X);
                });
                btnSpeedFast5x.setOnClickListener(v -> {
                    if (buttonSelectAndDeselectOthers(new ArrayList<TextView>() {{ add(btnSpeedFast3x); add(btnSpeedFast4x);
                        add(btnSpeedFast5x); }}, btnSpeedFast5x, "Коэффициент ускорения 5х"))
                        UserSettingsController.setFastModeScale(parentActivity.realm, FAST_MODE_SCALE_5X);
                });
                btnSpeedSlow2x.setOnClickListener(v -> {
                    if (buttonSelectAndDeselectOthers(new ArrayList<TextView>() {{ add(btnSpeedSlow2x); add(btnSpeedSlow3x);
                        add(btnSpeedSlow4x); }}, btnSpeedSlow2x, "Коэффициент замедления 2х"))
                        UserSettingsController.setSlowModeScale(parentActivity.realm, SLOW_MODE_SCALE_1_2X);
                });
                btnSpeedSlow3x.setOnClickListener(v -> {
                    if (buttonSelectAndDeselectOthers(new ArrayList<TextView>() {{ add(btnSpeedSlow2x); add(btnSpeedSlow3x);
                        add(btnSpeedSlow4x); }}, btnSpeedSlow3x, "Коэффициент замедления 3х"))
                        UserSettingsController.setSlowModeScale(parentActivity.realm, SLOW_MODE_SCALE_1_3X);
                });
                btnSpeedSlow4x.setOnClickListener(v -> {
                    if (buttonSelectAndDeselectOthers(new ArrayList<TextView>() {{ add(btnSpeedSlow2x); add(btnSpeedSlow3x);
                        add(btnSpeedSlow4x); }}, btnSpeedSlow4x, "Коэффициент замедления 4х"))
                        UserSettingsController.setSlowModeScale(parentActivity.realm, SLOW_MODE_SCALE_1_4X);
                });
                btnVideoQualityHD.setOnClickListener(v -> {
                    if (buttonSelectAndDeselectOthers(new ArrayList<TextView>() {{ add(btnVideoQualityHD); add(btnVideoQualitySD); }}
                        , btnVideoQualityHD, "Качество съемки HD"))
                        UserSettingsController.setQualityHeightAndWidth(parentActivity.realm, QUALITY_HD_WIDTH);
                });
                btnVideoQualitySD.setOnClickListener(v -> {
                    if (buttonSelectAndDeselectOthers(new ArrayList<TextView>() {{ add(btnVideoQualityHD); add(btnVideoQualitySD); }}
                        , btnVideoQualitySD, "Качество съемки SD"))
                        UserSettingsController.setQualityHeightAndWidth(parentActivity.realm, QUALITY_SD_WIDTH);
                });
                btnTimeLimit15.setOnClickListener(v -> {
                    if (buttonSelectAndDeselectOthers(new ArrayList<TextView>() {{ add(btnTimeLimit15); add(btnTimeLimit60);}}
                        , btnTimeLimit15, "Ограничение времени съемки 15с")) {
                        UserSettingsController.setTimeLimit(parentActivity.realm, TIME_LIMIT_15);
                        infinityTimeLimitChanges(btnTimeNoLimit, getResources().getDrawable(R.drawable.infinity_icon), getResources().getDimension(R.dimen.settings_infinity_time_size));
                    }
                });
                btnTimeLimit60.setOnClickListener(v -> {
                    if (buttonSelectAndDeselectOthers(new ArrayList<TextView>() {{ add(btnTimeLimit15); add(btnTimeLimit60);}}
                        , btnTimeLimit60, "Ограничение времени съемки 60с")) {
                        UserSettingsController.setTimeLimit(parentActivity.realm, TIME_LIMIT_60);
                        infinityTimeLimitChanges(btnTimeNoLimit, getResources().getDrawable(R.drawable.infinity_icon), getResources().getDimension(R.dimen.settings_infinity_time_size));
                    }
                });
                btnTimeNoLimit.setOnClickListener(v -> {
                    if (buttonSelectAndDeselectOthers(new ArrayList<TextView>() {{ add(btnTimeLimit15); add(btnTimeLimit60);}}
                        , btnTimeNoLimit, "Без ограничения времени съемки")) {
                        UserSettingsController.setTimeLimit(parentActivity.realm, TIME_LIMIT_INFINITY);
                        infinityTimeLimitChanges(btnTimeNoLimit, getResources().getDrawable(R.drawable.infinity_icon_blue), getResources().getDimension(R.dimen.selected_settings_infinity_time_size));
                    }
                });
                break;
            case 1:
                view = inflater.inflate(R.layout.fragment_settings_menu_2, null);
                TextView btn1x1 = view.findViewById(R.id.btn1x1);
                TextView btn4x5 = view.findViewById(R.id.btn4x5);
                TextView btn9x16 = view.findViewById(R.id.btn9x16);
                TextView btn16x10 = view.findViewById(R.id.btn16x10);
                TextView btn16x9 = view.findViewById(R.id.btn16x9);
                TextView btnYes = view.findViewById(R.id.btnYes);
                TextView btnNo = view.findViewById(R.id.btnNo);

                // turn on default or saved button from realm
                buttonSelectOrDeselectFormatScale(btn1x1, FORMAT_SCALE_1X1, null, true);
                buttonSelectOrDeselectFormatScale(btn4x5, FORMAT_SCALE_4X5, null, true);
                buttonSelectOrDeselectFormatScale(btn9x16, FORMAT_SCALE_9X16, null, true);
                buttonSelectOrDeselectFormatScale(btn16x10, FORMAT_SCALE_16X10, null, true);
                buttonSelectOrDeselectFormatScale(btn16x9, FORMAT_SCALE_16X9, null, true);
                if (parentActivity.userSettings.getPreviewAndEditPossibility()) {
                    buttonSelectTextView(btnYes);
                } else buttonSelectTextView(btnNo);

                // set click trigger to settings button
                btn1x1.setOnClickListener(v -> clickOnFormatScaleItem(btn1x1, 0, FORMAT_SCALE_1X1, getResources().getString(R.string.format_1x1)));
                btn4x5.setOnClickListener(v -> clickOnFormatScaleItem(btn4x5, 1, FORMAT_SCALE_4X5, getResources().getString(R.string.format_4x5)));
                btn9x16.setOnClickListener(v -> clickOnFormatScaleItem(btn9x16, 2, FORMAT_SCALE_9X16, getResources().getString(R.string.format_9x16)));
                btn16x10.setOnClickListener(v -> clickOnFormatScaleItem(btn16x10, 3, FORMAT_SCALE_16X10, getResources().getString(R.string.format_16x10)));
                btn16x9.setOnClickListener(v -> clickOnFormatScaleItem(btn16x9, 4, FORMAT_SCALE_16X9, getResources().getString(R.string.format_16x9)));
                btnYes.setOnClickListener(v -> {
                    DialogUtils.alert(getActivity(), getResources().getString(R.string.click_item_yes));
//                    if (buttonSelectAndDeselectOthers(new ArrayList<TextView>() {{ add(btnYes); add(btnNo); }}
//                        , btnYes, "Прсмотр и редактирование видео - да"))
//                        UserSettingsController.setPreviewAndEditPossibility(realm, true);
                });
                btnNo.setOnClickListener(v -> {
                    if (buttonSelectAndDeselectOthers(new ArrayList<TextView>() {{ add(btnYes); add(btnNo); }}
                        , btnNo, "Прсмотр и редактирование видео - нет"))
                        UserSettingsController.setPreviewAndEditPossibility(parentActivity.realm, false);
                });
        }
        return view;
    }

    private boolean buttonSelectAndDeselectOthers(ArrayList<TextView> viewArrayList, View selectedView,  String analyticsMessage) {
        if (!parentActivity.userSettings.isPaid()) {
            PaymentActivity.start(getActivity());
            return false;
        }
        for (TextView view: viewArrayList)
            if (view == selectedView) {
                view.setTextColor(getResources().getColor(R.color.selected_settings_item));
                view.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.selected_settings_item_text_size));
            } else {
                view.setTextColor(getResources().getColor(R.color.gray_in_settings));
                view.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.settings_item_text_size));
            }
        parentActivity.analyticsLogEvent(analyticsMessage);
        return true;
    }

    private boolean buttonSelectOrDeselectFormatScale(TextView selectedView, float formatScale, String aspectRatio, boolean isSilentClick) {
        if (!parentActivity.userSettings.getFormatScaleRealmList().contains(formatScale) && isSilentClick) return false;
        if (!parentActivity.userSettings.isPaid() && !isSilentClick) {
            PaymentActivity.start(getActivity());
            return false;
        }
        if (parentActivity.userSettings.getFormatScaleRealmList().contains(formatScale)) {
            selectedView.setTextColor(getResources().getColor(R.color.selected_settings_item));
            selectedView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.selected_settings_item_text_size));
            if (!isSilentClick) parentActivity.analyticsLogEvent("Включение пропорции экрана " + aspectRatio);
        } else {
            selectedView.setTextColor(getResources().getColor(R.color.gray_in_settings));
            selectedView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.settings_item_text_size));
            if (!isSilentClick) parentActivity.analyticsLogEvent("Отключение пропорции экрана " + aspectRatio);
        }
        return true;
    }

    private void buttonSelectTextView(TextView selectedView) {
        selectedView.setTextColor(getResources().getColor(R.color.selected_settings_item));
        selectedView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.selected_settings_item_text_size));
    }

    private void infinityTimeLimitChanges(ImageButton imageButton, Drawable icon, float size) {
        imageButton.setImageDrawable(icon);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) imageButton.getLayoutParams();
        lp.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, size, getResources().getDisplayMetrics());
        imageButton.setLayoutParams(lp);
    }

    private void clickOnFormatScaleItem(TextView btn, int index, float formatScale, String textFormat) {
        if (parentActivity.userSettings.getFormatScaleRealmList().size() == 1 && Objects.requireNonNull(parentActivity.userSettings.getFormatScaleRealmList().first()) == formatScale) {
            DialogUtils.alert(getActivity(), getResources().getString(R.string.can_not_remove_format_item));
            return;
        }
        if (buttonSelectOrDeselectFormatScale(btn, formatScale, textFormat, false))
            UserSettingsController.addOrDeleteFormatScale(parentActivity.realm, index, formatScale);
    }
}
