package com.example.mechanic_pc.uzhgorodschools.utils;


import android.content.Context;

import com.example.mechanic_pc.uzhgorodschools.R;

public class SchoolDataManager {

    public static int getSchoolId(int itemId) {
        int id = 0;
        switch (itemId) {
            case R.id.school_shishlivtsi:
                id = 1;
                break;
            case R.id.school_kontsivska:
                id = 2;
                break;
            case R.id.school_onokivska:
                id = 3;
                break;
            case R.id.school_surtivska:
                id = 4;
                break;
            case R.id.school_esenska:
                id = 5;
                break;
            case R.id.school_malodobronska:
                id = 6;
                break;
            case R.id.school_velikolazivska:
                id = 7;
                break;
            case R.id.school_storozhnitska:
                id = 8;
                break;
            case R.id.licey_velikodobronska:
                id = 9;
                break;
            case R.id.school_kamyanitska:
                id = 10;
                break;
            case R.id.school_ruskokomarivska:
                id = 11;
                break;
            case R.id.school_velikoheevtska:
                id = 12;
                break;
            case R.id.school_velikodobronska:
                id = 13;
                break;
            case R.id.school_rativetska:
                id = 14;
                break;
            case R.id.school_tisaashvanska:
                id = 15;
                break;
            case R.id.school_serednanska:
                id = 16;
                break;
            case R.id.school_koritnanska:
                id = 17;
                break;
            case R.id.school_chernivskoi:
                id = 18;
                break;
            case R.id.school_maloheevetska:
                id = 19;
                break;
            case R.id.school_palad_komarivetska:
                id = 20;
                break;
            case R.id.school_hudlivska:
                id = 21;
                break;
            case R.id.school_kiblarivska:
                id = 22;
                break;
            case R.id.school_solokivska:
                id = 23;
                break;
            case R.id.school_kholmkivska:
                id = 24;
                break;
            case R.id.school_solomonivska:
                id = 25;
                break;
            case R.id.mon_gov:
                id = 26;
                break;
            case R.id.metodkab:
                id = 27;
                break;
            case R.id.zippo:
                id = 28;
                break;
        }
        return id;
    }

    public static String getSchoolSiteUrl(Context context, int id) {
        String url = "";

        switch(id){
            case 1: url = context.getResources().getString(R.string.site_school_shishlivtsi); break;
            case 2: url = context.getResources().getString(R.string.site_school_kontsivska); break;
            case 3: url = context.getResources().getString(R.string.site_school_onokivska); break;
            case 4: url = context.getResources().getString(R.string.site_school_surtivska); break;
            case 5: url = context.getResources().getString(R.string.site_school_esenska); break;
            case 6: url = context.getResources().getString(R.string.site_school_malodobronska); break;
            case 7: url = context.getResources().getString(R.string.site_school_velikolazivska); break;
            case 8: url = context.getResources().getString(R.string.site_school_storozhnitska); break;
            case 9: url = context.getResources().getString(R.string.site_licey_velikodobronska); break;
            case 10: url = context.getResources().getString(R.string.site_school_kamyanitska); break;
            case 11: url = context.getResources().getString(R.string.site_school_ruskokomarivska); break;
            case 12: url = context.getResources().getString(R.string.site_school_velikoheevetska); break;
            case 13: url = context.getResources().getString(R.string.site_school_velikodobronska); break;
            case 14: url = context.getResources().getString(R.string.site_school_rativetska); break;
            case 15: url = context.getResources().getString(R.string.site_school_tisaashvanska); break;
            case 16: url = context.getResources().getString(R.string.site_school_serednanska); break;
            case 17: url = context.getResources().getString(R.string.site_school_koritnanska); break;
            case 18: url = context.getResources().getString(R.string.site_school_chernivskoi); break;
            case 19: url = context.getResources().getString(R.string.site_school_maloheevetska); break;
            case 20: url = context.getResources().getString(R.string.site_school_palad_komarivetska); break;
            case 21: url = context.getResources().getString(R.string.site_school_hudlivska); break;
            case 22: url = context.getResources().getString(R.string.site_school_kiblarivska); break;
            case 23: url = context.getResources().getString(R.string.site_school_solovkivska); break;
            case 24: url = context.getResources().getString(R.string.site_school_kholmkivksa); break;
            case 25: url = context.getResources().getString(R.string.site_school_solomonivska); break;
            case 26: url = context.getResources().getString(R.string.site_mon_gov); break;
            case 27: url = context.getResources().getString(R.string.site_metodkab); break;
            case 28: url = context.getResources().getString(R.string.site_zippo); break;
        }
        return url;
    }

    public static String getSchoolTitle(Context context, int id) {
        String title = "";
        switch(id) {
            case 1:
                title = context.getResources().getString(R.string.school_shishlivtsi);
                break;
            case 2:
                title = context.getResources().getString(R.string.school_kontsivska);
                break;
            case 3:
                title = context.getResources().getString(R.string.school_onokivska);
                break;
            case 4:
                title = context.getResources().getString(R.string.school_surtivska);
                break;
            case 5:
                title = context.getResources().getString(R.string.school_esenska);
                break;
            case 6:
                title = context.getResources().getString(R.string.school_malodobronska);
                break;
            case 7:
                title = context.getResources().getString(R.string.school_velikolazivska);
                break;
            case 8:
                title = context.getResources().getString(R.string.school_storozhnitska);
                break;
            case 9:
                title = context.getResources().getString(R.string.licey_velikodobronska);
                break;
            case 10:
                title = context.getResources().getString(R.string.school_kamyanitska);
                break;
            case 11:
                title = context.getResources().getString(R.string.school_ruskokomarivska);
                break;
            case 12:
                title = context.getResources().getString(R.string.school_velikoheevetska);
                break;
            case 13:
                title = context.getResources().getString(R.string.school_velikodobronska);
                break;
            case 14:
                title = context.getResources().getString(R.string.school_rativetska);
                break;
            case 15:
                title = context.getResources().getString(R.string.school_tisaashvanska);
                break;
            case 16:
                title = context.getResources().getString(R.string.school_serednanska);
                break;
            case 17:
                title = context.getResources().getString(R.string.school_koritnanska);
                break;
            case 18:
                title = context.getResources().getString(R.string.school_chernivskoi);
                break;
            case 19:
                title = context.getResources().getString(R.string.school_maloheevetska);
                break;
            case 20:
                title = context.getResources().getString(R.string.school_palad_komarivetska);
                break;
            case 21:
                title = context.getResources().getString(R.string.school_hudlivska);
                break;
            case 22:
                title = context.getResources().getString(R.string.school_kiblarivska);
                break;
            case 23:
                title = context.getResources().getString(R.string.school_solovkivska);
                break;
            case 24:
                title = context.getResources().getString(R.string.school_kholmkivksa);
                break;
            case 25:
                title = context.getResources().getString(R.string.school_solomonivska);
                break;
            case 26:
                title = context.getResources().getString(R.string.mon_gov);
                break;
            case 27:
                title = context.getResources().getString(R.string.metodkab);
                break;
            case 28:
                title = context.getResources().getString(R.string.zippo);
                break;
        }
        return title;
    }
}
