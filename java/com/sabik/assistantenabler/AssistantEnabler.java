package com.sabik.assistantenabler;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

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
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class AssistantEnabler implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    private XSharedPreferences prefs;
    private static final String GOOGLE_PACKAGE_NAME = "com.google.android.googlequicksearchbox";
    private static final String GSA_PACKAGE = "com.google.android.apps.gsa";
    private static final String ASSISTANT_PACKAGE = GSA_PACKAGE + ".assistant.a.e";
    private static final String CONFIG_FLAGS_PACKAGE = GSA_PACKAGE + ".search.core.config.GsaConfigFlags";
    private static final String TELEPHONY_CLASS = "android.telephony.TelephonyManager";
    private static final String BROADCAST_LISTENER_CLASS = "com.google.android.apps.gsa.search.core.BroadcastListenerService";
    private static final List<String> NOW_PACKAGE_NAMES = new ArrayList<>(Arrays.asList("com.google.android.gms", "com.google.android.apps.maps"));
    private String googleBHX;
    private String googleAG;
    private String googlePA;
    private String googleOZ;
    private String googlePB;
    private String googleSharedUtilsC;
    private String googleSharedUtilsV;
    private String googleHotwordAE;
    private String googleHotwordX;
    private String googleBroadcastListenerServiceE;

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
                Class assistantClass = findClass(ASSISTANT_PACKAGE, lpparam.classLoader);
                Class gsaConfigFlagsClass = findClass(CONFIG_FLAGS_PACKAGE, lpparam.classLoader);
                Class sharedUtilsClass = findClass(GSA_PACKAGE + googleSharedUtilsC, lpparam.classLoader);
                Class hotwordTransitionClass = findClass(GSA_PACKAGE + googleHotwordAE, lpparam.classLoader);
                Class broadcastListenerServiceClass = findClass(BROADCAST_LISTENER_CLASS, lpparam.classLoader);

                findAndHookConstructor(assistantClass, gsaConfigFlagsClass, SharedPreferences.class, assistantBHXHook);
                findAndHookMethod(assistantClass, googleAG, boolean.class, assistantAGHook);
                findAndHookMethod(assistantClass, googlePA, assistantPAHook);
                findAndHookMethod(assistantClass, googleOZ, assistantOZHook);
                findAndHookMethod(assistantClass, googlePB, assistantPBHook);
                findAndHookMethod(sharedUtilsClass, googleSharedUtilsV, String.class, boolean.class, assistantGSAHook);
                findAndHookMethod(hotwordTransitionClass, googleHotwordX, Bundle.class, hotwordSkipHook);
                findAndHookMethod(broadcastListenerServiceClass, googleBroadcastListenerServiceE, Context.class, boolean.class, listenerHook);
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
            googleBHX = "bhX";
            googleAG = "aG";
            googlePA = "pa";
            googleOZ = "oZ";
            googlePB = "pb";
            googleSharedUtilsC = ".shared.util.c";
            googleSharedUtilsV = "v";
            googleHotwordAE = ".opa.ag";
            googleHotwordX = "z";
            googleBroadcastListenerServiceE = "e";
        } else if (versionName.matches("6.7.*")) {
            googleBHX = "biJ";
            googleAG = "aK";
            googlePA = "pc";
            googleOZ = "pb";
            googlePB = "pd";
            googleSharedUtilsC = ".shared.util.c";
            googleSharedUtilsV = "v";
            googleHotwordAE = ".opa.ae";
            googleHotwordX = "w";
            googleBroadcastListenerServiceE = "e";
        } else if (versionName.matches("6.8.*")) {
            googleBHX = "bnp";
            googleAG = "aK";
            googlePA = "pU";
            googleOZ = "pT";
            googlePB = "pV";
            googleSharedUtilsC = ".shared.util.common.a";
            googleSharedUtilsV = "y";
            googleHotwordAE = ".opa.ae";
            googleHotwordX = "x";
            googleBroadcastListenerServiceE = "e";
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

    private XC_MethodHook assistantBHXHook = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            prefs.reload();
            boolean assistantEnabled = prefs.getBoolean("assistantEnabled", true);
            if(assistantEnabled) {
                SharedPreferences googlePrefs = (SharedPreferences) getObjectField(param.thisObject, googleBHX);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    googlePrefs.edit().putBoolean("key_opa_eligible", true)
                            .putBoolean("opa_enabled", true).apply();
                }
            }
        }
    };

    private XC_MethodHook assistantAGHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            prefs.reload();
            boolean assistantEnabled = prefs.getBoolean("assistantEnabled", true);
            if(assistantEnabled) {
                param.args[0] = true;
            }
        }
    };

    private XC_MethodReplacement assistantPAHook = new XC_MethodReplacement() {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
            prefs.reload();
            return prefs.getBoolean("assistantEnabled", true);
        }
    };

    private XC_MethodReplacement assistantOZHook = new XC_MethodReplacement() {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
            prefs.reload();
            return prefs.getBoolean("assistantEnabled", true);
        }
    };

    private XC_MethodReplacement assistantPBHook = new XC_MethodReplacement() {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
            prefs.reload();
            return prefs.getBoolean("assistantEnabled", true);
        }
    };

    private XC_MethodHook assistantGSAHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            if (param.args == null)
                return;
            if (param.args.length < 1)
                return;
            if (!(param.args[0] instanceof String))
                return;
            prefs.reload();
            String key = (String) param.args[0];
            boolean assistantEnabled = prefs.getBoolean("assistantEnabled", true);
            if (key.equals("ro.opa.eligible_device")&&assistantEnabled) {
                param.setResult(true);
            }
        }
    };

    private XC_MethodHook hotwordSkipHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            prefs.reload();
            if(prefs.getBoolean("assistantEnabled", true))
            {
                param.setResult(false);
            }
        }
    };

    private XC_MethodHook listenerHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            prefs.reload();
            boolean enableOKGoogleEverywhere = prefs.getBoolean("enableOKGoogleEverywhere", false);
            if(enableOKGoogleEverywhere) {
                param.args[1] = true;
            }
        }
    };

}