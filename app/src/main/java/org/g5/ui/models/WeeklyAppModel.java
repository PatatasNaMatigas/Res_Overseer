package org.g5.ui.models;

import android.graphics.drawable.Drawable;

import java.util.Objects;

public class WeeklyAppModel {

    private final String name;
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

        name = month + "_" + date + "_" + day;
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

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeeklyAppModel that = (WeeklyAppModel) o;
        return Objects.equals(day, that.day) &&
                Objects.equals(month, that.month) &&
                Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, month, date);
    }
}
