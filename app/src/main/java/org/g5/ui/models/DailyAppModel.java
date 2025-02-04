package org.g5.ui.models;

import android.graphics.drawable.Drawable;

public class DailyAppModel {

    private final String name;
    private final String timeSpent;
    private final Drawable icon;
    private final int bg;

    public DailyAppModel(DailyAppModel lastView, String name, String timeSpent, Drawable icon) {
        this.name = name;
        this.timeSpent = timeSpent;
        this.icon = icon;

        bg = (lastView != null)
                ? (lastView.getBg() < 2)
                ? lastView.getBg() + 1
                : 0
                : 0;
    }

    public String getName() {
        return name;
    }

    public String getTimeSpent() {
        return timeSpent;
    }

    public Drawable getIcon() {
        return icon;
    }

    public int getBg() {
        return bg;
    }
}
