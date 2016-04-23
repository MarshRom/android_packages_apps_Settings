package com.android.settings.marshrom.fragments;

import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Camera;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManagerGlobal;

import java.util.ArrayList;

import com.android.settings.R;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
//import com.android.settings.util.CMDProcessor;
//import com.android.settings.marshrom.Helpers;
import com.android.settings.marshrom.AppMultiSelectListPreference;
import com.android.settings.cyanogenmod.SeekBarPreference;

import java.io.File;
import java.lang.Thread;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;

public class AppCircleBar extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, OnPreferenceClickListener {

    private static final String TAG = "AppCircleSidebar";

    private static final String PREF_INCLUDE_APP_CIRCLE_BAR_KEY = "app_circle_bar_included_apps";
    private static final String KEY_TRIGGER_WIDTH = "trigger_width";
    private static final String KEY_TRIGGER_TOP = "trigger_top";
    private static final String KEY_TRIGGER_BOTTOM = "trigger_bottom";

    private AppMultiSelectListPreference mIncludedAppCircleBar;

    private SeekBarPreference mTriggerWidthPref;
    private SeekBarPreference mTriggerTopPref;
    private SeekBarPreference mTriggerBottomPref;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getActivity().getContentResolver();
        Resources res = getResources();

        addPreferencesFromResource(R.xml.app_circlebar);

        PreferenceScreen prefSet = getPreferenceScreen();

        mIncludedAppCircleBar = (AppMultiSelectListPreference) prefSet.findPreference(PREF_INCLUDE_APP_CIRCLE_BAR_KEY);
        Set<String> includedApps = getIncludedApps();
        if (includedApps != null) mIncludedAppCircleBar.setValues(includedApps);
        mIncludedAppCircleBar.setOnPreferenceChangeListener(this);

        mTriggerWidthPref = (SeekBarPreference) findPreference(KEY_TRIGGER_WIDTH);
        mTriggerWidthPref.setValue(Settings.System.getInt(resolver,
                Settings.System.APP_CIRCLE_BAR_TRIGGER_WIDTH, 40));
        mTriggerWidthPref.setOnPreferenceChangeListener(this);

        mTriggerTopPref = (SeekBarPreference) findPreference(KEY_TRIGGER_TOP);
        mTriggerTopPref.setValue(Settings.System.getInt(resolver,
                Settings.System.APP_CIRCLE_BAR_TRIGGER_TOP, 0));
        mTriggerTopPref.setOnPreferenceChangeListener(this);

        mTriggerBottomPref = (SeekBarPreference) findPreference(KEY_TRIGGER_BOTTOM);
        mTriggerBottomPref.setValue(Settings.System.getInt(resolver,
                Settings.System.APP_CIRCLE_BAR_TRIGGER_HEIGHT, 100));
        mTriggerBottomPref.setOnPreferenceChangeListener(this);

    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.DIRTYTWEAKS;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        final String key = preference.getKey();
        if (preference == mIncludedAppCircleBar) {
            storeIncludedApps((Set<String>) objValue);
        } else if (preference == mTriggerWidthPref) {
            Settings.System.putInt(resolver,
                    Settings.System.APP_CIRCLE_BAR_TRIGGER_WIDTH, (Integer) objValue);
            return true;
        } else if (preference == mTriggerTopPref) {
            Settings.System.putInt(resolver,
                    Settings.System.APP_CIRCLE_BAR_TRIGGER_TOP, (Integer) objValue);
            return true;
        } else if (preference == mTriggerBottomPref) {
            Settings.System.putInt(resolver,
                    Settings.System.APP_CIRCLE_BAR_TRIGGER_HEIGHT, (Integer) objValue);
            return true;
        }

        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    private Set<String> getIncludedApps() {
        String included = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.WHITELIST_APP_CIRCLE_BAR);
        if (TextUtils.isEmpty(included)) {
            return null;
        }
        return new HashSet<String>(Arrays.asList(included.split("\\|")));
    }

    private void storeIncludedApps(Set<String> values) {
        StringBuilder builder = new StringBuilder();
        String delimiter = "";
        for (String value : values) {
            builder.append(delimiter);
            builder.append(value);
            delimiter = "|";
        }
        Settings.System.putString(getActivity().getContentResolver(),
                Settings.System.WHITELIST_APP_CIRCLE_BAR, builder.toString());
    }

    @Override
    public void onPause() {
        super.onPause();
        Settings.System.putInt(getContentResolver(),
                Settings.System.APP_CIRCLE_BAR_SHOW_TRIGGER, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.System.putInt(getContentResolver(),
                Settings.System.APP_CIRCLE_BAR_SHOW_TRIGGER, 1);
    }
}
