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
    private static final String CONFIG_FLAGS_PACKAGE = GSA_PACKAGE + ".search.core.config.GsaConfigFlags";
    private static final String TELEPHONY_CLASS = "android.telephony.TelephonyManager";
    private static final List<String> NOW_PACKAGE_NAMES = new ArrayList<>(Arrays.asList("com.google.android.gms", "com.google.android.apps.maps"));
    private static final String BROADCAST_LISTENER_CLASS = "com.google.android.apps.gsa.search.core.BroadcastListenerService";
    private String sharedPreferencesVariable;
    private String detectionMethod1;
    private String detectionMethod2;
    private String detectionMethod3;
    private String detectionMethod4;
    private String sharedUtilsClassName;
    private String sharedUtilsMethod;
    private String hotwordDetectionClassName;
    private String hotwordDetectionMethod;
    private String broadcastListenerServiceMethod;
    private String assistantClassName;

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
                Class gsaConfigFlagsClass = findClass(CONFIG_FLAGS_PACKAGE, lpparam.classLoader);
                Class sharedUtilsClass = findClass(GSA_PACKAGE + sharedUtilsClassName, lpparam.classLoader);
                Class hotWordTransitionClass = findClass(GSA_PACKAGE + hotwordDetectionClassName, lpparam.classLoader);
                Class broadcastListenerServiceClass = findClass(BROADCAST_LISTENER_CLASS, lpparam.classLoader);

                findAndHookConstructor(assistantClass, gsaConfigFlagsClass, SharedPreferences.class, sharedPreferencesHook);
                findAndHookMethod(assistantClass, detectionMethod1, boolean.class, detectionMethod1Hook);
                findAndHookMethod(assistantClass, detectionMethod2, detectionMethod2Hook);
                findAndHookMethod(assistantClass, detectionMethod3, detectionMethod3Hook);
                findAndHookMethod(assistantClass, detectionMethod4, detectionMethod4Hook);
                findAndHookMethod(sharedUtilsClass, sharedUtilsMethod, String.class, boolean.class, sharedUtilsMethodHook);
                findAndHookMethod(hotWordTransitionClass, hotwordDetectionMethod, Bundle.class, hotwordDetectionMethodHook);
                findAndHookMethod(broadcastListenerServiceClass, broadcastListenerServiceMethod, Context.class, boolean.class, broadcastListenerServiceMethodHook);
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
            sharedPreferencesVariable = "bhX";
            detectionMethod1 = "aG";
            detectionMethod2 = "pa";
            detectionMethod3 = "oZ";
            detectionMethod4 = "pb";
            sharedUtilsClassName = ".shared.util.c";
            sharedUtilsMethod = "v";
            hotwordDetectionClassName = ".opa.ag";
            hotwordDetectionMethod = "z";
            broadcastListenerServiceMethod = "e";
        } else if (versionName.matches("6.7.*")) {
            assistantClassName = ".assistant.a.e";
            sharedPreferencesVariable = "biJ";
            detectionMethod1 = "aK";
            detectionMethod2 = "pc";
            detectionMethod3 = "pb";
            detectionMethod4 = "pd";
            sharedUtilsClassName = ".shared.util.c";
            sharedUtilsMethod = "v";
            hotwordDetectionClassName = ".opa.ae";
            hotwordDetectionMethod = "w";
            broadcastListenerServiceMethod = "e";
        } else if (versionName.matches("6.8.*")) {
            assistantClassName = ".assistant.a.e";
            sharedPreferencesVariable = "bnp";
            detectionMethod1 = "aK";
            detectionMethod2 = "pU";
            detectionMethod3 = "pT";
            detectionMethod4 = "pV";
            sharedUtilsClassName = ".shared.util.common.a";
            sharedUtilsMethod = "y";
            hotwordDetectionClassName = ".opa.ae";
            hotwordDetectionMethod = "x";
            broadcastListenerServiceMethod = "e";
        } else if (versionName.matches("6.9.*")) {
            assistantClassName = ".assistant.shared.f";
            sharedPreferencesVariable = "bpY";
            detectionMethod1 = "aQ";
            detectionMethod2 = "rp";
            detectionMethod3 = "ro";
            detectionMethod4 = "rq";
            sharedUtilsClassName = ".shared.util.common.a";
            sharedUtilsMethod = "x";
            hotwordDetectionClassName = ".opa.ae";
            hotwordDetectionMethod = "y";
            broadcastListenerServiceMethod = "f";
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

    private XC_MethodHook sharedPreferencesHook = new XC_MethodHook() {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            prefs.reload();
            boolean assistantEnabled = prefs.getBoolean("assistantEnabled", true);
            if(assistantEnabled) {
                SharedPreferences googlePrefs = (SharedPreferences) getObjectField(param.thisObject, sharedPreferencesVariable);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    googlePrefs.edit().putBoolean("key_opa_eligible", true)
                            .putBoolean("opa_enabled", true).apply();
                }
            }
        }
    };

    private XC_MethodHook detectionMethod1Hook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            prefs.reload();
            boolean assistantEnabled = prefs.getBoolean("assistantEnabled", true);
            if(assistantEnabled) {
                param.args[0] = true;
            }
        }
    };

    private XC_MethodReplacement detectionMethod2Hook = new XC_MethodReplacement() {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
            prefs.reload();
            return prefs.getBoolean("assistantEnabled", true);
        }
    };

    private XC_MethodReplacement detectionMethod3Hook = new XC_MethodReplacement() {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
            prefs.reload();
            return prefs.getBoolean("assistantEnabled", true);
        }
    };

    private XC_MethodReplacement detectionMethod4Hook = new XC_MethodReplacement() {
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
            prefs.reload();
            return prefs.getBoolean("assistantEnabled", true);
        }
    };

    private XC_MethodHook sharedUtilsMethodHook = new XC_MethodHook() {
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

    private XC_MethodHook hotwordDetectionMethodHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            prefs.reload();
            if(prefs.getBoolean("assistantEnabled", true))
            {
                param.setResult(false);
            }
        }
    };

    private XC_MethodHook broadcastListenerServiceMethodHook = new XC_MethodHook() {
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