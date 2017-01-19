package com.siyanhui.mojif.bqss_demo.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;

import com.siyanhui.mojif.bqss_demo.BqssApplication;
import com.siyanhui.mojif.bqss_demo.BqssConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by fantasy on 17/1/6.
 */

public class BqssPreferenceHelper {
    public static final String KEY = "keywords";

    public static void clearSearchHistory() {
        SharedPreferences preference = BqssApplication.getApplication().getSharedPreferences(BqssConstants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        preference.edit().clear().commit();
    }

    public static void addSearchKeyword(String keyword) {
        if (Build.VERSION.SDK_INT >= 11) {
            writeKeyword(keyword);
        } else {
            writeKeywordForAPIBelow11(keyword);
        }
    }

    public static Set<String> getSearchHistory() {
        if (Build.VERSION.SDK_INT >= 11) {
            return readStringSet();
        } else {
            String[] strings = getStringArray();
            return (Set<String>) Arrays.asList(strings);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static Set<String> readStringSet() {
        SharedPreferences preference = BqssApplication.getApplication().getSharedPreferences(BqssConstants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        HashSet<String> historyKeywords = new HashSet<>();
        return preference.getStringSet(KEY, historyKeywords);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void writeKeyword(String v) {
        SharedPreferences preference = BqssApplication.getApplication().getSharedPreferences(BqssConstants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        HashSet<String> historyKeywords = (HashSet<String>) readStringSet();
        HashSet<String> newKeywords = new HashSet<>();
        newKeywords.addAll(historyKeywords);
        SharedPreferences.Editor editor = preference.edit();
        newKeywords.add(v);
        editor.putStringSet(KEY, newKeywords);
        editor.commit();
    }

    private static String[] getStringArray() {
        String regularEx = "##";
        String[] str;
        SharedPreferences sp = BqssApplication.getApplication().getSharedPreferences(BqssConstants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        String values;
        values = sp.getString(KEY, "");
        str = values.split(regularEx);
        return str;
    }

    private static void writeKeywordForAPIBelow11(String value) {
        String regularEx = "##";
        SharedPreferences sp = BqssApplication.getApplication().getSharedPreferences(BqssConstants.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        String str = sp.getString(KEY, "");
        if (!TextUtils.isEmpty(value)) {
            str += value;
            str += regularEx;
            SharedPreferences.Editor et = sp.edit();
            et.putString(KEY, str);
            et.commit();
        }
    }
}
