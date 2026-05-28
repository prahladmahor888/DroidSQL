package com.smartqueue.droidsql.utils;

import android.app.Activity;
import android.os.Build;
import android.view.WindowManager;

import java.io.File;

/**
 * Utility class providing screenshot protection and basic device root detection.
 */
public class SecurityUtils {

    /**
     * Prevents screenshots and screen recording on the given activity window.
     */
    public static void preventScreenshots(Activity activity) {
        if (activity != null && activity.getWindow() != null) {
            activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            );
        }
    }

    /**
     * Checks if the Android device is rooted by verifying system tags and checking
     * for standard su binary directories.
     */
    public static boolean isDeviceRooted() {
        return checkBuildTags() || checkSuBinaryPaths();
    }

    private static boolean checkBuildTags() {
        String buildTags = Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkSuBinaryPaths() {
        String[] paths = {
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        };
        for (String path : paths) {
            try {
                File file = new File(path);
                if (file.exists() && file.isFile()) {
                    return true;
                }
            } catch (Exception ignored) {}
        }
        return false;
    }
}
