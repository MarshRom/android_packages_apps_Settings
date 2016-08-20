/*
 * Copyright (C) 2014-2015 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.TwoStatePreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.view.View;

import com.android.settings.cyanogenmod.SeekBarPreference;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cyanogenmod.providers.CMSettings;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.android.internal.util.du.DuUtils;

public class MarshSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, Indexable {

    private static final String TAG = "Marsh";

    private static final String KEYGUARD_TOGGLE_TORCH = "keyguard_toggle_torch";

    private SwitchPreference mKeyguardTorch;

    private TwoStatePreference mExpand;
    private TwoStatePreference mNotiTrans;
    private TwoStatePreference mHeadSett;
    private TwoStatePreference mQuickSett;
    private TwoStatePreference mEditButton;
    private TwoStatePreference mDT2SLock;
    private SeekBarPreference mScale;
    private SeekBarPreference mRadius;
    private TwoStatePreference mSwipeNotifications;
    private TwoStatePreference mSwipeRecent;
    private ContentResolver mResolver;


    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.marsh_settings);
		PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mKeyguardTorch = (SwitchPreference) findPreference(KEYGUARD_TOGGLE_TORCH);
        mKeyguardTorch.setOnPreferenceChangeListener(this);
        if (!DuUtils.deviceSupportsFlashLight(getActivity())) {
            prefSet.removePreference(mKeyguardTorch);
        } else {
        mKeyguardTorch.setChecked((Settings.System.getInt(resolver,
                Settings.System.KEYGUARD_TOGGLE_TORCH, 0) == 1));
        }

        mExpand = (TwoStatePreference) findPreference("hook_system_ui_blurred_status_bar_expanded_enabled_pref");
        boolean mExpandint = (Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_EXPANDED_ENABLED_PREFERENCE_KEY, 1) == 1);
        mExpand.setChecked(mExpandint);
        mExpand.setOnPreferenceChangeListener(this);

        mScale = (SeekBarPreference) findPreference("statusbar_blur_scale");
        mScale.setValue(CMSettings.System.getInt(resolver, CMSettings.System.STATUSBAR_BLUR_SCALE, 10));
        mScale.setOnPreferenceChangeListener(this);

        mRadius = (SeekBarPreference) findPreference("statusbar_blur_radius");
        mRadius.setValue(CMSettings.System.getInt(resolver, CMSettings.System.STATUSBAR_BLUR_RADIUS, 5));
        mRadius.setOnPreferenceChangeListener(this);

        mNotiTrans = (TwoStatePreference) findPreference("hook_system_ui_translucent_notifications_pref");
        boolean mNotiTransint = (Settings.System.getInt(resolver,
                Settings.System.TRANSLUCENT_NOTIFICATIONS_PREFERENCE_KEY, 1) == 1);
        mNotiTrans.setChecked(mNotiTransint);
        mNotiTrans.setOnPreferenceChangeListener(this);

        mHeadSett = (TwoStatePreference) findPreference("hook_system_ui_translucent_header_pref");
        boolean mHeadSettint = (Settings.System.getInt(resolver,
                Settings.System.TRANSLUCENT_HEADER_PREFERENCE_KEY, 1) == 1);
        mHeadSett.setChecked(mHeadSettint);
        mHeadSett.setOnPreferenceChangeListener(this);

        mQuickSett = (TwoStatePreference) findPreference("hook_system_ui_translucent_quick_settings_pref");
        boolean mQuickSettint = (Settings.System.getInt(resolver,
                Settings.System.TRANSLUCENT_QUICK_SETTINGS_PREFERENCE_KEY, 1) == 1);
        mQuickSett.setChecked(mQuickSettint);
        mQuickSett.setOnPreferenceChangeListener(this);

        mEditButton = (TwoStatePreference) findPreference("hook_statusbar_editbutton_pref");
        boolean mEditButtonint = (Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_EDITBUTTON_PREFERENCE_KEY, 0) == 1);
        mEditButton.setChecked(mEditButtonint);
        mEditButton.setOnPreferenceChangeListener(this);

        mDT2SLock = (TwoStatePreference) findPreference("double_tap_sleep_lock_screen");
        boolean mDT2SLockint = (Settings.System.getInt(resolver,
                Settings.System.DOUBLE_TAP_SLEEP_LOCK_SCREEN, 1) == 1);
        mDT2SLock.setChecked(mDT2SLockint);
        mDT2SLock.setOnPreferenceChangeListener(this);

	mSwipeNotifications = (TwoStatePreference) findPreference("shake_to_clean_notification");
	boolean mSwipeNotificationsint = (Settings.System.getInt(resolver,
		Settings.System.SHAKE_TO_CLEAN_NOTIFICATION, 1) == 1);
	mSwipeNotifications.setChecked(mSwipeNotificationsint);
	mSwipeNotifications.setOnPreferenceChangeListener(this);

	mSwipeRecent = (TwoStatePreference) findPreference("shake_to_clean_recent");
	boolean mSwipeRecentint = (Settings.System.getInt(resolver,
		Settings.System.SHAKE_TO_CLEAN_RECENT, 1) == 1);
	mSwipeRecent.setChecked(mSwipeRecentint);
	mSwipeRecent.setOnPreferenceChangeListener(this);
	}

    public void refreshSettings() {
        PreferenceScreen prefs = getPreferenceScreen();
        if (prefs != null) {
            prefs.removeAll();
        }
        mResolver = getActivity().getContentResolver();

    }

    @Override
    protected int getMetricsCategory() {
        // todo add a constant in MetricsLogger.java
        // No.
        return MetricsLogger.MAIN_SETTINGS;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        boolean value;
        String hex;
        int intHex;
        Intent i = new Intent("serajr.blurred.system.ui.lp.UPDATE_PREFERENCES");
        if  (preference == mKeyguardTorch) {
            Settings.System.putInt(
                    resolver, Settings.System.KEYGUARD_TOGGLE_TORCH, (((Boolean) newValue) ? 1 : 0));
            return true;
        } else if (preference == mExpand) {
            Settings.System.putInt(
                    resolver, Settings.System.STATUS_BAR_EXPANDED_ENABLED_PREFERENCE_KEY, (((Boolean) newValue) ? 1 : 0));
            getContext().sendBroadcast(i);
            return true;
        } else if (preference == mScale) {
            CMSettings.System.putInt(
                resolver, CMSettings.System.STATUSBAR_BLUR_SCALE, (Integer) newValue);
            getContext().sendBroadcast(i);
            return true;
        } else if (preference == mRadius) {
            CMSettings.System.putInt(
                resolver, CMSettings.System.STATUSBAR_BLUR_RADIUS, (Integer) newValue);
            getContext().sendBroadcast(i);
            return true;
        } else if (preference == mNotiTrans) {
            Settings.System.putInt(
                    resolver, Settings.System.TRANSLUCENT_NOTIFICATIONS_PREFERENCE_KEY, (((Boolean) newValue) ? 1 : 0));
            getContext().sendBroadcast(i);
            return true;
        } else if (preference == mDT2SLock) {
            Settings.System.putInt(
                    resolver, Settings.System.DOUBLE_TAP_SLEEP_LOCK_SCREEN, (((Boolean) newValue) ? 1 : 0));
            getContext().sendBroadcast(i);
            return true;
        } else if (preference == mHeadSett) {
            Settings.System.putInt(
                    resolver, Settings.System.TRANSLUCENT_HEADER_PREFERENCE_KEY, (((Boolean) newValue) ? 1 : 0));
            getContext().sendBroadcast(i);
            return true;
        } else if (preference == mQuickSett) {
            Settings.System.putInt(
                    resolver, Settings.System.TRANSLUCENT_QUICK_SETTINGS_PREFERENCE_KEY, (((Boolean) newValue) ? 1 : 0));
            getContext().sendBroadcast(i);
            return true;
        } else if (preference == mEditButton) {
            Settings.System.putInt(
                    resolver, Settings.System.STATUSBAR_EDITBUTTON_PREFERENCE_KEY, (((Boolean) newValue) ? 1 : 0));
            getContext().sendBroadcast(i);
            return true;
        } else if (preference == mSwipeNotifications) {
            Settings.System.putInt(
		    resolver, Settings.System.SHAKE_TO_CLEAN_NOTIFICATION, (((Boolean) newValue) ? 1 : 0));
	    getContext().sendBroadcast(i);
	    return true;
	} else if (preference == mSwipeRecent) {
	    Settings.System.putInt(
		    resolver, Settings.System.SHAKE_TO_CLEAN_RECENT, (((Boolean) newValue) ? 1 : 0));
	    getContext().sendBroadcast(i);
	    return true;
	}
        return false;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                            boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.marsh_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
            };
}
