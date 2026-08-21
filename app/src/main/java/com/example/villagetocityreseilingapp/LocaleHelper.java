package com.example.villagetocityreseilingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREF_NAME = "app_language";
    private static final String KEY_LANGUAGE = "selected_language";

    // =========================================================
    // SET LOCALE — ACTIVITY ATTACH CONTEXT
    // =========================================================

    public static Context setLocale(Context context) {
        String language = getSavedLanguage(context);
        return updateResources(context, language);
    }

    // =========================================================
    // SET NEW LOCALE — JAB USER CHOOSE KARE
    // =========================================================

    public static Context setNewLocale(Context context, String language) {
        saveLanguage(context, language);
        return updateResources(context, language);
    }

    // =========================================================
    // SAVE LANGUAGE
    // =========================================================

    private static void saveLanguage(Context context, String language) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, language).apply();
    }

    // =========================================================
    // GET SAVED LANGUAGE — DEFAULT = ENGLISH
    // =========================================================

    public static String getSavedLanguage(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    // =========================================================
    // RESET TO ENGLISH — LOGOUT KE BAAD
    // =========================================================

    public static void resetLanguage(Context context) {
        saveLanguage(context, "en");
    }

    // =========================================================
    // UPDATE RESOURCES
    // =========================================================

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration config =
                new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }
}