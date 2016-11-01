package com.sabik.assistantenabler;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class AssistantEnabler implements IXposedHookLoadPackage {

	private static final String GOOGLE_PACKAGE_NAME = "com.google.android.googlequicksearchbox";
	private static final String GSA_PACKAGE = "com.google.android.apps.gsa";
	private static final String ASSISTANT_PACKAGE = GSA_PACKAGE + ".assistant";
	private String googleBHX;
	private String googleAG;
	private String googlePA;
	private String googleOZ;
	private String googlePB;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (GOOGLE_PACKAGE_NAME.equals(lpparam.packageName))
		{
			if(checkVersion(lpparam)) {
				try {
					Class assistantClass = findClass(ASSISTANT_PACKAGE + ".a.e", lpparam.classLoader);
					Class gsaConfigFlagsClass = findClass("com.google.android.apps.gsa.search.core.config.GsaConfigFlags", lpparam.classLoader);

					findAndHookConstructor(assistantClass, gsaConfigFlagsClass, SharedPreferences.class, assistantBHXHook);
					findAndHookMethod(assistantClass, googleAG, boolean.class, assistantAGHook);
					findAndHookMethod(assistantClass, googlePA, assistantPAHook);
					findAndHookMethod(assistantClass, googleOZ, assistantOZHook);
					findAndHookMethod(assistantClass, googlePB, assistantPBHook);
					findAndHookMethod(GSA_PACKAGE + ".shared.util.c", lpparam.classLoader, "v", String.class, boolean.class, assistantGSAHook);
				} catch (Throwable t) {
					log(t);
				}
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
        } else if (versionName.matches("6.7.*")) {
            googleBHX = "biJ";
            googleAG = "aK";
            googlePA = "pc";
            googleOZ = "pb";
            googlePB = "pd";
        } else if (versionName.matches("6.8.*")) {
            googleBHX = "bnp";
            googleAG = "aK";
            googlePA = "pU";
            googleOZ = "pT";
            googlePB = "pV";
        } else {
			return false;
		}

		return true;
	}

	private XC_MethodHook assistantBHXHook = new XC_MethodHook() {
		@Override
		protected void afterHookedMethod(MethodHookParam param) throws Throwable {
			SharedPreferences prefs = (SharedPreferences) getObjectField(param.thisObject, googleBHX);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				prefs.edit().putBoolean("key_opa_eligible", true)
						.putBoolean("opa_enabled", true)
						.putBoolean("opa_hotword_enabled", true)
						.putBoolean("opa_hotword_transition_seen", true).apply();
			}
		}
	};

	private XC_MethodHook assistantAGHook = new XC_MethodHook() {
		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			param.args[0] = true;
		}
	};

	private XC_MethodReplacement assistantPAHook = new XC_MethodReplacement() {
		@Override
		protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
			return true;
		}
	};

	private XC_MethodReplacement assistantOZHook = new XC_MethodReplacement() {
		@Override
		protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
			return true;
		}
	};

	private XC_MethodReplacement assistantPBHook = new XC_MethodReplacement() {
		@Override
		protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
			return true;
		}
	};

	private XC_MethodHook assistantGSAHook = new XC_MethodHook() {
		@Override
		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			if (param.args[0].toString().equals("ro.opa.eligible_device")) {
				param.setResult(true);
			}
		}
	};

}
