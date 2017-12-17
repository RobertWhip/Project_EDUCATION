package com.example.mechanic_pc.uzhgorodschools.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.mechanic_pc.uzhgorodschools.service.singleton.SingletonService;
import com.example.mechanic_pc.uzhgorodschools._activity.MainActivity;
import com.example.mechanic_pc.uzhgorodschools.R;
import com.example.mechanic_pc.uzhgorodschools.data.SchoolData;
import com.example.mechanic_pc.uzhgorodschools.data.db.SchoolDatabaseHelper;
import com.example.mechanic_pc.uzhgorodschools.utils.Constant;
import com.example.mechanic_pc.uzhgorodschools.utils.InternetConnection;
import com.example.mechanic_pc.uzhgorodschools.utils.SchoolDataManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class NotificationService extends Service {

    private static int i = 0;

    private SQLiteDatabase db;
    private Cursor cursor;
    private Context context;

    private String currentSchoolName = "Error";
    private String currentId = "0";

    private InternetConnection net;
    private Handler handler;
    private Runnable runb;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (context == null)
            context = getApplicationContext();
        if (net == null)
            net = new InternetConnection(context);

        handler = new Handler();

        if (runb == null) {
            runb = new Runnable() {
                public void run() {
                    if (net.connectionAvailable()) {
                        try {
                            SQLiteOpenHelper shopDatabaseHelper = new SchoolDatabaseHelper(context);
                            db = shopDatabaseHelper.getReadableDatabase();

                            cursor = db.query(SchoolDatabaseHelper.TABLE_NAME,
                                    new String[]{SchoolDatabaseHelper._URL,
                                            SchoolDatabaseHelper._ID},
                                    SchoolDatabaseHelper._SHOW + " = ?",
                                    new String[]{"1"},
                                    null, null, null);

                            if (cursor.moveToFirst()) {
                                currentId = cursor.getString(1);
                                currentSchoolName = SchoolDataManager.getSchoolTitle(context,
                                        Integer.parseInt(currentId));
                                new ParsingTask().execute(cursor.getString(0));
                            }
                            while (cursor.moveToNext()) {
                                currentId = cursor.getString(2);
                                currentSchoolName = cursor.getString(1);
                                new ParsingTask().execute(cursor.getString(0));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    handler.postDelayed(this, Constant.SECONDS_TO_SLEEP);

                }

            };
        }
        handler.postDelayed(runb, 0);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        SingletonService.execute(this);
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        SingletonService.execute(this);
        super.onTaskRemoved(rootIntent);
    }


    private void notification(String notificationTitle, String notificationMessage,
                              String newUrl, String url, String id) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Constant.NOTIFICATION_CLICKED, "1");
        intent.putExtra(Constant.ID, id);
        intent.putExtra(Constant.NEW_URL, newUrl);
        intent.putExtra(Constant.URL, url);
        intent.putExtra(Constant.TITLE, notificationMessage);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(i,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notific = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificationTitle)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentText(notificationMessage)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();

        NotificationManager notifManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        try {
            notifManager.notify(i++, notific);
            if (i == Integer.MAX_VALUE)
                i = 0;
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(String parsedTitle, String schoolName,
                                  String parsedUrl, String url, String id) {
        try {
            SchoolData oldSchoolData = getOldSchoolData(id);

            if (oldSchoolData != null) {
                if (!oldSchoolData.getNotificationTitle().equals(parsedTitle)) {

                    if (!isFirstNotification(id) && !parsedTitle.equals(""))
                        notification(schoolName, parsedTitle, parsedUrl, url, id);
                    writeNewNotificationData(parsedTitle, id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeNewNotificationData(String newTitle, String id) {
        final String NEW_TITLE = newTitle;
        final String ID = id;
        final Handler handler = new Handler();
        handler.post(new Runnable(){
            @Override
            public void run(){

                try {
                    SQLiteOpenHelper shopDatabaseHelper =
                            new SchoolDatabaseHelper(getApplicationContext());

                    db = shopDatabaseHelper.getWritableDatabase();

                    ContentValues values = new ContentValues();

                    values.put(SchoolDatabaseHelper._TITLE, NEW_TITLE);

                    db.update(SchoolDatabaseHelper.TABLE_NAME,
                            values,
                            SchoolDatabaseHelper._ID + " = ?",
                            new String[] { ID });
                    db.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private SchoolData getOldSchoolData(String id){
        SchoolData sd = new SchoolData();

        try {
            SQLiteOpenHelper shopDatabaseHelper = new SchoolDatabaseHelper(this);
            db = shopDatabaseHelper.getReadableDatabase();

            cursor = db.query(SchoolDatabaseHelper.TABLE_NAME,
                    new String[]{SchoolDatabaseHelper._TITLE},
                    SchoolDatabaseHelper._ID + " = ?",
                    new String[]{id},
                    null, null, null);

            if (cursor.moveToFirst()) {
                sd.setName(SchoolDataManager.getSchoolTitle(this, Integer.parseInt(id)));
                sd.setNotificationTitle(cursor.getString(0));
            }

            cursor.close();
            db.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sd;
    }

    private boolean isFirstNotification(String id){
        boolean result = false;

        try {
            SQLiteOpenHelper shopDatabaseHelper = new SchoolDatabaseHelper(this);
            db = shopDatabaseHelper.getReadableDatabase();

            cursor = db.query(SchoolDatabaseHelper.TABLE_NAME,
                    new String[]{
                            SchoolDatabaseHelper._TITLE},
                    SchoolDatabaseHelper._ID + " = ?",
                    new String[]{id},
                    null, null, null);

            if (cursor.moveToFirst()) {
                result = cursor.getString(0).length() == 0;

            }
            cursor.close();
            db.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    private class ParsingTask extends AsyncTask<String, Void, Void> {

        String schoolName = "";
        String id = "0";
        String url = "";
        String parsedTitle = "";
        String parsedUrl = "";
        Document doc = null;

        @Override
        protected void onPreExecute() {
            schoolName = currentSchoolName;
            id = currentId;
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void v) {
            sendNotification(parsedTitle, schoolName, parsedUrl, url, id);
        }

        @Override
        protected Void doInBackground(String... args){
            url = args[0];

            Elements titleElements = null;
            try {

                doc = Jsoup.connect(url).timeout(Constant.SECONDS_TO_TIMEOUT).get();

                if(!url.equals(getResources().getString(R.string.site_school_kiblarivska)))
                    titleElements = doc.getElementsByAttributeValue("class", "eTitle");
                else
                    titleElements = doc.getElementsByAttributeValue("class", "news-item");

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (doc != null) {
                try {

                    if (url.equals(getResources().getString(R.string.site_school_shishlivtsi))) {
                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).text();

                    }else if (url.equals(getResources().getString(R.string.site_mon_gov))) {
                        titleElements = doc.getElementsByAttributeValue("id", "tab1");
                        parsedUrl = titleElements.get(0).child(0).child(0).child(2).attr("href");
                        parsedTitle = titleElements.get(0).child(0).child(0).child(2).child(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_metodkab))) {
                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).child(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_school_kontsivska))) {
                        parsedUrl = titleElements.get(1).child(0).attr("href");
                        parsedTitle = titleElements.get(1).text();

                    }
                        /* else if(url.equals(getResources().getString(R.string.site_school_onokivska))){
                             //THERE IS NOTHING TO NOTIFY

                        } */
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
                            parsedUrl = titleElements.get(0).child(0).child(0).child(0).attr("href");
                            parsedTitle = titleElements.get(0).child(0).child(0).child(0).text();

                        } catch (Exception e) {
                            parsedUrl = titleElements.get(0).child(0).child(1).child(0).attr("href");
                            parsedTitle = titleElements.get(0).child(0).child(1).child(0).text();
                        }

                    } else if (url.equals(getResources().getString(R.string.site_school_rativetska))) {
                        titleElements = doc.getElementsByAttributeValue("class", "eMessage");
                        parsedTitle = titleElements.get(0).child(0).text();

                        titleElements = doc.getElementsByAttributeValue("class", "entryReadAllLink");
                        parsedUrl = titleElements.get(0).attr("href");

                    }
                         /* else if(url.equals(getResources().getString(R.string.site_school_tisaashvanska))){
                              //No site

                        } */
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
                           //There is nothing to notify

                        } */
                    else if (url.equals(getResources().getString(R.string.site_school_hudlivska))) {
                        parsedUrl = titleElements.get(0).child(0).attr("href");
                        parsedTitle = titleElements.get(0).text();

                    } else if (url.equals(getResources().getString(R.string.site_school_kiblarivska))) {
                        titleElements = doc.getElementsByAttributeValue("class", "eTitle");
                        parsedTitle = titleElements.get(0).child(1).text();
                        parsedUrl = titleElements.get(0).child(2).attr("href");

                    }
                        /* else if(url.equals(getResources().getString(R.string.site_school_solovkivska))){
                              //No website

                        } */
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
    }
}
