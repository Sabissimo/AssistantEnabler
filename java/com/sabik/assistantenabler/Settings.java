package com.sabik.assistantenabler;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

public class Settings extends AppCompatActivity {

    public Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final SharedPreferences prefs = getSharedPreferences("preferences",MODE_WORLD_READABLE);
        final SharedPreferences.Editor editor = prefs.edit();
        context = getApplicationContext();
        Switch assistantEnabled = (Switch) findViewById(R.id.assistantEnabled);
        Switch enableOKGoogleEverywhere = (Switch) findViewById(R.id.enableOKGoogleEverywhere);
        Switch googleNowEnabled = (Switch) findViewById(R.id.googleNowEnabled);
        Switch hiddenIcon = (Switch) findViewById(R.id.hiddenIcon);
        Button donateButton = (Button) findViewById(R.id.donateButton);

        if (googleNowEnabled!=null) {
            googleNowEnabled.setChecked(prefs.getBoolean("googleNowEnabled",false));
            googleNowEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    editor.putBoolean("googleNowEnabled", isChecked);
                    editor.apply();

                }
            });
        }

        if (assistantEnabled!=null) {
            assistantEnabled.setChecked(prefs.getBoolean("assistantEnabled",true));
            assistantEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    editor.putBoolean("assistantEnabled", isChecked);
                    editor.apply();

                }
            });
        }

        if (enableOKGoogleEverywhere!=null) {
            enableOKGoogleEverywhere.setChecked(prefs.getBoolean("enableOKGoogleEverywhere",false));
            enableOKGoogleEverywhere.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    editor.putBoolean("enableOKGoogleEverywhere", isChecked);
                    editor.apply();

                }
            });
        }

        if (hiddenIcon!=null) {
            hiddenIcon.setChecked(prefs.getBoolean("hiddenIcon",false));
            hiddenIcon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    PackageManager packageManager = context.getPackageManager();
                    int state = isChecked ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                            : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
                    String settings = BuildConfig.APPLICATION_ID + ".Preferences";
                    ComponentName alias = new ComponentName(context, settings);
                    packageManager.setComponentEnabledSetting(alias, state,
                            PackageManager.DONT_KILL_APP);

                    editor.putBoolean("hiddenIcon", isChecked);
                    editor.apply();

                }
            });
        }

        if (donateButton!=null){
            donateButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/Sabissimo")));
                }
            });
        }

    }
}
