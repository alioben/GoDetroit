package acb.godetroit.notsafe.godetroit.Views;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.SessionConfiguration;

import java.util.Arrays;

import acb.godetroit.notsafe.godetroit.R;
import acb.godetroit.notsafe.godetroit.Utils.GPSTracker;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private Toast guideToast;
    private boolean showGuideAlerts = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new AlertDialog.Builder(this)
                .setTitle("GPS location")
                .setMessage("For testing purposes we have hardcoded a location that will place your phone in Detroit. If you'd like to select this option in heed of others select OK, otherwise select CANCEL")
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        GPSTracker.hardcodeGPS = false;
                        initializeViews();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GPSTracker.hardcodeGPS = true;
                        initializeViews();
                    }
                })
                .create()
                .show();


        SessionConfiguration config = new SessionConfiguration.Builder()
                .setClientId("McyFrbyrokok73kOR5MZlO9r-VK-UMPm") //This is necessary
                .setEnvironment(SessionConfiguration.Environment.SANDBOX) //Useful for testing your app in the sandbox environment
                .setScopes(Arrays.asList(Scope.PROFILE, Scope.RIDE_WIDGETS)) //Your scopes for authentication here
                .build();

        UberSdk.initialize(config);

    }

    public void initializeViews(){
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Do nothing
            }
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {

                } else if (position == 1) {
                    if(showGuideAlerts) {
                        showGuideToast("Vote on an alert to verify it's authenticity, or write a new report to share to others.");
                        showGuideAlerts = false;
                    }
                } else if (position == 2) {
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                //Do nothing
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    public void showGuideToast(String text){
        if(guideToast != null)
            guideToast.cancel();

        guideToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        guideToast.show();
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new FragmentMain();
                case 1:
                    return new FragmentTwitter();
                case 2:
                    return new FragmentWatchMe();
            }
            return new FragmentMain();
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "FIND PATH";
                case 1:
                    return "ALERTS";
                case 2:
                    return "WATCH ME";
            }
            return null;
        }
    }
}
