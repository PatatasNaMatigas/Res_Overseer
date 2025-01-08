package org.g5.util;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class AccessibilityUtils {

    public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> service) {
        try {
            AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (am == null) {
                return false;
            }

            List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
            for (AccessibilityServiceInfo info : enabledServices) {
                String componentName = info.getResolveInfo().serviceInfo.name;
                if (componentName.equals(service.getName())) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.d("CODE DEBUG", e.getLocalizedMessage());
        }
        return false;
    }

    public static void launchDisplayOverOtherAppsPermission(AppCompatActivity appCompatActivity) {
        Toast.makeText(appCompatActivity, "Please enable display over other apps permission in this app", Toast.LENGTH_LONG).show();
        Intent overlayIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        ActivityResultLauncher<Intent> overlayPermissionLauncher =
                appCompatActivity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {});
        ActivityResultLauncher<Intent> finalOverlayPermissionLauncher = overlayPermissionLauncher;
        overlayPermissionLauncher = appCompatActivity.registerForActivityResult
            (new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (!Settings.canDrawOverlays(appCompatActivity)) {
                        Toast.makeText(appCompatActivity, "Overlay permission still not enabled", Toast.LENGTH_LONG).show();
                        launchSettings(finalOverlayPermissionLauncher, overlayIntent);
                    }
                }
            );
        overlayPermissionLauncher.launch(overlayIntent);
    }

    private static void launchSettings(ActivityResultLauncher<Intent> launcher, Intent intent) {
        launcher.launch(intent);
    }
}
