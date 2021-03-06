package com.cybexmobile.helper;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.cybexmobile.activity.LoginActivity;
import com.cybexmobile.activity.MarketsActivity;
import com.cybexmobile.activity.RegisterActivity;
import com.cybexmobile.R;

public class ActionBarTitleHelper {

    public static void centeredActionBarTitle(AppCompatActivity context) {
        if(context.getSupportActionBar() != null) {
            context.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        }
        if (context instanceof MarketsActivity) {
            context.getSupportActionBar().setCustomView(R.layout.actionbar_market_page_layout);
        } else if (context instanceof LoginActivity || context instanceof RegisterActivity) {
            context.getSupportActionBar().setCustomView(R.layout.action_bar_layout_login);
            context.getSupportActionBar().setElevation(0);
        } else {
            context.getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        }
    }
}
