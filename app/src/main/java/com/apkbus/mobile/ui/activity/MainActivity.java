package com.apkbus.mobile.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ClientCertRequest;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.apkbus.mobile.R;
import com.apkbus.mobile.bean.LoginInfo;
import com.apkbus.mobile.bean.User;
import com.apkbus.mobile.bean.UserProfile;
import com.apkbus.mobile.constract.MainContract;
import com.apkbus.mobile.presenter.MainPresenter;
import com.apkbus.mobile.ui.fragment.ArticleFragment;
import com.apkbus.mobile.utils.LToast;
import com.apkbus.mobile.utils.SharedPreferencesHelper;

import net.youmi.android.listener.OffersWallDialogListener;
import net.youmi.android.normal.banner.BannerManager;
import net.youmi.android.normal.banner.BannerViewListener;
import net.youmi.android.offers.OffersManager;

import java.util.Timer;
import java.util.TimerTask;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

public class MainActivity extends BaseActivity<MainContract.Presenter> implements MainContract.View, View.OnClickListener, TabLayout.OnTabSelectedListener, OffersWallDialogListener, ViewPager.OnPageChangeListener, BannerViewListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private TextView mTextUsername;
    private ImageView mAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mPresenter.initData();
    }

    @Override
    MainPresenter getPresenter() {
        return new MainPresenter(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OffersManager.getInstance(this).onAppExit();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        actionBarDrawerToggle.syncState();
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.removeOnPageChangeListener(this);
        mViewPager.addOnPageChangeListener(this);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.removeOnTabSelectedListener(this);
        tabLayout.addOnTabSelectedListener(this);
        tabLayout.setupWithViewPager(mViewPager);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        SwitchCompat item = (SwitchCompat) navigationView.getMenu().getItem(0).getActionView();
        item.setChecked(SharedPreferencesHelper.getInstance(this).needAutoRenew());
        item.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
                SharedPreferencesHelper.getInstance(mContext).setAutoRenew(isChecked)
        );
        navigationView.setNavigationItemSelectedListener((MenuItem menuItem) -> {
            switch (menuItem.getItemId()) {
                case R.id.navigation_item_autorenew:
                    //menuItem.getActionView().
                    break;
                case R.id.navigation_item_logout:
                    SharedPreferencesHelper.getInstance(mContext).saveToken(new LoginInfo());
                    startActivity(new Intent(mContext, LoginActivity.class));
                    finish();
                    break;
                case R.id.navigation_item_share:
                    // new OneKeyShare();
                    OnekeyShare share = new OnekeyShare();
                    share.disableSSOWhenAuthorize();
                    share.setTitle("ApkBus");
                    share.setTitleUrl("http://fir.im/ve7m");
                    share.setText("ApkBus_哈哈哈哦啦啦");
                    share.setUrl("http://fir.im/ve7m");
                    share.setImageUrl("http://www.apkbus.com/static/image/common/logo.png");
                    share.setSiteUrl("http://fir.im/ve7m");
                    share.setSite("ApkBus");
                    share.show(this);
                    drawerLayout.closeDrawers();
                    break;
                case R.id.navigation_item_gifts:
                    startActivity(new Intent(mContext, GiftsActivity.class));
                    drawerLayout.closeDrawers();
                    break;
                case R.id.navigation_menu_item_robot:
                    drawerLayout.closeDrawers();
                    startActivity(new Intent(mContext, ChatActivity.class));
                    break;
            }
            //menuItem.setChecked(!menuItem.isChecked());
            //drawerLayout.closeDrawers();
            return true;
        });
        mTextUsername = ((TextView) navigationView.getHeaderView(0).findViewById(R.id.navigation_header_username));
        mAvatar = ((ImageView) navigationView.getHeaderView(0).findViewById(R.id.navigation_header_avatar));
        mTextUsername.setOnClickListener(view -> {
            drawerLayout.closeDrawers();
            new MaterialDialog.Builder(mContext)
                    .input("昵称", "", (@NonNull MaterialDialog dialog, CharSequence input) -> {
                        if (input.length() == 0) {
                            LToast.show(mContext, "请输入昵称");
                            return;
                        }
                        mPresenter.setUserProfile(UserProfile.NICKNAME, input.toString());
                        dialog.dismiss();
                        loadingDialog.show();
                    }).build().show();
        });
        // 获取广告条
        View bannerView = BannerManager.getInstance(this).getBannerView(this);
        // 获取要嵌入广告条的布局
        LinearLayout bannerLayout = (LinearLayout) findViewById(R.id.ll_banner);
        // 将广告条加入到布局中
        bannerLayout.addView(bannerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            LToast.show(this, "...");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public void showMsg(CharSequence msg) {
        LToast.show(this, msg);
    }

    @Override
    public void bindData(User data) {
        loadingDialog.dismiss();
        if (data == null || TextUtils.isEmpty(data.getNickname())) return;
        mTextUsername.setText(data.getNickname());
    }

    private int dialogHeight = 0;
    private int dialogWidth = 0;

    @Override
    public void showAD() {
        if (dialogHeight == 0) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            dialogHeight = metrics.heightPixels / 2;
            dialogWidth = metrics.widthPixels;
        }
        OffersManager.getInstance(this).showOffersWallDialog(this, dialogWidth, dialogHeight, this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                Snackbar.make(v, "Developer? Help us expand this app", Snackbar.LENGTH_LONG)
                        .setAction("Join us", (View view) -> {
                            Intent intent = new Intent(Intent.ACTION_DEFAULT, Uri.parse("https://github.com/XanthusL/ApkBus"));
                            startActivity(intent);
                        }).show();
                break;
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mPresenter.sendScrollSignal(tab);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        mPresenter.sendScrollSignal(tab);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitBy2Click();
        }
        return false;
    }

    private boolean isExit = false;

    private void exitBy2Click() {
        Timer timer;
        if (!isExit) {
            isExit = true;
            LToast.show(this, "再按一次退出程序");
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        } else {
            finish();
        }
    }

    @Override
    public void onDialogClose() {
        mPresenter.onADClosed();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mPresenter.pageScrolled();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onRequestSuccess() {

    }

    @Override
    public void onSwitchBanner() {

    }

    @Override
    public void onRequestFailed() {

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ArticleFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "热门博文";
                case 1:
                    return "最新博文";
                case 2:
                    return "精品源码";
                case 3:
                    return "一周热点";
                case 4:
                    return "实例教程";
            }
            return null;
        }
    }
}
