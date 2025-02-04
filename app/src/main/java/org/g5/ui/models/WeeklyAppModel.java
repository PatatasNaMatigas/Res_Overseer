package org.g5.ui.models;

import android.graphics.drawable.Drawable;

public class WeeklyAppModel {

    private final String day;
    private final String month;
    private final String date;
    private final String[] timeSpent;
    private final Drawable[] icon;
    private final int bg;

    public WeeklyAppModel(String day, String month, String date, WeeklyAppModel lastView, String[] timeSpent, Drawable[] icon) {
        this.day = day;
        this.month = month;
        this.date = date;
        this.timeSpent = timeSpent;
        this.icon = icon;

        bg = (lastView != null)
                ? (lastView.getBg() < 2)
                ? lastView.getBg() + 1
                : 0
                : 0;
    }

    public Drawable[] getIcon() {
        return icon;
    }

    public int getBg() {
        return bg;
    }

    public String[] getTimeSpent() {
        return timeSpent;
    }

    public String getDay() {
        return day;
    }

    public String getMonth() {
        return month;
    }

    public String getDate() {
        return date;
    }
}
