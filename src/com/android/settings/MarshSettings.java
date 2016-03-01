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

public class MarshSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener, Indexable {

    private static final String TAG = "Marsh";

    private TwoStatePreference mExpand;
    private TwoStatePreference mNotiTrans;
    private TwoStatePreference mHeadSett;
    private TwoStatePreference mQuickSett;
    private TwoStatePreference mEditButton;
    private TwoStatePreference mDT2SLock;
    private SeekBarPreference mScale;
    private SeekBarPreference mRadius;

    private static final String PREF_CAT_COLORS = "weather_cat_colors";
    private static final String PREF_SHOW_WEATHER = "weather_show_weather";
    private static final String PREF_SHOW_LOCATION = "weather_show_location";
    private static final String PREF_STATUSBAR_WEATHER = "status_bar_show_weather";
    private static final String PREF_CONDITION_ICON = "weather_condition_icon";
    private static final String PREF_COLORIZE_ALL_ICONS = "weather_colorize_all_icons";
    private static final String PREF_TEXT_COLOR = "weather_text_color";
    private static final String PREF_ICON_COLOR = "weather_icon_color";

    private static final int MONOCHROME_ICON = 0;
    private static final int DEFAULT_COLOR = 0xffffffff;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET = 0;

    private SwitchPreference mShowWeather;
    private SwitchPreference mShowLocation;
    private SwitchPreference mShowStatusbarWeather;
    private ListPreference mConditionIcon;
    private SwitchPreference mColorizeAllIcons;
    private ColorPickerPreference mTextColor;
    private ColorPickerPreference mIconColor;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.marsh_settings);

        ContentResolver resolver = getActivity().getContentResolver();

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
                Settings.System.STATUSBAR_EDITBUTTON_PREFERENCE_KEY, 1) == 1);
        mEditButton.setChecked(mEditButtonint);
        mEditButton.setOnPreferenceChangeListener(this);

        mDT2SLock = (TwoStatePreference) findPreference("double_tap_sleep_lock_screen");
        boolean mDT2SLockint = (Settings.System.getInt(resolver,
                Settings.System.DOUBLE_TAP_SLEEP_LOCK_SCREEN, 1) == 1);
        mDT2SLock.setChecked(mDT2SLockint);
        mDT2SLock.setOnPreferenceChangeListener(this);

        boolean showWeather = Settings.System.getInt(mResolver,
                Settings.System.LOCK_SCREEN_SHOW_WEATHER, 0) == 1;
        int conditionIcon = Settings.System.getInt(mResolver,
               Settings.System.LOCK_SCREEN_WEATHER_CONDITION_ICON, MONOCHROME_ICON);
        boolean colorizeAllIcons = Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_COLORIZE_ALL_ICONS, 0) == 1;

        int intColor;
        String hexColor;

        mShowWeather = (SwitchPreference) findPreference(PREF_SHOW_WEATHER);
        mShowWeather.setChecked(showWeather);
        mShowWeather.setOnPreferenceChangeListener(this);

        mShowStatusbarWeather = (SwitchPreference) findPreference(PREF_STATUSBAR_WEATHER);
        mShowWeather.setChecked(showWeather);
        mShowStatusbarWeather.setOnPreferenceChangeListener(this);

        PreferenceCategory catColors = (PreferenceCategory) findPreference(PREF_CAT_COLORS);
        mTextColor = (ColorPickerPreference) findPreference(PREF_TEXT_COLOR);
        mIconColor = (ColorPickerPreference) findPreference(PREF_ICON_COLOR);

        if (showWeather) {
            mShowLocation = (SwitchPreference) findPreference(PREF_SHOW_LOCATION);
            mShowLocation.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_SHOW_WEATHER_LOCATION, 1) == 1);
            mShowLocation.setOnPreferenceChangeListener(this);

            mShowStatusbarWeather = (SwitchPreference) findPreference(PREF_STATUSBAR_WEATHER);
            mShowStatusbarWeather.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_SHOW_WEATHER, 0) == 1);
            mShowStatusbarWeather.setOnPreferenceChangeListener(this);

            mConditionIcon = (ListPreference) findPreference(PREF_CONDITION_ICON);
            mConditionIcon.setValue(String.valueOf(conditionIcon));
            mConditionIcon.setSummary(mConditionIcon.getEntry());
            mConditionIcon.setOnPreferenceChangeListener(this);

            mColorizeAllIcons = (SwitchPreference) findPreference(PREF_COLORIZE_ALL_ICONS);
            mColorizeAllIcons.setChecked(colorizeAllIcons);
            mColorizeAllIcons.setOnPreferenceChangeListener(this);

            intColor = Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_TEXT_COLOR,
                    DEFAULT_COLOR);
            mTextColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mTextColor.setSummary(hexColor);
            mTextColor.setOnPreferenceChangeListener(this);
        } else {
            removePreference(PREF_SHOW_LOCATION);
            removePreference(PREF_STATUSBAR_WEATHER);
            removePreference(PREF_STATUSBAR_WEATHER);
            removePreference(PREF_CONDITION_ICON);
            removePreference(PREF_COLORIZE_ALL_ICONS);
            catColors.removePreference(mTextColor);
        }
        if (showWeather && ((conditionIcon == MONOCHROME_ICON)
                || (conditionIcon != MONOCHROME_ICON && colorizeAllIcons))) {
            intColor = Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_ICON_COLOR,
                    DEFAULT_COLOR);
            mIconColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mIconColor.setSummary(hexColor);
            mIconColor.setOnPreferenceChangeListener(this);
        } else {
            catColors.removePreference(mIconColor);
            if (!showWeather) {
                removePreference(PREF_CAT_COLORS);
            }
        }
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
        if (preference == mExpand) {
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
            int value = Integer.parseInt((String) newValue);
            CMSettings.System.putInt(
                resolver, CMSettings.System.STATUSBAR_BLUR_RADIUS, (Integer) newValue);
            getContext().sendBroadcast(i);
            return true;
        } else if (preference == mNotiTrans) {
            Settings.System.putInt(
                    resolver, Settings.System.TRANSLUCENT_NOTIFICATIONS_PREFERENCE_KEY, (((Boolean) newValue) ? 1 : 0));
            getContext().sendBroadcast(i);
            return true;
        } else if (preference == mHeadSett) {
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
        } else if (preference == mShowWeather) {
            value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.LOCK_SCREEN_SHOW_WEATHER,
                    value ? 1 : 0);
            refreshSettings();
            return true;
        } else if (preference == mShowLocation) {
            value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.LOCK_SCREEN_SHOW_WEATHER_LOCATION,
                    value ? 1 : 0);
            return true;
        } else if (preference == mShowStatusbarWeather) {
            value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_SHOW_WEATHER,
                    value ? 1 : 0);
            return true;
        } else if (preference == mConditionIcon) {
            int intValue = Integer.valueOf((String) newValue);
            int index = mConditionIcon.findIndexOfValue((String) newValue);
            Settings.System.putInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_CONDITION_ICON, intValue);
            mConditionIcon.setSummary(mConditionIcon.getEntries()[index]);
            refreshSettings();
            return true;
        } else if (preference == mColorizeAllIcons) {
            value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_COLORIZE_ALL_ICONS,
                    value ? 1 : 0);
            refreshSettings();
            return true;
        } else if (preference == mTextColor) {
            hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_TEXT_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mIconColor) {
            hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_ICON_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(com.android.internal.R.drawable.ic_menu_refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                showDialogInner(DLG_RESET);
                return true;
             default:
                return super.onContextItemSelected(item);
        }
    }


    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        Weather getOwner() {
            return (Weather) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_RESET:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.reset)
                    .setMessage(R.string.reset_values_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.reset_lockscreen_weather,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.LOCK_SCREEN_SHOW_WEATHER, 1);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.LOCK_SCREEN_SHOW_WEATHER_LOCATION, 1);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.LOCK_SCREEN_WEATHER_CONDITION_ICON, 2);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.LOCK_SCREEN_WEATHER_COLORIZE_ALL_ICONS, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.LOCK_SCREEN_WEATHER_TEXT_COLOR,
                                    DEFAULT_COLOR);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.LOCK_SCREEN_WEATHER_ICON_COLOR,
                                    DEFAULT_COLOR);
                            getOwner().refreshSettings();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {

        }
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
