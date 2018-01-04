package com.example.mechanic_pc.uzhgorodschools.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mechanic_pc.uzhgorodschools.R;

public class SchoolDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "school_db";
    private static final int DB_VERSION = 2;

    public static final String TABLE_NAME = "data_table";

    public static final String _ID = "id";
    public static final String _TITLE = "title";
    public static final String _URL = "url";
    public static final String _SHOW = "show";
    public static final String _TELEPHONE_NUMBER = "telephoneNumber";
    public static final String _EMAIL = "email";


    private Context context;

    public SchoolDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        updateDatabase(db, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateDatabase(db, 0, DB_VERSION);
    }

    private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < 2) {
           db.execSQL("CREATE TABLE " + TABLE_NAME + " ( " + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + _TITLE +" TEXT, "
                    + _URL + " TEXT, "
                    + _SHOW + " TEXT, "
                    + _TELEPHONE_NUMBER + " TEXT, "
                    + _EMAIL + " TEXT);");

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_shishlivtsi), "", "0",
                    context.getResources().getString(R.string.phone_school_shishlivtsi),
                    context.getResources().getString(R.string.email_school_shishlivtsi));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_kontsivska), "", "0",
                    context.getResources().getString(R.string.phone_school_kontsivska),
                    context.getResources().getString(R.string.email_school_kontsivska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_onokivska), "", "0",
                    context.getResources().getString(R.string.phone_school_onokivska),
                    context.getResources().getString(R.string.email_school_onokivska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_surtivska), "", "0",
                    context.getResources().getString(R.string.phone_school_surtivska),
                    context.getResources().getString(R.string.email_school_surtivska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_esenska), "", "0",
                    context.getResources().getString(R.string.phone_school_esenska),
                    context.getResources().getString(R.string.email_school_esenska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_malodobronska), "", "0",
                    context.getResources().getString(R.string.phone_school_malodobronska),
                    context.getResources().getString(R.string.email_school_malodobronska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_velikolazivska), "", "0",
                    context.getResources().getString(R.string.phone_school_velikolazivska),
                    context.getResources().getString(R.string.email_school_velikolazivska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_storozhnitska), "", "0",
                    context.getResources().getString(R.string.phone_school_storozhnitska),
                    context.getResources().getString(R.string.email_school_storozhnitska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_licey_velikodobronska), "", "0",
                    context.getResources().getString(R.string.phone_licey_velikodobronska),
                    context.getResources().getString(R.string.email_licey_velikodobronska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_kamyanitska), "", "0",
                    context.getResources().getString(R.string.phone_school_kamyanitska),
                    context.getResources().getString(R.string.email_school_kamyanitska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_ruskokomarivska), "", "0",
                    context.getResources().getString(R.string.phone_school_ruskokomarivska),
                    context.getResources().getString(R.string.email_school_ruskokomarivska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_velikoheevetska), "", "0",
                    context.getResources().getString(R.string.phone_school_velikoheevetska),
                    context.getResources().getString(R.string.email_school_velikoheevetska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_velikodobronska), "", "0",
                    context.getResources().getString(R.string.phone_school_velikodobronska),
                    context.getResources().getString(R.string.email_school_velikodobronska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_rativetska), "", "0",
                    context.getResources().getString(R.string.phone_school_rativetska),
                    context.getResources().getString(R.string.email_school_rativetska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_tisaashvanska), "", "0",
                    context.getResources().getString(R.string.phone_school_tisaashvanska),
                    context.getResources().getString(R.string.email_school_tisaashvanska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_serednanska), "", "0",
                    context.getResources().getString(R.string.phone_school_serednanska),
                    context.getResources().getString(R.string.email_school_serednanska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_koritnanska), "", "0",
                    context.getResources().getString(R.string.phone_school_koritnanska),
                    context.getResources().getString(R.string.email_school_koritnanska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_chernivskoi), "", "0",
                    context.getResources().getString(R.string.phone_school_chernivskoi),
                    context.getResources().getString(R.string.email_school_chernivskoi));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_maloheevetska), "", "0",
                    context.getResources().getString(R.string.phone_school_maloheevetska),
                    context.getResources().getString(R.string.email_school_maloheevetska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_palad_komarivetska), "", "0",
                    context.getResources().getString(R.string.phone_school_palad_komarivetska),
                    context.getResources().getString(R.string.email_school_palad_komarivetska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_hudlivska), "", "0",
                    context.getResources().getString(R.string.phone_school_hudlivska),
                    context.getResources().getString(R.string.email_school_hudlivska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_kiblarivska), "", "0",
                    context.getResources().getString(R.string.phone_school_kiblarivska),
                    context.getResources().getString(R.string.email_school_kiblarivska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_solovkivska), "", "0",
                    context.getResources().getString(R.string.phone_school_solovkivska),
                    context.getResources().getString(R.string.email_school_solovkivska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_kholmkivksa), "", "0",
                    context.getResources().getString(R.string.phone_school_kholmkivksa),
                    context.getResources().getString(R.string.email_school_kholmkivksa));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_school_solomonivska), "", "0",
                    context.getResources().getString(R.string.phone_school_solomonivska),
                    context.getResources().getString(R.string.email_school_solomonivska));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_mon_gov), "", "0",
                    context.getResources().getString(R.string.phone_mon_gov),
                    context.getResources().getString(R.string.email_mon_gov));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_metodkab), "", "0",
                    context.getResources().getString(R.string.phone_metodkab),
                    context.getResources().getString(R.string.email_metodkab));

            insertSchoolData(db,
                    context.getResources().getString(R.string.site_zippo), "", "0",
                    context.getResources().getString(R.string.phone_zippo),
                    context.getResources().getString(R.string.email_zippo));
        }
    }

    private static void insertSchoolData(SQLiteDatabase db,
                                           String url,
                                           String title,
                                           String show,
                                           String telephoneNumber,
                                           String email) {

        ContentValues values = new ContentValues();

        values.put(_TITLE, title);
        values.put(_URL, url);
        values.put(_SHOW, show);
        values.put(_TELEPHONE_NUMBER, telephoneNumber);
        values.put(_EMAIL, email);

        db.insert(TABLE_NAME, null, values);
    }
}

