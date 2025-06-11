package com.example.jobhunter.utils;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.jobhunter.R;

public class ToolbarManager {

    /**
     * Sets up a Toolbar with a navigation drawer toggle.
     * This method finds the DrawerLayout from the activity's view hierarchy
     * and sets up the ActionBarDrawerToggle to link the Toolbar and DrawerLayout.
     *
     * @param activity The hosting activity, which must be an AppCompatActivity.
     * @param toolbar The Toolbar to be set up.
     */
    public static void setupToolbarWithDrawer(AppCompatActivity activity, Toolbar toolbar) {
        if (activity == null || toolbar == null) {
            return;
        }

        activity.setSupportActionBar(toolbar);

        // Find the DrawerLayout from the activity's layout
        DrawerLayout drawerLayout = activity.findViewById(R.id.drawer_layout);

        if (drawerLayout != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    activity, drawerLayout, toolbar,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }
    }
} 