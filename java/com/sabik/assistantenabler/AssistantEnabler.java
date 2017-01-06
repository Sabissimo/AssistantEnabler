package com.sabik.assistantenabler;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.BaseBundle;
import android.os.BatteryManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class AssistantEnabler implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    private XSharedPreferences prefs;
    private static final String GOOGLE_PACKAGE_NAME = "com.google.android.googlequicksearchbox";
    private static final String GSA_PACKAGE = "com.google.android.apps.gsa";
    private static final String TELEPHONY_CLASS = "android.telephony.TelephonyManager";
    private static final List<String> NOW_PACKAGE_NAMES = new ArrayList<>(Arrays.asList("com.google.android.gms", "com.google.android.apps.maps"));
    private String detectionMethod1;
    private String detectionMethod2;
    private String assistantClassName;
    private String prefsClassName;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        prefs = new XSharedPreferences(AssistantEnabler.class.getPackage().getName(), "preferences");
        prefs.makeWorldReadable();
    }

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        prefs.reload();
        if (GOOGLE_PACKAGE_NAME.equals(lpparam.packageName) && checkVersion(lpparam)) {
            try {
                Class assistantClass = findClass(GSA_PACKAGE + assistantClassName, lpparam.classLoader);
                Class prefsClass = findClass(GSA_PACKAGE + prefsClassName, lpparam.classLoader);
                Class systemPropClass = findClass("android.os.SystemProperties", lpparam.classLoader);

                // Spoof_hotword value in bundles so that the Setup Ok Google screen never shows up
                findAndHookMethod(BaseBundle.class, "getBoolean", String.class, boolean.class, baseBundleHook);

                // Spoof build.prop values
                findAndHookMethod(systemPropClass, "getBoolean", String.class, boolean.class, systemPropHook);

                // If the power has disconnected, tell Google it has connected instead
                findAndHookMethod(Intent.class, "getAction", intentHook1);

                // If Google asks status for BATTERY_CHANGED, tell it it's charging
                findAndHookMethod(Intent.class, "getIntExtra", String.class, int.class, intentHook2);

                // Spoof opa-related values in config file
                // TODO: Find a way to make them name-independent
                findAndHookMethod(prefsClass, "getBoolean", String.class, boolean.class, prefsHook);

                // TODO: Find a way to make them name-independent
                findAndHookMethod(assistantClass, detectionMethod1, detectionMethodHook);
                findAndHookMethod(assistantClass, detectionMethod2, detectionMethodHook);
            } catch (Throwable t) {
                log(t);
            }
        }
        if (NOW_PACKAGE_NAMES.contains(lpparam.packageName) && prefs.getBoolean("googleNowEnabled", false)){
            try{
                findAndHookMethod(TELEPHONY_CLASS, lpparam.classLoader, "getSimOperator", nowOperatorCodeHook);
                findAndHookMethod(TELEPHONY_CLASS, lpparam.classLoader, "getSimCountryIso", nowCountryISOHook);
                findAndHookMethod(TELEPHONY_CLASS, lpparam.classLoader, "getSimOperatorName", nowOperatorNameHook);
            }
            catch (Throwable t){
                log(t);
            }
        }
    }

    private Boolean checkVersion(LoadPackageParam lpparam) throws PackageManager.NameNotFoundException {

        Object activityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
        Context context = (Context) XposedHelpers.callMethod(activityThread, "getSystemContext");
        String versionName = context.getPackageManager().getPackageInfo(lpparam.packageName, 0).versionName;

        if (versionName.matches("6.6.*")) {
            assistantClassName = ".assistant.a.e";
            detectionMethod1 = "oZ";
            detectionMethod2 = "pb";
            prefsClassName = ".search.core.preferences.bf";
        } else if (versionName.matches("6.7.*")) {
            assistantClassName = ".assistant.a.e";
            detectionMethod1 = "pb";
            detectionMethod2 = "pd";
            prefsClassName = ".search.core.preferences.bg";
        } else if (versionName.matches("6.8.*")) {
            assistantClassName = ".assistant.a.e";
            detectionMethod1 = "pT";
            detectionMethod2 = "pV";
            prefsClassName = ".search.core.preferences.bg";
        } else if (versionName.matches("6.9.*")) {
            assistantClassName = ".assistant.shared.f";
            detectionMethod1 = "ro";
            detectionMethod2 = "rq";
            prefsClassName = ".search.core.preferences.bk";
        } else {
            return false;
        }

        return true;
    }

    private XC_MethodReplacement nowOperatorCodeHook = new XC_MethodReplacement() {
        @Override
        protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
            return "310004";
        }
    };

    private XC_MethodReplacement nowCountryISOHook = new XC_MethodReplacement() {
        @Override
        protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
            return "us";
        }
    };

    private XC_MethodReplacement nowOperatorNameHook = new XC_MethodReplacement() {
        @Override
        protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
            return "Verizon";
        }
    };

    private XC_MethodReplacement detectionMethodHook = new XC_MethodReplacement() {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
            prefs.reload();
            return prefs.getBoolean("assistantEnabled", true);
        }
    };

    private XC_MethodHook prefsHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            String key = (String) param.args[0];
            if (key.equals("key_opa_eligible") || key.equals("opa_enabled")) {
                prefs.reload();
                if (prefs.getBoolean("assistantEnabled", true))
                    param.setResult(true);
            }
        }
    };

    private XC_MethodHook baseBundleHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            String key = (String) param.args[0];
            if (key.equals("from_hotword")) {
                prefs.reload();
                if (prefs.getBoolean("assistantEnabled", true))
                    param.setResult(false);
            }
        }
    };

    private XC_MethodHook systemPropHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
            String key = (String) param.args[0];
            if (key.equals("ro.opa.eligible_device")) {
                prefs.reload();
                if (prefs.getBoolean("assistantEnabled", true))
                    param.setResult(true);
            }
        }
    };

    private XC_MethodHook intentHook1 = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            String action = (String) getObjectField(param.thisObject, "mAction");
            if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                prefs.reload();
                if (prefs.getBoolean("enableOKGoogleEverywhere", false))
                    param.setResult(Intent.ACTION_POWER_CONNECTED);
            }
        }
    };

    private XC_MethodHook intentHook2 = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            String action = (String) getObjectField(param.thisObject, "mAction");
            String key = (String) param.args[0];
            if (Intent.ACTION_BATTERY_CHANGED.equals(action) && key.equals(BatteryManager.EXTRA_STATUS)) {
                prefs.reload();
                if (prefs.getBoolean("enableOKGoogleEverywhere", false))
                    param.setResult(BatteryManager.BATTERY_STATUS_CHARGING);
            }
        }
    };
}