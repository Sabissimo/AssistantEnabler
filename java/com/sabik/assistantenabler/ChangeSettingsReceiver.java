package com.sabik.assistantenabler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class ChangeSettingsReceiver extends BroadcastReceiver {
    public ChangeSettingsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        final SharedPreferences prefs = context.getSharedPreferences("preferences",Context.MODE_WORLD_READABLE);
        final SharedPreferences.Editor editor = prefs.edit();

        if(intent.getAction().equals("com.sabik.assistantenabler.CHANGE_SETTINGS"))
        {
            Bundle extras = intent.getExtras();
            String settingName = extras.getString("setting");
            Boolean settingValue = extras.getBoolean("value", false);
            if(settingName != null)
            switch(settingName){
                case "googleNowEnabled":
                case "assistantEnabled":
                case "enableOKGoogleEverywhere":
                    editor.putBoolean(settingName,settingValue);
                    editor.apply();
            }
        }
    }
}
