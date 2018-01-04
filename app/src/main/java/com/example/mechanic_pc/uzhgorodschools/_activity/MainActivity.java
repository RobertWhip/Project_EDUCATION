package com.example.mechanic_pc.uzhgorodschools._activity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.net.Uri;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mechanic_pc.uzhgorodschools.R;
import com.example.mechanic_pc.uzhgorodschools.service.singleton.SingletonService;
import com.example.mechanic_pc.uzhgorodschools.data.db.SchoolDatabaseHelper;
import com.example.mechanic_pc.uzhgorodschools.utils.Constant;
import com.example.mechanic_pc.uzhgorodschools.utils.InternetConnection;
import com.example.mechanic_pc.uzhgorodschools.utils.LocaleManager;
import com.example.mechanic_pc.uzhgorodschools.utils.SchoolDataManager;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // FAB
        // boolean flag to know if main FAB is in open or closed state.
        private boolean fabExpanded = false;
        private FloatingActionButton fab;

        // Linear layout holding the Save submenu
        private LinearLayout layoutFabCall;

        // Linear layout holding the Edit submenu
        private LinearLayout layoutFabSendSms;
        private LinearLayout layoutFabSendEmail;

    /* QA code
        private static HashMap<String, String> schoolsUrls = new HashMap<>();
    */

    private static final String CURRENT_URL = "currentUrl";
    private static final String CURRENT_ID = "currentId";
    private static final String TRANSITION = "transition";

    private String currentPhone = "0";
    private String currentEmail = "none";
    private String currentUrl = "";
    private String currentLanguage = "";
    private int currentId = 0;
    private int transition = 0;
    private boolean animationInProcess = false;

    private WebView site;
    private Menu menu = null;
    private SQLiteDatabase db;
    private Cursor cursor;
    private ImageView splashZero, splashOne, progressBack;
    private ProgressBar progress;
    private Animation anim;
    private WebViewClientWithProgressBar webClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        webClient = new WebViewClientWithProgressBar();

        if (!(new InternetConnection(this).connectionAvailable())) {
            Toast.makeText(this, getResources().getString(R.string.error_no_internet),
                    Toast.LENGTH_LONG).show();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // FAB <
        fab = (FloatingActionButton) findViewById(R.id.fab);

        layoutFabCall = (LinearLayout) findViewById(R.id.layoutFabCall);
        layoutFabSendSms = (LinearLayout) findViewById(R.id.layoutFabSendSms);
        layoutFabSendEmail = (LinearLayout) findViewById(R.id.layoutFabSendEmail);

        layoutFabCall.findViewById(R.id.fabCall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!currentPhone.equals(Constant.PHONE_EMPTY)) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel",
                            currentPhone, null));
                    startActivity(Intent.createChooser(intent,
                            (getResources().getString(R.string.call_text) +
                                    " " + getResources().getString(R.string.via))));
                } else {
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.error_no_contanct_data),
                            Toast.LENGTH_SHORT).show();
                }
                closeSubMenusFab();
            }
        });

        layoutFabSendSms.findViewById(R.id.fabSendSms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!currentPhone.equals(Constant.PHONE_EMPTY)) {
                    Intent sendSms = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms",
                            currentPhone, null));
                    startActivity(Intent.createChooser(sendSms,
                            (getResources().getString(R.string.send_sms_text) +
                                    " " + getResources().getString(R.string.via))));
                } else {
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.error_no_contanct_data),
                            Toast.LENGTH_SHORT).show();
                }
                closeSubMenusFab();
            }
        });

        layoutFabSendEmail.findViewById(R.id.fabSendEmail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!currentEmail.equals(Constant.EMAIL_EMPTY)) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"
                            + currentEmail));

                    startActivity(Intent.createChooser(emailIntent,
                            (getResources().getString(R.string.send_email_text) +
                                    " " + getResources().getString(R.string.via))));
                } else {
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.error_no_contanct_data),
                            Toast.LENGTH_SHORT).show();
                }
                closeSubMenusFab();
            }
        });

        // When main Fab is clicked, it expands if not expanded already.
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabExpanded && !animationInProcess) {
                    closeSubMenusFab();
                } else if (!fabExpanded && !animationInProcess){
                    openSubMenusFab();
                }
            }
        });

        // Only main FAB is visible in the beginning
        setInvisibleSubMenusFab();
        hideFab();
        // /> FAB

        site = (WebView) findViewById(R.id.site);
        WebSettings webViewSettings = site.getSettings();
        webViewSettings.setJavaScriptEnabled(true);
        site.getSettings().setBuiltInZoomControls(true);
        site.getSettings().setDisplayZoomControls(false);

        SingletonService.execute(this);

        splashZero = (ImageView) findViewById(R.id.splash_0); // picture
        splashOne = (ImageView) findViewById(R.id.splash_1); // arrow
        progress = (ProgressBar) findViewById(R.id.progress_bar);
        progressBack = (ImageView) findViewById(R.id.progress_back);

        anim = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.05f);
        anim.setDuration(3000);
        anim.setRepeatCount(-1);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setInterpolator(new LinearInterpolator());

        splashOne.startAnimation(anim);

        // Notification clicked <
        try {
            if (getIntent().getExtras().get(Constant.NOTIFICATION_CLICKED).equals("1")) {
                hideSplash();
                currentUrl = getIntent().getExtras().get(Constant.URL).toString();
                String newUrl = getIntent().getExtras().get(Constant.NEW_URL).toString();
                currentId = Integer.parseInt(getIntent().getExtras().get(Constant.ID).toString());

                setSchoolData(currentId);
                if (newUrl.startsWith("/"))
                    currentUrl = currentUrl + newUrl;
                else
                    currentUrl = newUrl;

                site.loadUrl(currentUrl);
                site.setWebViewClient(webClient);


                //setCheckedIfCheckedShowingNotification();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // /> Notification clicked

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /* QA code
            schoolsUrls.put(getResources().getString(R.string.school_shishlivtsi), getResources().getString(R.string.site_school_shishlivtsi));
            schoolsUrls.put(getResources().getString(R.string.school_kontsivska), getResources().getString(R.string.site_school_kontsivska));
            schoolsUrls.put(getResources().getString(R.string.school_onokivska), getResources().getString(R.string.site_school_onokivska));
            schoolsUrls.put(getResources().getString(R.string.school_surtivska), getResources().getString(R.string.site_school_surtivska));
            schoolsUrls.put(getResources().getString(R.string.school_esenska), getResources().getString(R.string.site_school_esenska));
            schoolsUrls.put(getResources().getString(R.string.school_malodobronska), getResources().getString(R.string.site_school_malodobronska));
            schoolsUrls.put(getResources().getString(R.string.school_velikolazivska), getResources().getString(R.string.site_school_velikolazivska));
            schoolsUrls.put(getResources().getString(R.string.school_storozhnitska), getResources().getString(R.string.site_school_storozhnitska));
            schoolsUrls.put(getResources().getString(R.string.licey_velikodobronska), getResources().getString(R.string.site_licey_velikodobronska));
            schoolsUrls.put(getResources().getString(R.string.school_kamyanitska), getResources().getString(R.string.site_school_kamyanitska));
            schoolsUrls.put(getResources().getString(R.string.school_ruskokomarivska), getResources().getString(R.string.site_school_ruskokomarivska));
            schoolsUrls.put(getResources().getString(R.string.school_velikoheevetska), getResources().getString(R.string.site_school_velikoheevetska));
            schoolsUrls.put(getResources().getString(R.string.school_velikodobronska), getResources().getString(R.string.site_school_velikodobronska));
            schoolsUrls.put(getResources().getString(R.string.school_rativetska), getResources().getString(R.string.site_school_rativetska));
            schoolsUrls.put(getResources().getString(R.string.school_tisaashvanska), getResources().getString(R.string.site_school_tisaashvanska));
            schoolsUrls.put(getResources().getString(R.string.school_serednanska), getResources().getString(R.string.site_school_serednanska));
            schoolsUrls.put(getResources().getString(R.string.school_koritnanska), getResources().getString(R.string.site_school_koritnanska));
            schoolsUrls.put(getResources().getString(R.string.school_chernivskoi), getResources().getString(R.string.site_school_chernivskoi));
            schoolsUrls.put(getResources().getString(R.string.school_maloheevetska), getResources().getString(R.string.site_school_maloheevetska));
            schoolsUrls.put(getResources().getString(R.string.school_palad_komarivetska), getResources().getString(R.string.site_school_palad_komarivetska));
            schoolsUrls.put(getResources().getString(R.string.school_hudlivska), getResources().getString(R.string.site_school_hudlivska));
            schoolsUrls.put(getResources().getString(R.string.school_kiblarivska), getResources().getString(R.string.site_school_kiblarivska));
            schoolsUrls.put(getResources().getString(R.string.school_solovkivska), getResources().getString(R.string.site_school_solovkivska));
            schoolsUrls.put(getResources().getString(R.string.school_kholmkivksa), getResources().getString(R.string.site_school_kholmkivksa));
            schoolsUrls.put(getResources().getString(R.string.school_solomonivska), getResources().getString(R.string.site_school_solomonivska));
            schoolsUrls.put(getResources().getString(R.string.mon_gov), getResources().getString(R.string.site_mon_gov));
            schoolsUrls.put(getResources().getString(R.string.zippo), getResources().getString(R.string.site_zippo));
        */

        if (savedInstanceState != null) {
            currentUrl = savedInstanceState.getString(CURRENT_URL, "");
            currentId = savedInstanceState.getInt(CURRENT_ID, 0);
            transition = savedInstanceState.getInt(TRANSITION, 0);
            orientationChanged();
        }

        SharedPreferences sharedPreferences = getSharedPreferences(Constant.PREFERENCES,
                Context.MODE_PRIVATE);
        if (sharedPreferences.contains(Constant.LANGUAGE))
            currentLanguage = sharedPreferences.getString(Constant.LANGUAGE, "");
        else
            currentLanguage = getResources().getString(R.string.language);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(CURRENT_URL, currentUrl);
        outState.putInt(CURRENT_ID, currentId);
        outState.putInt(TRANSITION, transition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (site.canGoBack() && transition != 0){
            transition--;
            site.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;

        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean changed = false;

        if (id == R.id.action_show_notification) {
            // set checked or unchecked the checkbox
            // and then write it to the database
            if(item.isChecked()) {
                boolean writed = writeToDatabaseNews(false);
                if(writed) {
                    item.setChecked(false);
                    setDbOldTitleToNull(currentId);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.error_cannot_write_data),
                            Toast.LENGTH_SHORT).show();
                }
            } else if(!item.isChecked()){
                boolean writed = writeToDatabaseNews(true);
                if(writed) {
                    item.setChecked(true);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.error_cannot_write_data),
                            Toast.LENGTH_SHORT).show();
                }
            }
            return true;

        } else if (id == R.id.action_refresh) {
            site.reload();
            return true;
        } else if (id == R.id.action_english) {
            if (!currentLanguage.equals(Constant.ENGLISH)) {
                currentLanguage = Constant.ENGLISH;
                changed = true;
            }
        } else if (id == R.id.action_ukrainian) {
            if (!currentLanguage.equals(Constant.UKRAINIAN)) {
                currentLanguage = Constant.UKRAINIAN;
                changed = true;
            }
        } else if (id == R.id.action_hungarian) {
            if (!currentLanguage.equals(Constant.HUNGARIAN)) {
                currentLanguage = Constant.HUNGARIAN;
                changed = true;
            }
        }

        if (id == R.id.action_english || id == R.id.action_ukrainian ||
                                            id == R.id.action_hungarian) {
            SharedPreferences sharedPreferences = getSharedPreferences(Constant.PREFERENCES,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constant.LANGUAGE, currentLanguage);
            editor.apply();

            if (changed) {
                recreate();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        transition = 0;

        loadWebPageAndSchoolData(id, true);
        setInvisibleSubMenusFab();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(currentId == 0) {
            menu.findItem(R.id.action_show_notification).setVisible(false);
            menu.findItem(R.id.action_refresh).setVisible(false);
        } else
            menu.findItem(R.id.action_show_notification)
                .setChecked(checkIfCurrentUrlIsCheckedShowingNotification());
        return true;
    }

    private void orientationChanged() {
        if (currentId != 0) {
            site.loadUrl(currentUrl);
            site.setWebViewClient(webClient);
            hideSplash();
            setSchoolData(currentId);
        }
    }

    private void hideFab() {
        fab.setVisibility(View.INVISIBLE);
    }

    private void showFab() {
        fab.setVisibility(View.VISIBLE);
    }

    private void setCheckedIfCheckedShowingNotification(){
        boolean checked = checkIfCurrentUrlIsCheckedShowingNotification();
        //((MenuItem)findViewById(R.id.action_show_notification)).setChecked(checked);
        menu.findItem(R.id.action_show_notification).setChecked(checked);
    }

    private boolean checkIfCurrentUrlIsCheckedShowingNotification() {
        // TO DO get _SHOW from database
        // if == 0 then false, if == 1 then true
        boolean checked = false;

        try {
            SQLiteOpenHelper shopDatabaseHelper = new SchoolDatabaseHelper(this);
            db = shopDatabaseHelper.getReadableDatabase();

            cursor = db.query(SchoolDatabaseHelper.TABLE_NAME,
                    new String[] {SchoolDatabaseHelper._SHOW},
                    SchoolDatabaseHelper._ID + " = ?",
                    new String[] { Integer.toString(currentId) },
                    null, null, null);

            if(cursor.moveToFirst()){
                checked = cursor.getString(0).equals("1");
            }

            cursor.close();
            db.close();

        } catch (SQLiteException e) {
            Toast.makeText(this, getResources().getString(R.string.error_database_unavailable), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, getResources().getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
        }

        return checked;
    }
    //closes FAB submenus
    private void closeSubMenusFab(){
        animationInProcess = true;
        Handler handler = new Handler();

        fab.setImageResource(R.drawable.ic_contacts_black_24dp);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.scale_to_big);
        fab.startAnimation(animation);

        anim = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, -0.6f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f);
        anim.setInterpolator(new LinearInterpolator());

        anim.setDuration(300);
        layoutFabSendSms.findViewById(R.id.textField2).startAnimation(anim);
        layoutFabSendEmail.findViewById(R.id.textField1).startAnimation(anim);
        layoutFabCall.findViewById(R.id.textField3).startAnimation(anim);

        anim.setDuration(600);
        layoutFabCall.startAnimation(anim);
        layoutFabSendSms.startAnimation(anim);
        layoutFabSendEmail.startAnimation(anim);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setInvisibleSubMenusFab();
                clearAnimation();
                fabExpanded = false;
                animationInProcess = false;
            }
        }, 600);
    }

    //Opens FAB submenus
    private void openSubMenusFab(){
        animationInProcess = true;
        Handler handler = new Handler();

        setVisibleSubMenusFab();

        fab.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.scale_to_big);
        fab.startAnimation(animation);


        anim = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_PARENT, -0.6f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f);
        anim.setInterpolator(new LinearInterpolator());

        anim.setDuration(300);
        layoutFabCall.findViewById(R.id.textField3).startAnimation(anim);
        layoutFabSendSms.findViewById(R.id.textField2).startAnimation(anim);
        layoutFabSendEmail.findViewById(R.id.textField1).startAnimation(anim);

        anim = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_PARENT, -0.6f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f);
        anim.setInterpolator(new LinearInterpolator());

        anim.setDuration(300);

        layoutFabSendEmail.startAnimation(anim);
        layoutFabSendSms.startAnimation(anim);
        layoutFabCall.startAnimation(anim);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                clearAnimation();
                fabExpanded = true;
                animationInProcess = false;
            }
        }, 600);
    }

    private void clearAnimation() {
        layoutFabCall.clearAnimation();
        layoutFabSendSms.clearAnimation();
        layoutFabSendEmail.clearAnimation();
        anim.cancel();
        anim.reset();
    }

    private void setInvisibleSubMenusFab() {
        layoutFabCall.setVisibility(View.INVISIBLE);
        layoutFabSendSms.setVisibility(View.INVISIBLE);
        layoutFabSendEmail.setVisibility(View.INVISIBLE);
    }
    private void setVisibleSubMenusFab() {
        layoutFabCall.setVisibility(View.VISIBLE);
        layoutFabSendSms.setVisibility(View.VISIBLE);
        layoutFabSendEmail.setVisibility(View.VISIBLE);
    }

    private void hideSplash() {
        splashOne.clearAnimation();
        anim.cancel();
        anim.reset();
        splashZero.setVisibility(View.GONE);
        splashOne.setVisibility(View.GONE);
    }

    private void setDbOldTitleToNull (int id) {
        try {
            SQLiteOpenHelper shopDatabaseHelper = new SchoolDatabaseHelper(this);
            db = shopDatabaseHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(SchoolDatabaseHelper._TITLE, "");

            db.update(SchoolDatabaseHelper.TABLE_NAME,
                    values,
                    SchoolDatabaseHelper._ID + " = ?",
                    new String[] { Integer.toString(id)});

            db.close();

        } catch (Exception e) {

            Toast.makeText(this, getResources().getString(R.string.error_cannot_write_data),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean writeToDatabaseNews(boolean checked) {
        boolean result;

        try {
            SQLiteOpenHelper shopDatabaseHelper = new SchoolDatabaseHelper(this);
            db = shopDatabaseHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(SchoolDatabaseHelper._URL, currentUrl);
            values.put(SchoolDatabaseHelper._SHOW, checked ? "1" : "0");

            db.update(SchoolDatabaseHelper.TABLE_NAME,
                    values,
                    SchoolDatabaseHelper._ID + " = ?",
                    new String[] { Integer.toString(currentId) });

            db.close();
            result = true;

        } catch (Exception e) {
            result = false;
            Toast.makeText(this, getResources().getString(R.string.error_cannot_write_data),
                    Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    private void loadWebPageAndSchoolData(int itemId, boolean pickedInDrawer) {
        if (pickedInDrawer) {
            currentId = SchoolDataManager.getSchoolId(itemId);
        }

        hideSplash();
        setSchoolData(currentId);
        menuItemVisibility();

        if (pickedInDrawer) {
            currentUrl = getUrlById(currentId);
            site.loadUrl(currentUrl);
            site.setWebViewClient(webClient);
        }

        /* QA code
        if (new InternetConnection(this).connectionAvailable()) {
            ParsingTask task = new ParsingTask();
            task.execute(currentUrl);
        }*/

    }
    private String getUrlById (int id) {
        return SchoolDataManager.getSchoolSiteUrl(this, id);
    }

    private void menuItemVisibility () {
            if (currentId == 3 || currentId == 15 || currentId == 20 || currentId == 23 || currentId == 0)
                menu.findItem(R.id.action_show_notification).setVisible(false);
            else {
                if(!menu.findItem(R.id.action_show_notification).isVisible())
                    menu.findItem(R.id.action_show_notification).setVisible(true);
                setCheckedIfCheckedShowingNotification();
            }
            if(!menu.findItem(R.id.action_refresh).isVisible())
                menu.findItem(R.id.action_refresh).setVisible(true);
    }

    private boolean setContactData(int id) {
        boolean result = false;

        try {
            SQLiteOpenHelper helper = new SchoolDatabaseHelper(this);
            db = helper.getReadableDatabase();

            cursor = db.query(SchoolDatabaseHelper.TABLE_NAME,
                    new String[]{SchoolDatabaseHelper._TELEPHONE_NUMBER,
                            SchoolDatabaseHelper._EMAIL},
                    SchoolDatabaseHelper._ID + " = ?",
                    new String[]{Integer.toString(id)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                currentPhone = cursor.getString(0);
                currentEmail = cursor.getString(1);
                if(!currentPhone.equals(Constant.PHONE_EMPTY) || !currentEmail.equals(Constant.EMAIL_EMPTY))
                    result = true;
            }

        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    private void setSchoolData (int id) {
        if (!setContactData(id))
            hideFab();
        else
            showFab();

        setSchoolNameTitle(id);
    }

    private void setSchoolNameTitle(int id) {
        setTitle(SchoolDataManager.getSchoolTitle(this, id));
    }

    private void showProgressBar () {
        progressBack.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.scale_to_big);
        progressBack.startAnimation(animation);
        progress.startAnimation(animation);
    }

    private void hideProgressBar() {
        progressBack.setVisibility(View.INVISIBLE);
        progress.setVisibility(View.INVISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.scale_to_small);
        progressBack.startAnimation(animation);
        progress.startAnimation(animation);
    }

    private class WebViewClientWithProgressBar extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            showProgressBar();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            hideProgressBar();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);

            transition++;
            currentUrl = url;
            return super.shouldOverrideUrlLoading(view, url);
        }
    }
    /* QA code
    private void show(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    private class ParsingTask extends AsyncTask<String, Void, Void> {

        String url = "";
        String parsedTitle = "";
        String parsedUrl = "";
        Document doc = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void v) {
            show(parsedTitle + "///" +parsedUrl);
        }

        @Override
        protected Void doInBackground(String... args){
            url = args[0];

            Elements titleElements = null;
            try {

                doc = Jsoup.connect(url).timeout(Constant.SECONDS_TO_TIMEOUT).get();
                titleElements = doc.getElementsByAttributeValue("class", "eTitle");

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (doc != null) {
                try {

                    if (url.equals(getResources().getString(R.string.site_school_shishlivtsi))) {
                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_mon_gov))) {
                        titleElements = doc.getElementsByAttributeValue("class", "news-block");
                        parsedUrl = titleElements.get(0).child(1).child(0).attr("href");
                        parsedTitle = titleElements.get(0).child(1).child(0).child(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_metodkab))) {
                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).child(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_school_kontsivska))) {
                        parsedUrl = titleElements.get(1).child(0).attr("href");
                        parsedTitle = titleElements.get(1).text();

                    }
                        /* else if(url.equals(getResources().getString(R.string.site_school_onokivska))){
                             //INVALID

                        } *//*
                    else if (url.equals(getResources().getString(R.string.site_school_surtivska))) {
                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_school_esenska))) {
                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_school_malodobronska))) {
                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_school_velikolazivska))) {
                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_school_storozhnitska))) {
                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_licey_velikodobronska))) {
                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_school_kamyanitska))) {
                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_school_ruskokomarivska))) {
                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_school_velikoheevetska))) {
                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_school_velikodobronska))) {
                        titleElements = doc.getElementsByAttributeValue("class", "ja-bullettin clearfix");

                        try {
                            parsedUrl = titleElements.get(0).child(0).child(1).child(0).attr("href");
                            parsedTitle = titleElements.get(0).child(0).child(1).child(0).text();
                        } catch (Exception e) {
                            parsedUrl = titleElements.get(0).child(0).child(0).child(0).attr("href");
                            parsedTitle = titleElements.get(0).child(0).child(0).child(0).text();
                        }

                    } else if (url.equals(getResources().getString(R.string.site_school_rativetska))) {
                        titleElements = doc.getElementsByAttributeValue("class", "eMessage");
                        parsedTitle = titleElements.get(0).child(0).text();

                        titleElements = doc.getElementsByAttributeValue("class", "entryReadAllLink");
                        parsedUrl = titleElements.get(0).attr("href");

                    }
                         /* else if(url.equals(getResources().getString(R.string.site_school_tisaashvanska))){
                              //INVALID

                        } *//*
                    else if (url.equals(getResources().getString(R.string.site_school_serednanska))) {

                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_school_koritnanska))) {

                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_school_chernivskoi))) {

                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_school_maloheevetska))) {

                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).text();

                    }
                        /* else if(url.equals(getResources().getString(R.string.site_school_palad_komarivetska))){
                           //INVALID

                        } *//*
                    else if (url.equals(getResources().getString(R.string.site_school_hudlivska))) {
                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_school_kiblarivska))) {
                        titleElements = doc.getElementsByAttributeValue("class", "news-item");
                        parsedTitle = titleElements.get(0).child(1).child(0).text();
                        parsedUrl = titleElements.get(0).child(1).child(0).attr("href");

                    }
                        /* else if(url.equals(getResources().getString(R.string.site_school_solovkivska))){
                              //INVALID

                        } *//*
                    else if (url.equals(getResources().getString(R.string.site_school_kholmkivksa))) {
                        parsedTitle = titleElements.get(0).child(0).text();
                        parsedUrl = titleElements.get(0).child(0).attr("href");

                    } else if (url.equals(getResources().getString(R.string.site_school_solomonivska))) {
                        parsedTitle = titleElements.get(0).child(0).text();
                        parsedUrl = titleElements.get(0).child(0).attr("href");

                    } else if (url.equals(getResources().getString(R.string.site_zippo))) {
                        titleElements = doc.getElementsByAttributeValue("class", "title");
                        parsedTitle = titleElements.get(0).text();
                        titleElements = doc.getElementsByAttributeValue("class", "item");
                        parsedUrl = titleElements.get(0).attr("data-permalink");
                        if (parsedUrl.startsWith(url)) {
                            parsedUrl = parsedUrl.substring(url.length(), parsedUrl.length());
                        }
                    }else
                        parsedTitle = "";

                    if(!parsedUrl.startsWith("/"))
                        parsedUrl = "/" + parsedUrl;

                } catch (Exception e) {
                    e.printStackTrace();
                    parsedTitle = "";
                    parsedUrl = "";
                }
            }

            return null;
        }
    }*/
}
