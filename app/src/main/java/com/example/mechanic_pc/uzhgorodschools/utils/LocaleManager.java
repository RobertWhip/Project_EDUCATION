package com.example.mechanic_pc.uzhgorodschools.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import com.example.mechanic_pc.uzhgorodschools.R;

import java.util.Locale;

public class LocaleManager {
    private static String language;

    public static Context setLocale(Context c) {
        language = getLanguage(c);
        return setNewLocale(c);
    }

    private static Context setNewLocale(Context c) {
        updateResources(c);
        return persistLanguage(c);
    }

    private static Context persistLanguage(Context context) {


        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale);
            context = context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }

        return context;
    }

    private static void updateResources(Context context) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.locale = locale;
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    private static String getLanguage(Context context) {
        String result;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.PREFERENCES,
                Context.MODE_PRIVATE);
        if(sharedPreferences.contains(Constant.LANGUAGE))
            result = sharedPreferences.getString(Constant.LANGUAGE, "en");
        else
            result = context.getResources().getString(R.string.language);

        return result;
    }
}