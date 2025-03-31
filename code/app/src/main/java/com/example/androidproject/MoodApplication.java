package com.example.androidproject;

import android.app.Activity;
import android.app.Application;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MoodApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // taken from https://stackoverflow.com/questions/10606408/automatically-log-android-lifecycle-events-using-activitylifecyclecallbacks
        // By Haasya on March 29th
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                // Not using it
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                // not using it
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                // This applies the theme to the activity when it is resumed
                applyThemeToActivity(activity);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                // not using it
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                // not using it
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
                // not using it
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                // not using it
            }
        });
    }

    /**
     * Apply the current theme to an activity
     */
    public static void applyThemeToActivity(Activity activity) {
        try {
            int themeResId = ThemeManager.getThemeResourceId(activity);
            View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
            applyThemeToView(rootView, themeResId, activity);
        } catch (Exception e) {
            // added so it doesnt crash but log can be viewable
        }
    }

    /**
     * Recursively find and apply theme to views
     */
    private static void applyThemeToView(View view, int themeResId, Activity activity) {
        // We don't do it to the navbar so the background can be kept the same
        if (view.getId() == R.id.nav_bar_container || view.getId() == R.id.main || view.getId() == R.id.login_page || view.getId() == R.id.sign_up_page || view.getId() == R.id.mood_details_container || view.getId() == R.id.feed_box || view.getId() == R.id.my_feed_box) {
            return;
        }

        // ConstraintLayout and Viewgroup recursion logic obtained from https://stackoverflow.com/questions/40275152/how-to-programmatically-add-views-and-constraints-to-a-constraintlayout
        // Implemented foundation individually, used ChatGPT to refine the code for issues that arised
        // Prompt "given this code, and this original implementation why does it not change for all pages of my application"
        if (view instanceof ConstraintLayout) {
            ConstraintLayout layout = (ConstraintLayout) view;

            // if there is a drawable background that can be modified
            if (layout.getBackground() != null) {

                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null && !(parent instanceof androidx.fragment.app.FragmentContainerView)) {
                    layout.setBackgroundResource(themeResId);
                }
            }
        }

        // for viewgroup we recurse through children
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyThemeToView(group.getChildAt(i), themeResId, activity);
            }
        }
    }
}
