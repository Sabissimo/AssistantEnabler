package com.sabik.assistantenabler;

import android.content.SharedPreferences;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class AssistantEnabler implements IXposedHookLoadPackage {

	private static final String PACKAGE_NAME = "com.google.android.googlequicksearchbox";
	private static final String GSA_PACKAGE = "com.google.android.apps.gsa";
	private static final String ASSISTANT_PACKAGE = GSA_PACKAGE + ".assistant";

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!PACKAGE_NAME.equals(lpparam.packageName))
			return;

		try {
			Class a = findClass(ASSISTANT_PACKAGE + ".a.e", lpparam.classLoader);

			findAndHookConstructor(a, findClass("com.google.android.apps.gsa.search.core.config.GsaConfigFlags", lpparam.classLoader), SharedPreferences.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					SharedPreferences prefs = (SharedPreferences) getObjectField(param.thisObject, "bhX");
					prefs.edit().putBoolean("key_opa_eligible", true)
							.putBoolean("opa_enabled", true)
							.putBoolean("opa_hotword_enabled", true)
							.putBoolean("opa_hotword_transition_seen", true).apply();
				}
			});

			findAndHookMethod(a, "aG", boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					param.args[0] = true;
				}
			});

			findAndHookMethod(a, "pa", new XC_MethodReplacement() {
				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
					return true;
				}
			});

			findAndHookMethod(a, "oZ", new XC_MethodReplacement() {
				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
					return true;
				}
			});

			findAndHookMethod(a, "pb", new XC_MethodReplacement() {
				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
					return true;
				}
			});

			findAndHookMethod(GSA_PACKAGE + ".shared.util.c", lpparam.classLoader, "v", String.class, boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					if (param.args[0].toString().equals("ro.opa.eligible_device")) {
						param.setResult(true);
					}
				}
			});
		} catch (Throwable t) {
			log(t);
		}
	}

}
