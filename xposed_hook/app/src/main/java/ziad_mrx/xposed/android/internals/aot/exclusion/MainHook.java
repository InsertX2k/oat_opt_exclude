package ziad_mrx.xposed.android.internals.aot.exclusion;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedBridge;

public class MainHook implements IXposedHookLoadPackage {
    private static final String TAG = "AOT-Package-Exclusion-Hook";
    private static final String EXCLUDED_PACKAGES_LIST_FILE_PATH = "/data/aot_excluded.list";
    private static final int DEX_OPT_SKIPPED = 0;




    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!"android".equalsIgnoreCase(lpparam.packageName)) {
            return;
        }
        final XC_MethodHook METHOD_HOOK = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Object arg0 = param.args[0];
                String pkg = null;
                if (arg0 instanceof String) {
                    pkg = (String) arg0;
                } else {
                    try {
                        // Dynamically call getPackageName() on the AndroidPackage object
                        pkg = (String) XposedHelpers.callMethod(arg0, "getPackageName");
                    } catch (Throwable cmt) {
                        XposedBridge.log(TAG + ".METHOD_HOOK: " + param.method.getName() + ": failed to invoke getPackageName(): " + cmt.getMessage());
                    }
                }
                // check if pkg is in our excluded packages list or not.
                LinkedList<String> excludedPkgsList = fetchAOTExcludedPackagesNames();
                if (excludedPkgsList == null) {
                    XposedBridge.log(TAG + param.method.getName() + " called but with an invalid AOT excluded packages list, will not exclude anything this time");
                } else {
                    if ((!excludedPkgsList.isEmpty()) && (pkg != null)) {
                        // iterate over it to see if our package is inside
                        for (String pn : excludedPkgsList) {
                            if (pn.equalsIgnoreCase(pkg)) {
                                param.setResult(DEX_OPT_SKIPPED);
                                break;
                            }
                        }
                    } else {
                        XposedBridge.log(TAG + param.method.getName() + " called but AOT excluded packages list is empty!");
                    }
                }
            }
        };
        // we will not care what package we are hooked onto for now.
        try {
            Class<?> pkgDexOptClazz = XposedHelpers.findClass(
                    "com.android.server.pm.PackageDexOptimizer",
                    lpparam.classLoader
            );
            for (java.lang.reflect.Method m : pkgDexOptClazz.getDeclaredMethods()) {
                if ((m.getName().equalsIgnoreCase("performDexOpt")) || (m.getName().equalsIgnoreCase("performDexOptLI"))) {
                    XposedBridge.hookMethod(m, METHOD_HOOK);
                }
            }

            XposedBridge.log(TAG + "Successfully hooked all package dex opt perform methods from the class! from package " + lpparam.packageName);
        } catch (Throwable t) {
            XposedBridge.log(TAG + "Failed to hook from package "+ lpparam.packageName +": " + t.getMessage());
            return;
        }
    }

    /*
    * Fetches the list of AOT Excluded package names from the list file and returns excluded package names
    * in a LinkedList
    *
    * returns null on failure/excluded packages list file does not exist.
    * */
    private LinkedList<String> fetchAOTExcludedPackagesNames() {
        LinkedList<String> _excludedPkgs = new LinkedList<>();
        BufferedReader bufferedFileReader = null;
        try {
            bufferedFileReader = new BufferedReader(new FileReader(EXCLUDED_PACKAGES_LIST_FILE_PATH));
        } catch (FileNotFoundException fnotfounde) {
            XposedBridge.log(TAG + "Failed to open excluded packages list file \"" + EXCLUDED_PACKAGES_LIST_FILE_PATH + "\": " + fnotfounde.getMessage());
            return null;
        }
        try {
            String line;
            while ((line = bufferedFileReader.readLine()) != null) {
                // append package name:
                _excludedPkgs.add(line.trim());
                XposedBridge.log(TAG + "Excluding package from AOT Compilation: " + line);
            }
            bufferedFileReader.close();
            return _excludedPkgs;
        } catch (IOException ioe) {
            try {bufferedFileReader.close();} catch (IOException fdcioe) {XposedBridge.log(TAG + "IOException while trying to handle another I/O Exception!: " + fdcioe.getMessage());}
            XposedBridge.log(TAG + "IOException occured!: " + ioe.getMessage());
            return null;
        }
    }
}
