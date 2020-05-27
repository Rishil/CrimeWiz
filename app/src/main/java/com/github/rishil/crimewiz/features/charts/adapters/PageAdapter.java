package com.github.rishil.crimewiz.features.charts.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.github.rishil.crimewiz.features.charts.bar.PerCategoryFragment;
import com.github.rishil.crimewiz.features.charts.line.PerMonthFragment;
import com.github.rishil.crimewiz.features.charts.pie.PerYearFragment;
import com.github.rishil.crimewiz.features.charts.scatter.RegressionFragment;

public class PageAdapter extends FragmentPagerAdapter {

    private int numberOfTabs;

    public PageAdapter(FragmentManager fm, int numberOfTabs) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.numberOfTabs = numberOfTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new PerCategoryFragment();
            case 1:
                return new PerYearFragment();
            case 2:
                return new PerMonthFragment();
            case 3:
                return new RegressionFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}
