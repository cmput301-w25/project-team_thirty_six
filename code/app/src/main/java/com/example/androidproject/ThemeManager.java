package com.example.androidproject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class ThemeManager {
    private static final String PREF_NAME = "ThemePreferences";
    private static final String KEY_THEME = "selected_theme";

    // Theme constants
    public static final String THEME_DEFAULT = "gradient_color_background";
    public static final String THEME_BLUE = "gradient_color_light_dark_blue";
    public static final String THEME_PURPLE = "gradient_color_light_dark_purple";
    public static final String THEME_RED_GOLD = "gradient_color_red_gold";
    public static final String THEME_BROWN_ORANGE = "gradient_color_brown_orange";

    // the theme that is currently displayed is
    public static String getCurrentTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_THEME, THEME_DEFAULT);
    }

    // users selected theme is now saved
    // NEED TO ADD IT TO DATABASE
    public static void saveTheme(Context context, String themeName) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_THEME, themeName);
        editor.apply();

        // This allows us to apply it to any activity
        if (context instanceof Activity) {
            MoodApplication.applyThemeToActivity((Activity) context);
        }
    }

    // the R.id for each theme is gotten
    public static int getThemeResourceId(Context context) {
        String themeName = getCurrentTheme(context);

        switch (themeName) {
            case THEME_BLUE:
                return R.drawable.gradient_color_light_dark_blue;
            case THEME_PURPLE:
                return R.drawable.gradient_color_light_dark_purple;
            case THEME_RED_GOLD:
                return R.drawable.gradient_color_red_gold;
            case THEME_BROWN_ORANGE:
                return R.drawable.gradient_color_brown_orange;
            case THEME_DEFAULT:
            default:
                return R.drawable.gradient_color_background;
        }
    }
}