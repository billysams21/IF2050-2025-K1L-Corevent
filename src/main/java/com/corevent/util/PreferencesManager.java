package com.corevent.util;

import java.util.prefs.Preferences;

public class PreferencesManager {
    private static final Preferences prefs = Preferences.userNodeForPackage(PreferencesManager.class);
    
    // Keys
    private static final String KEY_USERNAME = "username";
    private static final String KEY_REMEMBER_ME = "rememberMe";
    private static final String KEY_THEME = "theme";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_LAST_SYNC = "lastSync";
    
    public static void saveCredentials(String username) {
        prefs.put(KEY_USERNAME, username);
        prefs.putBoolean(KEY_REMEMBER_ME, true);
    }
    
    public static String getSavedUsername() {
        return prefs.get(KEY_USERNAME, "");
    }
    
    public static boolean isRememberMe() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }
    
    public static void clearCredentials() {
        prefs.remove(KEY_USERNAME);
        prefs.putBoolean(KEY_REMEMBER_ME, false);
    }
    
    public static void setTheme(String theme) {
        prefs.put(KEY_THEME, theme);
    }
    
    public static String getTheme() {
        return prefs.get(KEY_THEME, "light");
    }
    
    public static void setLanguage(String language) {
        prefs.put(KEY_LANGUAGE, language);
    }
    
    public static String getLanguage() {
        return prefs.get(KEY_LANGUAGE, "en");
    }
    
    public static void setLastSyncTime(long timestamp) {
        prefs.putLong(KEY_LAST_SYNC, timestamp);
    }
    
    public static long getLastSyncTime() {
        return prefs.getLong(KEY_LAST_SYNC, 0);
    }
}