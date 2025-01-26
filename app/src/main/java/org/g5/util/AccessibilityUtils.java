package org.g5.util;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityManager;


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
        } catch (Exception e) {}
        return false;
    }
}
