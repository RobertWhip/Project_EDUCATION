package com.example.mechanic_pc.uzhgorodschools._activity;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.mechanic_pc.uzhgorodschools.R;
import com.example.mechanic_pc.uzhgorodschools.data.db.SchoolDatabaseHelper;
import com.example.mechanic_pc.uzhgorodschools.service.NotificationService;
import com.example.mechanic_pc.uzhgorodschools.utils.Constant;
import com.example.mechanic_pc.uzhgorodschools.utils.InternetConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;

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

    /* ---- QA ---- */
        private static HashMap<String, String> schoolsUrls = new HashMap<>();
    /* ---- QA ---- */

    private WebView site;

    private String currentPhone = "0";
    private String currentEmail = "none";
    private String currentUrl = "";
    private String schoolName = "";
    private int schoolId = 1;

    private boolean choosedSite = false;
    private boolean start = true;

    private Menu menu = null;
    private Context context;

    private SQLiteDatabase db;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!(new InternetConnection(this).connectionAvailable())) {
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
                    if (!currentPhone.equals(Constant.EMAIL_EMPTY)) {
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
                    if (!currentPhone.equals(Constant.PHONE_EMPTY)) {
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
                    if (fabExpanded){
                        closeSubMenusFab();
                    } else {
                        openSubMenusFab();
                    }
                }
            });

            // Only main FAB is visible in the beginning
            closeSubMenusFab();
            hideFab();
        // /> FAB


        context = this;

        site = (WebView) findViewById(R.id.site);
        WebSettings webViewSettings = site.getSettings();
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setBuiltInZoomControls(true);

        if(!isMyServiceRunning(NotificationService.class)){
            startService();
        }

        // Notification clicked <
            try {

                String url = getIntent().getExtras().get(Constant.URL).toString();
                String newUrl = getIntent().getExtras().get(Constant.NEW_URL).toString();
                setTitle(getIntent().getExtras().get(Constant.SCHOOL_NAME).toString());

                currentUrl = url;

                site.loadUrl(currentUrl + newUrl);
                site.setWebViewClient(new WebViewClient());

            } catch(Exception e) {
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

        /* ---- QA ---- */
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
         /* ---- QA ---- */
    }

    private void hideFab() {
        fab.setVisibility(View.INVISIBLE);
    }

    private void showFab() {
        fab.setVisibility(View.VISIBLE);
    }

    //closes FAB submenus
    private void closeSubMenusFab(){
        layoutFabCall.setVisibility(View.INVISIBLE);
        layoutFabSendSms.setVisibility(View.INVISIBLE);
        layoutFabSendEmail.setVisibility(View.INVISIBLE);
        fab.setImageResource(R.drawable.ic_contacts_black_24dp);
        fabExpanded = false;
    }

    //Opens FAB submenus
    private void openSubMenusFab(){
        layoutFabCall.setVisibility(View.VISIBLE);
        layoutFabSendSms.setVisibility(View.VISIBLE);
        layoutFabSendEmail.setVisibility(View.VISIBLE);
        //Change settings icon to 'X' icon
        fab.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp);
        fabExpanded = true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (site.canGoBack()) {
            site.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;

        if(choosedSite) {
            getMenuInflater().inflate(R.menu.main, menu);
        }
        return true;
    }

    private void setCheckedIfCheckedShowingNotification(){
        boolean checked = checkIfCurrentUrlIsCheckedShowingNotification();
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
                    new String[] {SchoolDatabaseHelper._SHOW,
                            SchoolDatabaseHelper._ID},
                    SchoolDatabaseHelper._ID + " = ?",
                    new String[] { Integer.toString(schoolId) },
                    null, null, null);

            if(cursor.moveToFirst()){
                checked = cursor.getString(0).equals("1");
                Toast.makeText(this, cursor.getString(0) + ":" + cursor.getString(1) + ":"
                        + schoolId, Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_show_notification) {
            // set checked or unchecked the checkbox
            // and then write it to the database
            if(item.isChecked()) {
                boolean writed = writeToDatabaseNews(false);
                if(writed) {
                    item.setChecked(false);
                    setDbOldTitleToNull(schoolId);
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
        }

        return super.onOptionsItemSelected(item);
    }

        private void startService() {
            stopService(new Intent(this, NotificationService.class));
            startService(new Intent(this, NotificationService.class));
        }

        private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean setDbOldTitleToNull (int id) {
        boolean result = false;

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
            result = true;

        } catch (Exception e) {
            result = false;
            Toast.makeText(this, getResources().getString(R.string.error_cannot_write_data),
                    Toast.LENGTH_SHORT).show();
        }

        return result;
    }

    private boolean writeToDatabaseNews(boolean checked) {
        boolean result = false;

        try {
            SQLiteOpenHelper shopDatabaseHelper = new SchoolDatabaseHelper(this);
            db = shopDatabaseHelper.getWritableDatabase();

            ContentValues values = new ContentValues();

            values.put(SchoolDatabaseHelper._URL, currentUrl);
            values.put(SchoolDatabaseHelper._SHOW, checked ? "1" : "0");

            db.update(SchoolDatabaseHelper.TABLE_NAME,
                    values,
                    SchoolDatabaseHelper._ID + " = ?",
                    new String[] { ""+schoolId });

            db.close();
            result = true;

        } catch (Exception e) {
            result = false;
            Toast.makeText(this, getResources().getString(R.string.error_cannot_write_data),
                    Toast.LENGTH_SHORT).show();
        }

        return result;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.school_shishlivtsi) {
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_shishlivtsi));
            schoolId = 1;
        } else if (id == R.id.school_kontsivska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_kontsivska));
            schoolId = 2;
        } else if (id == R.id.school_onokivska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_onokivska));
            schoolId = 3;
        } else if (id == R.id.school_surtivska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_surtivska));
            schoolId = 4;
        } else if (id == R.id.school_esenska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_esenska));
            schoolId = 5;
        } else if (id == R.id.school_malodobronska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_malodobronska));
            schoolId = 6;
        } else if (id == R.id.school_velikolazivska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_velikolazivska));
            schoolId = 7;
        } else if (id == R.id.school_storozhnitska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_storozhnitska));
            schoolId = 8;
        } else if (id == R.id.licey_velikodobronska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.licey_velikodobronska));
            schoolId = 9;
        } else if (id == R.id.school_kamyanitska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_kamyanitska));
            schoolId = 10;
        } else if (id == R.id.school_ruskokomarivska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_ruskokomarivska));
            schoolId = 11;
        } else if (id == R.id.school_velikoheevtska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_velikoheevetska));
            schoolId = 12;
        } else if (id == R.id.school_velikodobronska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_velikodobronska));
            schoolId = 13;
        } else if (id == R.id.school_rativetska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_rativetska));
            schoolId = 14;
        } else if (id == R.id.school_tisaashvanska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_tisaashvanska));
            schoolId = 15;
        } else if (id == R.id.school_serednanska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_serednanska));
            schoolId = 16;
        } else if (id == R.id.school_koritnanska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_koritnanska));
            schoolId = 17;
        } else if (id == R.id.school_chernivskoi){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_chernivskoi));
            schoolId = 18;
        } else if (id == R.id.school_maloheevetska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_maloheevetska));
            schoolId = 19;
        } else if (id == R.id.school_palad_komarivetska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_palad_komarivetska));
            schoolId = 20;
        } else if (id == R.id.school_hudlivska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_hudlivska));
            schoolId = 21;
        } else if (id == R.id.school_kiblarivska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_kiblarivska));
            site.loadUrl(getResources().getString(R.string.site_school_kiblarivska));
            schoolId = 22;
        } else if (id == R.id.school_solokivska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_solovkivska));
            schoolId = 23;
        } else if (id == R.id.school_kholmkivska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_kholmkivksa));
            schoolId = 24;
        } else if (id == R.id.school_solomonivska){
            currentUrl = schoolsUrls.get(getResources().getString(R.string.school_solomonivska));
            schoolId = 25;
        } else if (id == R.id.mon_gov){
            currentUrl = "http://mon.gov.ua/";
            schoolId = 26;
        } else if (id == R.id.metodkab){
            currentUrl = "http://metodkab-uz.ucoz.ua/";
            schoolId = 27;
        }

        setSchoolData(schoolId);

        choosedSite = true;
        if(start) {
            onCreateOptionsMenu(menu);
            start = false;
        }
        if(schoolId == 3 || schoolId == 15 || schoolId == 20 || schoolId == 23)
            menu.findItem(R.id.action_show_notification).setVisible(false);
        else {
            menu.findItem(R.id.action_show_notification).setVisible(true);
            setCheckedIfCheckedShowingNotification();
        }

        /* ---- QA ---- */
            if (new InternetConnection (this).connectionAvailable()){
                ParsingTask task = new ParsingTask();
                task.execute(currentUrl);
            }
         /* ---- QA ---- */

        site.loadUrl(currentUrl);
        site.setWebViewClient(new WebViewClient());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private String getOneWord(String word) {
        return (word.split(" "))[0];
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

    private boolean setSchoolData (int id) {
        boolean result = false;

        if (!setContactData(id))
            hideFab();
        else
            showFab();

        setSchoolNameTitle(id);



        return result;
    }

    private boolean setSchoolNameTitle(int id) {
        boolean result = false;
        SQLiteDatabase db;
        Cursor cursor;

        try {
            SQLiteOpenHelper helper = new SchoolDatabaseHelper(this);
            db = helper.getReadableDatabase();

            cursor = db.query(SchoolDatabaseHelper.TABLE_NAME,
                    new String[]{SchoolDatabaseHelper._SCHOOL},
                    SchoolDatabaseHelper._ID + " = ?",
                    new String[]{Integer.toString(id)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                schoolName = cursor.getString(0);
                setTitle(getOneWord(schoolName));
                    result = true;
            }

            cursor.close();

        } catch (Exception e) {
            result = false;
        }

        return result;
    }

    /* ---- QA ---- */
        private class ParsingTask extends AsyncTask<String, Void, Void>{
            String url;
            String parsedTitle;
            String parsedDate = "";
            String parsedUrl = "";
            Document doc = null;
            @Override
            protected Void doInBackground(String... args){
                url = args[0];
                Elements titleElements = null, dateElements = null;
                try {

                    doc = Jsoup.connect(url).get();

                    if(!url.equals(getResources().getString(R.string.site_school_kiblarivska)))
                        titleElements = doc.getElementsByAttributeValue("class", "eTitle");
                    else
                        titleElements = doc.getElementsByAttributeValue("class", "news-item");
                        dateElements = doc.getElementsByAttributeValue("class", "eDetails");
                    } catch(Exception e) {
                    e.printStackTrace();
                    }

                try {
                    if (doc != null) {
                        if (url.equals(getResources().getString(R.string.site_school_shishlivtsi))) {
                            parsedUrl = titleElements.get(0).child(0).attr("href");
                            parsedTitle = titleElements.get(0).text();
                            parsedDate = dateElements.get(0).child(5).child(1).text();

                        } else if (url.equals(getResources().getString(R.string.site_mon_gov))) {
                            titleElements = doc.getElementsByAttributeValue("id", "tab1");
                            parsedUrl = titleElements.get(0).child(0).child(0).child(2).attr("href");
                            parsedTitle = titleElements.get(0).child(0).child(0).child(2).child(0).text();

                        } else if (url.equals(getResources().getString(R.string.site_metodkab))) {
                            parsedUrl = titleElements.get(0).child(0).attr("href");
                            parsedTitle = titleElements.get(0).child(0).text();

                        } else if (url.equals(getResources().getString(R.string.site_school_kontsivska))) {
                            parsedUrl = titleElements.get(1).child(0).attr("href");
                            parsedTitle = titleElements.get(1).text();

                            try {
                                parsedDate = dateElements.get(1).child(7).child(1).text();
                            } catch (Exception e) {
                                parsedDate = dateElements.get(1).child(5).child(1).text();
                            }

                        } else if (url.equals(getResources().getString(R.string.site_school_surtivska))) {
                            parsedUrl = titleElements.get(0).child(0).attr("href");
                            parsedTitle = titleElements.get(0).text();
                            parsedDate = dateElements.get(0).child(5).child(1).text();

                        } else if (url.equals(getResources().getString(R.string.site_school_esenska))) {
                            parsedUrl = titleElements.get(0).child(0).attr("href");
                            parsedTitle = titleElements.get(0).text();
                            parsedDate = dateElements.get(0).child(5).child(1).text();

                            if (parsedDate.equals("sulieszeny")) {
                            parsedDate = dateElements.get(0).child(7).child(1).text();
                            }

                        } else if (url.equals(getResources().getString(R.string.site_school_malodobronska))) {
                            parsedUrl = titleElements.get(0).child(0).attr("href");
                            parsedTitle = titleElements.get(0).text();
                            parsedDate = dateElements.get(0).child(1).text();

                        } else if (url.equals(getResources().getString(R.string.site_school_velikolazivska))) {
                            parsedUrl = titleElements.get(0).child(0).attr("href");
                            parsedTitle = titleElements.get(0).text();
                            parsedDate = dateElements.get(0).child(5).child(1).text();

                        } else if (url.equals(getResources().getString(R.string.site_school_storozhnitska))) {
                            parsedUrl = titleElements.get(0).child(0).attr("href");
                            parsedTitle = titleElements.get(0).text();
                            parsedDate = dateElements.get(0).child(5).child(1).text();

                        } else if (url.equals(getResources().getString(R.string.site_licey_velikodobronska))) {
                            parsedUrl = titleElements.get(0).child(0).attr("href");
                            parsedTitle = titleElements.get(0).text();
                            parsedDate = dateElements.get(0).child(1).text();

                        } else if (url.equals(getResources().getString(R.string.site_school_kamyanitska))) {
                            parsedUrl = titleElements.get(0).child(0).attr("href");
                            parsedTitle = titleElements.get(0).text();
                            parsedDate = dateElements.get(0).child(2).text();

                        } else if (url.equals(getResources().getString(R.string.site_school_ruskokomarivska))) {
                            parsedUrl = titleElements.get(0).child(0).attr("href");
                            parsedTitle = titleElements.get(0).text();
                            parsedDate = dateElements.get(0).child(5).child(1).text();

                        } else if (url.equals(getResources().getString(R.string.site_school_velikoheevetska))) {
                            parsedUrl = titleElements.get(0).child(0).attr("href");
                            parsedTitle = titleElements.get(0).text();
                            parsedDate = dateElements.get(0).child(5).child(1).text();

                        } else if (url.equals(getResources().getString(R.string.site_school_velikodobronska))) {
                            titleElements = doc.getElementsByAttributeValue("class", "ja-bullettin clearfix");
                            try {
                                parsedUrl = titleElements.get(0).child(0).child(0).child(0).attr("href");
                                parsedTitle = titleElements.get(0).child(0).child(0).child(0).text();
                                parsedDate = titleElements.get(0).child(0).child(0).child(1).text();
                            } catch (Exception e) {
                                parsedUrl = titleElements.get(0).child(0).child(1).child(0).attr("href");
                                parsedTitle = titleElements.get(0).child(0).child(1).child(0).text();
                                parsedDate = titleElements.get(0).child(0).child(1).child(1).text();
                            }
                        } else if (url.equals(getResources().getString(R.string.site_school_rativetska))) {
                            titleElements = doc.getElementsByAttributeValue("class", "eMessage");
                            parsedTitle = titleElements.get(0).child(0).text();

                            titleElements = doc.getElementsByAttributeValue("class", "entryReadAllLink");
                            parsedUrl = titleElements.get(0).attr("href");
                            parsedDate = dateElements.get(0).child(5).child(1).text();

                        } else if (url.equals(getResources().getString(R.string.site_school_serednanska))) {
                            parsedUrl = titleElements.get(0).child(0).attr("href");
                            parsedTitle = titleElements.get(0).text();
                            parsedDate = dateElements.get(0).child(5).child(1).text();

                        } else if (url.equals(getResources().getString(R.string.site_school_koritnanska))) {
                            parsedUrl = titleElements.get(0).child(0).attr("href");
                            parsedTitle = titleElements.get(0).text();
                            parsedDate = dateElements.get(0).child(4).child(1).text();

                        } else if (url.equals(getResources().getString(R.string.site_school_chernivskoi))) {
                            parsedUrl = titleElements.get(0).child(0).attr("href");
                            parsedTitle = titleElements.get(0).text();
                            parsedDate = dateElements.get(0).child(5).child(1).text();

                        } else if (url.equals(getResources().getString(R.string.site_school_maloheevetska))) {
                            parsedUrl = titleElements.get(0).child(0).attr("href");
                            parsedTitle = titleElements.get(0).text();
                            parsedDate = dateElements.get(0).child(5).child(1).text();

                        } else if (url.equals(getResources().getString(R.string.site_school_hudlivska))) {
                            parsedUrl = titleElements.get(0).child(0).attr("href");
                            parsedTitle = titleElements.get(0).text();
                            parsedDate = dateElements.get(0).child(5).child(1).text();

                        } else if (url.equals(getResources().getString(R.string.site_school_kiblarivska))) {
                            parsedTitle = titleElements.get(0).child(1).text();
                            parsedDate = titleElements.get(0).child(0).text();
                            parsedUrl = titleElements.get(0).child(2).attr("href");

                        } else if (url.equals(getResources().getString(R.string.site_school_kholmkivksa))) {
                            parsedTitle = titleElements.get(0).child(0).text();
                            parsedDate = dateElements.get(0).child(5).child(1).text();
                            parsedUrl = titleElements.get(0).child(0).attr("href");

                        } else if (url.equals(getResources().getString(R.string.site_school_solomonivska))) {
                            parsedTitle = titleElements.get(0).child(0).text();
                            parsedDate = dateElements.get(0).child(5).child(1).text();
                            parsedUrl = titleElements.get(0).child(0).attr("href");
                        }

                    } else
                        parsedTitle = "";
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.error_at_parsing), Toast.LENGTH_SHORT).show();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                Toast.makeText(MainActivity.this, parsedTitle, Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this, parsedUrl, Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this, parsedDate, Toast.LENGTH_LONG).show();
            }
        }
    /* ---- QA ---- */
}
