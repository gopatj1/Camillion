package ru.irev.camillionforinst.settings;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.circlenavigator.CircleNavigator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.irev.camillionforinst.BaseActivity;
import ru.irev.camillionforinst.R;

public class SettingsActivity extends BaseActivity {

    static final int PAGE_COUNT = 2;

    @BindView(R.id.pager) ViewPager pager;
    @BindView(R.id.magicIndicator) MagicIndicator magicIndicator;

    @OnClick(R.id.btnCloseSettings)
    void closeSettings() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        PagerAdapter pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        initMagicIndicator();
    }

    // custom FragmentPagerAdapter, where we set a number of current settings fragment
    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return SettingsPageFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }

    private void initMagicIndicator() {
        CircleNavigator circleNavigator = new CircleNavigator(this);
        circleNavigator.setCircleCount(PAGE_COUNT);
        circleNavigator.setCircleColor(Color.WHITE);
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, r.getDisplayMetrics());
        circleNavigator.setRadius((int) px);
        circleNavigator.setCircleSpacing((int) px * 4);
        circleNavigator.setStrokeWidth((int) (px / 4));
        circleNavigator.setCircleClickListener(index -> pager.setCurrentItem(index));
        magicIndicator.setNavigator(circleNavigator);
        ViewPagerHelper.bind(magicIndicator, pager);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeSettings();
    }

    @Override
    public String getScreenName() {
        return "Экран настроек";
    }
}
