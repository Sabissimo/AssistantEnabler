package com.sabik.assistantenabler;

import android.content.SharedPreferences;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class AssistantEnabler implements IXposedHookLoadPackage {

	private static final String PACKAGE_NAME = "com.google.android.googlequicksearchbox";
	private static final String ASSISTANT_PACKAGE = "com.google.android.apps.gsa.assistant";

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!PACKAGE_NAME.equals(lpparam.packageName))
			return;

		try {
			Class a = findClass(ASSISTANT_PACKAGE + ".a.e", lpparam.classLoader);

			findAndHookConstructor(a, findClass("com.google.android.apps.gsa.search.core.config.GsaConfigFlags", lpparam.classLoader), SharedPreferences.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					callMethod(param.thisObject, "aG", true);
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

			findAndHookMethod(a, "pb", new XC_MethodReplacement() {
				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
					return true;
				}
			});

			findAndHookMethod(a, "pc", new XC_MethodReplacement() {
				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
					return true;
				}
			});

			findAndHookMethod(a, "oY", new XC_MethodReplacement() {
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
		} catch (Throwable t) {
			log(t);
		}
	}

}
