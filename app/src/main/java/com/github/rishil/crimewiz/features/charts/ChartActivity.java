package com.github.rishil.crimewiz.features.charts;

import android.net.Uri;
import android.os.Bundle;

import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import com.github.rishil.crimewiz.R;
import com.github.rishil.crimewiz.base.BaseActivity;
import com.github.rishil.crimewiz.features.charts.adapters.PageAdapter;
import com.github.rishil.crimewiz.features.charts.bar.PerCategoryFragment;

public class ChartActivity extends BaseActivity implements PerCategoryFragment.OnFragmentInteractionListener {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private TabItem barChartTab, pieChartTab;

    private PageAdapter pageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        loadUi();

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        barChartTab = findViewById(R.id.categoryTab);
        pieChartTab = findViewById(R.id.yearTab);

        pageAdapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(pageAdapter);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), true);
                if (tab.getPosition() == 0) {
                    giveHapticFeedback();
                    pageAdapter.notifyDataSetChanged();
                } else if (tab.getPosition() == 1) {
                    giveHapticFeedback();
                    pageAdapter.notifyDataSetChanged();
                } else if (tab.getPosition() == 2) {
                    giveHapticFeedback();
                    pageAdapter.notifyDataSetChanged();
                } else if (tab.getPosition() == 3) {
                    giveHapticFeedback();
                    pageAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                pageAdapter.notifyDataSetChanged();
            }
        });


        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
